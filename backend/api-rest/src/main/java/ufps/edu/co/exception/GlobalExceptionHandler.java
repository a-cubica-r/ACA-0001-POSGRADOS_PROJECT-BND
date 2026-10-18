package ufps.edu.co.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.DuplicateAdmisionException;
import ufps.edu.co.domain.exceptions.errorcodes.CriterioevaluacionErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
