package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Unidades con las que se guardan los valores globales.
 */
@Getter
public enum UnidadValorGlobal implements ValorCatalogo {
    MB("MB"),
    SMMLV("SMMLV");

    private final String valor;

    UnidadValorGlobal(String valor) {
        this.valor = valor;
    }
}
