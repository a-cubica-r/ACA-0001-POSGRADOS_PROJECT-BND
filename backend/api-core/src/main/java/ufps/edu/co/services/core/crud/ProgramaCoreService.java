package ufps.edu.co.services.core.crud;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.OtrosvaloresErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.ProgramaErrorCode;
import ufps.edu.co.domain.exceptions.errorcodes.SedeErrorCode;
import ufps.edu.co.maps.specific.ProgramaMap;
import ufps.edu.co.records.input.entity.OtrosvaloresInput.OTROSVALORES_CREATE;
import ufps.edu.co.records.input.entity.ProgramaInput.*;
import ufps.edu.co.records.output.entity.ModalidadOutput;
import ufps.edu.co.records.output.entity.ProgramaOutput;
import ufps.edu.co.rest.dto.OtrosvaloresDTO;
import ufps.edu.co.rest.dto.ProgramaDTO;
import ufps.edu.co.rest.dto.SedeDTO;
import ufps.edu.co.rest.dto.TiporegistroDTO;
import ufps.edu.co.rest.dto.ModalidadDTO;
import ufps.edu.co.rest.services.ModalidadService;
import ufps.edu.co.rest.services.OtrosvaloresService;
import ufps.edu.co.rest.services.ProgramaService;
import ufps.edu.co.rest.services.SedeService;
import ufps.edu.co.rest.services.TiporegistroService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class ProgramaCoreService implements
        GlobalUseCase<PROGRAMA_CREATE, PROGRAMA_UPDATE, PROGRAMA_DELETE, PROGRAMA_PATCH, PROGRAMA_FIND, ProgramaOutput> {

    @Autowired
    private ProgramaService service;

    @Autowired
    private ProgramaMap map;

    @Autowired
    private CohorteCoreService cohorteProcessor;

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
            ProgramaDTO created = service.create(dto);
            return map.toOutput(created);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Programa: " + e.getMessage(), e);
        }
    }

    private void resolveModalidad(ProgramaDTO dto) {
        if (Integer.valueOf(1).equals(dto.getIdTiporegistro())) {
            ModalidadDTO hibrida = modalidadService.findByNombre("HIBRIDA");
            if (hibrida == null) {
                throw new DomainException(ProgramaErrorCode.MODALIDAD_NOT_FOUND, "HIBRIDA");
            }
            dto.setIdModalidad(hibrida.getId());
        } else if (Integer.valueOf(2).equals(dto.getIdTiporegistro())) {
            if (dto.getIdModalidad() == null) {
                throw new DomainException(ProgramaErrorCode.PROGRAMA_PARAMETRO_REQUERIDO, "idModalidad");
            }
        }
    }

    @Override
    public ProgramaOutput update(PROGRAMA_UPDATE input) {
        try {
            ProgramaDTO dto = map.toDto(input);
            ProgramaDTO updated = service.update(input.id(), dto);
            return map.toOutput(updated);
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
    public void deleteById(PROGRAMA_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Programa: " + e.getMessage(), e);
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

            if ("UNICO".equalsIgnoreCase(tipoRegistro)) {
                return modalidadService.findAll().stream()
                        .filter(modalidad -> modalidad != null
                                && modalidad.getNombre() != null
                                && !"HIBRIDA".equalsIgnoreCase(modalidad.getNombre()))
                        .map(this::toModalidadOutput)
                        .toList();
            }

            if ("ESTANDAR".equalsIgnoreCase(tipoRegistro)) {
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
