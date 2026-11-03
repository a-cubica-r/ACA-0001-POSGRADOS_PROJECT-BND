package ufps.edu.co.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.auth.contract.TokenIssuer;
import ufps.edu.co.auth.exception.InvalidTokenException;
import ufps.edu.co.auth.exception.MissingCredentialsException;
import ufps.edu.co.auth.model.AuthPrincipal;
import ufps.edu.co.auth.records.output.LoginOutput;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private TokenIssuer tokenIssuer;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    // --- Validaciones de entrada ---

    @Test
    void refresh_conTokenNulo_lanzaMissingCredentialsException() {
        assertThrows(MissingCredentialsException.class, () -> refreshTokenService.refresh(null));
    }

    @Test
    void refresh_conTokenVacio_lanzaMissingCredentialsException() {
        assertThrows(MissingCredentialsException.class, () -> refreshTokenService.refresh(""));
    }

    @Test
    void refresh_conTokenSoloEspacios_lanzaMissingCredentialsException() {
        assertThrows(MissingCredentialsException.class, () -> refreshTokenService.refresh("   "));
    }

    // --- Token inválido / expirado ---

    @Test
    void refresh_conTokenExpirado_propagaInvalidTokenException() {
        when(tokenIssuer.refreshTokens("token.expirado")).thenThrow(new InvalidTokenException("Token expired"));

        assertThrows(InvalidTokenException.class, () -> refreshTokenService.refresh("token.expirado"));
    }

    @Test
    void refresh_conTokenMalformado_propagaInvalidTokenException() {
        when(tokenIssuer.refreshTokens("no-es-jwt")).thenThrow(new InvalidTokenException("Invalid token"));

        assertThrows(InvalidTokenException.class, () -> refreshTokenService.refresh("no-es-jwt"));
    }

    // --- Refresh exitoso ---

    @Test
    void refresh_conTokenValido_retornaLoginOutput() {
        AuthPrincipal principal = new AuthPrincipal(3, "director", List.of("DIRECTOR"));
        LoginOutput expected = new LoginOutput("new-access", "new-refresh", 3, "director", List.of("DIRECTOR"));
        when(tokenIssuer.refreshTokens("valid.refresh.token")).thenReturn(expected);

        LoginOutput result = refreshTokenService.refresh("valid.refresh.token");

        assertNotNull(result);
        assertEquals("new-access", result.accessToken());
        assertEquals("new-refresh", result.refreshToken());
        assertEquals(3, result.userId());
        assertEquals("director", result.username());
        verify(tokenIssuer, times(1)).refreshTokens("valid.refresh.token");
    }

    @Test
    void refresh_conTokenValido_delegaExactamenteAlTokenIssuer() {
        LoginOutput expected = new LoginOutput("a", "b", 1, "u", List.of());
        when(tokenIssuer.refreshTokens("mi-token")).thenReturn(expected);

        refreshTokenService.refresh("mi-token");

        verify(tokenIssuer).refreshTokens("mi-token");
        verifyNoMoreInteractions(tokenIssuer);
    }
}
