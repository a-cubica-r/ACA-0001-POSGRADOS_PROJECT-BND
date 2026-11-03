package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ClaveErrorCode implements BaseErrorCode {
    CLAVE_VALOR_OBLIGATORIO(
            "CLAVE_VALOR_OBLIGATORIO",
            "El valor de la clave es obligatorio");

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
