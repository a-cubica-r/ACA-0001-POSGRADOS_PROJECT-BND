package ufps.edu.co.controllers.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import ufps.edu.co.auth.contract.AuthenticationUseCase;
import ufps.edu.co.auth.contract.RefreshTokenUseCase;
import ufps.edu.co.auth.exception.InvalidCredentialsException;
import ufps.edu.co.auth.exception.InvalidTokenException;
import ufps.edu.co.auth.exception.MissingCredentialsException;
import ufps.edu.co.auth.exception.RoleMismatchException;
import ufps.edu.co.auth.records.input.LoginInput;
import ufps.edu.co.auth.records.output.LoginOutput;
import ufps.edu.co.exception.GlobalExceptionHandler;

/**
 * Pruebas del contrato HTTP de /auth/login y /auth/refresh.
 * Cubre códigos de estado, cuerpo de respuesta y manejo de errores de autenticación.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationUseCase authenticationUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static final String LOGIN_VALIDO = """
            {"username":"admin@ufps.edu.co","password":"Clave123*","requestedRole":"SUPER_ADMINISTRADOR"}
            """;

    private static final LoginOutput TOKENS = new LoginOutput(
            "access.token.jwt",
            "refresh.token.jwt",
            1,
            "admin@ufps.edu.co",
            List.of("SUPER_ADMINISTRADOR"));

    // ─── POST /auth/login ───────────────────────────────────────────────────

    @Test
    @DisplayName("Login exitoso → 200 con accessToken y refreshToken en el cuerpo")
    void login_credencialesValidas_responde200ConTokens() throws Exception {
        when(authenticationUseCase.login(any(LoginInput.class))).thenReturn(TOKENS);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token.jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token.jwt"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("admin@ufps.edu.co"));
    }

    @Test
    @DisplayName("Credenciales incorrectas → 401 sin cuerpo")
    void login_credencialesInvalidas_responde401() throws Exception {
        when(authenticationUseCase.login(any(LoginInput.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rol incorrecto → 403 sin cuerpo")
    void login_rolIncorrecto_responde403() throws Exception {
        when(authenticationUseCase.login(any(LoginInput.class)))
                .thenThrow(new RoleMismatchException("rol no coincide"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Credenciales vacías en servicio → 400 sin cuerpo")
    void login_missingCredentials_responde400() throws Exception {
        when(authenticationUseCase.login(any(LoginInput.class)))
                .thenThrow(new MissingCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_VALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Username vacío en JSON → 400 VALIDACION_FALLIDA por @NotBlank")
    void login_usernameVacio_responde400Validacion() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"pass\",\"requestedRole\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.username").exists());
    }

    @Test
    @DisplayName("Password ausente en JSON → 400 VALIDACION_FALLIDA por @NotBlank")
    void login_passwordAusente_responde400Validacion() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"requestedRole\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.password").exists());
    }

    @Test
    @DisplayName("Cuerpo JSON malformado → 400 CUERPO_INVALIDO")
    void login_jsonMalformado_responde400CuerpoInvalido() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CUERPO_INVALIDO"));
    }

    // ─── POST /auth/refresh ─────────────────────────────────────────────────

    @Test
    @DisplayName("Refresh con token válido → 200 con nuevos tokens")
    void refresh_tokenValido_responde200ConTokensNuevos() throws Exception {
        LoginOutput nuevosTokens = new LoginOutput(
                "nuevo-access", "nuevo-refresh", 1, "admin@ufps.edu.co", List.of("SUPER_ADMINISTRADOR"));
        when(refreshTokenUseCase.refresh("token-de-refresco")).thenReturn(nuevosTokens);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"token-de-refresco\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("nuevo-access"))
                .andExpect(jsonPath("$.refreshToken").value("nuevo-refresh"));
    }

    @Test
    @DisplayName("Refresh con token inválido/expirado → 401 sin cuerpo")
    void refresh_tokenInvalido_responde401() throws Exception {
        when(refreshTokenUseCase.refresh("token-expirado"))
                .thenThrow(new InvalidTokenException("token expirado"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"token-expirado\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh con token ausente en servicio → 400 sin cuerpo")
    void refresh_missingToken_responde400() throws Exception {
        when(refreshTokenUseCase.refresh(any()))
                .thenThrow(new MissingCredentialsException());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"algo\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("refreshToken vacío en JSON → 400 VALIDACION_FALLIDA por @NotBlank")
    void refresh_refreshTokenVacio_responde400Validacion() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.param.refreshToken").exists());
    }
}
