package ufps.edu.co.controllers.cases.administrativo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
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

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.PersonaErrorCode;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.records.output.entity.PersonaOutput;
import ufps.edu.co.services.core.cases.PersonaCompletaCoreService;

/**
 * D-00 — POST /superadmin/persona/crear-completo: contrato HTTP del alta de
 * personas del Superadministrador.
 */
@ExtendWith(MockitoExtension.class)
class SuperAdminCaseTest {

    private static final String JSON_FRONT = """
            {
              "nombres": "Adolfo Alejandro",
              "apellidos": "Arenas Ramos",
              "correo": "adolfoalejandroar@ufps.edu.co",
              "fechanacimiento": null,
              "celular": "3138726612",
              "telefono": null,
              "idGenero": null,
              "idEstadocivil": null,
              "idGrupoetnico": null,
              "idPoblacionindigena": null,
              "idDiscapacidad": null,
              "idCapacidadexepcional": null,
              "promediopregrado": null,
              "titulopregrado": null,
              "titulosposgrados": null,
              "empresa": null,
              "experiencialaboral": null,
              "egresadoufps": null,
              "ubicacionvivienda": null,
              "ubicacionnacimiento": null,
              "ubicaciontrabajo": null,
              "numerodocumento": "",
              "idTipodocumento": null,
              "lugarexpedicion": null
            }
            """;

    @Mock
    private PersonaCompletaCoreService service;

    @InjectMocks
    private SuperAdminCase controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("El JSON exacto del front responde 201 con el id de la persona")
    void jsonDelFront_devuelve201ConId() throws Exception {
        when(service.crear(any())).thenReturn(PersonaOutput.builder().id(42).build());

        mockMvc.perform(post("/superadmin/persona/crear-completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_FRONT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    @DisplayName("Sin correo responde 400 VALIDACION_FALLIDA y no llega al servicio")
    void sinCorreo_devuelve400ValidacionFallida() throws Exception {
        String sinCorreo = JSON_FRONT.replace("\"correo\": \"adolfoalejandroar@ufps.edu.co\",", "");

        mockMvc.perform(post("/superadmin/persona/crear-completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sinCorreo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.correo").exists());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Celular de 3 dígitos responde 400 VALIDACION_FALLIDA y no llega al servicio")
    void celularCorto_devuelve400ValidacionFallida() throws Exception {
        String celularCorto = JSON_FRONT.replace("\"celular\": \"3138726612\"", "\"celular\": \"123\"");

        mockMvc.perform(post("/superadmin/persona/crear-completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(celularCorto))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.celular").exists());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Correo duplicado del dominio responde 409 PERSONA_CORREO_DUPLICADO")
    void correoDuplicado_responde409() throws Exception {
        when(service.crear(any())).thenThrow(
                new DomainException(PersonaErrorCode.PERSONA_CORREO_DUPLICADO, "adolfoalejandroar@ufps.edu.co"));

        mockMvc.perform(post("/superadmin/persona/crear-completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_FRONT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PERSONA_CORREO_DUPLICADO"));
    }

    @Test
    @DisplayName("Ubicación a medias del dominio responde 400 PERSONA_UBICACION_INCOMPLETA con el campo")
    void ubicacionIncompleta_responde400() throws Exception {
        when(service.crear(any())).thenThrow(
                new DomainException(PersonaErrorCode.PERSONA_UBICACION_INCOMPLETA, "ubicacionvivienda"));

        mockMvc.perform(post("/superadmin/persona/crear-completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_FRONT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PERSONA_UBICACION_INCOMPLETA"))
                .andExpect(jsonPath("$.param").value("ubicacionvivienda"));
    }
}
