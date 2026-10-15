package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Valores de la tabla {@code modalidad}.
 */
@Getter
public enum Modalidad implements ValorCatalogo {
    PRESENCIAL("PRESENCIAL"),
    VIRTUAL("VIRTUAL"),
    HIBRIDA("HIBRIDA");

    private final String valor;

    Modalidad(String valor) {
        this.valor = valor;
    }
}
