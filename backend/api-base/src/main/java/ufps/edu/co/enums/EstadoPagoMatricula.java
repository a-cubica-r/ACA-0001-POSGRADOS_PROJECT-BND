package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code pagomatricula} (recibos de matrícula) en la tabla {@code estado}.
 */
@Getter
public enum EstadoPagoMatricula implements EstadoCatalogo {
    RECHAZADO("RECHAZADO"),
    EN_CURSO("EN CURSO"),
    VENCIDO("VENCIDO"),
    COMPLETADO("COMPLETADO");

    private final String valor;

    EstadoPagoMatricula(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.PAGO_MATRICULA;
    }
}
