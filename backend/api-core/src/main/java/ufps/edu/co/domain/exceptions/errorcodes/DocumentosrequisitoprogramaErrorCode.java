package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum DocumentosrequisitoprogramaErrorCode implements BaseErrorCode {
    DOCUMENTO_REQUISITO_NOT_FOUND(
            "DOCUMENTO_REQUISITO_NOT_FOUND",
            "Documento requerido no encontrado"),
    DOCUMENTO_REQUISITO_EN_USO_CONFLICT(
            "DOCUMENTO_REQUISITO_EN_USO_CONFLICT",
            "El documento no se puede eliminar porque está asignado a una o más cohortes");

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
