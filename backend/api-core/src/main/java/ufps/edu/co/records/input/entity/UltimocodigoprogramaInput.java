package ufps.edu.co.records.input.entity;

/**
 * Entradas del consecutivo del código de estudiante.
 *
 * <p>No se usa {@code @NotNull} en {@code codigo}: la validación de rango la
 * hace el servicio, para que un cuerpo nulo responda
 * {@code ULTIMOCODIGOPROGRAMA_CODIGO_INVALIDO} y no un error genérico de
 * validación.</p>
 */
public enum UltimocodigoprogramaInput {
    ;

    public record ULTIMOCODIGOPROGRAMA_UPDATE_CODIGO(Integer codigo) {
    }
}
