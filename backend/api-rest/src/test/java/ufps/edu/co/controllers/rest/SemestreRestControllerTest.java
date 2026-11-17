package ufps.edu.co.controllers.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.services.core.crud.SemestreCoreService;

@ExtendWith(MockitoExtension.class)
class SemestreRestControllerTest {

    @Mock
    private SemestreCoreService processor;

    @InjectMocks
    private SemestreRestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("PUT /semestre/update con FK inexistente responde 400 REFERENCIA_INEXISTENTE")
    void updateReferenciaInexistente_responde400() throws Exception {
        when(processor.update(any()))
                .thenThrow(new DataIntegrityViolationException("fk",
                        new SQLException("Cannot add or update a child row", "23000", 1452)));

        mockMvc.perform(put("/semestre/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":1,"nombre":"2026-1","fechaInicio":"2026-01-31","fechaFin":"2026-06-30","idEstado":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REFERENCIA_INEXISTENTE"));
    }
}
