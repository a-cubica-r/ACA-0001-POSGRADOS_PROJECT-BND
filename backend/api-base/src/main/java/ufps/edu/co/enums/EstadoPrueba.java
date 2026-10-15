package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code prueba} en la tabla {@code estado}.
 */
@Getter
public enum EstadoPrueba implements EstadoCatalogo {
    PENDIENTE_DE_CONFIRMACION("PENDIENTE DE CONFIRMACION"),
    CONFIRMADA("CONFIRMADA"),
    SOLICITUD_DE_CAMBIO("SOLICITUD DE CAMBIO"),
    COMPLETADA("COMPLETADA"),
    CANCELADA("CANCELADA");

    private final String valor;

    EstadoPrueba(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.PRUEBA;
    }
}
