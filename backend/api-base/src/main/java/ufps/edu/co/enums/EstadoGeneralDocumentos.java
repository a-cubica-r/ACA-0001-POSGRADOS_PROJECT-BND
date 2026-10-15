package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estado general de los documentos de un aspirante que se muestra en el resumen.
 */
@Getter
public enum EstadoGeneralDocumentos implements ValorCatalogo {
    VALIDADOS("validados"),
    EN_PROGRESO("en progreso"),
    PENDIENTE("pendiente");

    private final String valor;

    EstadoGeneralDocumentos(String valor) {
        this.valor = valor;
    }
}
