package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code administrativo} en la tabla {@code estado}.
 */
@Getter
public enum EstadoAdministrativo implements EstadoCatalogo {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO");

    private final String valor;

    EstadoAdministrativo(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.ADMINISTRATIVO;
    }
}
