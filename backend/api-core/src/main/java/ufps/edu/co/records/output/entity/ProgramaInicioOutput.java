package ufps.edu.co.records.output.entity;

import java.time.LocalDate;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record ProgramaInicioOutput(
        CohorteResumen cohorteActual,
        long totalInscritos,
        long totalPazysalvo,
        long totalValidados,
        long totalCalificados,
        long totalAdmitidos,
        long totalLegalizados
) implements OutputResponse {

    @Builder
    public record CohorteResumen(
            Integer id,
            String nombre,
            boolean activa,
            LocalDate fechaLimiteDocumentos,
            LocalDate fechaLimitePago,
            LocalDate fechaInicioDocumentacion,
            LocalDate fechaFinDocumentacion,
            LocalDate fechaInicioInscripcion,
            LocalDate fechaFinInscripcion,
            LocalDate fechaInicioPago,
            LocalDate fechaFinPago
    ) {}
}
