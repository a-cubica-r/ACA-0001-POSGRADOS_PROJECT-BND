package ufps.edu.co.controllers.cases.aspirante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.records.output.entity.AspiranteCohorteOutput;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.core.crud.AspiranteCoreService;

/**
 * SC-07 — CP-SC07-10 a CP-SC07-12
 * Endpoint: GET /cohortes/{idCohorte}/aspirantes
 */
@ExtendWith(MockitoExtension.class)
class CohorteAspirantesPaginacionTest {

    @Mock private AspiranteCoreService aspiranteProcessor;
    @Mock private UsuarioService usuarioService;
    @Mock private AdministrativoService administrativoService;

    @InjectMocks
    private CohorteCase controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // CP-SC07-10
    @Test
    @DisplayName("CP-SC07-10 — Sin params → defaults page=0, size=20 llegan al servicio con idCohorte=3")
    void sinParams_usaDefaults_page0_size20() throws Exception {
        Page<AspiranteCohorteOutput> paginaMock = new PageImpl<>(
                List.of(AspiranteCohorteOutput.builder().id(1).nombre("Juan").build()),
                PageRequest.of(0, 20), 1);
        when(aspiranteProcessor.findByCohorteConResumen(eq(3), any(Pageable.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/cohortes/3/aspirantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(aspiranteProcessor).findByCohorteConResumen(eq(3), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    // CP-SC07-11
    @Test
    @DisplayName("CP-SC07-11 — Con ?page=1&size=10 → Pageable(1,10) con idCohorte=3 llega al servicio")
    void conParams_propagaPageableYIdCohorteCorrectamente() throws Exception {
        // total=20: offset(10)+pageSize(10)=20 <= 20, evita que Spring Data 4.x recalcule totalElements
        Page<AspiranteCohorteOutput> paginaMock = new PageImpl<>(
                List.of(AspiranteCohorteOutput.builder().id(2).build()),
                PageRequest.of(1, 10), 20);
        when(aspiranteProcessor.findByCohorteConResumen(eq(3), any(Pageable.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/cohortes/3/aspirantes").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(20))
                .andExpect(jsonPath("$.totalPages").value(2));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(aspiranteProcessor).findByCohorteConResumen(eq(3), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
    }

    // CP-SC07-12
    @Test
    @DisplayName("CP-SC07-12 — Cohorte sin aspirantes → 200 con content vacío, no 500")
    void cohorteSinAspirantes_devuelve200ConContentVacio() throws Exception {
        Page<AspiranteCohorteOutput> paginaVacia = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(aspiranteProcessor.findByCohorteConResumen(eq(99), any(Pageable.class))).thenReturn(paginaVacia);

        mockMvc.perform(get("/cohortes/99/aspirantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
