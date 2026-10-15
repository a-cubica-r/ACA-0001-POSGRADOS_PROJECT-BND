package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estados de la entidad {@code pagoinscripcion} (recibos de inscripción) en la tabla {@code estado}.
 */
@Getter
public enum EstadoPagoInscripcion implements EstadoCatalogo {
    RECHAZADO("RECHAZADO"),
    EN_CURSO("EN CURSO"),
    VENCIDO("VENCIDO"),
    COMPLETADO("COMPLETADO");

    private final String valor;

    EstadoPagoInscripcion(String valor) {
        this.valor = valor;
    }

    @Override
    public EntidadEstado getEntidad() {
        return EntidadEstado.PAGO_INSCRIPCION;
    }
}
