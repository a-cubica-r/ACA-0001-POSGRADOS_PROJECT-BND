package ufps.edu.co.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.auth.contract.TokenIssuer;
import ufps.edu.co.auth.exception.InvalidCredentialsException;
import ufps.edu.co.auth.exception.MissingCredentialsException;
import ufps.edu.co.auth.exception.RoleMismatchException;
import ufps.edu.co.auth.model.AuthPrincipal;
import ufps.edu.co.auth.records.input.LoginInput;
import ufps.edu.co.auth.records.output.LoginOutput;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private CredentialService credentialService;

    @Mock
    private TokenIssuer tokenIssuer;

    @InjectMocks
    private AuthenticationService authService;

    // --- Validaciones de entrada nula/vacía ---

    @Test
    void login_conInputNulo_lanzaMissingCredentialsException() {
        assertThrows(MissingCredentialsException.class, () -> authService.login(null));
    }

    @Test
    void login_conUsernameNulo_lanzaMissingCredentialsException() {
        LoginInput input = new LoginInput(null, "password", null);
        assertThrows(MissingCredentialsException.class, () -> authService.login(input));
    }

    @Test
    void login_conUsernameVacio_lanzaMissingCredentialsException() {
        LoginInput input = new LoginInput("  ", "password", null);
        assertThrows(MissingCredentialsException.class, () -> authService.login(input));
    }

    @Test
    void login_conPasswordNulo_lanzaMissingCredentialsException() {
        LoginInput input = new LoginInput("usuario", null, null);
        assertThrows(MissingCredentialsException.class, () -> authService.login(input));
    }

    @Test
    void login_conPasswordVacio_lanzaMissingCredentialsException() {
        LoginInput input = new LoginInput("usuario", "   ", null);
        assertThrows(MissingCredentialsException.class, () -> authService.login(input));
    }

    // --- Credenciales inválidas ---

    @Test
    void login_conCredencialesInvalidas_lanzaInvalidCredentialsException() {
        LoginInput input = new LoginInput("usuario", "wrongpass", null);
        when(credentialService.authenticate("usuario", "wrongpass")).thenReturn(null);
        assertThrows(InvalidCredentialsException.class, () -> authService.login(input));
    }

    // --- Login exitoso sin rol requerido ---

    @Test
    void login_sinRolRequerido_retornaLoginOutput() {
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        LoginOutput expected = new LoginOutput("access", "refresh", 1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);
        when(tokenIssuer.issueTokens(principal)).thenReturn(expected);

        LoginOutput result = authService.login(input);

        assertNotNull(result);
        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        assertEquals(1, result.userId());
        assertEquals("usuario", result.username());
    }

    // --- Validación de rol requerido ---

    @Test
    void login_conRolCoincidente_retornaLoginOutput() {
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        LoginOutput expected = new LoginOutput("access", "refresh", 1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);
        when(tokenIssuer.issueTokens(principal)).thenReturn(expected);

        assertNotNull(authService.login(input, "ASPIRANTE"));
    }

    @Test
    void login_conRolIncorrecto_lanzaRoleMismatchException() {
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);

        assertThrows(RoleMismatchException.class, () -> authService.login(input, "DIRECTOR"));
    }

    @Test
    void login_conRolEnInputYEnParametro_parametroTienePrioridad() {
        // input.requestedRole=ASPIRANTE, parámetro=DIRECTOR → parámetro gana → falla
        LoginInput input = new LoginInput("usuario", "pass", "ASPIRANTE");
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);

        assertThrows(RoleMismatchException.class, () -> authService.login(input, "DIRECTOR"));
    }

    @Test
    void login_conRolNormalizadoConAcento_comparaCorrectamente() {
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        LoginOutput expected = new LoginOutput("access", "refresh", 1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);
        when(tokenIssuer.issueTokens(principal)).thenReturn(expected);

        // "Aspirante" normaliza a "ASPIRANTE" → debe coincidir
        assertDoesNotThrow(() -> authService.login(input, "Aspirante"));
    }

    @Test
    void login_conRolVacioComoParametro_noValidaRol() {
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        LoginOutput expected = new LoginOutput("access", "refresh", 1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);
        when(tokenIssuer.issueTokens(principal)).thenReturn(expected);

        assertDoesNotThrow(() -> authService.login(input, ""));
    }
}
