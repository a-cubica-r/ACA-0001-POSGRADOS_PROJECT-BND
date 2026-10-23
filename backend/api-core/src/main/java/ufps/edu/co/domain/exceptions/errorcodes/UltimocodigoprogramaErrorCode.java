package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum UltimocodigoprogramaErrorCode implements BaseErrorCode {

    ULTIMOCODIGOPROGRAMA_NOT_FOUND(
            "ULTIMOCODIGOPROGRAMA_NOT_FOUND",
            "El programa no tiene registro de último código"),
    ULTIMOCODIGOPROGRAMA_CODIGO_INVALIDO(
            "ULTIMOCODIGOPROGRAMA_CODIGO_INVALIDO",
            "El código debe ser un entero entre 0 y 9999"),
    ULTIMOCODIGOPROGRAMA_CODIGO_MENOR_QUE_ESTUDIANTES_CONFLICT(
            "ULTIMOCODIGOPROGRAMA_CODIGO_MENOR_QUE_ESTUDIANTES_CONFLICT",
            "El código es menor que el siguiente al último estudiante del programa"),
    ULTIMOCODIGOPROGRAMA_AGOTADO_CONFLICT(
            "ULTIMOCODIGOPROGRAMA_AGOTADO_CONFLICT",
            "El programa agotó los 10000 códigos de estudiante disponibles");

    private final String code;
    private final String defaultMessage;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
