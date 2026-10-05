package ufps.edu.co.services.core.crud;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;
import ufps.edu.co.maps.specific.CriterioevaluacionMap;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.*;
import ufps.edu.co.records.output.entity.CriterioevaluacionOutput;
import ufps.edu.co.rest.dto.CalificacioncriterioDTO;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.CriterioevaluacionDTO;
import ufps.edu.co.rest.dto.CriteriocohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CalificacioncriterioService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.CriterioevaluacionService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class CriterioevaluacionCoreService implements
        GlobalUseCase<CRITERIOEVALUACION_CREATE, CRITERIOEVALUACION_UPDATE, CRITERIOEVALUACION_DELETE, CRITERIOEVALUACION_PATCH, CRITERIOEVALUACION_FIND, CriterioevaluacionOutput> {

    @Autowired
    private CriterioevaluacionService service;

    @Autowired
    private CriterioevaluacionMap map;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private CalificacioncriterioService calificacioncriterioService;

    @Autowired
    private CalificacioncriterioCoreService calificacioncriterioProcessor;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private CriteriocohorteService criteriocohorteService;

    @Autowired
    private EstadoService estadoService;

    @Override
    public CriterioevaluacionOutput create(CRITERIOEVALUACION_CREATE input) {
        CriterioevaluacionDTO dto = map.toDto(input);
        CriterioevaluacionOutput output = map.toOutput(service.create(dto));
        marcarAspirantesEnProgreso(input.idPrograma());
        return output;
    }

    @Override
    public CriterioevaluacionOutput update(CRITERIOEVALUACION_UPDATE input) {
        try {
            assertCriterioSinCalificaciones(input.id());
            CriterioevaluacionDTO old = service.findById(input.id());
            BigDecimal oldPeso = old != null ? old.getPeso() : null;
            CriterioevaluacionDTO dto = map.toDto(input);
            CriterioevaluacionOutput output = map.toOutput(service.update(input.id(), dto));
            if (input.peso() != null && oldPeso != null && input.peso().compareTo(oldPeso) != 0) {
                calificacioncriterioProcessor.actualizarPesoSnapshotYRecalcular(input.id(), input.peso());
            }
            return output;
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIOEVALUACION_NOT_FOUND, input.id());
        }
    }

    @Override
    public CriterioevaluacionOutput patch(CRITERIOEVALUACION_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Criterioaceptacion");
    }

    @Override
    public CriterioevaluacionOutput findById(CRITERIOEVALUACION_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIOEVALUACION_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<CriterioevaluacionOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();
    }

    @Override
    public void deleteById(CRITERIOEVALUACION_DELETE input) {
        assertCriterioSinCalificaciones(input.id());
        CriterioevaluacionDTO criterio = service.findById(input.id());
        Integer programaId = criterio != null ? criterio.getIdprograma() : null;
        try {
            service.deleteById(input.id());
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIOEVALUACION_NOT_FOUND, input.id());
        }
        if (programaId != null) {
            recalcularEstadosTrasBorrado(programaId);
        }
    }

    public CriterioevaluacionOutput createForCohorte(Integer programaId, Integer cohorteId,
            CRITERIO_CREATE_BODY body) {
        validateCohortePerteneceProg(programaId, cohorteId);
        return create(CRITERIOEVALUACION_CREATE.builder()
                .nombre(body.nombre())
                .activo(body.activo() != null ? body.activo() : true)
                .descripcion(body.descripcion())
                .peso(body.peso())
                .idPrograma(programaId)
                .build());
    }

    public CriterioevaluacionOutput createForPrograma(Integer programaId, CRITERIO_CREATE_BODY body) {
        return create(CRITERIOEVALUACION_CREATE.builder()
                .nombre(body.nombre())
                .activo(body.activo() != null ? body.activo() : true)
                .descripcion(body.descripcion())
                .peso(body.peso())
                .idPrograma(programaId)
                .build());
    }

    public CriterioevaluacionOutput updateForPrograma(Integer programaId, Integer criterioId,
            CRITERIO_UPDATE_BODY body) {
        CriterioevaluacionDTO criterio = service.findById(criterioId);
        if (criterio == null || !programaId.equals(criterio.getIdprograma())) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, criterioId);
        }
        return update(CRITERIOEVALUACION_UPDATE.builder()
                .id(criterioId)
                .nombre(body.nombre())
                .activo(body.activo())
                .descripcion(body.descripcion())
                .peso(body.peso())
                .idPrograma(programaId)
                .build());
    }

    public void deleteForPrograma(Integer programaId, Integer criterioId) {
        CriterioevaluacionDTO criterio = service.findById(criterioId);
        if (criterio == null || !programaId.equals(criterio.getIdprograma())) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, criterioId);
        }
        deleteById(new CRITERIOEVALUACION_DELETE(criterioId));
    }

    public CriterioevaluacionOutput desactivarCriterio(Integer programaId, Integer criterioId) {
        CriterioevaluacionDTO criterio = service.findById(criterioId);
        if (criterio == null || !programaId.equals(criterio.getIdprograma())) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, criterioId);
        }
        assertCriterioSinCalificaciones(criterioId);
        criterio.setActivo(false);
        return map.toOutput(service.update(criterioId, criterio));
    }

    public List<CriterioevaluacionOutput> findByIdPrograma(Integer programaId) {
        return service.findByIdPrograma(programaId).stream().map(map::toOutput).toList();
    }

    public CriterioevaluacionOutput updateForCohorte(Integer programaId, Integer cohorteId,
            Integer criterioId, CRITERIO_UPDATE_BODY body) {
        validateCohortePerteneceProg(programaId, cohorteId);
        validateCriterioPertenece(criterioId, cohorteId);
        return update(
            CRITERIOEVALUACION_UPDATE.builder()
                .id(criterioId)
                .nombre(body.nombre())
                .activo(body.activo())
                .descripcion(body.descripcion())
                .peso(body.peso())
                .idPrograma(programaId)
                .build()
        );
    }

    public void deleteForCohorte(Integer programaId, Integer cohorteId, Integer criterioId) {
        validateCohortePerteneceProg(programaId, cohorteId);
        validateCriterioPertenece(criterioId, cohorteId);
        deleteById(new CRITERIOEVALUACION_DELETE(criterioId));
    }

    public void bulkSaveForCohorte(Integer programaId, Integer cohorteId, CRITERIO_BULK_SAVE input) {
        validateCohortePerteneceProg(programaId, cohorteId);

        List<CriterioevaluacionDTO> existing = service.findByIdPrograma(programaId);
        Set<Integer> existingIds = existing.stream()
                .map(CriterioevaluacionDTO::getId)
                .collect(Collectors.toSet());

        // Validate all provided ids belong to this cohorte before saving anything
        for (CRITERIO_BULK_ITEM item : input.criterios()) {
            if (item.id() != null && !existingIds.contains(item.id())) {
                throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, item.id());
            }
        }

        for (CRITERIO_BULK_ITEM item : input.criterios()) {
            CriterioevaluacionDTO dto = CriterioevaluacionDTO.builder()
                    .nombre(item.nombre())
                    .descripcion(item.descripcion())
                    .peso(item.peso())
                    .idprograma(programaId)
                    .build();
            if (item.id() != null) {
                assertCriterioSinCalificaciones(item.id());
                CriterioevaluacionDTO old = service.findById(item.id());
                BigDecimal oldPeso = old != null ? old.getPeso() : null;
                service.update(item.id(), dto);
                if (item.peso() != null && oldPeso != null && item.peso().compareTo(oldPeso) != 0) {
                    calificacioncriterioProcessor.actualizarPesoSnapshotYRecalcular(item.id(), item.peso());
                }
            } else {
                service.create(dto);
            }
        }
    }

    private void validateCohortePerteneceProg(Integer programaId, Integer cohorteId) {
        CohorteDTO cohorte = cohorteService.findById(cohorteId);
        if (cohorte == null) {
            throw new DomainException(CriterioevaluacionErrorCode.COHORTE_PROGRAMA_MISMATCH, cohorteId);
        }
        if (!programaId.equals(cohorte.getIdPrograma())) {
            throw new DomainException(CriterioevaluacionErrorCode.COHORTE_PROGRAMA_MISMATCH, cohorteId);
        }
    }

    private void validateCriterioPertenece(Integer criterioId, Integer programaId) {
        CriterioevaluacionDTO criterio = service.findById(criterioId);
        if (criterio == null || !programaId.equals(criterio.getIdprograma())) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, criterioId);
        }
    }

    private void recalcularEstadosTrasBorrado(Integer programaId) {
        EstadoDTO estadoCalificado = estadoService.findByTipoAndEntidad("VALIDADO_CALIFICADO", "aspirante");
        if (estadoCalificado == null) return;

        cohorteService.findByIdPrograma(programaId).forEach(cohorte -> {
            Set<Integer> criteriocohorteIds = criteriocohorteService.findByIdCohorte(cohorte.getId()).stream()
                    .map(CriteriocohorteDTO::getId)
                    .collect(Collectors.toSet());

            aspiranteService.findByCohorte(cohorte.getId()).stream()
                    .filter(a -> a.getEstado() != null
                            && "VALIDADO_EN_PROGRESO".equalsIgnoreCase(a.getEstado().getTipo()))
                    .forEach(aspirante -> {
                        Set<Integer> calificadosIds = calificacioncriterioService
                                .findByIdAspirante(aspirante.getId()).stream()
                                .map(CalificacioncriterioDTO::getIdCriteriocohorte)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
                        boolean todosCalificados = !criteriocohorteIds.isEmpty()
                                && criteriocohorteIds.stream().allMatch(calificadosIds::contains);
                        if (todosCalificados) {
                            aspiranteService.updateEstado(aspirante.getId(), estadoCalificado.getId());
                        }
                    });
        });
    }

    private void marcarAspirantesEnProgreso(Integer programaId) {
        EstadoDTO estadoEnProgreso = estadoService.findByTipoAndEntidad("VALIDADO_EN_PROGRESO", "aspirante");
        if (estadoEnProgreso == null) return;
        cohorteService.findByIdPrograma(programaId).forEach(cohorte ->
            aspiranteService.findByCohorte(cohorte.getId()).stream()
                    .filter(a -> a.getEstado() != null
                            && "VALIDADO_CALIFICADO".equalsIgnoreCase(a.getEstado().getTipo()))
                    .forEach(a -> aspiranteService.updateEstado(a.getId(), estadoEnProgreso.getId()))
        );
    }

    private void assertCriterioSinCalificaciones(Integer criterioId) {
        boolean tieneCalificaciones = criteriocohorteService.findByIdCriterio(criterioId).stream()
                .anyMatch(cc -> calificacioncriterioService.existsByCriterio(cc.getId()));
        if (tieneCalificaciones) {
            throw new DomainException(CriterioevaluacionErrorCode.CRITERIO_CON_CALIFICACIONES_BLOQUEADO, criterioId);
        }
    }

}
