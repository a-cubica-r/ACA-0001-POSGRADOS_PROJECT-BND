package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.enums.TipoPlazo;
import ufps.edu.co.records.input.entity.CohorteInput.COHORTE_DIRECTOR_UPDATE;
import ufps.edu.co.records.output.entity.CohorteListadoOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PlazoDTO;
import ufps.edu.co.rest.dto.SemestreDTO;
import ufps.edu.co.rest.dto.TipoplazoDTO;
import ufps.edu.co.rest.services.AdmitidoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.CriteriocohorteService;
import ufps.edu.co.rest.services.DocumentosrequisitoconsejocohorteService;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramacohorteService;
import ufps.edu.co.rest.services.PlazoService;
import ufps.edu.co.rest.services.SemestreService;
import ufps.edu.co.rest.services.TipoplazoService;
import ufps.edu.co.services.core.crud.AspiranteCoreService;

/**
 * D-00 - Editar cohorte con las seis fechas y separar el plazo que las cohortes
 * antiguas comparten entre documentación e inscripción.
 */
@ExtendWith(MockitoExtension.class)
class EditarCohorteTest {

    private static final LocalDate INICIO_SEMESTRE = LocalDate.of(2026, 8, 1);
    private static final LocalDate INICIO_DOC = LocalDate.of(2026, 8, 1);
    private static final LocalDate FIN_DOC = LocalDate.of(2026, 8, 15);
    private static final LocalDate INICIO_PAGO = LocalDate.of(2026, 8, 1);
    private static final LocalDate FIN_PAGO = LocalDate.of(2026, 11, 30);

    @Mock private CohorteService cohorteService;
    @Mock private PlazoService plazoService;
    @Mock private TipoplazoService tipoplazoService;
    @Mock private SemestreService semestreService;
    @Mock private CriteriocohorteService criteriocohorteService;
    @Mock private DocumentosrequisitoconsejocohorteService documentosrequisitoconsejocohorteService;
    @Mock private DocumentosrequisitoprogramacohorteService documentosrequisitoprogramacohorteService;
    @Mock private AspiranteService service;
    @Mock private AdmitidoService admitidoService;

    @InjectMocks private AspiranteCoreService processor;

    private static PlazoDTO plazo(Integer id, LocalDate inicio, LocalDate fin) {
        return PlazoDTO.builder().id(id).fechainicio(inicio).fechafin(fin).build();
    }

    private static COHORTE_DIRECTOR_UPDATE body(
            LocalDate fechaInicioDocumentacion, LocalDate fechaFinDocumentacion,
            LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioPago, LocalDate fechaFinPago,
            LocalDate fechaLimiteDocumentos, LocalDate fechaLimitePago) {
        return new COHORTE_DIRECTOR_UPDATE(
                1, "Cohorte editada", 10, 3, 1,
                null, fechaLimiteDocumentos, fechaLimitePago,
                fechaInicioDocumentacion, fechaFinDocumentacion,
                fechaInicioInscripcion, fechaFinInscripcion,
                fechaInicioPago, fechaFinPago,
                null, null, null);
    }

    /** Cohorte antigua: documentación e inscripción apuntan a la misma fila (10). */
    private CohorteDTO cohorteConPlazoCompartido() {
        PlazoDTO compartido = plazo(10, INICIO_DOC, FIN_DOC);
        return CohorteDTO.builder()
                .id(1).nombre("Cohorte vieja").cupos(10)
                .idSemestre(3).idModalidad(1)
                .idPlazodocumentacion(10).idPlazoinscripcion(10).idPlazopago(12)
                .plazo(compartido)
                .plazo2(compartido)
                .plazo3(plazo(12, INICIO_PAGO, FIN_PAGO))
                .semestre(SemestreDTO.builder().id(3).nombre("2026-II").fechaInicio(INICIO_SEMESTRE).build())
                .estado(EstadoDTO.builder().id(5).tipo("ABIERTA").build())
                .build();
    }

    private CohorteDTO cohorteYaSeparada() {
        return CohorteDTO.builder()
                .id(1).nombre("Cohorte vieja").cupos(10)
                .idSemestre(3).idModalidad(1)
                .idPlazodocumentacion(10).idPlazoinscripcion(11).idPlazopago(12)
                .plazo(plazo(10, INICIO_DOC, FIN_DOC))
                .plazo2(plazo(11, LocalDate.of(2026, 8, 15), LocalDate.of(2026, 10, 15)))
                .plazo3(plazo(12, INICIO_PAGO, FIN_PAGO))
                .semestre(SemestreDTO.builder().id(3).nombre("2026-II").fechaInicio(INICIO_SEMESTRE).build())
                .estado(EstadoDTO.builder().id(5).tipo("ABIERTA").build())
                .build();
    }

