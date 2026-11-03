package ufps.edu.co.auth.records.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginInputTest {

    @Test
    @DisplayName("toString no imprime la contraseña")
    void toString_noImprimePassword() {
        LoginInput input = new LoginInput("admin", "Clave123*", "SUPER_ADMINISTRADOR");

        String texto = input.toString();

        assertFalse(texto.contains("Clave123*"));
        assertTrue(texto.contains("admin"));
        assertTrue(texto.contains("SUPER_ADMINISTRADOR"));
    }
}
