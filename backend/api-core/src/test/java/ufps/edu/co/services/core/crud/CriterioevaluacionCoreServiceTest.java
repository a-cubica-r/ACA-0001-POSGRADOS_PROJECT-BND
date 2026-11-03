package ufps.edu.co.services.core.crud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;
import ufps.edu.co.maps.specific.CriterioevaluacionMap;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_UPDATE_BODY;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIOEVALUACION_UPDATE;
import ufps.edu.co.records.output.entity.CriterioevaluacionOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.CriterioevaluacionDTO;
import ufps.edu.co.rest.dto.CriteriocohorteDTO;
import ufps.edu.co.rest.services.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class CriterioevaluacionCoreServiceTest {

    @Mock private CriterioevaluacionService service;
    @Mock private CriterioevaluacionMap map;
    @Mock private CohorteService cohorteService;
    @Mock private CalificacioncriterioService calificacioncriterioService;
    @Mock private CalificacioncriterioCoreService calificacioncriterioCoreService;
    @Mock private AspiranteService aspiranteService;
    @Mock private CriteriocohorteService criteriocohorteService;
    @Mock private EstadoService estadoService;

    @InjectMocks
    private CriterioevaluacionCoreService coreService;

    // ─── updateForPrograma: criterio no pertenece al programa ─────────────────

    @Test
    void updateForPrograma_criterioNoEncontrado_lanzaCriterioCohortemismatch() {
        when(service.findById(10)).thenReturn(null);

        CRITERIO_UPDATE_BODY body = CRITERIO_UPDATE_BODY.builder()
                .nombre("Nombre").activo(true).descripcion("Desc")
                .peso(new BigDecimal("20")).build();

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.updateForPrograma(1, 10, body));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    @Test
    void updateForPrograma_criterioDeOtroPrograma_lanzaCriterioCohortemismatch() {
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(99).build();
        when(service.findById(10)).thenReturn(criterio);

        CRITERIO_UPDATE_BODY body = CRITERIO_UPDATE_BODY.builder()
                .nombre("Nuevo").activo(true).descripcion("Desc")
                .peso(new BigDecimal("20")).build();

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.updateForPrograma(1, 10, body));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    // ─── deleteForPrograma: criterio no pertenece al programa ─────────────────

    @Test
    void deleteForPrograma_criterioNoEncontrado_lanzaCriterioCohortemismatch() {
        when(service.findById(10)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.deleteForPrograma(1, 10));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    @Test
    void deleteForPrograma_criterioDeOtroPrograma_lanzaCriterioCohortemismatch() {
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(5).build();
        when(service.findById(10)).thenReturn(criterio);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.deleteForPrograma(1, 10));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    // ─── createForCohorte: cohorte no pertenece al programa ───────────────────

    @Test
    void createForCohorte_cohorteNoEncontrada_lanzaCohorteProgmramaMismatch() {
        when(cohorteService.findById(5)).thenReturn(null);

        ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY body =
                ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY.builder()
                        .nombre("Crit").activo(true).descripcion("Desc")
                        .peso(new BigDecimal("20")).build();

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.createForCohorte(1, 5, body));
        assertEquals(CriterioevaluacionErrorCode.COHORTE_PROGRAMA_MISMATCH, ex.getCode());
    }

    @Test
    void createForCohorte_cohorteDeOtroPrograma_lanzaCohorteProgmramaMismatch() {
        CohorteDTO cohorte = CohorteDTO.builder().id(5).idPrograma(9).build();
        when(cohorteService.findById(5)).thenReturn(cohorte);

        ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY body =
                ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY.builder()
                        .nombre("Crit").activo(true).descripcion("Desc")
                        .peso(new BigDecimal("20")).build();

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.createForCohorte(1, 5, body));
        assertEquals(CriterioevaluacionErrorCode.COHORTE_PROGRAMA_MISMATCH, ex.getCode());
    }

    // ─── desactivarCriterio: criterio con calificaciones no puede desactivarse ─

    @Test
    void desactivarCriterio_criterioConCalificaciones_lanzaCalificacionesBloqueado() {
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(1).activo(true).build();
        CriteriocohorteDTO criteriocohorte = CriteriocohorteDTO.builder().id(55).build();
        when(service.findById(10)).thenReturn(criterio);
        when(criteriocohorteService.findByIdCriterio(10)).thenReturn(List.of(criteriocohorte));
        when(calificacioncriterioService.existsByCriterio(55)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.desactivarCriterio(1, 10));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_CON_CALIFICACIONES_BLOQUEADO, ex.getCode());
    }

    @Test
    void desactivarCriterio_sinCalificaciones_desactivaExitosamente() {
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(1).activo(true).build();
        CriterioevaluacionOutput outputEsperado = CriterioevaluacionOutput.builder()
                .id(10).nombre("Crit").activo(false).build();
        when(service.findById(10)).thenReturn(criterio);
        when(criteriocohorteService.findByIdCriterio(10)).thenReturn(List.of());
        when(service.update(eq(10), any())).thenReturn(criterio);
        when(map.toOutput(criterio)).thenReturn(outputEsperado);

        CriterioevaluacionOutput resultado = coreService.desactivarCriterio(1, 10);

        assertNotNull(resultado);
        verify(service).update(eq(10), argThat(dto -> Boolean.FALSE.equals(dto.getActivo())));
    }

    @Test
    void desactivarCriterio_criterioDeOtroPrograma_lanzaCriterioCohortemismatch() {
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(99).activo(true).build();
        when(service.findById(10)).thenReturn(criterio);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.desactivarCriterio(1, 10));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    // ─── update: la integridad se propaga; solo "no encontrado" sigue siendo 404 ─

    @Test
    void update_conViolacionDeIntegridad_propagaLaExcepcion() {
        CriterioevaluacionDTO actual = CriterioevaluacionDTO.builder()
                .id(10).peso(new BigDecimal("20")).build();
        when(service.findById(10)).thenReturn(actual);
        when(service.update(eq(10), any())).thenThrow(new DataIntegrityViolationException("duplicado"));

        CRITERIOEVALUACION_UPDATE input = CRITERIOEVALUACION_UPDATE.builder()
                .id(10).nombre("Repetido").peso(new BigDecimal("20")).idPrograma(1).build();

        assertThrows(DataIntegrityViolationException.class, () -> coreService.update(input));
    }

    @Test
    void update_conRegistroNoEncontrado_sigueDandoNotFound() {
        CriterioevaluacionDTO actual = CriterioevaluacionDTO.builder()
                .id(11).peso(new BigDecimal("20")).build();
        when(service.findById(11)).thenReturn(actual);
        when(service.update(eq(11), any())).thenThrow(new RuntimeException("no existe"));

        CRITERIOEVALUACION_UPDATE input = CRITERIOEVALUACION_UPDATE.builder()
                .id(11).nombre("X").peso(new BigDecimal("20")).idPrograma(1).build();

        DomainException ex = assertThrows(DomainException.class, () -> coreService.update(input));
        assertEquals(CriterioevaluacionErrorCode.CRITERIOEVALUACION_NOT_FOUND, ex.getCode());
    }
}
