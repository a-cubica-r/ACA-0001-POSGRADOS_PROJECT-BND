package ufps.edu.co.records.output.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record PagoreciboDirectorOutput(
        Integer id,
        Integer idPago,
        Integer idAspirante,
        String aspirante,
        LocalDate fechavencimiento,
        String urlrecibo,
        String urlfactura,
        String referenciapago,
        BigDecimal valorpago,
        Integer idEstado,
        String estado
) implements OutputResponse {}
