package ufps.edu.co.records.input.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClaveInputTest {

    @Test
    @DisplayName("Los records de clave no imprimen el valor en toString")
    void toString_noImprimeValor() {
        String crudo = "Clave123*";

        String create = new ClaveInput.CLAVE_CREATE(crudo).toString();
        String update = new ClaveInput.CLAVE_UPDATE(1, crudo).toString();
        String patch = new ClaveInput.CLAVE_PATCH(1, crudo).toString();

        assertFalse(create.contains(crudo));
        assertFalse(update.contains(crudo));
        assertFalse(patch.contains(crudo));
        assertTrue(create.contains("valor=***"));
        assertTrue(update.contains("valor=***"));
        assertTrue(patch.contains("valor=***"));
    }
}
