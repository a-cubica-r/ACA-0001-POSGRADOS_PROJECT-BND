package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Valores de la columna {@code estado.entidad}.
 */
@Getter
public enum EntidadEstado implements ValorCatalogo {
    ADMINISTRATIVO("administrativo"),
    ASPIRANTE("aspirante"),
    COHORTE("cohorte"),
    DOCUMENTO("documento"),
    ENTREVISTA("entrevista"),
    ESTUDIANTE("estudiante"),
    GLOBAL("global"),
    PAGO("pago"),
    PAGO_INSCRIPCION("pagoinscripcion"),
    PAGO_MATRICULA("pagomatricula"),
    PRUEBA("prueba"),
    SEMESTRE("semestre");

    private final String valor;

    EntidadEstado(String valor) {
        this.valor = valor;
    }
}
