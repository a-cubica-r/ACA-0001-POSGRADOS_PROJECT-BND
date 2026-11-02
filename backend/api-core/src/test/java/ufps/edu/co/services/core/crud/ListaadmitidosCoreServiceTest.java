package ufps.edu.co.services.core.crud;

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

@ExtendWith(MockitoExtension.class)
class ListaadmitidosCoreServiceTest {

    @Mock private CohorteService cohorteService;
    @Mock private AdministrativoService administrativoService;
    @Mock private AspiranteService aspiranteService;
    @Mock private ListaadmitidosService listaadmitidosService;
    @Mock private EstadoService estadoService;
    @Mock private ListaadmitidosMap map;
    @Mock private SESService sesService;
    @Mock private PdfGeneratorService pdfGeneratorService;

    @InjectMocks
    private ListaadmitidosCoreService coreService;

    // ─── generateAdmittedList: cohorte no encontrada ──────────────────────────

    @Test
    void generateAdmittedList_cohorteNoEncontrada_lanzaDomainException() {
        when(cohorteService.findById(99)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> coreService.generateAdmittedList(99));
        assertEquals(CohorteErrorCode.COHORTE_NOT_FOUND, ex.getCode());
    }

    // ─── generateAdmittedList: respeta cupos ──────────────────────────────────

    @Test
    void generateAdmittedList_masAspirantesQueCupos_retornaHastaElLimiteDeCupos() {
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("Cohorte I").cupos(3).build();
        List<AspiranteDTO> admitidos = List.of(
                crearAspirante(1, "Ana", "García"),
                crearAspirante(2, "Luis", "Pérez"),
                crearAspirante(3, "Marta", "López"),
                crearAspirante(4, "Juan", "Torres"),
                crearAspirante(5, "Sara", "Ruiz"));
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(admitidos);

        ListaAdmitidosResumenOutput resultado = coreService.generateAdmittedList(1);

        assertNotNull(resultado);
        assertEquals(3, resultado.aspirantes().size());
        assertEquals(3, resultado.cohorteActual().totalAdmitidos());
    }

    @Test
    void generateAdmittedList_menosAspirantesQueCupos_retornaTodosLosAdmitidos() {
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("Cohorte II").cupos(5).build();
        List<AspiranteDTO> admitidos = List.of(
                crearAspirante(1, "Ana", "García"),
                crearAspirante(2, "Luis", "Pérez"));
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(admitidos);

        ListaAdmitidosResumenOutput resultado = coreService.generateAdmittedList(1);

        assertEquals(2, resultado.aspirantes().size());
        assertEquals(2, resultado.cohorteActual().totalAdmitidos());
    }

    // ─── generateAdmittedList: cuposDisponibles ───────────────────────────────

    @Test
    void generateAdmittedList_cuposExactamenteOcupados_cuposDisponiblesEsCero() {
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(3).build();
        List<AspiranteDTO> admitidos = List.of(
                crearAspirante(1, "A", "B"),
                crearAspirante(2, "C", "D"),
                crearAspirante(3, "E", "F"));
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(admitidos);

        ListaAdmitidosResumenOutput resultado = coreService.generateAdmittedList(1);

        assertEquals(0, resultado.cohorteActual().cuposDisponibles());
    }

    @Test
    void generateAdmittedList_sinAdmitidos_cuposDisponiblesEsIgualACupos() {
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(4).build();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(List.of());

        ListaAdmitidosResumenOutput resultado = coreService.generateAdmittedList(1);

        assertEquals(4, resultado.cohorteActual().cuposDisponibles());
        assertEquals(0, resultado.cohorteActual().totalAdmitidos());
    }

    // ─── generateAdmittedList: estado cohorte activa ─────────────────────────

    @Test
    void generateAdmittedList_cohorteEstadoAbierta_activaEsTrue() {
        EstadoDTO estadoAbierto = new EstadoDTO();
        estadoAbierto.setTipo("ABIERTA");
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(5).estado(estadoAbierto).build();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(List.of());

        ListaAdmitidosResumenOutput resultado = coreService.generateAdmittedList(1);

        assertTrue(resultado.cohorteActual().activa());
    }

    @Test
    void generateAdmittedList_cohorteEstadoCerrada_activaEsFalse() {
        EstadoDTO estadoCerrado = new EstadoDTO();
        estadoCerrado.setTipo("CERRADA");
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(5).estado(estadoCerrado).build();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(List.of());

        ListaAdmitidosResumenOutput resultado = coreService.generateAdmittedList(1);

        assertFalse(resultado.cohorteActual().activa());
    }

    @Test
    void generateAdmittedList_cohorteEstadoNulo_activaEsFalse() {
        CohorteDTO cohorte = CohorteDTO.builder().id(1).nombre("C").cupos(5).estado(null).build();
        when(cohorteService.findById(1)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(1)).thenReturn(List.of());

        ListaAdmitidosResumenOutput resultado = coreService.generateAdmittedList(1);

        assertFalse(resultado.cohorteActual().activa());
    }

    // ─── generateAdmittedList: nombre correcto ────────────────────────────────

    @Test
    void generateAdmittedList_retornaNombreYCohorteCorrectos() {
        CohorteDTO cohorte = CohorteDTO.builder().id(7).nombre("Cohorte 2025-1").cupos(10).build();
        when(cohorteService.findById(7)).thenReturn(cohorte);
        when(aspiranteService.findAdmitidosByCohorte(7)).thenReturn(List.of());

        ListaAdmitidosResumenOutput resultado = coreService.generateAdmittedList(7);

        assertEquals(7, resultado.cohorteActual().id());
        assertEquals("Cohorte 2025-1", resultado.cohorteActual().nombre());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private AspiranteDTO crearAspirante(int id, String nombre, String apellido) {
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
