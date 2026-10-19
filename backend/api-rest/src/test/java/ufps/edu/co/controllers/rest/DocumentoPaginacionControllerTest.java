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

import ufps.edu.co.records.output.entity.DocumentoOutput;
import ufps.edu.co.services.core.crud.DocumentoCoreService;

/**
 * SC-07 — CP-SC07-04 a CP-SC07-06
 * Endpoint: GET /documento/listall
 */
@ExtendWith(MockitoExtension.class)
class DocumentoPaginacionControllerTest {

    @Mock
    private DocumentoCoreService processor;

    @InjectMocks
    private DocumentoRestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // CP-SC07-04
    @Test
    @DisplayName("CP-SC07-04 — Sin params → defaults page=0, size=20 llegan al servicio")
    void sinParams_usaDefaults_page0_size20() throws Exception {
        Page<DocumentoOutput> paginaMock = new PageImpl<>(
                List.of(DocumentoOutput.builder().id(1).build()),
                PageRequest.of(0, 20), 1);
        when(processor.findAll(any(Pageable.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/documento/listall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(processor).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    // CP-SC07-05
    @Test
    @DisplayName("CP-SC07-05 — Con ?page=2&size=10 → Pageable(pageNumber=2, pageSize=10) llega al servicio")
    void conParams_propagaPageableCorrectamente() throws Exception {
        // total=30: offset(20)+pageSize(10)=30 <= 30, evita que Spring Data 4.x recalcule totalElements
        Page<DocumentoOutput> paginaMock = new PageImpl<>(
                List.of(DocumentoOutput.builder().id(3).build()),
                PageRequest.of(2, 10), 30);
        when(processor.findAll(any(Pageable.class))).thenReturn(paginaMock);

        mockMvc.perform(get("/documento/listall").param("page", "2").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.totalPages").value(3));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(processor).findAll(captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
    }

    // CP-SC07-06
    @Test
    @DisplayName("CP-SC07-06 — Sin registros → 200 con content vacío, no 500")
    void sinRegistros_devuelve200ConContentVacio() throws Exception {
        Page<DocumentoOutput> paginaVacia = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(processor.findAll(any(Pageable.class))).thenReturn(paginaVacia);

        mockMvc.perform(get("/documento/listall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
