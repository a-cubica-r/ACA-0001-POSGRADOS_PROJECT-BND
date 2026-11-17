package ufps.edu.co.controllers.cases.aspirante;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.exception.GlobalExceptionHandler;
import ufps.edu.co.persistence.entities.PersonaEntity;
import ufps.edu.co.persistence.entities.UsuarioEntity;
import ufps.edu.co.persistence.repositories.PersonaRepository;
import ufps.edu.co.persistence.repositories.UsuarioRepository;
import ufps.edu.co.rest.dto.ClaveDTO;
import ufps.edu.co.rest.services.ClaveService;
import ufps.edu.co.services.PasswordResetTokenService;
import ufps.edu.co.services.SESService;

@ExtendWith(MockitoExtension.class)
class RecuperarContrasenaControllerTest {

    @Mock private PersonaRepository personaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ClaveService claveService;
    @Mock private PasswordHashService passwordHashService;
    @Mock private PasswordResetTokenService passwordResetTokenService;
    @Mock private SESService sesService;

    @InjectMocks
    private RecuperarContrasenaController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        ReflectionTestUtils.setField(controller, "frontendResetPasswordUrl", "https://front/reset");
    }

    private PersonaEntity persona(int id, String correo) {
        PersonaEntity persona = new PersonaEntity();
        persona.setId(id);
        persona.setNombres("Adolfo");
        persona.setCorreo(correo);
        return persona;
    }

    private UsuarioEntity usuario(int idClave) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdClave(idClave);
        return usuario;
    }

    @Test
    @DisplayName("solicitar normaliza el correo a minúsculas antes de buscar y enviar")
    void solicitar_normalizaCorreo() throws Exception {
        PersonaEntity persona = persona(1, "adolfo@mail.com");
        when(personaRepository.findByCorreoIgnoreCase("adolfo@mail.com")).thenReturn(Optional.of(persona));
        when(usuarioRepository.findAllByIdPersona(1)).thenReturn(List.of(usuario(10)));
        when(passwordResetTokenService.generateToken("adolfo@mail.com")).thenReturn("tok");

        mockMvc.perform(post("/recuperarContrasena/solicitar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ADOLFO@MAIL.COM\"}"))
                .andExpect(status().isOk());

        verify(personaRepository).findByCorreoIgnoreCase("adolfo@mail.com");
        verify(sesService).enviarCorreoAsync(eq("adolfo@mail.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("solicitar con correo inexistente responde 200 sin enviar")
    void solicitarInexistente_responde200SinEnvio() throws Exception {
        when(personaRepository.findByCorreoIgnoreCase("nadie@mail.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/recuperarContrasena/solicitar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"nadie@mail.com\"}"))
                .andExpect(status().isOk());

        verifyNoInteractions(sesService);
    }

    @Test
    @DisplayName("cambiar actualiza la clave de todos los usuarios de la persona")
    void cambiar_actualizaTodosLosUsuarios() throws Exception {
        PersonaEntity persona = persona(1, "adolfo@mail.com");
        when(passwordResetTokenService.validateAndExtract("bueno")).thenReturn("ADOLFO@MAIL.COM");
        when(personaRepository.findByCorreoIgnoreCase("adolfo@mail.com")).thenReturn(Optional.of(persona));
        when(usuarioRepository.findAllByIdPersona(1)).thenReturn(List.of(usuario(10), usuario(11)));
        when(passwordHashService.encode("Nueva123*")).thenReturn("hash");

        mockMvc.perform(post("/recuperarContrasena/cambiar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"bueno\",\"nuevaContrasena\":\"Nueva123*\"}"))
                .andExpect(status().isOk());

        verify(claveService).update(eq(10), any(ClaveDTO.class));
        verify(claveService).update(eq(11), any(ClaveDTO.class));
        verify(claveService, never()).update(eq(12), any(ClaveDTO.class));
    }

    @Test
    @DisplayName("cambiar con token inválido responde 400 con código")
    void cambiar_tokenInvalido_responde400() throws Exception {
        when(passwordResetTokenService.validateAndExtract("malo"))
                .thenThrow(new IllegalArgumentException("token"));

        mockMvc.perform(post("/recuperarContrasena/cambiar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"malo\",\"nuevaContrasena\":\"Nueva123*\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOKEN_RECUPERACION_INVALIDO"));
    }

    @Test
    @DisplayName("cambiar con persona ya inexistente responde 404")
    void cambiar_personaInexistente_responde404() throws Exception {
        when(passwordResetTokenService.validateAndExtract("bueno")).thenReturn("adolfo@mail.com");
        when(personaRepository.findByCorreoIgnoreCase("adolfo@mail.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/recuperarContrasena/cambiar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"bueno\",\"nuevaContrasena\":\"Nueva123*\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PERSONA_NOT_FOUND"));
    }
}
