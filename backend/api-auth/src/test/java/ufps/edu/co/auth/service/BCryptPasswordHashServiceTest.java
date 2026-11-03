package ufps.edu.co.auth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ufps.edu.co.auth.config.AuthProperties;
import ufps.edu.co.auth.util.PasswordFormats;

class BCryptPasswordHashServiceTest {

    private static final String CLAVE = "Clave123*";

    private static BCryptPasswordHashService service(boolean legacy) {
        AuthProperties properties = new AuthProperties();
        properties.getAuth().getPassword().setAllowPlainTextLegacy(legacy);
        return new BCryptPasswordHashService(new BCryptPasswordEncoder(), properties);
    }

    @Test
    @DisplayName("encode produce un hash BCrypt que PasswordFormats reconoce y matches valida")
    void encode_produceHashBcryptYMatches() {
        BCryptPasswordHashService service = service(true);

        String hash = service.encode(CLAVE);

        assertTrue(PasswordFormats.isBcrypt(hash));
        assertNotEquals(CLAVE, hash);
        assertTrue(service.matches(CLAVE, hash));
        assertFalse(service.matches("Otra123*", hash));
    }

    @Test
    @DisplayName("matches con texto plano y legacy=true compara exactamente")
    void matches_textoPlano_legacyTrue() {
        BCryptPasswordHashService service = service(true);

        assertTrue(service.matches(CLAVE, CLAVE));
        assertFalse(service.matches("Otra123*", CLAVE));
    }

    @Test
    @DisplayName("matches con texto plano y legacy=false rechaza la comparación")
    void matches_textoPlano_legacyFalse() {
        BCryptPasswordHashService service = service(false);

        assertFalse(service.matches(CLAVE, CLAVE));
    }

    @Test
    @DisplayName("PasswordFormats rechaza prefijos incompletos y acepta $2a$, $2b$ y $2y$")
    void passwordFormats_soloAceptaHashCompleto() {
        String hash = new BCryptPasswordEncoder().encode(CLAVE);

        assertTrue(PasswordFormats.isBcrypt(hash));
        assertTrue(PasswordFormats.isBcrypt("$2b$10$" + "a".repeat(53)));
        assertTrue(PasswordFormats.isBcrypt("$2y$10$" + "b".repeat(53)));
        assertFalse(PasswordFormats.isBcrypt("$2a$10$"));
        assertFalse(PasswordFormats.isBcrypt("$2a$10$" + "a".repeat(52)));
        assertFalse(PasswordFormats.isBcrypt("$2a$99$" + "a".repeat(53)));
        assertFalse(PasswordFormats.isBcrypt("$2a$10$" + "$".repeat(53)));
        assertFalse(PasswordFormats.isBcrypt(CLAVE));
        assertFalse(PasswordFormats.isBcrypt(null));
    }

    @Test
    @DisplayName("needsRehash es true para texto plano y false para un hash")
    void needsRehash_textoPlanoYHash() {
        BCryptPasswordHashService service = service(true);

        assertTrue(service.needsRehash(CLAVE));
        assertFalse(service.needsRehash(service.encode(CLAVE)));
    }
}
