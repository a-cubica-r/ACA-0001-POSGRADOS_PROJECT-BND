package ufps.edu.co.records.output.entity;

import java.math.BigDecimal;
import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record AspiranteCalificacionOutput(
        Integer id,
        String nombreCompleto,
        Integer idEstado,
        String estado,
        String correo,
        BigDecimal puntajeTotal,
        String numerodocumento
) implements OutputResponse {}
