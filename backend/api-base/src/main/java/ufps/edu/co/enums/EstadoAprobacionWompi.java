package ufps.edu.co.enums;

import java.util.Arrays;
import java.util.Locale;

import lombok.Getter;

/**
 * Estados de transacción que Wompi (o el simulador) reporta y que el sistema
 * considera como pago aprobado.
 */
@Getter
public enum EstadoAprobacionWompi implements ValorCatalogo {
    APPROVED("APPROVED"),
    APPROVAL("APPROVAL"),
    APPROVAL_SUCCESS("APPROVAL_SUCCESS"),
    SUCCESS("SUCCESS"),
    PAID("PAID"),
    PAYED("PAYED");

    private final String valor;

    EstadoAprobacionWompi(String valor) {
        this.valor = valor;
    }

    public static boolean esAprobado(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).anyMatch(estado -> estado.getValor().equals(normalized));
    }
}
