package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Valores de la tabla {@code tiporegistro}. El {@code id} corresponde a la llave
 * primaria del registro en la base de datos.
 */
@Getter
public enum TipoRegistro implements ValorCatalogo {
    UNICO(1, "UNICO"),
    ESTANDAR(2, "ESTANDAR");

    private final Integer id;
    private final String valor;

    TipoRegistro(Integer id, String valor) {
        this.id = id;
        this.valor = valor;
    }
}
