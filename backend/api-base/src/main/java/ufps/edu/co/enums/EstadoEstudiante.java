package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code estudiante}. La tabla {@code estado} todavía no tiene registros para esta entidad, por lo que la búsqueda devuelve {@code null}.
 */
@Getter
public enum EstadoEstudiante implements EstadoCatalogo {
    ACTIVO("ACTIVO");

    private final String valor;

    EstadoEstudiante(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.ESTUDIANTE;
    }
}
