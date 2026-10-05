package ufps.edu.co.processor.crud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;
import ufps.edu.co.maps.specific.CriterioevaluacionMap;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_UPDATE_BODY;
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

/**
 * DEPENDE_DE_PROCESSOR - Tests sobre CriterioevaluacionProcessor.
 * Cuando se elimine la capa Processor y la lógica pase a servicio de dominio,
 * reescribir estas pruebas contra la nueva clase.
 */
@ExtendWith(MockitoExtension.class)
class CriterioevaluacionProcessorTest {

    @Mock private CriterioevaluacionService service;
    @Mock private CriterioevaluacionMap map;
    @Mock private CohorteService cohorteService;
    @Mock private CalificacioncriterioService calificacioncriterioService;
    @Mock private CalificacioncriterioProcessor calificacioncriterioProcessor;
    @Mock private AspiranteService aspiranteService;
    @Mock private CriteriocohorteService criteriocohorteService;
    @Mock private EstadoService estadoService;

    @InjectMocks
    private CriterioevaluacionProcessor processor;

    // ─── updateForPrograma: criterio no pertenece al programa ─────────────────

    @Test
    void updateForPrograma_criterioNoEncontrado_lanzaCriterioCohortemismatch() {
        // Dado: criterio no existe
        when(service.findById(10)).thenReturn(null);

        CRITERIO_UPDATE_BODY body = CRITERIO_UPDATE_BODY.builder()
                .nombre("Nombre").activo(true).descripcion("Desc")
                .peso(new BigDecimal("20")).build();

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.updateForPrograma(1, 10, body));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    @Test
    void updateForPrograma_criterioDeOtroPrograma_lanzaCriterioCohortemismatch() {
        // Dado: criterio existe pero su programaId=99, se pide para programa=1
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(99).build();
        when(service.findById(10)).thenReturn(criterio);

        CRITERIO_UPDATE_BODY body = CRITERIO_UPDATE_BODY.builder()
                .nombre("Nuevo").activo(true).descripcion("Desc")
                .peso(new BigDecimal("20")).build();

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.updateForPrograma(1, 10, body));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    // ─── deleteForPrograma: criterio no pertenece al programa ─────────────────

    @Test
    void deleteForPrograma_criterioNoEncontrado_lanzaCriterioCohortemismatch() {
        // Dado
        when(service.findById(10)).thenReturn(null);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteForPrograma(1, 10));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    @Test
    void deleteForPrograma_criterioDeOtroPrograma_lanzaCriterioCohortemismatch() {
        // Dado: criterio con programaId=5, se pide para programa=1
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(5).build();
        when(service.findById(10)).thenReturn(criterio);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteForPrograma(1, 10));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }

    // ─── createForCohorte: cohorte no pertenece al programa ───────────────────

    @Test
    void createForCohorte_cohorteNoEncontrada_lanzaCohorteProgmramaMismatch() {
        // Dado: cohorte no existe
        when(cohorteService.findById(5)).thenReturn(null);

        ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY body =
                ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY.builder()
                        .nombre("Crit").activo(true).descripcion("Desc")
                        .peso(new BigDecimal("20")).build();

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.createForCohorte(1, 5, body));
        assertEquals(CriterioevaluacionErrorCode.COHORTE_PROGRAMA_MISMATCH, ex.getCode());
    }

    @Test
    void createForCohorte_cohorteDeOtroPrograma_lanzaCohorteProgmramaMismatch() {
        // Dado: cohorte con idPrograma=9, se pide para programa=1
        CohorteDTO cohorte = CohorteDTO.builder().id(5).idPrograma(9).build();
        when(cohorteService.findById(5)).thenReturn(cohorte);

        ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY body =
                ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIO_CREATE_BODY.builder()
                        .nombre("Crit").activo(true).descripcion("Desc")
                        .peso(new BigDecimal("20")).build();

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.createForCohorte(1, 5, body));
        assertEquals(CriterioevaluacionErrorCode.COHORTE_PROGRAMA_MISMATCH, ex.getCode());
    }

    // ─── desactivarCriterio: criterio con calificaciones no puede desactivarse ─

    @Test
    void desactivarCriterio_criterioConCalificaciones_lanzaCalificacionesBloqueado() {
        // Dado: criterio del programa correcto, pero con calificaciones registradas
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(1).activo(true).build();
        CriteriocohorteDTO criteriocohorte = CriteriocohorteDTO.builder().id(55).build();
        when(service.findById(10)).thenReturn(criterio);
        when(criteriocohorteService.findByIdCriterio(10)).thenReturn(List.of(criteriocohorte));
        when(calificacioncriterioService.existsByCriterio(55)).thenReturn(true);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.desactivarCriterio(1, 10));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_CON_CALIFICACIONES_BLOQUEADO, ex.getCode());
    }

    @Test
    void desactivarCriterio_sinCalificaciones_desactivaExitosamente() {
        // Dado: criterio del programa correcto, sin calificaciones
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(1).activo(true).build();
        CriterioevaluacionOutput outputEsperado = CriterioevaluacionOutput.builder()
                .id(10).nombre("Crit").activo(false).build();
        when(service.findById(10)).thenReturn(criterio);
        when(criteriocohorteService.findByIdCriterio(10)).thenReturn(List.of());
        when(service.update(eq(10), any())).thenReturn(criterio);
        when(map.toOutput(criterio)).thenReturn(outputEsperado);

        // Cuando
        CriterioevaluacionOutput resultado = processor.desactivarCriterio(1, 10);

        // Entonces: activo debe ser false y se llamó a update
        assertNotNull(resultado);
        verify(service).update(eq(10), argThat(dto -> Boolean.FALSE.equals(dto.getActivo())));
    }

    @Test
    void desactivarCriterio_criterioDeOtroPrograma_lanzaCriterioCohortemismatch() {
        // Dado: criterio con programaId=99, se pide para programa=1
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(10).nombre("Crit").idprograma(99).activo(true).build();
        when(service.findById(10)).thenReturn(criterio);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.desactivarCriterio(1, 10));
        assertEquals(CriterioevaluacionErrorCode.CRITERIO_COHORTE_MISMATCH, ex.getCode());
    }
}
