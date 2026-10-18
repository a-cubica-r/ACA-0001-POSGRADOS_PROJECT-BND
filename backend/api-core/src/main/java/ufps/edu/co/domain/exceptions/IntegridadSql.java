package ufps.edu.co.domain.exceptions;

import java.sql.SQLException;

/**
 * Interpreta los errores de integridad que devuelve MySQL (código 1451, 1062,
 * 1452 y el nombre de la restricción). Se usa en el handler global de
 * {@code DataIntegrityViolationException} y en el servicio core que borra en
 * cascada, para reportar el mismo detalle en ambos lados.
 */
public final class IntegridadSql {

    private IntegridadSql() {
    }

    /** Código de error de MySQL (1451, 1062, 1452...) o 0 si no se puede leer. */
    public static int codigoError(Throwable throwable) {
        SQLException sql = buscarSqlException(throwable);
        return sql != null ? sql.getErrorCode() : 0;
    }

    /** Primera {@link SQLException} de la cadena de causas, o {@code null}. */
    public static SQLException buscarSqlException(Throwable throwable) {
        Throwable actual = throwable;
        while (actual != null) {
            if (actual instanceof SQLException sql) {
                return sql;
            }
            actual = actual.getCause();
        }
        return null;
    }

    /**
     * Nombre de la restricción de un mensaje tipo
     * {@code "... CONSTRAINT `cargo_ibfk_1` FOREIGN KEY ..."}. Devuelve
     * {@code null} si el mensaje no la incluye.
     */
    public static String extraerRestriccion(String mensaje) {
        if (mensaje == null) {
            return null;
        }
        String marca = "CONSTRAINT `";
        int inicio = mensaje.indexOf(marca);
        if (inicio < 0) {
            return null;
        }
        int desde = inicio + marca.length();
        int fin = mensaje.indexOf('`', desde);
        return fin > desde ? mensaje.substring(desde, fin) : null;
    }

    /** Mensaje de la causa SQL, o el de la excepción si no hay {@link SQLException}. */
    public static String mensajeCausa(Throwable throwable) {
        SQLException sql = buscarSqlException(throwable);
        if (sql != null && sql.getMessage() != null) {
            return sql.getMessage();
        }
        return throwable != null ? throwable.getMessage() : null;
    }
}
