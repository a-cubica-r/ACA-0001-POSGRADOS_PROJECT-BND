package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;
import ufps.edu.co.enums.EstadoAspirante;
import ufps.edu.co.maps.specific.CriterioevaluacionMap;
import ufps.edu.co.records.input.entity.CriterioevaluacionInput.CRITERIOEVALUACION_DELETE;
import ufps.edu.co.rest.dto.CriterioevaluacionDTO;
import ufps.edu.co.rest.dto.CriteriocohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CalificacioncriterioService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.CriterioevaluacionService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.services.core.crud.CalificacioncriterioCoreService;
import ufps.edu.co.services.core.crud.CriterioevaluacionCoreService;

/**
 * PLN0010 T2 — Un criterio asignado a una cohorte no se borra: responde 409
 * CRITERIO_EN_USO_CONFLICT con las cohortes afectadas, en vez de dejar que la
 * FK RESTRICT produzca un 500.
 */
@ExtendWith(MockitoExtension.class)
class CriterioEnUsoTest {

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

    @Test
    @DisplayName("deleteById de un criterio asignado a la cohorte 10 → CRITERIO_EN_USO_CONFLICT")
    @SuppressWarnings("unchecked")
    void criterioAsignadoACohorte() {
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(9).nombre("Criterio").idprograma(16).build();
        CriteriocohorteDTO asignacion = CriteriocohorteDTO.builder()
                .id(50).idCriterio(9).idCohorte(10).build();
        when(service.findById(9)).thenReturn(criterio);
        when(criteriocohorteService.findByIdCriterio(9)).thenReturn(List.of(asignacion));

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new CRITERIOEVALUACION_DELETE(9)));

        assertEquals(CriterioevaluacionErrorCode.CRITERIO_EN_USO_CONFLICT, ex.getCode());
        Map<String, Object> param = (Map<String, Object>) ex.getParam();
        assertEquals(9, param.get("criterioId"));
        assertEquals(List.of(10), param.get("cohortes"));
        verify(service, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteById sin asignaciones → borra y recalcula estados")
    void criterioSinAsignaciones() {
        CriterioevaluacionDTO criterio = CriterioevaluacionDTO.builder()
                .id(9).nombre("Criterio").idprograma(16).build();
        when(service.findById(9)).thenReturn(criterio);
        when(criteriocohorteService.findByIdCriterio(9)).thenReturn(List.of());
        when(estadoService.findByTipoAndEntidad(EstadoAspirante.VALIDADO_CALIFICADO))
                .thenReturn(EstadoDTO.builder().id(3).build());
        when(cohorteService.findByIdPrograma(16)).thenReturn(List.of());

        processor.deleteById(new CRITERIOEVALUACION_DELETE(9));

        verify(service).deleteById(9);
        verify(cohorteService).findByIdPrograma(16);
    }
}
