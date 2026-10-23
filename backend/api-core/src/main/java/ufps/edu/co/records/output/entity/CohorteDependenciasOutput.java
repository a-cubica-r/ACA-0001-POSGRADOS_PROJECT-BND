package ufps.edu.co.records.output.entity;

import java.util.List;

import ufps.edu.co.records.OutputResponse;

/**
 * Detalle de las dependencias de una cohorte (D-09). Viaja como {@code param}
 * del {@code ErrorResponse} cuando no se puede eliminar, para que el frontend
 * muestre un mensaje por cada conflicto.
 */
public record CohorteDependenciasOutput(
        Integer idCohorte,
        long aspirantes,
        long admitidos,
        long pruebas,
        List<String> conflictos) implements OutputResponse {
}
