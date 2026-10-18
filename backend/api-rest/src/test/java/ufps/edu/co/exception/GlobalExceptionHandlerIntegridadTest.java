package ufps.edu.co.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * D-05 / D-08.1 — Los errores de integridad de MySQL dejan de llegar como 500
 * vacío y se traducen a un {@link ErrorResponse} con código.
 */
class GlobalExceptionHandlerIntegridadTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static DataIntegrityViolationException conSql(int codigo, String mensaje) {
        return new DataIntegrityViolationException("fallo de integridad",
                new SQLException(mensaje, "23000", codigo));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> param(ResponseEntity<ErrorResponse> respuesta) {
        return (Map<String, Object>) respuesta.getBody().param();
    }

    @Test
    @DisplayName("1451 (llave foránea) → 409 REGISTRO_EN_USO_CONFLICT con el nombre de la restricción")
    void llaveForaneaEnUso() {
        DataIntegrityViolationException ex = conSql(1451,
                "Cannot delete or update a parent row: a foreign key constraint fails "
                        + "(`railway`.`cargo`, CONSTRAINT `cargo_ibfk_1` FOREIGN KEY (`id_programa`) "
                        + "REFERENCES `programas` (`id`) ON DELETE RESTRICT)");

        ResponseEntity<ErrorResponse> respuesta = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("REGISTRO_EN_USO_CONFLICT", respuesta.getBody().code());
        assertEquals("cargo_ibfk_1", param(respuesta).get("restriccion"));
    }

    @Test
    @DisplayName("1062 (duplicado) → 409 REGISTRO_DUPLICADO_CONFLICT")
    void registroDuplicado() {
        DataIntegrityViolationException ex = conSql(1062,
                "Duplicate entry '2102' for key 'programas.programas_index_10'");

        ResponseEntity<ErrorResponse> respuesta = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("REGISTRO_DUPLICADO_CONFLICT", respuesta.getBody().code());
        assertNull(param(respuesta).get("restriccion"));
    }

    @Test
    @DisplayName("1452 (padre inexistente) → 400 REFERENCIA_INEXISTENTE")
    void referenciaInexistente() {
        DataIntegrityViolationException ex = conSql(1452,
                "Cannot add or update a child row: a foreign key constraint fails "
                        + "(`railway`.`cargo`, CONSTRAINT `cargo_ibfk_1` FOREIGN KEY (`id_programa`) "
                        + "REFERENCES `programas` (`id`))");

        ResponseEntity<ErrorResponse> respuesta = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("REFERENCIA_INEXISTENTE", respuesta.getBody().code());
        assertEquals("cargo_ibfk_1", param(respuesta).get("restriccion"));
    }

    @Test
    @DisplayName("Código desconocido → 409 INTEGRIDAD_DATOS_CONFLICT")
    void codigoDesconocido() {
        DataIntegrityViolationException ex = conSql(9999, "otro error de integridad");

        ResponseEntity<ErrorResponse> respuesta = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("INTEGRIDAD_DATOS_CONFLICT", respuesta.getBody().code());
    }

    @Test
    @DisplayName("Sin SQLException en la cadena → 409 INTEGRIDAD_DATOS_CONFLICT, sin reventar")
    void sinSqlException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("sin causa SQL");

        ResponseEntity<ErrorResponse> respuesta = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("INTEGRIDAD_DATOS_CONFLICT", respuesta.getBody().code());
        assertNull(param(respuesta).get("restriccion"));
    }
}
