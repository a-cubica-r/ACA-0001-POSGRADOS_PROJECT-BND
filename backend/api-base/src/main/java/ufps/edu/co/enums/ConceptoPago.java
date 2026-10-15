package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Valores de la tabla {@code pagoconcepto}.
 */
@Getter
public enum ConceptoPago implements ValorCatalogo {
    INSCRIPCION("INSCRIPCION"),
    MATRICULA("MATRICULA"),
    OTRO("OTRO");

    private final String valor;

    ConceptoPago(String valor) {
        this.valor = valor;
    }
}
