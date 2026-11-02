package ufps.edu.co.services.core.crud;

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

@ExtendWith(MockitoExtension.class)
class CalificacioncriterioCoreServiceTest {

    @Mock private CalificacioncriterioService service;
    @Mock private CalificacioncriterioMap map;
    @Mock private AspiranteService aspiranteService;
    @Mock private CriteriocohorteService criteriocohorteService;
    @Mock private EstadoService estadoService;
    @Mock private SESService sesService;

    @InjectMocks
    private CalificacioncriterioCoreService coreService;

    // ─── Criterio no encontrado ───────────────────────────────────────────────

    @Test
    void calificarCriterio_criterioNoEncontrado_lanzaDomainException() {
        when(criteriocohorteService.findById(99)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.calificarCriterio(1, 99, BigDecimal.TEN));
        assertEquals(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, ex.getCode());
    }

    // ─── Aspirante no encontrado / no pertenece a la cohorte ─────────────────

    @Test
    void calificarCriterio_aspiranteNoEncontrado_lanzaDomainException() {
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.calificarCriterio(5, 1, new BigDecimal("20")));
        assertEquals(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, ex.getCode());
    }

    @Test
    void calificarCriterio_aspiranteEnCohorteDiferente_lanzaDomainException() {
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(20).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.calificarCriterio(5, 1, new BigDecimal("20")));
        assertEquals(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, ex.getCode());
    }

    // ─── Peso snapshot nulo ───────────────────────────────────────────────────

    @Test
    void calificarCriterio_pesoSnapshotNulo_lanzaDomainException() {
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(null).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.calificarCriterio(5, 1, new BigDecimal("20")));
        assertEquals(CalificacioncriterioErrorCode.CALIFICACIONCRITERIO_NOT_FOUND, ex.getCode());
    }

    // ─── Puntaje con decimales (debe ser entero) ──────────────────────────────

    @Test
    void calificarCriterio_puntajeConDecimales_lanzaPuntuacionInvalida() {
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.calificarCriterio(5, 1, new BigDecimal("10.5")));
        assertEquals(CalificacioncriterioErrorCode.PUNTUACION_INVALIDA, ex.getCode());
    }

    // ─── Puntaje negativo ─────────────────────────────────────────────────────

    @Test
    void calificarCriterio_puntajeNegativo_lanzaPuntuacionInvalida() {
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("50")).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.calificarCriterio(5, 1, new BigDecimal("-1")));
        assertEquals(CalificacioncriterioErrorCode.PUNTUACION_INVALIDA, ex.getCode());
    }

    // ─── Puntaje excede peso máximo ───────────────────────────────────────────

    @Test
    void calificarCriterio_puntajeExcedePesoMaximo_lanzaPuntajeExcedeMaximo() {
        CriteriocohorteDTO criterio = CriteriocohorteDTO.builder()
                .id(1).idCohorte(10).pesoSnapshot(new BigDecimal("30")).build();
        AspiranteDTO aspirante = AspiranteDTO.builder().id(5).idCohorte(10).build();
        when(criteriocohorteService.findById(1)).thenReturn(criterio);
        when(aspiranteService.findById(5)).thenReturn(aspirante);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.calificarCriterio(5, 1, new BigDecimal("31")));
        assertEquals(CalificacioncriterioErrorCode.PUNTAJE_EXCEDE_MAXIMO, ex.getCode());
    }

    @Test
    void calificarCriterio_puntajeIgualAlPesoMaximo_noLanzaExcepcion() {
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
                        .puntuacion(new BigDecimal("30")).build();
        when(map.toOutput(any())).thenReturn(mockOutput);
        when(service.findByIdAspirante(5)).thenReturn(java.util.List.of());
        when(aspiranteService.findById(5)).thenReturn(aspirante);
        when(criteriocohorteService.findByIdCohorte(10)).thenReturn(java.util.List.of());

        assertDoesNotThrow(() -> coreService.calificarCriterio(5, 1, new BigDecimal("30")));
    }

    @Test
    void calificarCriterio_puntajeJustaDebajoDelMaximo_noLanzaExcepcion() {
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

        assertDoesNotThrow(() -> coreService.calificarCriterio(5, 1, new BigDecimal("29")));
    }

    // ─── Nota: DEFECTO POTENCIAL ──────────────────────────────────────────────
    // El mensaje de PUNTUACION_INVALIDA dice "mayor o igual a 1",
    // pero el código permite puntaje = 0 (condición: puntaje < 0, no <= 0).
    // La siguiente prueba documenta este comportamiento actual.

    @Test
    void calificarCriterio_puntajeCero_esPermitidoPorElCodigo() {
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

        assertDoesNotThrow(() -> coreService.calificarCriterio(5, 1, BigDecimal.ZERO));
    }
}
