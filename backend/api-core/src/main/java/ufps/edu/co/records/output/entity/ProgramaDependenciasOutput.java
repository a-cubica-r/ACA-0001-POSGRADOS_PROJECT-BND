package ufps.edu.co.records.output.entity;

import java.util.List;

import ufps.edu.co.records.OutputResponse;

/**
 * Detalle de las dependencias de un programa (D-05). Viaja como {@code param}
 * del {@code ErrorResponse} cuando no se puede eliminar, para que el frontend
 * muestre un mensaje por cada conflicto.
 */
public record ProgramaDependenciasOutput(
        Integer idPrograma,
        long estudiantes,
        long cohortes,
        boolean directorAsignado,
        long administrativos,
        List<String> conflictos) implements OutputResponse {
}
