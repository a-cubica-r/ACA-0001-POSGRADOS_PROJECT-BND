package ufps.edu.co.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ufps.edu.co.auth.config.AuthProperties;
import ufps.edu.co.auth.exception.InvalidTokenException;
import ufps.edu.co.auth.model.AuthPrincipal;
import ufps.edu.co.auth.records.output.LoginOutput;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private JwtTokenService service;

    @BeforeEach
    void setUp() {
        // Configuración mínima: secreto ≥ 32 caracteres para HMAC-SHA256
        AuthProperties props = new AuthProperties();
        props.getJwt().setSecret("test-secret-at-least-32-characters!!");
        props.getJwt().setIssuer("ufps-test");
        props.getJwt().setAccessExpirationMinutes(60);
        props.getJwt().setRefreshExpirationMinutes(1440);
        service = new JwtTokenService(props);
    }

    // --- Emisión de tokens ---

    @Test
    void issueTokens_retornaAccessYRefreshNoNulos() {
        // Dado
        AuthPrincipal principal = new AuthPrincipal(1, "usuario@test.com", List.of("ASPIRANTE"));

        // Cuando
        LoginOutput output = service.issueTokens(principal);

        // Entonces
        assertNotNull(output);
        assertNotNull(output.accessToken());
        assertFalse(output.accessToken().isBlank());
        assertNotNull(output.refreshToken());
        assertFalse(output.refreshToken().isBlank());
    }

    @Test
    void issueTokens_mapea_idUsuarioCorrectamente() {
        // Dado
        AuthPrincipal principal = new AuthPrincipal(42, "director@test.com", List.of("DIRECTOR"));

        // Cuando
        LoginOutput output = service.issueTokens(principal);

        // Entonces
        assertEquals(42, output.userId());
        assertEquals("director@test.com", output.username());
        assertTrue(output.roles().contains("DIRECTOR"));
    }

    // --- Validación de tokens de acceso ---

    @Test
    void isValid_conAccessTokenEmitido_retornaTrue() {
        // Dado
        AuthPrincipal principal = new AuthPrincipal(1, "user", List.of("ASPIRANTE"));
        LoginOutput output = service.issueTokens(principal);

        // Cuando/Entonces
        assertTrue(service.isValid(output.accessToken()));
    }

    @Test
    void isValid_conTokenMalformado_retornaFalse() {
        // Dado: token inválido
        assertFalse(service.isValid("esto.no.es.un.jwt.valido"));
    }

    @Test
    void isValid_conTokenNulo_retornaFalse() {
        assertFalse(service.isValid(null));
    }

    @Test
    void isValid_conCadenaVacia_retornaFalse() {
        assertFalse(service.isValid(""));
    }

    @Test
    void isValid_conRefreshTokenUsadoComoAccess_retornaFalse() {
        // Dado: el refresh token NO debe validar como access token
        AuthPrincipal principal = new AuthPrincipal(1, "user", List.of("ASPIRANTE"));
        LoginOutput output = service.issueTokens(principal);

        // Cuando/Entonces: refresh != access
        assertFalse(service.isValid(output.refreshToken()),
                "El refresh token no debe ser válido como access token");
    }

    // --- Parsing de claims ---

    @Test
    void parse_conAccessTokenValido_retornaPrincipalCorrecto() {
        // Dado
        AuthPrincipal originalPrincipal = new AuthPrincipal(7, "aspirante@ufps.edu.co", List.of("ASPIRANTE"));
        LoginOutput output = service.issueTokens(originalPrincipal);

        // Cuando
        AuthPrincipal parsed = service.parse(output.accessToken());

        // Entonces
        assertNotNull(parsed);
        assertEquals(7, parsed.userId());
        assertEquals("aspirante@ufps.edu.co", parsed.username());
        assertTrue(parsed.roles().contains("ASPIRANTE"));
    }

    @Test
    void parse_conRefreshTokenComoAccess_lanzaInvalidTokenException() {
        // Dado: intentar parsear el refresh como access
        AuthPrincipal principal = new AuthPrincipal(1, "user", List.of("ASPIRANTE"));
        LoginOutput output = service.issueTokens(principal);

        // Cuando/Entonces
        assertThrows(InvalidTokenException.class, () -> service.parse(output.refreshToken()));
    }

    // --- Refresco de tokens ---

    @Test
    void refreshTokens_conRefreshTokenValido_retornaTokensNuevos() {
        // Dado
        AuthPrincipal principal = new AuthPrincipal(5, "director", List.of("DIRECTOR"));
        LoginOutput original = service.issueTokens(principal);

        // Cuando
        LoginOutput refreshed = service.refreshTokens(original.refreshToken());

        // Entonces
        assertNotNull(refreshed);
        assertNotNull(refreshed.accessToken());
        assertEquals("director", refreshed.username());
    }

    @Test
    void refreshTokens_conAccessTokenComoRefresh_lanzaInvalidTokenException() {
        // Dado: no se puede refrescar con un access token
        AuthPrincipal principal = new AuthPrincipal(1, "user", List.of("ASPIRANTE"));
        LoginOutput output = service.issueTokens(principal);

        // Cuando/Entonces
        assertThrows(InvalidTokenException.class,
                () -> service.refreshTokens(output.accessToken()));
    }

    // --- Token expirado ---

    @Test
    void isValid_conTokenExpirado_retornaFalse() {
        // Dado: servicio configurado con expiración de -1 minuto (ya expirado)
        AuthProperties props = new AuthProperties();
        props.getJwt().setSecret("test-secret-at-least-32-characters!!");
        props.getJwt().setIssuer("ufps-test");
        props.getJwt().setAccessExpirationMinutes(-1);
        props.getJwt().setRefreshExpirationMinutes(1440);
        JwtTokenService expiredService = new JwtTokenService(props);

        AuthPrincipal principal = new AuthPrincipal(1, "user", List.of("ASPIRANTE"));
        LoginOutput output = expiredService.issueTokens(principal);

        // Cuando/Entonces: el token ya expiró
        assertFalse(service.isValid(output.accessToken()),
                "Un token emitido con expiración pasada debe ser inválido");
    }
}
