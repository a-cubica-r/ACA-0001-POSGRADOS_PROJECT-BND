package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code semestre} en la tabla {@code estado}.
 */
@Getter
public enum EstadoSemestre implements EstadoCatalogo {
    PROGRAMADO("PROGRAMADO"),
    EN_CURSO("EN CURSO"),
    FINALIZADO("FINALIZADO");

    private final String valor;

    EstadoSemestre(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.SEMESTRE;
    }
}
