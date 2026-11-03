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

import ufps.edu.co.records.output.entity.ClaveOutput;
import ufps.edu.co.records.output.entity.UsuarioOutput;
import ufps.edu.co.services.core.crud.UsuarioCoreService;

/**
 * T3 — la clave anidada en la respuesta de usuario solo expone su id.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioRestControllerTest {

    @Mock
    private UsuarioCoreService processor;

    @InjectMocks
    private UsuarioRestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /usuario/listall no expone clave.valor")
    void listall_noExponeValorDeClave() throws Exception {
        UsuarioOutput output = UsuarioOutput.builder()
                .id(1)
                .nombreusuario("admin")
                .clave(ClaveOutput.builder().id(2).build())
                .build();
        when(processor.findAll()).thenReturn(List.of(output));

        mockMvc.perform(get("/usuario/listall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clave.id").value(2))
                .andExpect(jsonPath("$[0].clave.valor").doesNotExist());
    }
}
