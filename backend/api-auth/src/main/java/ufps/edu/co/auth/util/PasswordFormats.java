package ufps.edu.co.auth.util;

import java.util.regex.Pattern;

/**
 * Formatos de contraseña almacenada. Un hash BCrypt válido es
 * {@code $2a$|$2b$|$2y$} + costo 04-31 + 53 caracteres base64 (60 en total).
 */
public final class PasswordFormats {

    private static final Pattern BCRYPT = Pattern
            .compile("^\\$2[aby]\\$(0[4-9]|[12]\\d|3[01])\\$[./A-Za-z0-9]{53}$");

    private PasswordFormats() {
    }

    public static boolean isBcrypt(String value) {
        return value != null && BCRYPT.matcher(value).matches();
    }
}
