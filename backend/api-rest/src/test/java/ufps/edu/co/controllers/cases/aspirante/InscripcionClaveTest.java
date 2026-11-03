package ufps.edu.co.controllers.cases.aspirante;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.auth.util.PasswordFormats;
import ufps.edu.co.enums.Rol;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.records.output.entity.RolOutput;
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.dto.UsuarioDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.rest.services.UsuarioService;
import ufps.edu.co.services.core.crud.RolCoreService;

/**
 * T1 — el registro de aspirante de {@link InscripcionCase} cifra la clave
 * antes de persistirla.
 */
@ExtendWith(MockitoExtension.class)
class InscripcionClaveTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ClaveService claveService;

    @Mock
    private PasswordHashService passwordHashService;

    @Mock
    private RolCoreService rolProcessor;

    @InjectMocks
    private InscripcionCase controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /inscripciones/usuario guarda la clave cifrada con BCrypt")
    void registrarUsuario_cifraLaClave() throws Exception {
        String hash = new BCryptPasswordEncoder().encode("Clave123*");
        when(usuarioService.findByNombreusuario("nuevo")).thenReturn(null);
        when(passwordHashService.encode("Clave123*")).thenReturn(hash);
        when(claveService.create(any())).thenReturn(ClaveDTO.builder().id(10).build());
        when(rolProcessor.findByNombre(Rol.ASPIRANTE.getValor())).thenReturn(RolOutput.builder().id(4).build());
        when(usuarioService.create(any())).thenReturn(UsuarioDTO.builder().id(2).build());

        mockMvc.perform(post("/inscripciones/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPersona\":1,\"usuario\":\"nuevo\",\"contrasena\":\"Clave123*\"}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<ClaveDTO> captor = ArgumentCaptor.forClass(ClaveDTO.class);
        verify(claveService).create(captor.capture());
        assertTrue(PasswordFormats.isBcrypt(captor.getValue().getValor()));
        assertNotEquals("Clave123*", captor.getValue().getValor());
    }
}
