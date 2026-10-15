package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code pago} en la tabla {@code estado}.
 */
@Getter
public enum EstadoPago implements EstadoCatalogo {
    PENDIENTE("PENDIENTE"),
    REALIZADO("REALIZADO");

    private final String valor;

    EstadoPago(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.PAGO;
    }
}
