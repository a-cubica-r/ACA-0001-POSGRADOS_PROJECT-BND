package ufps.edu.co.exception;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.DuplicateAdmisionException;
import ufps.edu.co.domain.exceptions.IntegridadSql;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;
import ufps.edu.co.rest.exceptions.RegistroNoEncontradoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String CODE_REGISTRO_EN_USO = "REGISTRO_EN_USO_CONFLICT";
    private static final String CODE_REGISTRO_DUPLICADO = "REGISTRO_DUPLICADO_CONFLICT";
    private static final String CODE_REFERENCIA_INEXISTENTE = "REFERENCIA_INEXISTENTE";
    private static final String CODE_INTEGRIDAD_DATOS = "INTEGRIDAD_DATOS_CONFLICT";

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        HttpStatus status = resolveStatus(ex.getCode().getCode());
        ErrorResponse body = new ErrorResponse(
                ex.getCode().getCode(),
                ex.getCode().getDefaultMessage(),
                ex.getParam()
        );
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Entidades sin un {@code *_NOT_FOUND} propio (la mayoría del CRUD
     * genérico) usan esta excepción para distinguir "no existe" de cualquier
     * otro fallo. Se mantiene el 404 que antes producía el catch del
     * controlador, pero ahora con cuerpo y sin ocultar los errores de
     * integridad.
     */
    @ExceptionHandler(RegistroNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRegistroNoEncontrado(RegistroNoEncontradoException ex) {
        ErrorResponse body = new ErrorResponse("REGISTRO_NOT_FOUND", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DuplicateAdmisionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAdmision(DuplicateAdmisionException ex) {
        ErrorResponse body = new ErrorResponse(
                "LISTAADMITIDOS_DUPLICADO",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(PdfEmailException.class)
    public ResponseEntity<ErrorResponse> handlePdfEmailException(PdfEmailException ex) {
        ErrorResponse body = new ErrorResponse(
                "PDF_EMAIL_ERROR",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> param = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            param.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        for (ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
            param.putIfAbsent(objectError.getObjectName(), objectError.getDefaultMessage());
        }
        ErrorResponse body = new ErrorResponse(
                "VALIDACION_FALLIDA",
                "Hay campos inválidos en la solicitud",
                param
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getMostSpecificCause();
        String detalle = causa != null ? causa.getMessage() : ex.getMessage();
        ErrorResponse body = new ErrorResponse(
                "CUERPO_INVALIDO",
                "El cuerpo de la solicitud no se pudo leer",
                detalle
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        int codigo = IntegridadSql.codigoError(ex);
        String restriccion = IntegridadSql.extraerRestriccion(IntegridadSql.mensajeCausa(ex));
        log.warn("Violación de integridad en la base (MySQL {}): {}", codigo, IntegridadSql.mensajeCausa(ex));

        Map<String, Object> param = new HashMap<>();
        param.put("restriccion", restriccion);

        String code;
        String message;
        HttpStatus status;
        switch (codigo) {
            case 1451 -> {
                code = CODE_REGISTRO_EN_USO;
                message = "No se puede eliminar o modificar: el registro está en uso";
                status = HttpStatus.CONFLICT;
            }
            case 1062 -> {
                code = CODE_REGISTRO_DUPLICADO;
                message = "Ya existe un registro con esos datos";
                status = HttpStatus.CONFLICT;
            }
            case 1452 -> {
                code = CODE_REFERENCIA_INEXISTENTE;
                message = "Se hace referencia a un registro que no existe";
                status = HttpStatus.BAD_REQUEST;
            }
            default -> {
                code = CODE_INTEGRIDAD_DATOS;
                message = "La operación viola una restricción de datos";
                status = HttpStatus.CONFLICT;
            }
        }

        return ResponseEntity.status(status).body(new ErrorResponse(code, message, param));
    }

    /**
     * Errores HTTP que Spring ya sabe traducir (por ejemplo, el 404 de una ruta
     * inexistente). Se conserva el status original para no convertirlos en 500.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponse(ErrorResponseException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String detalle = ex.getBody() != null ? ex.getBody().getDetail() : null;
        ErrorResponse body = new ErrorResponse(
                "ERROR_HTTP_" + status.value(),
                detalle != null ? detalle : status.getReasonPhrase(),
                null
        );
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Último recurso: una excepción no controlada responde 500 con cuerpo y sin
     * traza, en lugar del 500/404 vacío que devolvían los catch de los
     * controladores del CRUD. Los handlers más específicos tienen prioridad.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(RuntimeException ex) {
        log.error("Error no controlado en la API", ex);
        ErrorResponse body = new ErrorResponse("ERROR_INTERNO", "Error interno del servidor", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private HttpStatus resolveStatus(String code) {
        if (CriterioevaluacionErrorCode.CRITERIO_CON_CALIFICACIONES_BLOQUEADO.getCode().equals(code)) {
            return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        }
        if (code.endsWith("_NOT_FOUND")) return HttpStatus.NOT_FOUND;
        if (code.endsWith("_ALREADY_EXISTS") || code.endsWith("_DUPLICADO")) return HttpStatus.CONFLICT;
        if (code.endsWith("_CONFLICT")) return HttpStatus.CONFLICT;
        if (code.endsWith("_FORBIDDEN")) return HttpStatus.FORBIDDEN;
        return HttpStatus.BAD_REQUEST;
    }
}
