package ufps.edu.co.enums;

import lombok.Getter;

/**
 * Valores de la tabla {@code roles}.
 * <ul>
 * <li>{@link #getValor()}: nombre tal como está guardado en la base de datos.</li>
 * <li>{@link #getCodigo()}: nombre normalizado que viaja en el JWT y que usa
 * Spring Security ({@code RoleNameNormalizer.normalize(valor)}).</li>
 * </ul>
 */
@Getter
public enum Rol implements ValorCatalogo {
    SUPER_ADMINISTRADOR("Super Administrador"),
    POSGRADOS("Posgrados"),
    DECANO_DE_FACULTAD("Decano de Facultad"),
    DIRECTOR_DE_PROGRAMA("Director de Programa"),
    ASPIRANTE("Aspirante");

    private final String valor;

    Rol(String valor) {
        this.valor = valor;
    }

    public String getCodigo() {
        return name();
    }
}
