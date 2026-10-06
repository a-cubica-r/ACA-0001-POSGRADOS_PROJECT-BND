package ufps.edu.co.processor.crud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.maps.specific.ListaadmitidosMap;
import ufps.edu.co.records.output.entity.ListaAdmitidosResumenOutput;
import ufps.edu.co.rest.dto.AspiranteDTO;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.services.*;
import ufps.edu.co.services.PdfGeneratorService;
import ufps.edu.co.services.SESService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DEPENDE_DE_PROCESSOR - Tests sobre ListaadmitidosProcessor.
 * Cuando se elimine la capa Processor y la lógica pase a servicio de dominio,
 * reescribir estas pruebas contra la nueva clase.
 */
@ExtendWith(MockitoExtension.class)
class ListaadmitidosProcessorTest {

    @Mock private CohorteService cohorteService;
    @Mock private AdministrativoService administrativoService;
    @Mock private AspiranteService aspiranteService;
    @Mock private ListaadmitidosService listaadmitidosService;
    @Mock private EstadoService estadoService;
    @Mock private ListaadmitidosMap map;
    @Mock private SESService sesService;
    @Mock private PdfGeneratorService pdfGeneratorService;

    @InjectMocks
    private ListaadmitidosProcessor processor;

    // ─── generateAdmittedList: cohorte no encontrada ──────────────────────────

    @Test
    void generateAdmittedList_cohorteNoEncontrada_lanzaDomainException() {
        // Dado
        when(cohorteService.findById(99)).thenReturn(null);

        // Cuando/Entonces
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.generateAdmittedList(99));
        assertEquals(CohorteErrorCode.COHORTE_NOT_FOUND, ex.getCode());
    }

    // ─── generateAdmittedList: respeta cupos ──────────────────────────────────

    @Test
    void generateAdmittedList_masAspirantesQueCupos_retornaHastaElLimiteDeCupos() {
        // Dado: cohorte con 3 cupos, 5 aspirantes admitidos
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("Cohorte I").cupos(3).build();
        List<AspiranteDTO> admitidos = List.of(
                crearAspirante(1, "Ana", "García", "puntaje:80"),
                crearAspirante(2, "Luis", "Pérez", "puntaje:75"),
                crearAspirante(3, "Marta", "López", "puntaje:70"),
                crearAspirante(4, "Juan", "Torres", "puntaje:65"),
                crearAspirante(5, "Sara", "Ruiz", "puntaje:60"));
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(admitidos);

        // Cuando
        ListaAdmitidosResumenOutput resultado = processor.generateAdmittedList(1);

        // Entonces: solo 3 aspirantes (límite de cupos)
        assertNotNull(resultado);
        assertEquals(3, resultado.aspirantes().size());
        assertEquals(3, resultado.cohorteActual().totalAdmitidos());
    }

    @Test
    void generateAdmittedList_menosAspirantesQueCupos_retornaTodosLosAdmitidos() {
        // Dado: cohorte con 5 cupos, solo 2 aspirantes admitidos
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("Cohorte II").cupos(5).build();
        List<AspiranteDTO> admitidos = List.of(
                crearAspirante(1, "Ana", "García", null),
                crearAspirante(2, "Luis", "Pérez", null));
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(admitidos);

        // Cuando
        ListaAdmitidosResumenOutput resultado = processor.generateAdmittedList(1);

        // Entonces: los 2 disponibles
        assertEquals(2, resultado.aspirantes().size());
        assertEquals(2, resultado.cohorteActual().totalAdmitidos());
    }

    // ─── generateAdmittedList: cuposDisponibles ───────────────────────────────

    @Test
    void generateAdmittedList_cuposExactamenteOcupados_cuposDisponiblesEsCero() {
        // Dado: 3 cupos, 3 admitidos → cuposDisponibles = 0
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(3).build();
        List<AspiranteDTO> admitidos = List.of(
                crearAspirante(1, "A", "B", null),
                crearAspirante(2, "C", "D", null),
                crearAspirante(3, "E", "F", null));
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(admitidos);

        // Cuando
        ListaAdmitidosResumenOutput resultado = processor.generateAdmittedList(1);

        // Entonces
        assertEquals(0, resultado.cohorteActual().cuposDisponibles());
    }

    @Test
    void generateAdmittedList_sinAdmitidos_cuposDisponiblesEsIgualACupos() {
        // Dado: 4 cupos, 0 admitidos → cuposDisponibles = 4
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(4).build();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(List.of());

        // Cuando
        ListaAdmitidosResumenOutput resultado = processor.generateAdmittedList(1);

        // Entonces
        assertEquals(4, resultado.cohorteActual().cuposDisponibles());
        assertEquals(0, resultado.cohorteActual().totalAdmitidos());
    }

    // ─── generateAdmittedList: estado cohorte activa ─────────────────────────

    @Test
    void generateAdmittedList_cohorteEstadoAbierta_activaEsTrue() {
        // Dado: estado tipo = ABIERTA
        EstadoDTO estadoAbierto = new EstadoDTO();
        estadoAbierto.setTipo("ABIERTA");
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(5).estado(estadoAbierto).build();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(List.of());

        // Cuando
        ListaAdmitidosResumenOutput resultado = processor.generateAdmittedList(1);

        // Entonces
        assertTrue(resultado.cohorteActual().activa());
    }

    @Test
    void generateAdmittedList_cohorteEstadoCerrada_activaEsFalse() {
        // Dado: estado tipo = CERRADA
        EstadoDTO estadoCerrado = new EstadoDTO();
        estadoCerrado.setTipo("CERRADA");
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(5).estado(estadoCerrado).build();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(List.of());

        // Cuando
        ListaAdmitidosResumenOutput resultado = processor.generateAdmittedList(1);

        // Entonces
        assertFalse(resultado.cohorteActual().activa());
    }

    @Test
    void generateAdmittedList_cohorteEstadoNulo_activaEsFalse() {
        // Dado: cohorte sin estado
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(5).estado(null).build();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(List.of());

        // Cuando
        ListaAdmitidosResumenOutput resultado = processor.generateAdmittedList(1);

        // Entonces
        assertFalse(resultado.cohorteActual().activa());
    }

    // ─── generateAdmittedList: nombre correcto ────────────────────────────────

    @Test
    void generateAdmittedList_retornaNombreYCohorteCorrectos() {
        // Dado
        CohorteDTO cohorte = CohorteDTO.builder().id(7).nombre("Cohorte 2025-1").cupos(10).build();
        when(cohorteService.findById(7)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(7)).thenReturn(List.of());

        // Cuando
        ListaAdmitidosResumenOutput resultado = processor.generateAdmittedList(7);

        // Entonces
        assertEquals(7, resultado.cohorteActual().id());
        assertEquals("Cohorte 2025-1", resultado.cohorteActual().nombre());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private AspiranteDTO crearAspirante(int id, String nombre, String apellido, String ignorado) {
        PersonaDTO persona = new PersonaDTO();
        persona.setNombres(nombre);
        persona.setApellidos(apellido);
        persona.setCorreo(nombre.toLowerCase() + "@test.com");
        return AspiranteDTO.builder()
                .id(id)
                .puntuacion(BigDecimal.valueOf(80 - id))
                .persona(persona)
                .build();
    }
}
