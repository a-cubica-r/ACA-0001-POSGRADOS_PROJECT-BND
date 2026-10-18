package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.records.output.entity.CohorteDetalleOutput;
import ufps.edu.co.records.output.entity.FechasCohorte;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.PlazoDTO;
import ufps.edu.co.rest.dto.SemestreDTO;
import ufps.edu.co.rest.services.AdmitidoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.CriterioevaluacionService;
import ufps.edu.co.rest.services.DocumentosrequisitoconsejocohorteService;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramacohorteService;
import ufps.edu.co.services.core.crud.AspiranteCoreService;

/**
 * D-00 - Las seis fechas de una cohorte se derivan de sus tres plazos y se
 * exponen en las salidas sin alterar {@code fechaInicio}, que sigue siendo el
 * inicio del semestre.
 */
@ExtendWith(MockitoExtension.class)
class CohorteFechasTest {

    private static final LocalDate INICIO_DOC = LocalDate.of(2026, 10, 16);
    private static final LocalDate FIN_DOC = LocalDate.of(2026, 11, 15);
    private static final LocalDate INICIO_INS = LocalDate.of(2026, 10, 16);
    private static final LocalDate FIN_INS = LocalDate.of(2026, 11, 30);
    private static final LocalDate INICIO_PAGO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIN_PAGO = LocalDate.of(2026, 12, 15);
    private static final LocalDate INICIO_SEMESTRE = LocalDate.of(2026, 8, 1);

    @Mock private CohorteService cohorteService;
    @Mock private AspiranteService service;
    @Mock private AdmitidoService admitidoService;
    @Mock private CriteriocohorteService criteriocohorteService;
    @Mock private CriterioevaluacionService criterioevaluacionService;
    @Mock private DocumentosrequisitoconsejocohorteService documentosrequisitoconsejocohorteService;
    @Mock private DocumentosrequisitoprogramacohorteService documentosrequisitoprogramacohorteService;

    @InjectMocks private AspiranteCoreService processor;

    private static PlazoDTO plazo(LocalDate inicio, LocalDate fin) {
        return PlazoDTO.builder().fechainicio(inicio).fechafin(fin).build();
    }

    @Test
    void desde_conLosTresPlazos_devuelveLasSeisFechas() {
        CohorteDTO cohorte = CohorteDTO.builder()
                .plazo(plazo(INICIO_DOC, FIN_DOC))
                .plazo2(plazo(INICIO_INS, FIN_INS))
                .plazo3(plazo(INICIO_PAGO, FIN_PAGO))
                .build();

        FechasCohorte fechas = FechasCohorte.desde(cohorte);

        assertEquals(INICIO_DOC, fechas.fechaInicioDocumentacion());
        assertEquals(FIN_DOC, fechas.fechaFinDocumentacion());
        assertEquals(INICIO_INS, fechas.fechaInicioInscripcion());
        assertEquals(FIN_INS, fechas.fechaFinInscripcion());
        assertEquals(INICIO_PAGO, fechas.fechaInicioPago());
        assertEquals(FIN_PAGO, fechas.fechaFinPago());
    }

    @Test
    void desde_sinPlazoDeInscripcion_dejaLaInscripcionEnNull() {
        CohorteDTO cohorte = CohorteDTO.builder()
                .plazo(plazo(INICIO_DOC, FIN_DOC))
                .plazo3(plazo(INICIO_PAGO, FIN_PAGO))
                .build();

        FechasCohorte fechas = FechasCohorte.desde(cohorte);

        assertNull(fechas.fechaInicioInscripcion());
        assertNull(fechas.fechaFinInscripcion());
        assertEquals(INICIO_DOC, fechas.fechaInicioDocumentacion());
        assertEquals(FIN_PAGO, fechas.fechaFinPago());
    }

    @Test
    void desde_sinCohorte_devuelveTodoNull() {
        FechasCohorte fechas = FechasCohorte.desde(null);

        assertNull(fechas.fechaInicioDocumentacion());
        assertNull(fechas.fechaFinDocumentacion());
        assertNull(fechas.fechaInicioInscripcion());
        assertNull(fechas.fechaFinInscripcion());
        assertNull(fechas.fechaInicioPago());
        assertNull(fechas.fechaFinPago());
    }

    @Test
    void getCohorteDetalle_exponeLasSeisFechasYElInicioDelSemestre() {
        CohorteDTO cohorte = CohorteDTO.builder()
                .id(1)
                .nombre("PUSI TAI")
                .cupos(20)
                .semestre(SemestreDTO.builder().id(3).nombre("2026-II").fechaInicio(INICIO_SEMESTRE).build())
                .plazo(plazo(INICIO_DOC, FIN_DOC))
                .plazo2(plazo(INICIO_INS, FIN_INS))
                .plazo3(plazo(INICIO_PAGO, FIN_PAGO))
                .build();
        when(cohorteService.findById(1)).thenReturn(cohorte);

        CohorteDetalleOutput salida = processor.getCohorteDetalle(1);

        assertEquals(INICIO_DOC, salida.fechaInicioDocumentacion());
        assertEquals(FIN_DOC, salida.fechaFinDocumentacion());
        assertEquals(INICIO_INS, salida.fechaInicioInscripcion());
        assertEquals(FIN_INS, salida.fechaFinInscripcion());
        assertEquals(INICIO_PAGO, salida.fechaInicioPago());
        assertEquals(FIN_PAGO, salida.fechaFinPago());
        assertEquals(INICIO_SEMESTRE, salida.fechaInicio());
    }
}
