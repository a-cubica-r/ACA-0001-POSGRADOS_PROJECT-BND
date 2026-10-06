package ufps.edu.co.auth.processor;

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
import ufps.edu.co.auth.service.CredentialService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationProcessorTest {

    @Mock
    private CredentialService credentialService;

    @Mock
    private TokenIssuer tokenIssuer;

    @InjectMocks
    private AuthenticationProcessor processor;

    // --- Validaciones de entrada nula/vacía ---

    @Test
    void login_conInputNulo_lanzaMissingCredentialsException() {
        // Dado: input completamente nulo
        // Cuando/Entonces
        assertThrows(MissingCredentialsException.class, () -> processor.login(null));
    }

    @Test
    void login_conUsernameNulo_lanzaMissingCredentialsException() {
        // Dado
        LoginInput input = new LoginInput(null, "password", null);
        // Cuando/Entonces
        assertThrows(MissingCredentialsException.class, () -> processor.login(input));
    }

    @Test
    void login_conUsernameVacio_lanzaMissingCredentialsException() {
        // Dado
        LoginInput input = new LoginInput("  ", "password", null);
        // Cuando/Entonces
        assertThrows(MissingCredentialsException.class, () -> processor.login(input));
    }

    @Test
    void login_conPasswordNulo_lanzaMissingCredentialsException() {
        // Dado
        LoginInput input = new LoginInput("usuario", null, null);
        // Cuando/Entonces
        assertThrows(MissingCredentialsException.class, () -> processor.login(input));
    }

    @Test
    void login_conPasswordVacio_lanzaMissingCredentialsException() {
        // Dado
        LoginInput input = new LoginInput("usuario", "   ", null);
        // Cuando/Entonces
        assertThrows(MissingCredentialsException.class, () -> processor.login(input));
    }

    // --- Credenciales inválidas ---

    @Test
    void login_conCredencialesInvalidas_lanzaInvalidCredentialsException() {
        // Dado: authenticate retorna null
        LoginInput input = new LoginInput("usuario", "wrongpass", null);
        when(credentialService.authenticate("usuario", "wrongpass")).thenReturn(null);
        // Cuando/Entonces
        assertThrows(InvalidCredentialsException.class, () -> processor.login(input));
    }

    // --- Login exitoso sin rol requerido ---

    @Test
    void login_sinRolRequerido_retornaLoginOutput() {
        // Dado
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        LoginOutput expectedOutput = new LoginOutput("access", "refresh", 1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);
        when(tokenIssuer.issueTokens(principal)).thenReturn(expectedOutput);

        // Cuando
        LoginOutput result = processor.login(input);

        // Entonces
        assertNotNull(result);
        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        assertEquals(1, result.userId());
        assertEquals("usuario", result.username());
    }

    // --- Validación de rol requerido ---

    @Test
    void login_conRolCoincidente_retornaLoginOutput() {
        // Dado: usuario tiene ASPIRANTE y pide ASPIRANTE
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        LoginOutput expectedOutput = new LoginOutput("access", "refresh", 1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);
        when(tokenIssuer.issueTokens(principal)).thenReturn(expectedOutput);

        // Cuando
        LoginOutput result = processor.login(input, "ASPIRANTE");

        // Entonces
        assertNotNull(result);
    }

    @Test
    void login_conRolIncorrecto_lanzaRoleMismatchException() {
        // Dado: usuario tiene ASPIRANTE pero pide DIRECTOR
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);

        // Cuando/Entonces
        assertThrows(RoleMismatchException.class, () -> processor.login(input, "DIRECTOR"));
    }

    @Test
    void login_conRolEnInputYEnParametro_parametroTienePrioridad() {
        // Dado: input.requestedRole = ASPIRANTE, parámetro = DIRECTOR
        //       usuario solo tiene ASPIRANTE → el parámetro DIRECTOR debe fallar
        LoginInput input = new LoginInput("usuario", "pass", "ASPIRANTE");
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);

        // Cuando/Entonces: el parámetro DIRECTOR gana y el usuario no lo tiene
        assertThrows(RoleMismatchException.class, () -> processor.login(input, "DIRECTOR"));
    }

    @Test
    void login_conRolNormalizadoConAcento_comparaCorrectamente() {
        // Dado: rol del usuario ya normalizado = ASPIRANTE; se pide con acento = Aspiranté
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        LoginOutput expectedOutput = new LoginOutput("access", "refresh", 1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);
        when(tokenIssuer.issueTokens(principal)).thenReturn(expectedOutput);

        // Cuando: "Aspiranté" normaliza a "ASPIRANTE" → debe coincidir
        assertDoesNotThrow(() -> processor.login(input, "Aspirante"));
    }

    @Test
    void login_conRolVacioComoParametro_noValidaRol() {
        // Dado: parámetro rol vacío → no se valida rol
        LoginInput input = new LoginInput("usuario", "pass", null);
        AuthPrincipal principal = new AuthPrincipal(1, "usuario", List.of("ASPIRANTE"));
        LoginOutput expectedOutput = new LoginOutput("access", "refresh", 1, "usuario", List.of("ASPIRANTE"));
        when(credentialService.authenticate("usuario", "pass")).thenReturn(principal);
        when(tokenIssuer.issueTokens(principal)).thenReturn(expectedOutput);

        // Cuando/Entonces: no lanza excepción
        assertDoesNotThrow(() -> processor.login(input, ""));
    }
}
