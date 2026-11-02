package ufps.edu.co.rest.exceptions;

/**
 * Se lanza cuando el registro solicitado no existe. Permite distinguir el
 * "no encontrado" de cualquier otro fallo (por ejemplo, una violaciÃ³n de
 * integridad) sin depender del mensaje de una RuntimeException genÃ©rica.
 */
public class RegistroNoEncontradoException extends RuntimeException {

    public RegistroNoEncontradoException(String message) {
        super(message);
    }
}