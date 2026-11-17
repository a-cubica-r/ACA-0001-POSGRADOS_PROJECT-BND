package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum SemestreErrorCode implements BaseErrorCode {
    SEMESTRE_PERIODO_DUPLICADO(
            "SEMESTRE_PERIODO_DUPLICADO",
            "Ya existe un semestre para ese año y periodo"),
    SEMESTRE_NOMBRE_DUPLICADO(
            "SEMESTRE_NOMBRE_DUPLICADO",
            "Ya existe un semestre con ese nombre"),
    SEMESTRE_FECHAS_INVALIDAS(
            "SEMESTRE_FECHAS_INVALIDAS",
            "La fecha de inicio debe ser anterior a la fecha de fin"),
    SEMESTRE_PARAMETRO_REQUERIDO(
            "SEMESTRE_PARAMETRO_REQUERIDO",
            "Falta un parámetro requerido para procesar el semestre");

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
