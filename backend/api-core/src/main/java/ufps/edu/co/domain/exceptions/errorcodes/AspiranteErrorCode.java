package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AspiranteErrorCode implements BaseErrorCode {
    ASPIRANTE_NOT_FOUND(
        "ASPIRANTE_NOT_FOUND",
        "Aspirante no encontrado"),
    DOCUMENTO_REQUISITO_NOT_FOUND(
        "DOCUMENTO_REQUISITO_NOT_FOUND",
        "Documento requisito no encontrado"),
    DOCUMENTACION_FUERA_DE_PLAZO_FORBIDDEN(
            "DOCUMENTACION_FUERA_DE_PLAZO_FORBIDDEN",
        "El plazo de documentación venció. No es posible subir documentos después de la fecha límite"),
    DOCUMENTO_REQUERIDO_YA_EXISTE_CONFLICT(
        "DOCUMENTO_REQUERIDO_YA_EXISTE_CONFLICT",
        "Ya ha subido un archivo para este requisito"),
    TELEFONO_INSCRIPCION_INVALIDO_CONFLICT(
        "TELEFONO_INSCRIPCION_INVALIDO_CONFLICT",
        "El teléfono de contacto debe tener exactamente 10 caracteres"),
    PERSONA_INSCRIPCION_YA_EXISTE_CONFLICT(
        "PERSONA_INSCRIPCION_YA_EXISTE_CONFLICT",
        "La persona ya existe en la base de datos"),
    CORREO_YA_REGISTRADO_CONFLICT(
        "CORREO_YA_REGISTRADO_CONFLICT",
        "El correo electrónico ya está registrado en el sistema. Por favor utilice otro correo"),
    INSCRIPCION_CORREO_NO_ENVIADO_CONFLICT(
        "INSCRIPCION_CORREO_NO_ENVIADO_CONFLICT",
        "No se pudo enviar el correo de confirmación de inscripción"),
    ESTADO_TRANSICION_INVALIDA_CONFLICT(
        "ESTADO_TRANSICION_INVALIDA_CONFLICT",
        "El aspirante no se encuentra en el estado requerido para esta operación");

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