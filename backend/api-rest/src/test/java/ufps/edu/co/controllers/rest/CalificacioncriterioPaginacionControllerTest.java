package ufps.edu.co.controllers.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

import ufps.edu.co.records.output.entity.CalificacioncriterioOutput;
import ufps.edu.co.services.core.crud.CalificacioncriterioCoreService;

/**
 * SC-07 — CP-SC07-07 a CP-SC07-09
 * Endpoint: GET /calificacioncriterio/listall
 */
@ExtendWith(MockitoExtension.class)
class CalificacioncriterioPaginacionControllerTest {

    @Mock
    private CalificacioncriterioCoreService processor;

    @InjectMocks
    private CalificacioncriterioRestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // CP-SC07-07
    @Test
    @DisplayName("CP-SC07-07 — Sin params → defaults page=0, size=20 llegan al servicio")
    void sinParams_usaDefaults_page0_size20() throws Exception {
        Page<CalificacioncriterioOutput> paginaMock = new PageImpl<>(
                List.of(CalificacioncriterioOutput.builder().id(1).build()),
                PageRequest.of(0, 20), 1);
        when(processor.findAll(any(Pageable.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/calificacioncriterio/listall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(processor).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    // CP-SC07-08
    @Test
    @DisplayName("CP-SC07-08 — Con ?page=0&size=50 → Pageable(pageNumber=0, pageSize=50) llega al servicio")
    void conParams_propagaPageableCorrectamente() throws Exception {
        Page<CalificacioncriterioOutput> paginaMock = new PageImpl<>(
                List.of(CalificacioncriterioOutput.builder().id(1).build()),
                PageRequest.of(0, 50), 400);
        when(processor.findAll(any(Pageable.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/calificacioncriterio/listall").param("page", "0").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(400))
                .andExpect(jsonPath("$.totalPages").value(8));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(processor).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(50, captor.getValue().getPageSize());
    }

    // CP-SC07-09
    @Test
    @DisplayName("CP-SC07-09 — Sin registros → 200 con content vacío, no 500")
    void sinRegistros_devuelve200ConContentVacio() throws Exception {
        Page<CalificacioncriterioOutput> paginaVacia = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(processor.findAll(any(Pageable.class))).thenReturn(paginaVacia);

        mockMvc.perform(get("/calificacioncriterio/listall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
