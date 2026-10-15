package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code cohorte} en la tabla {@code estado}.
 */
@Getter
public enum EstadoCohorte implements EstadoCatalogo {
    CERRADA("CERRADA"),
    ABIERTA("ABIERTA");

    private final String valor;

    EstadoCohorte(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.COHORTE;
    }
}
