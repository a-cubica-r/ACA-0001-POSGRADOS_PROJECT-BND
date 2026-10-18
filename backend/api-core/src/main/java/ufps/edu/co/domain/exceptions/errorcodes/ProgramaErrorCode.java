package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ProgramaErrorCode implements BaseErrorCode {
    PROGRAMA_NOT_FOUND(
            "PROGRAMA_NOT_FOUND",
            "Programa no encontrado"),
    PROGRAMA_SIN_TIPO_REGISTRO(
            "PROGRAMA_SIN_TIPO_REGISTRO",
            "El programa no tiene tipo de registro configurado"),
    PROGRAMA_TIPO_REGISTRO_NO_VALIDO(
            "PROGRAMA_TIPO_REGISTRO_NO_VALIDO",
            "Tipo de registro no soportado para consultar modalidades"),
    PROGRAMA_SIN_MODALIDAD_ASIGNADA(
            "PROGRAMA_SIN_MODALIDAD_ASIGNADA",
            "El programa no tiene modalidad asignada"),
    PROGRAMA_PARAMETRO_REQUERIDO(
            "PROGRAMA_PARAMETRO_REQUERIDO",
            "Falta un parámetro requerido para procesar el programa"),
    TIPOREGISTRO_NOT_FOUND(
            "TIPOREGISTRO_NOT_FOUND",
            "Tiporegistro no encontrado"),
    MODALIDAD_NOT_FOUND(
            "MODALIDAD_NOT_FOUND",
            "Modalidad no encontrada"),
    PROGRAMA_CON_ESTUDIANTES_CONFLICT(
            "PROGRAMA_CON_ESTUDIANTES_CONFLICT",
            "El programa tiene estudiantes registrados"),
    PROGRAMA_CON_COHORTES_CONFLICT(
            "PROGRAMA_CON_COHORTES_CONFLICT",
            "El programa tiene cohortes creadas"),
    PROGRAMA_CON_DIRECTOR_CONFLICT(
            "PROGRAMA_CON_DIRECTOR_CONFLICT",
            "El programa tiene un director asignado"),
    PROGRAMA_REFERENCIADO_CONFLICT(
            "PROGRAMA_REFERENCIADO_CONFLICT",
            "El programa está referenciado por otros registros");

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