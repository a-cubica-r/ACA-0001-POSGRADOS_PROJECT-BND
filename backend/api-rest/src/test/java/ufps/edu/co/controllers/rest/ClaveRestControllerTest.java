package ufps.edu.co.controllers.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.auth.util.PasswordFormats;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.records.input.entity.ClaveInput.CLAVE_CREATE;
import ufps.edu.co.records.input.entity.ClaveInput.CLAVE_UPDATE;
import ufps.edu.co.records.output.entity.ClaveOutput;
import ufps.edu.co.services.core.crud.ClaveCoreService;

/**
 * T1 — las rutas de clave cifran cualquier valor recibido antes de guardarlo
 * y rechazan un valor vacío.
 */
@ExtendWith(MockitoExtension.class)
class ClaveRestControllerTest {

    @Mock
    private ClaveCoreService processor;

    @Mock
    private PasswordHashService passwordHashService;

    @InjectMocks
    private ClaveRestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static String bcrypt(String raw) {
        return new BCryptPasswordEncoder().encode(raw);
    }

    @Test
    @DisplayName("POST /clave/create cifra el valor antes de llamar al processor")
    void create_cifraElValor() throws Exception {
        String hash = bcrypt("Clave123*");
        when(passwordHashService.encode("Clave123*")).thenReturn(hash);
        when(processor.create(any(CLAVE_CREATE.class))).thenReturn(ClaveOutput.builder().id(1).build());

        mockMvc.perform(post("/clave/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":\"Clave123*\"}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<CLAVE_CREATE> captor = ArgumentCaptor.forClass(CLAVE_CREATE.class);
        verify(processor).create(captor.capture());
        assertTrue(PasswordFormats.isBcrypt(captor.getValue().valor()));
        assertNotEquals("Clave123*", captor.getValue().valor());
    }

    @Test
    @DisplayName("PUT /clave/update cifra el valor y conserva el id")
    void update_cifraElValorYConservaId() throws Exception {
        String hash = bcrypt("Nueva123*");
        when(passwordHashService.encode("Nueva123*")).thenReturn(hash);
        when(processor.update(any(CLAVE_UPDATE.class))).thenReturn(ClaveOutput.builder().id(2).build());

        mockMvc.perform(put("/clave/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2,\"valor\":\"Nueva123*\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<CLAVE_UPDATE> captor = ArgumentCaptor.forClass(CLAVE_UPDATE.class);
        verify(processor).update(captor.capture());
        assertEquals(2, captor.getValue().id());
        assertTrue(PasswordFormats.isBcrypt(captor.getValue().valor()));
        assertNotEquals("Nueva123*", captor.getValue().valor());
    }

    @Test
    @DisplayName("GET /clave/listall no expone el campo valor")
    void listall_noExponeValor() throws Exception {
        when(processor.findAll()).thenReturn(List.of(ClaveOutput.builder().id(1).build()));

        mockMvc.perform(get("/clave/listall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].valor").doesNotExist());
    }

    @Test
    @DisplayName("POST /clave/create con valor vacío responde 400 CLAVE_VALOR_OBLIGATORIO")
    void create_valorVacio_responde400() throws Exception {
        mockMvc.perform(post("/clave/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CLAVE_VALOR_OBLIGATORIO"));

        verify(processor, never()).create(any());
    }

    @Test
    @DisplayName("PUT /clave/update con valor vacío responde 400 CLAVE_VALOR_OBLIGATORIO")
    void update_valorVacio_responde400() throws Exception {
        mockMvc.perform(put("/clave/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2,\"valor\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CLAVE_VALOR_OBLIGATORIO"));

        verify(processor, never()).update(any());
    }
}
