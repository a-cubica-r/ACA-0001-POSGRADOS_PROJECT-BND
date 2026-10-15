package ufps.edu.co.enums;

import java.util.List;

import lombok.Getter;

/**
 * Prefijos de las claves de la tabla {@code valoresglobales}
 * (la clave completa es {@code PREFIJO_AÑO_VERSION}).
 */
@Getter
public enum PrefijoValorGlobal implements ValorCatalogo {
    TAMANO_MAXIMO_ARCHIVOS("TAMANO_MAXIMO_ARCHIVOS"),
    /** Prefijo antiguo del tamaño máximo de archivos; se sigue aceptando como alias. */
    TAMANO_MAXIMO("TAMANO_MAXIMO"),
    SALARIO_MINIMO("SALARIO_MINIMO"),
    VALOR_INSCRIPCION("VALOR_INSCRIPCION");

    private final String valor;

    PrefijoValorGlobal(String valor) {
        this.valor = valor;
    }

    /** Prefijos que se consideran equivalentes al tamaño máximo de archivos. */
    public static List<String> aliasesTamanoMaximo() {
        return List.of(TAMANO_MAXIMO.getValor(), TAMANO_MAXIMO_ARCHIVOS.getValor());
    }
}
