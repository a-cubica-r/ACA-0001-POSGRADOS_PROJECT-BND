package ufps.edu.co.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verifica que {@link GlobalExceptionHandler} devuelva un cuerpo legible en los
 * dos errores que hoy llegaban vacíos: campos inválidos y cuerpo ilegible.
 */
class GlobalExceptionHandlerValidacionTest {

    public record PayloadDePrueba(@NotNull LocalDate fecha, String nombre) {
    }

    @RestController
    static class ControladorDePrueba {

        @PostMapping(value = "/prueba", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<Void> crear(@Valid @RequestBody PayloadDePrueba body) {
            return ResponseEntity.ok().build();
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Body sin un campo obligatorio responde 400 con VALIDACION_FALLIDA y el nombre del campo")
    void bodySinCampoObligatorio_devuelve400ConValidacionFallida() throws Exception {
        mockMvc.perform(post("/prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"sin fecha\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.message").value("Hay campos inválidos en la solicitud"))
                .andExpect(jsonPath("$.param.fecha").exists());
    }

    @Test
    @DisplayName("Fecha con formato inválido responde 400 con CUERPO_INVALIDO")
    void fechaMalFormateada_devuelve400ConCuerpoInvalido() throws Exception {
        mockMvc.perform(post("/prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-13-45\",\"nombre\":\"fecha inválida\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CUERPO_INVALIDO"))
                .andExpect(jsonPath("$.message").value("El cuerpo de la solicitud no se pudo leer"));
    }
}
