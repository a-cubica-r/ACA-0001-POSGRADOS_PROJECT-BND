package ufps.edu.co.processor.crud;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CalificacioncriterioErrorCode;
import ufps.edu.co.maps.specific.CalificacioncriterioMap;
import ufps.edu.co.records.input.entity.CalificacioncriterioInput.*;
import ufps.edu.co.records.output.entity.CalificacionCriterioSimpleOutput;
import ufps.edu.co.records.output.entity.CalificacioncriterioOutput;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.CalificacioncriterioDTO;
import ufps.edu.co.rest.dto.CriteriocohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CalificacioncriterioService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.services.*;
import ufps.edu.co.utils.*;
import ufps.edu.co.usecase.GlobalUseCase;

@Service
public class CalificacioncriterioProcessor implements
        GlobalUseCase<CALIFICACIONCRITERIO_CREATE, CALIFICACIONCRITERIO_UPDATE, CALIFICACIONCRITERIO_DELETE, CALIFICACIONCRITERIO_PATCH, CALIFICACIONCRITERIO_FIND, CalificacioncriterioOutput> {

    private static final Logger logger = LoggerFactory.getLogger(CalificacioncriterioProcessor.class);

    @Autowired
    private CalificacioncriterioService service;

    @Autowired
    private CalificacioncriterioMap map;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private CriteriocohorteService criteriocohorteService;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private SESService sesService;

    // @Autowired
    // private EmailTemplates emailTemplates;

    @Override
    public CalificacioncriterioOutput create(CALIFICACIONCRITERIO_CREATE input) {
        if (service.existsByAspiranteAndCriterio(input.idAspirante(), input.idCriterio())) {
            throw new DomainException(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_ALREADY_EXISTS,
                    "aspirante=" + input.idAspirante() + ", criterio=" + input.idCriterio());
        }
        try {
            CalificacioncriterioDTO dto = map.toDto(input);
            CalificacioncriterioOutput output = map.toOutput(service.create(dto));
            recalcularPuntuacionAspirante(input.idAspirante());
            actualizarEstadoCalificacion(input.idAspirante());
            return output;
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Calificacioncriterio: " + e.getMessage(), e);
        }
    }

    @Override
    public CalificacioncriterioOutput update(CALIFICACIONCRITERIO_UPDATE input) {
        try {
            CalificacioncriterioDTO dto = map.toDto(input);
            CalificacioncriterioOutput output = map.toOutput(service.update(input.id(), dto));
            recalcularPuntuacionAspirante(input.idAspirante());
            actualizarEstadoCalificacion(input.idAspirante());
            return output;
        } catch (Exception e) {
            throw new DomainException(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, input.id());
        }
    }

    @Override
    public CalificacioncriterioOutput patch(CALIFICACIONCRITERIO_PATCH input) {
        throw new UnsupportedOperationException("Patch operation is not supported for Calificacioncriterio");
    }

    @Override
    public CalificacioncriterioOutput findById(CALIFICACIONCRITERIO_FIND input) {
        try {
            return map.toOutput(service.findById(input.id()));
        } catch (Exception e) {
            throw new DomainException(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, input.id());
        }
    }

    @Override
    public List<CalificacioncriterioOutput> findAll() {
        return service.findAll().stream().map(map::toOutput).toList();
    }

    @Override
    public void deleteById(CALIFICACIONCRITERIO_DELETE input) {
        try {
            service.deleteById(input.id());
        } catch (Exception e) {
            throw new DomainException(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, input.id());
        }
    }

    public List<CalificacioncriterioOutput> findByIdAspirante(Integer idAspirante) {
        return service.findByIdAspirante(idAspirante).stream().map(map::toOutput).toList();
    }

    public List<CalificacioncriterioOutput> findByIdCriterio(Integer idCriterio) {
        return service.findByIdCriterio(idCriterio).stream().map(map::toOutput).toList();
    }

    public CalificacionCriterioSimpleOutput calificarCriterio(Integer idAspirante, Integer idCriterio,
            BigDecimal puntaje) {
        CriteriocohorteDTO criteriocohorte = criteriocohorteService.findById(idCriterio);
        if (criteriocohorte == null) {
            throw new DomainException(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, idCriterio);
        }

        AspiranteDTO aspiranteValidacion = aspiranteService.findById(idAspirante);
        if (aspiranteValidacion == null
                || !java.util.Objects.equals(aspiranteValidacion.getIdCohorte(), criteriocohorte.getIdCohorte())) {
            throw new DomainException(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND,
                    "El aspirante no pertenece a la cohorte del criterio");
        }

        BigDecimal pesoMaximo = criteriocohorte.getPesoSnapshot();
        if (pesoMaximo == null) {
            throw new DomainException(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, idCriterio);
        }
        if (puntaje != null && (puntaje.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0
            || puntaje.compareTo(BigDecimal.ZERO) < 0)) {
            throw new DomainException(CalificacioncriterioErrorCode.PUNTUACION_INVALIDA, puntaje);
        }
        if (puntaje != null && puntaje.compareTo(pesoMaximo) > 0) {
            throw new DomainException(CalificacioncriterioErrorCode.PUNTAJE_EXCEDE_MAXIMO, pesoMaximo.stripTrailingZeros().toPlainString());
        }
        CalificacioncriterioOutput result = service.findByIdAspiranteAndIdCriterio(idAspirante, idCriterio)
                .map(existing -> {
                    CALIFICACIONCRITERIO_UPDATE updateInput = new CALIFICACIONCRITERIO_UPDATE(
                            existing.getId(), idAspirante, idCriterio, puntaje);
                    return update(updateInput);
                })
                .orElseGet(() -> {
                    CALIFICACIONCRITERIO_CREATE createInput = new CALIFICACIONCRITERIO_CREATE(
                            idAspirante, idCriterio, puntaje);
                    return create(createInput);
                });
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        String nombreCriterio = criteriocohorte.getCriterioevaluacion() != null
                ? criteriocohorte.getCriterioevaluacion().getNombre() : "Criterio";
        try {
            sesService.enviarCorreo(aspirante.getPersona().getCorreo(), EmailTemplates.ASUNTO_CALIFICACION_CRITERIO,
                    EmailTemplates.cuerpoCalificacionCriterio(aspirante.getPersona().getNombres(), nombreCriterio,
                            result.puntuacion(), aspirante.getPuntuacion()));
        } catch (Exception ex) {
            logger.error("[CALIFICACION_EMAIL] Fallo al enviar correo de calificación a '{}': {}",
                    aspirante.getPersona().getCorreo(), ex.getMessage(), ex);
        }
        return CalificacionCriterioSimpleOutput.builder()
                .idAspirante(result.idAspirante())
                .idCriterio(result.idCriteriocohorte())
                .puntajeObtenido(result.puntuacion())
                .puntajeTotal(aspirante.getPuntuacion())
                .build();
    }

    private void actualizarEstadoCalificacion(Integer idAspirante) {
        try {
            AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
            if (aspirante == null || aspirante.getIdCohorte() == null) return;

            List<CriteriocohorteDTO> criteriosCohorte = criteriocohorteService.findByIdCohorte(aspirante.getIdCohorte());
            if (criteriosCohorte.isEmpty()) return;

            List<CalificacioncriterioDTO> calificaciones = service.findByIdAspirante(idAspirante);
            java.util.Set<Integer> calificadosIds = calificaciones.stream()
                    .filter(c -> c.getPuntuacion() != null)
                    .map(CalificacioncriterioDTO::getIdCriteriocohorte)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());

            boolean todosCalificados = criteriosCohorte.stream()
                    .map(CriteriocohorteDTO::getId)
                    .allMatch(calificadosIds::contains);

            if (todosCalificados) {
                EstadoDTO estado = estadoService.findByTipoAndEntidad("VALIDADO_CALIFICADO", "aspirante");
                if (estado != null) aspiranteService.updateEstado(idAspirante, estado.getId());
            } else if (!calificadosIds.isEmpty()) {
                EstadoDTO estado = estadoService.findByTipoAndEntidad("VALIDADO_EN_PROGRESO", "aspirante");
                if (estado != null) aspiranteService.updateEstado(idAspirante, estado.getId());
            }
        } catch (Exception e) {
            logger.warn("No se pudo actualizar estado de calificación del aspirante {}: {}", idAspirante, e.getMessage());
        }
    }

    public void actualizarPesoSnapshotYRecalcular(Integer idCriterio, BigDecimal newPeso) {
        List<CalificacioncriterioDTO> calificaciones = service.findByIdCriterio(idCriterio);
        calificaciones.forEach(cal -> {
            service.update(cal.getId(), cal);
        });
        calificaciones.stream()
                .map(CalificacioncriterioDTO::getIdAspirante)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(this::recalcularPuntuacionAspirante);
    }

    public void recalcularPuntuacionAspirantePublic(Integer idAspirante) {
        recalcularPuntuacionAspirante(idAspirante);
    }

    private void recalcularPuntuacionAspirante(Integer idAspirante) {
        List<CalificacioncriterioDTO> calificaciones = service.findByIdAspirante(idAspirante);
        BigDecimal total = calificaciones.stream()
                .filter(c -> c.getPuntuacion() != null)
                .map(CalificacioncriterioDTO::getPuntuacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        aspirante.setPuntuacion(total);
        aspiranteService.update(idAspirante, aspirante);
    }
}
