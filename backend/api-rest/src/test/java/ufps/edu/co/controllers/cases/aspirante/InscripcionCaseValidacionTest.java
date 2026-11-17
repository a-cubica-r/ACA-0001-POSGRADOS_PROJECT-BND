package ufps.edu.co.controllers.cases.aspirante;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.DocumentopersonaService;
import ufps.edu.co.rest.services.PersonaService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.core.crud.ProgramaCoreService;

@ExtendWith(MockitoExtension.class)
class InscripcionCaseValidacionTest {

    @Mock
    private CohorteService cohorteService;

    @Mock
    private PersonaService personaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private DocumentopersonaService documentopersonaService;

    @Mock
    private ProgramaCoreService programaProcessor;

    @InjectMocks
    private InscripcionCase controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String cuerpoConCohorte(String idCohorte) {
        return """
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
                  "idCohorte": %s,
                  "idTipoVinculacion": 1,
                  "usuario": "aspirante",
                  "contrasena": "clave"
                }
                """.formatted(idCohorte);
    }

    @Test
    @DisplayName("registrar-nuevo-usuario sin idCohorte responde 400 de validación sin crear persona")
    void sinCohorte_responde400() throws Exception {
        mockMvc.perform(post("/inscripciones/formulario/registrar-nuevo-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConCohorte("null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.idCohorte").exists());

        verify(personaService, never()).create(any());
    }

    @Test
    @DisplayName("registrar-nuevo-usuario con cohorte inexistente responde 404 sin crear persona")
    void cohorteInexistente_responde404() throws Exception {
        when(cohorteService.findById(999)).thenReturn(null);

        mockMvc.perform(post("/inscripciones/formulario/registrar-nuevo-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConCohorte("999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COHORTE_NOT_FOUND"));

        verify(personaService, never()).create(any());
    }

    @Test
    @DisplayName("registrar-nuevo-usuario con cohorte cerrada responde 409 sin crear persona")
    void cohorteCerrada_responde409() throws Exception {
        CohorteDTO cohorte = CohorteDTO.builder()
                .id(5)
                .idPrograma(1)
                .estado(EstadoDTO.builder().tipo("CERRADA").build())
                .build();
        when(cohorteService.findById(5)).thenReturn(cohorte);

        mockMvc.perform(post("/inscripciones/formulario/registrar-nuevo-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConCohorte("5")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSCRIPCION_COHORTE_NO_ABIERTA_CONFLICT"));

        verify(personaService, never()).create(any());
    }
}
