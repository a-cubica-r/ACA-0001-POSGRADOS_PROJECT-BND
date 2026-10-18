package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum CohorteErrorCode implements BaseErrorCode {
    COHORTE_NOT_FOUND(
            "COHORTE_NOT_FOUND",
            "Cohorte no encontrada"),
    COHORTE_DUPLICADO(
            "COHORTE_DUPLICADO",
            "Cohorte duplicada"),
    COHORTE_SEMESTRE_NO_VALIDO_CONFLICT(
            "COHORTE_SEMESTRE_NO_VALIDO_CONFLICT",
            "La cohorte solo puede crearse en un semestre EN CURSO o PROGRAMADO"),
    COHORTE_SEMESTRE_FECHA_INVALIDA_CONFLICT(
            "COHORTE_SEMESTRE_FECHA_INVALIDA_CONFLICT",
            "La fecha de inicio de la cohorte debe coincidir con la fecha de inicio del semestre o estar dentro de los 2 meses posteriores"),
    COHORTE_PLAZO_FECHAS_INVALIDAS(
            "COHORTE_PLAZO_FECHAS_INVALIDAS",
            "La fecha de inicio de un plazo es posterior a su fecha de fin"),
    COHORTE_CON_ASIGNACIONES_BLOQUEADAS(
            "COHORTE_CON_ASIGNACIONES_BLOQUEADAS",
            "No se pueden eliminar asignaciones porque existen documentos cargados que dependen de ellas");

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
