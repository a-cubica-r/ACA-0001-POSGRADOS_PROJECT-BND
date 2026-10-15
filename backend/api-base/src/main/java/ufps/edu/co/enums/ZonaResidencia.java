package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Zonas de residencia que se ofrecen en el formulario de inscripción.
 */
@Getter
public enum ZonaResidencia implements ValorCatalogo {
    URBANA(1, "Urbana"),
    RURAL(2, "Rural");

    private final Integer id;
    private final String valor;

    ZonaResidencia(Integer id, String valor) {
        this.id = id;
        this.valor = valor;
    }
}
