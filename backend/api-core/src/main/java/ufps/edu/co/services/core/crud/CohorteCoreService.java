package ufps.edu.co.services.core.crud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.IntegridadSql;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.maps.specific.CohorteMap;
import ufps.edu.co.persistence.entities.CriteriocohorteEntity;
import ufps.edu.co.persistence.entities.DocumentosrequisitoconsejocohorteEntity;
import ufps.edu.co.persistence.entities.DocumentosrequisitoprogramacohorteEntity;
import ufps.edu.co.persistence.repositories.CohorteRepository;
import ufps.edu.co.persistence.repositories.CriteriocohorteRepository;
import ufps.edu.co.persistence.repositories.DocumentoRepository;
import ufps.edu.co.persistence.repositories.DocumentosrequisitoconsejocohorteRepository;
import ufps.edu.co.persistence.repositories.DocumentosrequisitoprogramacohorteRepository;
import ufps.edu.co.records.input.entity.CohorteInput.*;
import ufps.edu.co.records.output.entity.CohorteDependenciasOutput;
import ufps.edu.co.records.output.entity.CohorteOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.services.AdmitidoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.PlazoService;
import ufps.edu.co.rest.services.PruebaService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class CohorteCoreService implements GlobalUseCase<COHORTE_CREATE, COHORTE_UPDATE, COHORTE_DELETE, COHORTE_PATCH, COHORTE_FIND, CohorteOutput> {

    private static final Logger log = LoggerFactory.getLogger(CohorteCoreService.class);

    @Autowired
    private CohorteService service;

    @Autowired
    private CohorteMap map;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private CriteriocohorteRepository criteriocohorteRepository;

    @Autowired
    private DocumentosrequisitoconsejocohorteRepository documentosrequisitoconsejocohorteRepository;

    @Autowired
    private DocumentosrequisitoprogramacohorteRepository documentosrequisitoprogramacohorteRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private AdmitidoService admitidoService;

    @Autowired
    private PruebaService pruebaService;

    @Autowired
    private PlazoService plazoService;

    @Override
    public CohorteOutput create(COHORTE_CREATE input) {
        try {
            CohorteDTO dto = map.toDto(input);
            return map.toOutput(service.create(dto));
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_DUPLICADO, input);
        }
    }

    @Override
    public CohorteOutput update(COHORTE_UPDATE input) {
        try {
            CohorteDTO dto = map.toDto(input);
            return map.toOutput(service.update(input.id(), dto));
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, input.id());
        }
    }

    @Override
    public CohorteOutput findById(COHORTE_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<CohorteOutput> findAll() {
        try {
            return service.findAll().stream()
                    .map(map::toOutput)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, "findAll");
        }
    }

    @Override
    @Transactional
    public void deleteById(COHORTE_DELETE input) {
        Integer id = input != null ? input.id() : null;
        if (id == null) {
            throw new DomainException(CohorteErrorCode.COHORTE_PARAMETRO_REQUERIDO, "id");
        }

        CohorteDTO cohorte = service.findById(id);
        if (cohorte == null) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, id);
        }

        CohorteDependenciasOutput dependencias = evaluarDependencias(id);
        if (!dependencias.conflictos().isEmpty()) {
            throw new DomainException(CohorteErrorCode.valueOf(dependencias.conflictos().get(0)), dependencias);
        }

        Set<Integer> plazos = new LinkedHashSet<>();
        agregarSiPresente(plazos, cohorte.getIdPlazodocumentacion());
        agregarSiPresente(plazos, cohorte.getIdPlazoinscripcion());
        agregarSiPresente(plazos, cohorte.getIdPlazopago());

        try {
            eliminarHijosDeLaCohorte(id);
            service.deleteById(id);
            // Ejecutar el DELETE de la cohorte antes de contar los plazos.
            cohorteRepository.flush();
            eliminarPlazosSinUso(plazos);
            // Forzar los DELETE dentro del try: si una llave que la verificación
            // previa no cubre lo impide, se informa como 409 y la transacción
            // se revierte entera.
            cohorteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            String restriccion = Optional.ofNullable(
                    IntegridadSql.extraerRestriccion(IntegridadSql.mensajeCausa(e))).orElse("desconocida");
            Map<String, Object> param = new HashMap<>();
            param.put("idCohorte", id);
            param.put("restriccion", restriccion);
            log.warn("No se pudo eliminar la cohorte {} por la restricción {}", id, restriccion);
            throw new DomainException(CohorteErrorCode.COHORTE_REFERENCIADO_CONFLICT, param);
        }
    }

    /**
     * Cuenta lo que impide eliminar una cohorte y arma la lista de conflictos
     * en orden de prioridad: aspirantes &gt; admitidos &gt; pruebas.
     */
    public CohorteDependenciasOutput evaluarDependencias(Integer idCohorte) {
        long aspirantes = aspiranteService.countByCohorte(idCohorte);
        long admitidos = admitidoService.countByCohorte(idCohorte);
        long pruebas = pruebaService.countByCohorte(idCohorte);

        List<String> conflictos = new ArrayList<>();
        if (aspirantes > 0) {
            conflictos.add(CohorteErrorCode.COHORTE_CON_ASPIRANTES_CONFLICT.name());
        }
        if (admitidos > 0) {
            conflictos.add(CohorteErrorCode.COHORTE_CON_ADMITIDOS_CONFLICT.name());
        }
        if (pruebas > 0) {
            conflictos.add(CohorteErrorCode.COHORTE_CON_PRUEBAS_CONFLICT.name());
        }

        return new CohorteDependenciasOutput(idCohorte, aspirantes, admitidos, pruebas, conflictos);
    }

    /**
     * Borra lo que pertenece a la cohorte, en orden: criterios, documentos
     * requisito de consejo y de programa. Los aspirantes, admitidos y pruebas
     * ya se descartaron en {@link #evaluarDependencias}.
     */
    private void eliminarHijosDeLaCohorte(Integer id) {
        List<CriteriocohorteEntity> criterios = criteriocohorteRepository.findByIdCohorte(id);
        if (!criterios.isEmpty()) {
            // calificacioncriterio tiene ON DELETE CASCADE sobre estos registros.
            criteriocohorteRepository.deleteAll(criterios);
        }

        List<DocumentosrequisitoconsejocohorteEntity> documentosConsejo =
                documentosrequisitoconsejocohorteRepository.findByIdCohorte(id);
        if (!documentosConsejo.isEmpty()) {
            documentosrequisitoconsejocohorteRepository.deleteAll(documentosConsejo);
        }

        List<DocumentosrequisitoprogramacohorteEntity> documentosPrograma =
                documentosrequisitoprogramacohorteRepository.findByIdCohorte(id);
        if (!documentosPrograma.isEmpty()) {
            documentosrequisitoprogramacohorteRepository.deleteAll(documentosPrograma);
        }

        // Los hijos se borran como entidades y se ejecutan antes que el padre.
        cohorteRepository.flush();
    }

    /**
     * Borra los plazos de la cohorte solo si ninguna otra cohorte ni ningún
     * documento los usan. Los compartidos se borran una sola vez gracias al
     * {@link Set}.
     */
    private void eliminarPlazosSinUso(Set<Integer> plazos) {
        for (Integer idPlazo : plazos) {
            boolean usadoPorOtraCohorte = cohorteRepository.countByPlazo(idPlazo) > 0;
            boolean usadoPorDocumento = documentoRepository.countByIdPlazo(idPlazo) > 0;
            if (!usadoPorOtraCohorte && !usadoPorDocumento) {
                plazoService.deleteById(idPlazo);
            }
        }
    }

    private static void agregarSiPresente(Set<Integer> plazos, Integer idPlazo) {
        if (idPlazo != null) {
            plazos.add(idPlazo);
        }
    }

    @Override
    public CohorteOutput patch(COHORTE_PATCH input) {
        return null;
    }

    public CohorteOutput createWithPlazo(COHORTE_WITHPLAZO_CREATE input) {
        throw new UnsupportedOperationException("No se puede crear una cohorte con plazo desde este endpoint");
    }

    public long countAspirantesEnProcesoEnCohorteAbierta(Integer cohorteId) {
        try {
            return service.countAspirantesEnProcesoEnCohorteAbierta(cohorteId);
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, cohorteId);
        }
    }

    public List<CohorteOutput> findActivasByIdPrograma(Integer idPrograma) {
        try {
            return service.findActivasByIdPrograma(idPrograma).stream()
                    .map(map::toOutput)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, idPrograma);
        }
    }

    public long countAspirantesEnProcesoByCohorteId(Integer cohorteId) {
        try {
            return service.countAspirantesEnProcesoByCohorteId(cohorteId);
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, cohorteId);
        }
    }

    /**
     * Valida que el usuario pueda consultar la cohorte indicada.
     *
     * <p>Es lógica de dominio pura (sin {@code SecurityContext}): el
     * controlador resuelve el programa del usuario y lo pasa aquí.</p>
     *
     * @param idCohorte         cohorte solicitada; si no existe se informa 404.
     * @param idProgramaDirector programa del Director de Programa autenticado;
     *                           {@code null} significa acceso total (Posgrados y
     *                           Super Administrador) y no se comprueba nada más.
     */
    public void validarAccesoCohorte(Integer idCohorte, Integer idProgramaDirector) {
        CohorteDTO cohorte = service.findById(idCohorte);
        if (cohorte == null) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, idCohorte);
        }
        if (idProgramaDirector == null) {
            return;
        }
        if (cohorte.getIdPrograma() == null || !cohorte.getIdPrograma().equals(idProgramaDirector)) {
            throw new DomainException(CohorteErrorCode.COHORTE_ACCESO_FORBIDDEN, idCohorte);
        }
    }

}
