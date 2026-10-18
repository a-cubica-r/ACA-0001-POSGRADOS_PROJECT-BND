package ufps.edu.co.services.core.crud;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.IntegridadSql;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.OtrosvaloresErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.ProgramaErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.SedeErrorCode;
import ufps.edu.co.maps.specific.ProgramaMap;
import ufps.edu.co.persistence.entities.CargoEntity;
import ufps.edu.co.persistence.entities.CriterioevaluacionEntity;
import ufps.edu.co.persistence.entities.DocumentosrequisitoprogramaEntity;
import ufps.edu.co.persistence.repositories.CargoRepository;
import ufps.edu.co.persistence.repositories.CriterioevaluacionRepository;
import ufps.edu.co.persistence.repositories.DocumentosrequisitoprogramaRepository;
import ufps.edu.co.persistence.repositories.ProgramaRepository;
import ufps.edu.co.records.input.entity.OtrosvaloresInput.OTROSVALORES_CREATE;
import ufps.edu.co.records.input.entity.ProgramaInput.*;
import ufps.edu.co.records.output.entity.ModalidadOutput;
import ufps.edu.co.records.output.entity.ProgramaDependenciasOutput;
import ufps.edu.co.records.output.entity.ProgramaOutput;
import ufps.edu.co.rest.dto.CargoDTO;
import ufps.edu.co.rest.dto.OtrosvaloresDTO;
import ufps.edu.co.rest.dto.ProgramaDTO;
import ufps.edu.co.rest.dto.SedeDTO;
import ufps.edu.co.rest.dto.TiporegistroDTO;
import ufps.edu.co.rest.dto.ModalidadDTO;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.CargoService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EstudiantesService;
import ufps.edu.co.rest.services.ModalidadService;
import ufps.edu.co.rest.services.OtrosvaloresService;
import ufps.edu.co.rest.services.ProgramaService;
import ufps.edu.co.rest.services.SedeService;
import ufps.edu.co.rest.services.TiporegistroService;
import ufps.edu.co.usecase.GlobalUseCase;
import ufps.edu.co.enums.Modalidad;
import ufps.edu.co.enums.TipoRegistro;