    private void semestreHabilitado() {
        when(semestreService.findById(3)).thenReturn(SemestreDTO.builder()
                .id(3).nombre("2026-II").fechaInicio(INICIO_SEMESTRE)
                .estado(EstadoDTO.builder().id(9).tipo("EN CURSO").build())
                .build());
    }

    @Test
    void plazoCompartido_seSepara_enEdicionDeDocumentacion() {
        CohorteDTO cohorte = cohorteConPlazoCompartido();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        semestreHabilitado();
        when(tipoplazoService.findByTipo(TipoPlazo.INSCRIPCION))
                .thenReturn(TipoplazoDTO.builder().id(4).tipo("INSCRIPCION").build());
        when(plazoService.create(any(PlazoDTO.class))).thenReturn(plazo(20, INICIO_DOC, FIN_DOC));

        CohorteListadoOutput salida = processor.updateCohorte(1,
                body(null, LocalDate.of(2026, 10, 31), null, null, null, null, null, null));

        // Se creó una fila nueva para inscripción y solo se editó la de documentación.
        ArgumentCaptor<PlazoDTO> creado = ArgumentCaptor.forClass(PlazoDTO.class);
        verify(plazoService).create(creado.capture());
        assertEquals(4, creado.getValue().getIdTipoplazo());
        assertEquals(INICIO_DOC, creado.getValue().getFechainicio());
        assertEquals(FIN_DOC, creado.getValue().getFechafin());

        verify(plazoService, times(1)).update(eq(10), any(PlazoDTO.class));
        verify(cohorteService).update(eq(1), any(CohorteDTO.class));

        assertEquals(10, cohorte.getIdPlazodocumentacion());
        assertEquals(20, cohorte.getIdPlazoinscripcion());
        assertNotEquals(cohorte.getIdPlazodocumentacion(), cohorte.getIdPlazoinscripcion());

        // La inscripción conserva sus fechas; la documentación cambia.
        assertEquals(LocalDate.of(2026, 10, 31), salida.fechaFinDocumentacion());
        assertEquals(INICIO_DOC, salida.fechaInicioInscripcion());
        assertEquals(FIN_DOC, salida.fechaFinInscripcion());
    }

    @Test
    void cohorteYaSeparada_editarInicioDeInscripcionActualizaSoloEsePlazo() {
        CohorteDTO cohorte = cohorteYaSeparada();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        semestreHabilitado();

        processor.updateCohorte(1,
                body(null, null, LocalDate.of(2026, 8, 20), null, null, null, null, null));

        verify(plazoService).update(eq(11), any(PlazoDTO.class));
        verify(plazoService, never()).create(any(PlazoDTO.class));
        assertEquals(LocalDate.of(2026, 8, 20), cohorte.getPlazo2().getFechainicio());
        assertEquals(10, cohorte.getIdPlazodocumentacion());
        assertEquals(11, cohorte.getIdPlazoinscripcion());
    }

    @Test
    void aliasViejos_siguenFuncionando() {
        CohorteDTO cohorte = cohorteYaSeparada();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        semestreHabilitado();

        CohorteListadoOutput salida = processor.updateCohorte(1,
                body(null, null, null, null, null, null,
                        LocalDate.of(2026, 10, 15), LocalDate.of(2026, 12, 1)));

        verify(plazoService).update(eq(10), any(PlazoDTO.class));
        verify(plazoService).update(eq(12), any(PlazoDTO.class));
        verify(plazoService, never()).update(eq(11), any(PlazoDTO.class));

        assertEquals(LocalDate.of(2026, 10, 15), salida.fechaFinDocumentacion());
        assertEquals(LocalDate.of(2026, 12, 1), salida.fechaFinPago());
    }

    @Test
    void inicioPosteriorAlFin_lanzaFechasInvalidasYNoTocaNingunPlazo() {
        CohorteDTO cohorte = cohorteConPlazoCompartido();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        semestreHabilitado();

        DomainException ex = assertThrows(DomainException.class, () -> processor.updateCohorte(1,
                body(LocalDate.of(2026, 9, 1), null, null, null, null, null, null, null)));

        assertEquals("COHORTE_PLAZO_FECHAS_INVALIDAS", ex.getCode().getCode());
        verify(plazoService, never()).update(any(), any(PlazoDTO.class));
        verify(plazoService, never()).create(any(PlazoDTO.class));
        verify(cohorteService, never()).update(any(), any(CohorteDTO.class));
    }
}
