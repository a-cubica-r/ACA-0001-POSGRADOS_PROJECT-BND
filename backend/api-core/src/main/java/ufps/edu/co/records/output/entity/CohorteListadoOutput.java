package ufps.edu.co.records.output.entity;

import java.time.LocalDate;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record CohorteListadoOutput(
        Integer id,
        String nombre,
        boolean activa,
        long inscritos,
        long admitidos,
        int cupos,
        LocalDate fechaLimiteDocumentos,
        LocalDate fechaLimitePago,
        // Inicio del SEMESTRE, no de un plazo. Se conserva por compatibilidad.
        LocalDate fechaInicio,
        LocalDate fechaInicioDocumentacion,
        LocalDate fechaFinDocumentacion,
        LocalDate fechaInicioInscripcion,
        LocalDate fechaFinInscripcion,
        LocalDate fechaInicioPago,
        LocalDate fechaFinPago
) implements OutputResponse {}
