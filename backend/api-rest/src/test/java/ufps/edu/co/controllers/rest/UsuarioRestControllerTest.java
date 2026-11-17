package ufps.edu.co.controllers.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.util.List;

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

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.UsuarioErrorCode;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.records.output.entity.ClaveOutput;
import ufps.edu.co.records.output.entity.UsuarioOutput;
import ufps.edu.co.rest.exceptions.RegistroNoEncontradoException;
import ufps.edu.co.services.core.crud.UsuarioCoreService;

/**
 * T3 — la clave anidada en la respuesta de usuario solo expone su id.
 * SC-16 — el controlador deja pasar los errores tipados al handler global.
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
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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

    @Test
    @DisplayName("PUT /usuario/update con nombre de otro usuario responde 409")
    void updateNombreDuplicado_responde409() throws Exception {
        when(processor.update(any()))
                .thenThrow(new DomainException(UsuarioErrorCode.USUARIO_YA_EXISTE_CONFLICT, "repetido"));

        mockMvc.perform(put("/usuario/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":7,"nombreusuario":"repetido","idPersona":1,"idRol":1,"idClave":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_YA_EXISTE_CONFLICT"));
    }

    @Test
    @DisplayName("PUT /usuario/update con índice único responde 409 REGISTRO_DUPLICADO_CONFLICT")
    void updateIndiceUnico_responde409() throws Exception {
        when(processor.update(any()))
                .thenThrow(new DataIntegrityViolationException("duplicado",
                        new SQLException("Duplicate entry", "23000", 1062)));

        mockMvc.perform(put("/usuario/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":7,"nombreusuario":"repetido","idPersona":1,"idRol":1,"idClave":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGISTRO_DUPLICADO_CONFLICT"));
    }

    @Test
    @DisplayName("PUT /usuario/update con id inexistente responde 404 con cuerpo")
    void updateNoEncontrado_responde404ConCuerpo() throws Exception {
        when(processor.update(any()))
                .thenThrow(new RegistroNoEncontradoException("Usuario no encontrado con id: 999999"));

        mockMvc.perform(put("/usuario/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":999999,"nombreusuario":"fantasma","idPersona":1,"idRol":1,"idClave":1}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REGISTRO_NOT_FOUND"));
    }
}
