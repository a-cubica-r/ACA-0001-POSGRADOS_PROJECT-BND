package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;
import ufps.edu.co.maps.specific.CriterioevaluacionMap;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIOEVALUACION_CREATE;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIOEVALUACION_UPDATE;
import ufps.edu.co.records.output.entity.CriterioevaluacionOutput;
import ufps.edu.co.rest.dto.CriterioevaluacionDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CalificacioncriterioService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.CriterioevaluacionService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.services.core.crud.CalificacioncriterioCoreService;
import ufps.edu.co.services.core.crud.CriterioevaluacionCoreService;

/**
 * PLN0010 T2 — Un criterio no se puede crear ni renombrar con un nombre que ya
 * exista en el mismo programa: responde 409 CRITERIO_DUPLICADO antes de tocar
 * la base.
 */
@ExtendWith(MockitoExtension.class)
class CriterioDuplicadoTest {

    private static final Integer ID_PROGRAMA = 16;

    @Mock private CriterioevaluacionService service;
    @Mock private CriterioevaluacionMap map;
    @Mock private CohorteService cohorteService;
    @Mock private CalificacioncriterioService calificacioncriterioService;
    @Mock private CalificacioncriterioCoreService calificacioncriterioProcessor;
    @Mock private AspiranteService aspiranteService;
    @Mock private CriteriocohorteService criteriocohorteService;
    @Mock private EstadoService estadoService;

    @InjectMocks
    private CriterioevaluacionCoreService processor;

    private static CRITERIOEVALUACION_CREATE crear(String nombre) {
        return CRITERIOEVALUACION_CREATE.builder()
                .nombre(nombre)
                .activo(true)
                .peso(new BigDecimal("20"))
                .idPrograma(ID_PROGRAMA)
                .build();
    }

    private static CRITERIOEVALUACION_UPDATE actualizar(Integer id, String nombre) {
        return CRITERIOEVALUACION_UPDATE.builder()
                .id(id)
                .nombre(nombre)
                .activo(true)
                .peso(new BigDecimal("20"))
                .idPrograma(ID_PROGRAMA)
                .build();
    }

    @Test
    @DisplayName("create con nombre existente → CRITERIO_DUPLICADO y no crea")
    void createConNombreExistente() {
        when(service.existsByIdProgramaAndNombre(ID_PROGRAMA, "Repetido")).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.create(crear("Repetido")));

        assertEquals(CriterioevaluacionErrorCode.CRITERIO_DUPLICADO, ex.getCode());
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("create con nombre nuevo → crea")
    void createConNombreNuevo() {
        CriterioevaluacionDTO dto = CriterioevaluacionDTO.builder()
                .nombre("Nuevo").peso(new BigDecimal("20")).idprograma(ID_PROGRAMA).build();
        CriterioevaluacionOutput output = CriterioevaluacionOutput.builder().id(1).nombre("Nuevo").build();
        when(service.existsByIdProgramaAndNombre(ID_PROGRAMA, "Nuevo")).thenReturn(false);
        when(map.toDto(any())).thenReturn(dto);
        when(service.create(dto)).thenReturn(dto);
        when(map.toOutput(dto)).thenReturn(output);

        assertNotNull(processor.create(crear("Nuevo")));
        verify(service).create(dto);
    }

    @Test
    @DisplayName("update renombrando a uno existente de otro criterio → CRITERIO_DUPLICADO")
    void updateRenombrandoADuplicado() {
        CriterioevaluacionDTO actual = CriterioevaluacionDTO.builder()
                .id(9).nombre("Original").peso(new BigDecimal("20")).idprograma(ID_PROGRAMA).build();
        when(criteriocohorteService.findByIdCriterio(9)).thenReturn(List.of());
        when(service.findById(9)).thenReturn(actual);
        when(service.existsByIdProgramaAndNombreAndIdNot(ID_PROGRAMA, "Repetido", 9)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.update(actualizar(9, "Repetido")));

        assertEquals(CriterioevaluacionErrorCode.CRITERIO_DUPLICADO, ex.getCode());
        verify(service, never()).update(any(), any());
    }

    @Test
    @DisplayName("update conservando su propio nombre → no lanza")
    void updateConservandoSuNombre() {
        CriterioevaluacionDTO actual = CriterioevaluacionDTO.builder()
                .id(9).nombre("Original").peso(new BigDecimal("20")).idprograma(ID_PROGRAMA).build();
        CriterioevaluacionOutput output = CriterioevaluacionOutput.builder().id(9).nombre("Original").build();
        when(criteriocohorteService.findByIdCriterio(9)).thenReturn(List.of());
        when(service.findById(9)).thenReturn(actual);
        when(service.existsByIdProgramaAndNombreAndIdNot(ID_PROGRAMA, "Original", 9)).thenReturn(false);
        when(map.toDto(any())).thenReturn(actual);
        when(service.update(9, actual)).thenReturn(actual);
        when(map.toOutput(actual)).thenReturn(output);

        assertDoesNotThrow(() -> processor.update(actualizar(9, "Original")));
        verify(service).update(9, actual);
    }
}
