package ufps.edu.co.processor.crud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CalificacioncriterioErrorCode;
import ufps.edu.co.maps.specific.CalificacioncriterioMap;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.CriteriocohorteDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CalificacioncriterioService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.services.SESService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DEPENDE_DE_PROCESSOR - Tests sobre CalificacioncriterioProcessor.
 * Cuando se elimine la capa Processor y la lógica pase a servicio de dominio,
 * reescribir estas pruebas contra la nueva clase.
 */
@ExtendWith(MockitoExtension.class)
class CalificacioncriterioProcessorTest {

    @Mock private CalificacioncriterioService service;
    @Mock private CalificacioncriterioMap map;
    @Mock private AspiranteService aspiranteService;
    @Mock private CriteriocohorteService criteriocohorteService;
    @Mock private EstadoService estadoService;
    @Mock private SESService sesService;

    @InjectMocks
    private CalificacioncriterioProcessor processor;

    // ─── Criterio no encontrado ───────────────────────────────────────────────

    @Test
    void calificarCriterio_criterioNoEncontrado_lanzaDomainException() {
        // Dado: criterio inexistente
        when(criteriocohorteService.findById(99)).thenReturn(null);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.calificarCriterio(1, 99, BigDecimal.TEN));
        assertEquals(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, ex.getCode());
    }

    // ─── Aspirante no encontrado / no pertenece a la cohorte ─────────────────

    @Test
    void calificarCriterio_aspiranteNoEncontrado_lanzaDomainException() {
        // Dado: criterio existe con cohorteId=10, aspirante es null
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(null);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.calificarCriterio(5, 1, new BigDecimal("20")));
        assertEquals(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, ex.getCode());
    }

    @Test
    void calificarCriterio_aspiranteEnCohorteDiferente_lanzaDomainException() {
        // Dado: criterio en cohorte=10, aspirante en cohorte=20
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(20).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.calificarCriterio(5, 1, new BigDecimal("20")));
        assertEquals(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, ex.getCode());
    }

    // ─── Peso snapshot nulo ───────────────────────────────────────────────────

    @Test
    void calificarCriterio_pesoSnapshotNulo_lanzaDomainException() {
        // Dado: pesoSnapshot es null (criterio mal configurado)
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(null).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.calificarCriterio(5, 1, new BigDecimal("20")));
        assertEquals(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, ex.getCode());
    }

    // ─── Puntaje con decimales (debe ser entero) ──────────────────────────────

    @Test
    void calificarCriterio_puntajeConDecimales_lanzaPuntuacionInvalida() {
        // Dado: peso=50, puntaje=10.5 (no entero)
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.calificarCriterio(5, 1, new BigDecimal("10.5")));
        assertEquals(CalificacioncriterioErrorCode.PUNTUACION_INVALIDA, ex.getCode());
    }

    // ─── Puntaje negativo ─────────────────────────────────────────────────────

    @Test
    void calificarCriterio_puntajeNegativo_lanzaPuntuacionInvalida() {
        // Dado: puntaje = -1
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.calificarCriterio(5, 1, new BigDecimal("-1")));
        assertEquals(CalificacioncriterioErrorCode.PUNTUACION_INVALIDA, ex.getCode());
    }

    // ─── Puntaje excede peso máximo ───────────────────────────────────────────

    @Test
    void calificarCriterio_puntajeExcedePesoMaximo_lanzaPuntajeExcedeMaximo() {
        // Dado: peso máximo = 30, puntaje = 31 (justo por encima)
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("30")).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.calificarCriterio(5, 1, new BigDecimal("31")));
        assertEquals(CalificacioncriterioErrorCode.PUNTAJE_EXCEDE_MAXIMO, ex.getCode());
    }

    @Test
    void calificarCriterio_puntajeIgualAlPesoMaximo_noLanzaExcepcion() {
        // Dado: peso máximo = 30, puntaje = 30 (exactamente el máximo)
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("30")).build();
        ufps.edu.co.rest.dto.PersonaDTO persona = ufps.edu.co.rest.dto.PersonaDTO.builder()
                .nombres("Test").apellidos("User").correo("test@test.com").build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).persona(persona).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);
        // Configurar mocks para que el flujo de create/update no falle
        when(service.findByIdAspiranteAndIdCriterio(5, 1)).thenReturn(java.util.Optional.empty());
        when(service.existsByAspiranteAndCriterio(5, 1)).thenReturn(false);
        when(map.toDto(any())).thenReturn(new ufps.edu.co.rest.dto.CalificacioncriterioDTO());
        when(service.create(any())).thenReturn(new ufps.edu.co.rest.dto.CalificacioncriterioDTO());
        ufps.edu.co.records.output.entity.CalificacioncriterioOutput mockOutput =
                ufps.edu.co.records.output.entity.CalificacioncriterioOutput.builder()
                        .id(1).idAspirante(5).idCriteriocohorte(1)
                        .puntuacion(new BigDecimal("30")).build();
        when(map.toOutput(any())).thenReturn(mockOutput);
        when(service.findByIdAspirante(5)).thenReturn(java.util.List.of());
        when(aspiranteService.findById(5)).thenReturn(aspirante);
        when(criteriocohorteService.findByIdCohorte(10)).thenReturn(java.util.List.of());

        // Cuando/Entonces: no debe lanzar excepción de validación de puntaje
        assertDoesNotThrow(() -> processor.calificarCriterio(5, 1, new BigDecimal("30")));
    }

    @Test
    void calificarCriterio_puntajeJustaDebajoDelMaximo_noLanzaExcepcion() {
        // Dado: peso máximo = 30, puntaje = 29 (un punto por debajo del máximo)
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("30")).build();
        ufps.edu.co.rest.dto.PersonaDTO persona = ufps.edu.co.rest.dto.PersonaDTO.builder()
                .nombres("Test").apellidos("User").correo("test@test.com").build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).persona(persona).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);
        when(service.findByIdAspiranteAndIdCriterio(5, 1)).thenReturn(java.util.Optional.empty());
        when(service.existsByAspiranteAndCriterio(5, 1)).thenReturn(false);
        when(map.toDto(any())).thenReturn(new ufps.edu.co.rest.dto.CalificacioncriterioDTO());
        when(service.create(any())).thenReturn(new ufps.edu.co.rest.dto.CalificacioncriterioDTO());
        ufps.edu.co.records.output.entity.CalificacioncriterioOutput mockOutput =
                ufps.edu.co.records.output.entity.CalificacioncriterioOutput.builder()
                        .id(1).idAspirante(5).idCriteriocohorte(1)
                        .puntuacion(new BigDecimal("29")).build();
        when(map.toOutput(any())).thenReturn(mockOutput);
        when(service.findByIdAspirante(5)).thenReturn(java.util.List.of());
        when(aspiranteService.findById(5)).thenReturn(aspirante);
        when(criteriocohorteService.findByIdCohorte(10)).thenReturn(java.util.List.of());

        assertDoesNotThrow(() -> processor.calificarCriterio(5, 1, new BigDecimal("29")));
    }

    // ─── Nota: DEFECTO POTENCIAL ──────────────────────────────────────────────
    // El mensaje de PUNTUACION_INVALIDA dice "mayor o igual a 1",
    // pero el código permite puntaje = 0 (condición: puntaje < 0, no <= 0).
    // La siguiente prueba documenta este comportamiento actual.

    @Test
    void calificarCriterio_puntajeCero_esPermitidoPorElCodigo() {
        // Dado: peso=50, puntaje=0 (el mensaje dice ≥1 pero el código lo permite)
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        ufps.edu.co.rest.dto.PersonaDTO persona = ufps.edu.co.rest.dto.PersonaDTO.builder()
                .nombres("Test").apellidos("User").correo("test@test.com").build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).persona(persona).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);
        when(service.findByIdAspiranteAndIdCriterio(5, 1)).thenReturn(java.util.Optional.empty());
        when(service.existsByAspiranteAndCriterio(5, 1)).thenReturn(false);
        when(map.toDto(any())).thenReturn(new ufps.edu.co.rest.dto.CalificacioncriterioDTO());
        when(service.create(any())).thenReturn(new ufps.edu.co.rest.dto.CalificacioncriterioDTO());
        ufps.edu.co.records.output.entity.CalificacioncriterioOutput mockOutput =
                ufps.edu.co.records.output.entity.CalificacioncriterioOutput.builder()
                        .id(1).idAspirante(5).idCriteriocohorte(1)
                        .puntuacion(BigDecimal.ZERO).build();
        when(map.toOutput(any())).thenReturn(mockOutput);
        when(service.findByIdAspirante(5)).thenReturn(java.util.List.of());
        when(aspiranteService.findById(5)).thenReturn(aspirante);
        when(criteriocohorteService.findByIdCohorte(10)).thenReturn(java.util.List.of());

        // El código actual NO lanza excepción con puntaje=0 (aunque el mensaje diga ≥1)
        assertDoesNotThrow(() -> processor.calificarCriterio(5, 1, BigDecimal.ZERO));
    }
}
