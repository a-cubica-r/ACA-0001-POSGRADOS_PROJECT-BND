package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Valores de la tabla {@code estadodocumento}.
 */
@Getter
public enum EstadoDocumento implements ValorCatalogo {
    PENDIENTE("PENDIENTE"),
    RADICADO("RADICADO"),
    APROBADO("APROBADO"),
    RECHAZADO("RECHAZADO"),
    EN_REVISION("EN REVISION"),
    SUBSANADO("SUBSANADO");

    private final String valor;

    EstadoDocumento(String valor) {
        this.valor = valor;
    }
}
