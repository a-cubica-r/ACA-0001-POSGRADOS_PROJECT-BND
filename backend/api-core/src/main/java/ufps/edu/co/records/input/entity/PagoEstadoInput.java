package ufps.edu.co.records.input.entity;

import ufps.edu.co.records.contracts.PatchType;

public enum PagoEstadoInput {
    ;

    public record PAGO_ESTADO_UPDATE(
            Integer idEstado,
            String estado) implements PatchType {
    }
}
