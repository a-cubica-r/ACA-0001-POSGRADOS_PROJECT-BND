package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Quién cancela una entrevista o una prueba (define a quién se notifica).
 */
@Getter
public enum ActorCancelacion implements ValorCatalogo {
    ASPIRANTE("ASPIRANTE"),
    DIRECTOR("DIRECTOR");

    private final String valor;

    ActorCancelacion(String valor) {
        this.valor = valor;
    }
}
