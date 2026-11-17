package ufps.edu.co.controllers.cases.aspirante;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.rest.services.PersonaService;

@ExtendWith(MockitoExtension.class)
class InscripcionCaseValidacionEntradaTest {

    private static final String CUERPO_VALIDO = """
            {
              "nombres": "Ana",
              "apellidos": "Pérez",
              "idTipoDoc": 1,
              "numeroDocumento": "123456",
              "idMunicipioExpedicionDoc": 1,
              "email": "aspirante@ufps.edu.co",
              "telefonoContacto": "3001234567",
              "ubicacionNacimiento": {"idMunicipioNacimiento": 1},
              "ubicacionResidencia": {"idMunicipioResidencia": 1, "direccionResidencia": "Calle 1"},
              "idCohorte": 5,
              "idTipoVinculacion": 1,
              "usuario": "aspirante",
              "contrasena": "clave"
            }
            """;

    @Mock
    private PersonaService personaService;

    @InjectMocks
    private InscripcionCase controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("correo inválido responde 400 con param.email")
    void correoInvalido_responde400() throws Exception {
        String cuerpo = CUERPO_VALIDO.replace("aspirante@ufps.edu.co", "no-es-correo");

        mockMvc.perform(post("/inscripciones/formulario/registrar-nuevo-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.email").exists());

        verify(personaService, never()).create(any());
    }

    @Test
    @DisplayName("campos obligatorios vacíos responden 400 con sus nombres en param")
    void camposObligatoriosVacios_responde400() throws Exception {
        String cuerpo = CUERPO_VALIDO
                .replace("\"nombres\": \"Ana\"", "\"nombres\": \" \"")
                .replace("\"usuario\": \"aspirante\"", "\"usuario\": \" \"")
                .replace("\"idCohorte\": 5", "\"idCohorte\": null");

        mockMvc.perform(post("/inscripciones/formulario/registrar-nuevo-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.nombres").exists())
                .andExpect(jsonPath("$.param.usuario").exists())
                .andExpect(jsonPath("$.param.idCohorte").exists());

        verify(personaService, never()).create(any());
    }

    @Test
    @DisplayName("usuario de 51 caracteres responde 400 con param.usuario")
    void usuarioDemasiadoLargo_responde400() throws Exception {
        String largo = "u".repeat(51);
        String cuerpo = CUERPO_VALIDO.replace("\"usuario\": \"aspirante\"",
                "\"usuario\": \"" + largo + "\"");

        mockMvc.perform(post("/inscripciones/formulario/registrar-nuevo-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.usuario").exists());

        verify(personaService, never()).create(any());
    }

    @Test
    @DisplayName("un cuerpo válido no produce violaciones de Bean Validation")
    void cuerpoValido_sinViolaciones() throws Exception {
        Validator validator = new LocalValidatorFactoryBean();
        ((LocalValidatorFactoryBean) validator).afterPropertiesSet();
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        InscripcionCase.RegistrarNuevoUsuarioRequest request = mapper.readValue(
                CUERPO_VALIDO, InscripcionCase.RegistrarNuevoUsuarioRequest.class);

        assertTrue(validator.validate(request).isEmpty());
    }
}
