package ufps.edu.co.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalización y validación básica de correos electrónicos.
 */
public final class CorreoNormalizer {

    private static final Pattern FORMATO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private CorreoNormalizer() {
    }

    public static String normalizar(String correo) {
        if (correo == null) {
            return null;
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean esValido(String correo) {
        return correo != null && FORMATO.matcher(normalizar(correo)).matches();
    }
}
