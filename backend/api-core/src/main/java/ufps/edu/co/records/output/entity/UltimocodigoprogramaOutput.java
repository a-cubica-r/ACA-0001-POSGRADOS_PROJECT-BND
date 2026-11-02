package ufps.edu.co.records.output.entity;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

/**
 * Consecutivo del código de estudiante de un programa.
 *
 * <p>{@code id} es el id de la fila de {@code ultimocodigoprograma}; si el
 * programa todavía no tiene fila, se usa {@code idPrograma} para que la pantalla
 * siempre tenga una clave. {@code codigo} es el <strong>próximo</strong>
 * secuencial a asignar (0 si el programa no tiene consecutivo); 10000 indica
 * que el programa agotó sus códigos.</p>
 */
@Builder
public record UltimocodigoprogramaOutput(
                Integer id,
                Integer idPrograma,
                String nombrePrograma,
                Integer codigo) implements OutputResponse {
}
