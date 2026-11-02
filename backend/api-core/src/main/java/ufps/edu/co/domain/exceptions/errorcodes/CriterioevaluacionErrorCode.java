package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum CriterioevaluacionErrorCode implements BaseErrorCode {
    CRITERIOEVALUACION_NOT_FOUND("CRITERIOEVALUACION_NOT_FOUND", "Criterio de aceptación no encontrado"),
    CRITERIO_COHORTE_MISMATCH("CRITERIO_COHORTE_NOT_FOUND", "El criterio no pertenece a la cohorte indicada"),
    COHORTE_PROGRAMA_MISMATCH("COHORTE_PROGRAMA_NOT_FOUND", "La cohorte no pertenece al programa indicado"),
    PESO_EXCEDE_LIMITE("PESO_EXCEDE_LIMITE", "La suma de los pesos de los criterios de aceptación no puede exceder 100"),
    CRITERIO_CON_ASPIRANTES_CALIFICADOS("CRITERIO_CON_ASPIRANTES_CALIFICADOS", "El criterio no se puede borrar porque hay aspirantes calificados"),
    CRITERIO_CON_CALIFICACIONES_BLOQUEADO("CRITERIO_CON_CALIFICACIONES_BLOQUEADO", "El criterio no se puede editar ni borrar porque ya tiene calificaciones registradas"),
    CRITERIO_DUPLICADO("CRITERIO_DUPLICADO", "Ya existe un criterio con ese nombre en el programa"),
    CRITERIO_EN_USO_CONFLICT("CRITERIO_EN_USO_CONFLICT", "El criterio no se puede eliminar porque está asignado a una o más cohortes");

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
