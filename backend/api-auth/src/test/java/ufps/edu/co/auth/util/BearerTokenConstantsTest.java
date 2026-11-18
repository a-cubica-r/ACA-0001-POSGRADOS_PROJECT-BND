package ufps.edu.co.auth.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BearerTokenConstantsTest {

    @Test
    @DisplayName("Header 'Bearer <token>' retorna solo el token")
    void extractToken_headerBearerValido_retornaToken() {
        assertEquals("mi-jwt-token", BearerTokenConstants.extractToken("Bearer mi-jwt-token"));
    }

    @Test
    @DisplayName("Header nulo retorna null")
    void extractToken_headerNulo_retornaNull() {
        assertNull(BearerTokenConstants.extractToken(null));
    }

    @Test
    @DisplayName("Header vacío retorna null")
    void extractToken_headerVacio_retornaNull() {
        assertNull(BearerTokenConstants.extractToken(""));
    }

    @Test
    @DisplayName("Header sin prefijo Bearer retorna null")
    void extractToken_sinPrefijo_retornaNull() {
        assertNull(BearerTokenConstants.extractToken("Basic dXNlcjpwYXNz"));
    }

    @Test
    @DisplayName("Header con solo 'Bearer ' (sin token) retorna null")
    void extractToken_soloPrefijo_retornaNull() {
        assertNull(BearerTokenConstants.extractToken("Bearer "));
    }

    @Test
    @DisplayName("Header con espacios alrededor del token: trim elimina los espacios")
    void extractToken_tokenConEspacios_trimeaElToken() {
        assertEquals("token", BearerTokenConstants.extractToken("Bearer   token"));
    }

    @Test
    @DisplayName("'bearer' en minúsculas no es reconocido (comparación exacta)")
    void extractToken_prefijoEnMinusculas_retornaNull() {
        assertNull(BearerTokenConstants.extractToken("bearer mi-token"));
    }

    @Test
    @DisplayName("Token que contiene puntos (JWT real) se extrae completo")
    void extractToken_jwtReal_retornaCompleto() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.abc123";
        assertEquals(jwt, BearerTokenConstants.extractToken("Bearer " + jwt));
    }

    @Test
    @DisplayName("AUTHORIZATION_HEADER tiene el valor correcto")
    void constanteAuthorizationHeader_esCorrecta() {
        assertEquals("Authorization", BearerTokenConstants.AUTHORIZATION_HEADER);
    }

    @Test
    @DisplayName("BEARER_PREFIX tiene el valor correcto (con espacio al final)")
    void constanteBearerPrefix_esCorrecta() {
        assertEquals("Bearer ", BearerTokenConstants.BEARER_PREFIX);
    }
}
