package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Valores de la tabla {@code tipoplazo}.
 */
@Getter
public enum TipoPlazo implements ValorCatalogo {
    CORRECCION("CORRECCION"),
    ENTREVISTA("ENTREVISTA"),
    DOCUMENTACION("DOCUMENTACION"),
    INSCRIPCION("INSCRIPCION"),
    MATRICULA("MATRICULA"),
    ADMISION("ADMISION"),
    PAGO("PAGO");

    private final String valor;

    TipoPlazo(String valor) {
        this.valor = valor;
    }
}
