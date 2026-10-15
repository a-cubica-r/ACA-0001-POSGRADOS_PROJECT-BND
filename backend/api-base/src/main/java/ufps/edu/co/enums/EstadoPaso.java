package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Estado de cada paso del proceso de admisión que se muestra al aspirante.
 */
@Getter
public enum EstadoPaso implements ValorCatalogo {
    COMPLETADO("completado"),
    EN_PROGRESO("en progreso"),
    PENDIENTE("pendiente");

    private final String valor;

    EstadoPaso(String valor) {
        this.valor = valor;
    }
}
