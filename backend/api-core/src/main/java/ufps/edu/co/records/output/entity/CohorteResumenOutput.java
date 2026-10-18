package ufps.edu.co.records.output.entity;

import java.time.LocalDate;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record CohorteResumenOutput(
        Integer id,
        String nombre,
        boolean activa,
        String semestre,
        int cupos,
        LocalDate fechaLimitePago,
        LocalDate fechaLimiteDocs,
        LocalDate fechaLimiteInscripcion,
        LocalDate fechaInicioDocumentacion,
        LocalDate fechaFinDocumentacion,
        LocalDate fechaInicioInscripcion,
        LocalDate fechaFinInscripcion,
        LocalDate fechaInicioPago,
        LocalDate fechaFinPago,
        long totalInscritos,
        long totalPazysalvo,
        long totalValidados,
        long totalCalificados,
        long totalAdmitidos
) implements OutputResponse {}
