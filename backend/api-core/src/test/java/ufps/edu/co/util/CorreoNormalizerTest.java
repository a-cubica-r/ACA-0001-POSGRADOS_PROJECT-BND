package ufps.edu.co.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CorreoNormalizerTest {

    @Test
    void normalizaMayusculasYEspacios() {
        assertEquals("adolfo@outlook.com", CorreoNormalizer.normalizar("  ADOLFO@Outlook.COM "));
    }

    @Test
    void normalizarNullDevuelveNull() {
        assertNull(CorreoNormalizer.normalizar(null));
    }

    @Test
    void validaFormatosBasicos() {
        assertTrue(CorreoNormalizer.esValido("A@B.CO"));
        assertTrue(CorreoNormalizer.esValido("nombre.apellido+etiqueta@dominio.com"));
        assertFalse(CorreoNormalizer.esValido("no-es-correo"));
        assertFalse(CorreoNormalizer.esValido("a@b"));
        assertFalse(CorreoNormalizer.esValido(null));
    }
}
