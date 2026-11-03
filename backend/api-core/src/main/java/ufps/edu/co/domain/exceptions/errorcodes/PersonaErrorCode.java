package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum PersonaErrorCode implements BaseErrorCode {
    PERSONA_CORREO_DUPLICADO(
            "PERSONA_CORREO_DUPLICADO",
            "Ya existe una persona registrada con ese correo"),
    PERSONA_DOCUMENTO_INCOMPLETO(
            "PERSONA_DOCUMENTO_INCOMPLETO",
            "Para registrar el documento se necesitan el tipo y el lugar de expedición"),
    PERSONA_DOCUMENTO_INVALIDO(
            "PERSONA_DOCUMENTO_INVALIDO",
            "El número de documento debe ser numérico"),
    PERSONA_UBICACION_INCOMPLETA(
            "PERSONA_UBICACION_INCOMPLETA",
            "La ubicación debe tener dirección y municipio");

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
