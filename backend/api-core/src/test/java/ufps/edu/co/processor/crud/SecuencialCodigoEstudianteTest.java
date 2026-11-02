package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ufps.edu.co.services.core.crud.SecuencialCodigoEstudiante;

/**
 * D-00 — Regla única del secuencial del código de estudiante.
 */
class SecuencialCodigoEstudianteTest {

    @Test
    @DisplayName("Toma los últimos 4 dígitos y suma 1")
    void ultimoCodigoNormal_sumaUno() {
        assertEquals(5, SecuencialCodigoEstudiante.siguiente("21020004"));
    }

    @Test
    @DisplayName("Sin último código, el secuencial arranca en 0")
    void sinUltimoCodigo_arrancaEnCero() {
        assertEquals(0, SecuencialCodigoEstudiante.siguiente(null));
    }

    @Test
    @DisplayName("Un código de menos de 4 caracteres arranca en 0")
    void codigoCorto_arrancaEnCero() {
        assertEquals(0, SecuencialCodigoEstudiante.siguiente("123"));
    }

    @Test
    @DisplayName("Si los últimos 4 no son numéricos, arranca en 0")
    void ultimosCuatroNoNumericos_arrancaEnCero() {
        assertEquals(0, SecuencialCodigoEstudiante.siguiente("2102ABCD"));
    }

    @Test
    @DisplayName("Después de 9999 el programa queda agotado (no vuelve a 0)")
    void secuencialAlLimite_quedaAgotado() {
        assertEquals(SecuencialCodigoEstudiante.AGOTADO, SecuencialCodigoEstudiante.siguiente("21029999"));
    }
}