@Service
public class ProgramaCoreService implements
        GlobalUseCase<PROGRAMA_CREATE, PROGRAMA_UPDATE, PROGRAMA_DELETE, PROGRAMA_PATCH, PROGRAMA_FIND, ProgramaOutput> {

    private static final Logger log = LoggerFactory.getLogger(ProgramaCoreService.class);

    @Autowired
    private ProgramaService service;

    @Autowired
    private ProgramaMap map;

    @Autowired
    private CohorteCoreService cohorteProcessor;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private EstudiantesService estudiantesService;

    @Autowired
    private CargoService cargoService;

    @Autowired
    private AdministrativoService administrativoService;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CriterioevaluacionRepository criterioevaluacionRepository;

    @Autowired
    private DocumentosrequisitoprogramaRepository documentosrequisitoprogramaRepository;

    @Autowired
    private TiporegistroService tiporegistroService;

    @Autowired
    private ModalidadService modalidadService;

    @Autowired
    private OtrosvaloresService otrosvaloresService;

    @Autowired
    private SedeService sedeService;

    @Override
    public ProgramaOutput create(PROGRAMA_CREATE input) {
        try {
            ProgramaDTO dto = map.toDto(input);
            resolveModalidad(dto);
            dto.setCodigo(normalizarCodigo(dto.getCodigo()));
            validarCodigoUnico(dto.getCodigo(), null);
            ProgramaDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (DomainException | DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Programa: " + e.getMessage(), e);
        }
    }

    private void resolveModalidad(ProgramaDTO dto) {
        if (TipoRegistro.UNICO.getId().equals(dto.getIdTiporegistro())) {
            ModalidadDTO hibrida = modalidadService.findByNombre(Modalidad.HIBRIDA.getValor());
            if (hibrida == null) {
                throw new DomainException(ProgramaErrorCode.MODALIDAD_NOT_FOUND, Modalidad.HIBRIDA.getValor());
            }
            dto.setIdModalidad(hibrida.getId());
        } else if (TipoRegistro.ESTANDAR.getId().equals(dto.getIdTiporegistro())) {
            if (dto.getIdModalidad() == null) {
                throw new DomainException(ProgramaErrorCode.PROGRAMA_PARAMETRO_REQUERIDO, "idModalidad");
            }
        }
    }

    /**
     * Rechaza el código si ya lo usa otro programa. La comparación la hace la
     * base (collation {@code utf8mb4_0900_ai_ci}: sin distinguir mayúsculas),
     * igual que su índice UNIQUE.
     */
    private void validarCodigoUnico(String codigo, Integer idQueSeEdita) {
        if (codigo == null) {
            return;
        }
        Integer existente = service.findIdByCodigo(codigo);
        if (existente != null && !existente.equals(idQueSeEdita)) {
            Map<String, Object> param = new HashMap<>();
            param.put("campo", "codigo");
            param.put("valor", codigo);
            param.put("idExistente", existente);
            throw new DomainException(ProgramaErrorCode.PROGRAMA_CODIGO_DUPLICADO, param);
        }
    }

    /** trim(); si queda vacío, {@code null} (la columna admite nulos). */
    static String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        String limpio = codigo.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    @Override
    public ProgramaOutput update(PROGRAMA_UPDATE input) {
        try {
            ProgramaDTO dto = map.toDto(input);
            // director_id no viaja en el body: se conserva el que ya tenía el programa.
            if (input.id() != null) {
                ProgramaDTO actual = service.findById(input.id());
                if (actual != null && dto.getDirectorId() == null) {
                    dto.setDirectorId(actual.getDirectorId());
                }
            }
            dto.setCodigo(normalizarCodigo(dto.getCodigo()));
            validarCodigoUnico(dto.getCodigo(), input.id());
            ProgramaDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (DomainException | DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating Programa: " + e.getMessage(), e);
        }
    }

    @Override
    public ProgramaOutput patch(PROGRAMA_PATCH input) {
        throw new UnsupportedOperationException("Patch not supported for Programa");
    }

    @Override
    public ProgramaOutput findById(PROGRAMA_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (DomainException | DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error finding Programa: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProgramaOutput> findAll() {
        try {
            return service.findAll().stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all Programas: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteById(PROGRAMA_DELETE input) {
        Integer id = input != null ? input.id() : null;
        if (id == null) {
            throw new DomainException(ProgramaErrorCode.PROGRAMA_PARAMETRO_REQUERIDO, "id");
        }

        if (service.findById(id) == null) {
            throw new DomainException(ProgramaErrorCode.PROGRAMA_NOT_FOUND, id);
        }

        ProgramaDependenciasOutput dependencias = evaluarDependencias(id);
        if (!dependencias.conflictos().isEmpty()) {
            throw new DomainException(ProgramaErrorCode.valueOf(dependencias.conflictos().get(0)), dependencias);
        }

        try {
            eliminarHijosDelPrograma(id);
            service.deleteById(id);
            // Forzar el DELETE del programa dentro del try: si una llave que la
            // verificación previa no cubre lo impide, se informa como 409 y la
            // transacción se revierte entera.
            programaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            String restriccion = IntegridadSql.extraerRestriccion(IntegridadSql.mensajeCausa(e));
            Map<String, Object> param = new HashMap<>();
            param.put("idPrograma", id);
            param.put("restriccion", restriccion != null ? restriccion : "desconocida");
            log.warn("No se pudo eliminar el programa {} por la restricción {}", id, restriccion);
            throw new DomainException(ProgramaErrorCode.PROGRAMA_REFERENCIADO_CONFLICT, param);
        }
    }

    /**
     * Cuenta lo que impide eliminar un programa y arma la lista de conflictos
     * en orden de prioridad: estudiantes &gt; cohortes &gt; director.
     */
    public ProgramaDependenciasOutput evaluarDependencias(Integer idPrograma) {
        ProgramaDTO programa = service.findById(idPrograma);
        boolean directorAsignado = programa != null && programa.getDirectorId() != null;

        long estudiantes = estudiantesService.countByProgramaId(idPrograma);
        long cohortes = cohorteService.countByIdPrograma(idPrograma);

        List<Integer> idsCargo = cargoService.findByIdPrograma(idPrograma).stream()
                .map(CargoDTO::getId)
                .toList();
        long administrativos = administrativoService.countByIdCargoIn(idsCargo);

        List<String> conflictos = new ArrayList<>();
        if (estudiantes > 0) {
            conflictos.add(ProgramaErrorCode.PROGRAMA_CON_ESTUDIANTES_CONFLICT.name());
        }
        if (cohortes > 0) {
            conflictos.add(ProgramaErrorCode.PROGRAMA_CON_COHORTES_CONFLICT.name());
        }
        if (directorAsignado || administrativos > 0) {
            conflictos.add(ProgramaErrorCode.PROGRAMA_CON_DIRECTOR_CONFLICT.name());
        }

        return new ProgramaDependenciasOutput(idPrograma, estudiantes, cohortes, directorAsignado, administrativos,
                conflictos);
    }

    /**
     * Borra lo que pertenece al programa, en orden: documentos requisito,
     * criterios de evaluación y cargos. Las cohortes, estudiantes y
     * administrativos ya se descartaron en {@link #evaluarDependencias}.
     */
    private void eliminarHijosDelPrograma(Integer id) {
        List<DocumentosrequisitoprogramaEntity> documentos = documentosrequisitoprogramaRepository.findByIdPrograma(id);
        if (!documentos.isEmpty()) {
            documentosrequisitoprogramaRepository.deleteAllInBatch(documentos);
            // Pendiente D-05: los archivos en S3 no se borran aquí.
            log.info("Programa {}: se eliminan {} documentos requisito. URLs que pueden quedar huérfanas en S3: {}",
                    id, documentos.size(), documentos.stream()
                            .map(DocumentosrequisitoprogramaEntity::getUrlformato)
                            .filter(Objects::nonNull)
                            .toList());
        }

        List<CriterioevaluacionEntity> criterios = criterioevaluacionRepository.findByIdPrograma(id);
        if (!criterios.isEmpty()) {
            criterioevaluacionRepository.deleteAllInBatch(criterios);
        }

        List<CargoEntity> cargos = cargoRepository.findByIdPrograma(id);
        if (!cargos.isEmpty()) {
            cargoRepository.deleteAllInBatch(cargos);
        }
    }

    // #region PERSONALIZADOS

    public List<ProgramaOutput> findByIdFacultad(Integer idFacultad) {
        try {
            return map.toOutputList(service.findByIdFacultad(idFacultad));
        } catch (Exception e) {
            throw new RuntimeException("Error finding Programas by Facultad ID: " + e.getMessage(), e);
        }
    }

    public List<ModalidadOutput> findModalidadesByPrograma(Integer programaId) {
        try {
            ProgramaDTO programa = service.findById(programaId);
            if (programa == null) {
                throw new DomainException(ProgramaErrorCode.PROGRAMA_NOT_FOUND, programaId);
            }

            String tipoRegistro = programa.getTiporegistro() != null ? programa.getTiporegistro().getTipo() : null;
            if (tipoRegistro == null) {
                throw new DomainException(ProgramaErrorCode.PROGRAMA_SIN_TIPO_REGISTRO, programaId);
            }

            if (TipoRegistro.UNICO.matches(tipoRegistro)) {
                return modalidadService.findAll().stream()
                        .filter(modalidad -> modalidad != null
                                && modalidad.getNombre() != null
                                && !Modalidad.HIBRIDA.matches(modalidad.getNombre()))
                        .map(this::toModalidadOutput)
                        .toList();
            }

            if (TipoRegistro.ESTANDAR.matches(tipoRegistro)) {
                if (programa.getIdModalidad() == null) {
                    throw new DomainException(ProgramaErrorCode.PROGRAMA_SIN_MODALIDAD_ASIGNADA, programaId);
                }

                ModalidadDTO modalidad = modalidadService.findById(programa.getIdModalidad());
                if (modalidad == null) {
                    throw new DomainException(ProgramaErrorCode.MODALIDAD_NOT_FOUND, programa.getIdModalidad());
                }

                return List.of(toModalidadOutput(modalidad));
            }

            throw new DomainException(ProgramaErrorCode.PROGRAMA_TIPO_REGISTRO_NO_VALIDO, tipoRegistro);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error finding modalidades by programa: " + e.getMessage(), e);
        }
    }

    private ModalidadOutput toModalidadOutput(ModalidadDTO dto) {
        return ModalidadOutput.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .build();
    }

    public long countAspirantesEnProcesoEnCohorteAbierta(Integer cohorteId) {
        try {
            return cohorteProcessor.countAspirantesEnProcesoEnCohorteAbierta(cohorteId);
        } catch (Exception e) {
            throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, cohorteId);
        }
    }

    public ProgramaOutput createWithRelations(PROGRAMA_CREATE_WITH_RELATIONS input, Integer idFacultad) {
        try {
            ProgramaDTO dto = buildDtoFromRelations(
                    input.codigo(),
                    input.nombre(),
                    input.duracion(),
                    input.correo(),
                    input.registrosnies(),
                    input.nivelformacion(),
                    input.titulo(),
                    input.rcmineducacion(),
                    input.creditos(),
                    input.periodicidad(),
                    input.valormatricula(),
                    input.sedeNombre(),
                    input.tiporegistroTipo(),
                    input.otrosvalores(),
                    idFacultad);
            ProgramaDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Programa with relations: " + e.getMessage(), e);
        }
    }

    public ProgramaOutput updateWithRelations(PROGRAMA_UPDATE_WITH_RELATIONS input, Integer idFacultad) {
        try {
            ProgramaDTO dto = buildDtoFromRelations(
                    input.codigo(),
                    input.nombre(),
                    input.duracion(),
                    input.correo(),
                    input.registrosnies(),
                    input.nivelformacion(),
                    input.titulo(),
                    input.rcmineducacion(),
                    input.creditos(),
                    input.periodicidad(),
                    input.valormatricula(),
                    input.sedeNombre(),
                    input.tiporegistroTipo(),
                    input.otrosvalores(),
                    idFacultad);
            ProgramaDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Programa with relations: " + e.getMessage(), e);
        }
    }

        private ProgramaDTO buildDtoFromRelations(
            String codigo,
            String nombre,
            Integer duracion,
            String correo,
            String registrosnies,
            String nivelformacion,
            String titulo,
            String rcmineducacion,
            Integer creditos,
            String periodicidad,
            BigDecimal valormatricula,
            String sedeNombre,
            String tiporegistroTipo,
            OTROSVALORES_CREATE otrosvalores,
            Integer idFacultad) {

        if (idFacultad == null) {
            throw new DomainException(ProgramaErrorCode.PROGRAMA_PARAMETRO_REQUERIDO, "idFacultad");
        }
        if (sedeNombre == null || sedeNombre.isBlank()) {
            throw new DomainException(ProgramaErrorCode.PROGRAMA_PARAMETRO_REQUERIDO, "sedeNombre");
        }
        if (tiporegistroTipo == null || tiporegistroTipo.isBlank()) {
            throw new DomainException(ProgramaErrorCode.PROGRAMA_PARAMETRO_REQUERIDO, "tiporegistroTipo");
        }
        if (otrosvalores == null || otrosvalores.carnet() == null
                || otrosvalores.estampilla() == null || otrosvalores.seguro() == null) {
            throw new DomainException(ProgramaErrorCode.PROGRAMA_PARAMETRO_REQUERIDO, "otrosvalores");
        }

        SedeDTO sede = sedeService.findFirstByNombre(sedeNombre.trim());
        if (sede == null || sede.getId() == null) {
            throw new DomainException(SedeErrorCode.SEDE_NOT_FOUND, sedeNombre);
        }

        TiporegistroDTO tiporegistro = tiporegistroService.findByTipo(tiporegistroTipo.trim());
        if (tiporegistro == null || tiporegistro.getId() == null) {
            throw new DomainException(ProgramaErrorCode.TIPOREGISTRO_NOT_FOUND, tiporegistroTipo);
        }

        OtrosvaloresDTO otros = otrosvaloresService.findByValores(
                otrosvalores.carnet(),
                otrosvalores.estampilla(),
                otrosvalores.seguro());
        if (otros == null || otros.getId() == null) {
            throw new DomainException(OtrosvaloresErrorCode.OTROSVALORES_NOT_FOUND, otrosvalores);
        }

        return ProgramaDTO.builder()
                .codigo(codigo)
                .nombre(nombre)
            .duracion(duracion)
                .correo(correo)
                .registrosnies(registrosnies)
                .nivelformacion(nivelformacion)
                .titulo(titulo)
                .rcmineducacion(rcmineducacion)
                .creditos(creditos)
                .periodicidad(periodicidad)
                .valormatricula(valormatricula)
                .idSede(sede.getId())
                .idFacultad(idFacultad)
                .idOtros(otros.getId())
                .idTiporegistro(tiporegistro.getId())
                .build();
    }

    // #endregion
}
