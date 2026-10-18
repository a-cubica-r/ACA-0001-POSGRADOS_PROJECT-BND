package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum FacultadErrorCode implements BaseErrorCode {
    FACULTAD_NOT_FOUND(
            "FACULTAD_NOT_FOUND",
            "Facultad no encontrada"),
    FACULTAD_NOMBRE_DUPLICADO(
            "FACULTAD_NOMBRE_DUPLICADO",
            "Ya existe una facultad con ese nombre"),
    FACULTAD_PARAMETRO_REQUERIDO(
            "FACULTAD_PARAMETRO_REQUERIDO",
            "Falta un parámetro requerido para procesar la facultad");

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
