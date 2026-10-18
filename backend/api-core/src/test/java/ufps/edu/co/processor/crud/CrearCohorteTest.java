package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.TipoplazoErrorCode;
import ufps.edu.co.enums.EstadoCatalogo;
import ufps.edu.co.enums.TipoPlazo;
import ufps.edu.co.records.input.entity.CohorteInput.COHORTE_DIRECTOR_CREATE;
import ufps.edu.co.records.output.entity.CohorteListadoOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PlazoDTO;
import ufps.edu.co.rest.dto.SemestreDTO;
import ufps.edu.co.rest.dto.TipoplazoDTO;
import ufps.edu.co.rest.services.AdmitidoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.PlazoService;
import ufps.edu.co.rest.services.SemestreService;
import ufps.edu.co.rest.services.TipoplazoService;
import ufps.edu.co.services.core.crud.AspiranteCoreService;

/**
 * D-00 - Crear cohorte con las seis fechas del contrato del frontend y tres
 * plazos tipados (documentación, inscripción y pago), sin reutilizar la fila
 * del plazo de documentación ni valerse de {@code tipoplazos.get(0)}.
 */
@ExtendWith(MockitoExtension.class)
class CrearCohorteTest {

    private static final LocalDate INICIO_DOC = LocalDate.of(2026, 10, 16);
    private static final LocalDate FIN_DOC = LocalDate.of(2027, 10, 31);
    private static final LocalDate INICIO_INS = LocalDate.of(2026, 10, 16);
    private static final LocalDate FIN_INS = LocalDate.of(2027, 10, 31);
    private static final LocalDate INICIO_PAGO = LocalDate.of(2026, 10, 16);
    private static final LocalDate FIN_PAGO = LocalDate.of(2027, 10, 31);
    private static final LocalDate INICIO_SEMESTRE = LocalDate.of(2026, 10, 1);

    @Mock private CohorteService cohorteService;
    @Mock private PlazoService plazoService;
    @Mock private TipoplazoService tipoplazoService;
    @Mock private SemestreService semestreService;
    @Mock private EstadoService estadoService;
    @Mock private AspiranteService service;
    @Mock private AdmitidoService admitidoService;

    @InjectMocks private AspiranteCoreService processor;

    private static COHORTE_DIRECTOR_CREATE body(LocalDate inicioPago, LocalDate finPago) {
        return new COHORTE_DIRECTOR_CREATE(
                "PUSI TAI", 20, 3, 1,
                INICIO_DOC, FIN_DOC,
                INICIO_INS, FIN_INS,
                inicioPago, finPago,
                null, null, null);
    }

    private void stubsDelCasoFeliz() {
        when(tipoplazoService.findByTipo(TipoPlazo.DOCUMENTACION))
                .thenReturn(TipoplazoDTO.builder().id(3).tipo("DOCUMENTACION").build());
        when(tipoplazoService.findByTipo(TipoPlazo.INSCRIPCION))
                .thenReturn(TipoplazoDTO.builder().id(4).tipo("INSCRIPCION").build());
        when(tipoplazoService.findByTipo(TipoPlazo.PAGO))
                .thenReturn(TipoplazoDTO.builder().id(7).tipo("PAGO").build());

        when(plazoService.create(any(PlazoDTO.class))).thenReturn(
                PlazoDTO.builder().id(10).build(),
                PlazoDTO.builder().id(11).build(),
                PlazoDTO.builder().id(12).build());

        when(semestreService.findById(anyInt())).thenReturn(SemestreDTO.builder()
                .id(3)
                .nombre("2026-II")
                .fechaInicio(INICIO_SEMESTRE)
                .estado(EstadoDTO.builder().id(9).tipo("EN CURSO").build())
                .build());

        when(estadoService.findByTipoAndEntidad(any(EstadoCatalogo.class)))
                .thenReturn(EstadoDTO.builder().id(6).tipo("CERRADA").build());

        when(cohorteService.createAndGetId(any(CohorteDTO.class))).thenReturn(55);
    }

    @Test
    void creaTresPlazosTipadosYLaCohorteApuntaAIdDistintos() {
        stubsDelCasoFeliz();

        CohorteListadoOutput salida = processor.createCohorte(16, body(INICIO_PAGO, FIN_PAGO));

        ArgumentCaptor<PlazoDTO> plazos = ArgumentCaptor.forClass(PlazoDTO.class);
        verify(plazoService, times(3)).create(plazos.capture());
        List<PlazoDTO> creados = plazos.getAllValues();

        assertEquals(3, creados.get(0).getIdTipoplazo());
        assertEquals(4, creados.get(1).getIdTipoplazo());
        assertEquals(7, creados.get(2).getIdTipoplazo());
        assertEquals(INICIO_DOC, creados.get(0).getFechainicio());
        assertEquals(FIN_DOC, creados.get(0).getFechafin());
        assertEquals(INICIO_INS, creados.get(1).getFechainicio());
        assertEquals(FIN_INS, creados.get(1).getFechafin());
        assertEquals(INICIO_PAGO, creados.get(2).getFechainicio());
        assertEquals(FIN_PAGO, creados.get(2).getFechafin());

        ArgumentCaptor<CohorteDTO> cohorte = ArgumentCaptor.forClass(CohorteDTO.class);
        verify(cohorteService).createAndGetId(cohorte.capture());
        assertEquals(10, cohorte.getValue().getIdPlazodocumentacion());
        assertEquals(11, cohorte.getValue().getIdPlazoinscripcion());
        assertEquals(12, cohorte.getValue().getIdPlazopago());
        assertNotEquals(cohorte.getValue().getIdPlazodocumentacion(),
                cohorte.getValue().getIdPlazoinscripcion());

        assertEquals(55, salida.id());
        assertEquals(INICIO_SEMESTRE, salida.fechaInicio());
        assertEquals(INICIO_DOC, salida.fechaInicioDocumentacion());
        assertEquals(FIN_PAGO, salida.fechaFinPago());
    }

    @Test
    void finDePagoAnteriorAlInicio_lanzaFechasInvalidasYNoCreaPlazos() {
        COHORTE_DIRECTOR_CREATE bodyInvalido = body(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1));

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.createCohorte(16, bodyInvalido));

        assertEquals("COHORTE_PLAZO_FECHAS_INVALIDAS", ex.getCode().getCode());
        verify(plazoService, never()).create(any(PlazoDTO.class));
    }

    @Test
    void tipoDePlazoDeInscripcionAusente_lanzaTipoPlazoNoConfigurado() {
        when(tipoplazoService.findByTipo(TipoPlazo.DOCUMENTACION))
                .thenReturn(TipoplazoDTO.builder().id(3).tipo("DOCUMENTACION").build());
        when(tipoplazoService.findByTipo(TipoPlazo.INSCRIPCION)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.createCohorte(16, body(INICIO_PAGO, FIN_PAGO)));

        assertEquals(TipoplazoErrorCode.TIPOPLAZO_NO_CONFIGURADO_CONFLICT, ex.getCode());
        verify(plazoService, never()).create(any(PlazoDTO.class));
    }
}
