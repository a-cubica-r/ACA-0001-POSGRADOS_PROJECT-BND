package ufps.edu.co.controllers.cases.login;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * T3 — el endpoint heredado se conserva, pero su controlador responde 403.
 */
class RecoveryPasswordRestCaseTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RecoveryPasswordRestCase()).build();
    }

    @Test
    @DisplayName("POST /login/recoveryPassword responde 403 y no ejecuta la lógica heredada")
    void recoveryPassword_responde403() throws Exception {
        mockMvc.perform(post("/login/recoveryPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"correo@ejemplo.com\""))
                .andExpect(status().isForbidden());
    }
}
