package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum IntegracionTramitesErrorCode implements BaseErrorCode {

    ESTUDIANTE_NOT_FOUND(
        "ESTUDIANTE_NOT_FOUND",
        "No existe un estudiante con esa cédula"),
    CEDULA_INVALIDA(
        "CEDULA_INVALIDA",
        "La cédula debe tener entre 5 y 15 dígitos");

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
