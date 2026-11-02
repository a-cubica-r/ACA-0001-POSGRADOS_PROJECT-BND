package ufps.edu.co.controllers.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.records.output.entity.UltimocodigoprogramaOutput;
import ufps.edu.co.services.core.crud.UltimocodigoprogramaCoreService;

/**
 * D-00 — Contrato de {@code GET /ultimocodigoprograma/listall}.
 */
@ExtendWith(MockitoExtension.class)
class UltimocodigoprogramaRestControllerTest {

    @Mock
    private UltimocodigoprogramaCoreService processor;

    @InjectMocks
    private UltimocodigoprogramaRestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("listall responde 200 con id, idPrograma, nombrePrograma y codigo")
    void listall_devuelveLaLista() throws Exception {
        when(processor.findAll()).thenReturn(List.of(
                UltimocodigoprogramaOutput.builder()
                        .id(4)
                        .idPrograma(2)
                        .nombrePrograma("Maestría B")
                        .codigo(12)
                        .build()));

        mockMvc.perform(get("/ultimocodigoprograma/listall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].idPrograma").value(2))
                .andExpect(jsonPath("$[0].nombrePrograma").value("Maestría B"))
                .andExpect(jsonPath("$[0].codigo").value(12));
    }
}
