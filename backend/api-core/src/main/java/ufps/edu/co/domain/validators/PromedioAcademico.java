package ufps.edu.co.domain.validators;

import java.math.BigDecimal;

/**
 * Regla única para el promedio ponderado acumulado de pregrado.
 */
public final class PromedioAcademico {

    public static final BigDecimal MIN = new BigDecimal("0.0");
    public static final BigDecimal MAX = new BigDecimal("5.0");
    private static final int MAX_DECIMALES = 2;

    private PromedioAcademico() {
    }

    public static boolean esValido(BigDecimal promedio) {
        if (promedio == null) {
            return false;
        }
        if (promedio.compareTo(MIN) < 0 || promedio.compareTo(MAX) > 0) {
            return false;
        }
        return promedio.stripTrailingZeros().scale() <= MAX_DECIMALES;
    }
}
