package ufps.edu.co.controllers.cases.aspirante;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.enums.Rol;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.records.output.entity.RolOutput;
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.core.crud.RolCoreService;

/**
 * D-08 (CP-030) — {@code POST /registroAspirante/registro}: no deja claves
 * huérfanas cuando el usuario ya existe y asigna el rol de aspirante.
 */
@ExtendWith(MockitoExtension.class)
class RegistroAspiranteControllerTest {

    @Mock private UsuarioService usuarioService;
    @Mock private ClaveService claveService;
    @Mock private PasswordHashService passwordHashService;
    @Mock private RolCoreService rolProcessor;

    @InjectMocks private RegistroAspiranteController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static String body(String usuario) {
        return "{\"idPersona\":10,\"usuario\":\"" + usuario + "\",\"contrasena\":\"secreta\"}";
    }

    @Test
    @DisplayName("Usuario existente → 409 USUARIO_YA_EXISTE_CONFLICT y no se crea la clave")
    void usuarioExistente_responde409SinCrearClave() throws Exception {
        when(usuarioService.findByNombreusuario("aspirante01")).thenReturn(UsuarioDTO.builder().id(5).build());

        mockMvc.perform(post("/registroAspirante/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("aspirante01")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_YA_EXISTE_CONFLICT"));

        verify(claveService, never()).create(any());
    }

    @Test
    @DisplayName("Usuario nuevo → se crea con el rol de aspirante")
    void usuarioNuevo_creaConRolAspirante() throws Exception {
        when(usuarioService.findByNombreusuario("nuevo")).thenReturn(null);
        when(passwordHashService.encode("secreta")).thenReturn("hash");
        when(claveService.create(any())).thenReturn(ClaveDTO.builder().id(1).build());
        when(rolProcessor.findByNombre(Rol.ASPIRANTE.getValor())).thenReturn(RolOutput.builder().id(4).build());
        when(usuarioService.create(any())).thenReturn(UsuarioDTO.builder().id(2).build());

        mockMvc.perform(post("/registroAspirante/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("nuevo")))
                .andExpect(status().isCreated());

        ArgumentCaptor<UsuarioDTO> captor = ArgumentCaptor.forClass(UsuarioDTO.class);
        verify(usuarioService).create(captor.capture());
        assertEquals(4, captor.getValue().getIdRol());
        assertEquals(1, captor.getValue().getIdClave());
    }
}
