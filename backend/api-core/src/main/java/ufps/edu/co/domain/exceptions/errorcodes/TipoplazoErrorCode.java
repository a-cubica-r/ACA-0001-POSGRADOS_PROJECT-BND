package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum TipoplazoErrorCode implements BaseErrorCode {
    TIPOPLAZO_NO_CONFIGURADO_CONFLICT(
            "TIPOPLAZO_NO_CONFIGURADO_CONFLICT",
            "No existe el tipo de plazo requerido");

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
