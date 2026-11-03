package ufps.edu.co.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.AspiranteErrorCode;

class GlobalExceptionHandlerCorreoTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void correoNoDisponibleResponde503() {
        ResponseEntity<ErrorResponse> response = handler.handleDomainException(
                new DomainException(AspiranteErrorCode.CORREO_ENVIO_UNAVAILABLE, 12));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("CORREO_ENVIO_UNAVAILABLE", response.getBody().code());
    }

    @Test
    void correoInvalidoResponde400() {
        ResponseEntity<ErrorResponse> response = handler.handleDomainException(
                new DomainException(AspiranteErrorCode.CORREO_FORMATO_INVALIDO, "no-es-correo"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("CORREO_FORMATO_INVALIDO", response.getBody().code());
    }
}
