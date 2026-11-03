package ufps.edu.co.domain.validators;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PromedioAcademicoTest {

    @Test
    void aceptaValoresDentroDelRangoConHastaDosDecimales() {
        assertTrue(PromedioAcademico.esValido(new BigDecimal("0.0")));
        assertTrue(PromedioAcademico.esValido(new BigDecimal("3.85")));
        assertTrue(PromedioAcademico.esValido(new BigDecimal("5.0")));
        assertTrue(PromedioAcademico.esValido(new BigDecimal("4.5")));
        assertTrue(PromedioAcademico.esValido(new BigDecimal("4.50")));
    }

    @Test
    void rechazaValoresFueraDelRango() {
        assertFalse(PromedioAcademico.esValido(new BigDecimal("5.01")));
        assertFalse(PromedioAcademico.esValido(new BigDecimal("6")));
        assertFalse(PromedioAcademico.esValido(new BigDecimal("-0.1")));
    }

    @Test
    void rechazaMasDeDosDecimales() {
        assertFalse(PromedioAcademico.esValido(new BigDecimal("4.567")));
    }

    @Test
    void rechazaNull() {
        assertFalse(PromedioAcademico.esValido(null));
    }
}
