package ufps.edu.co.records.output.entity;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record ListaAdmitidosResumenOutput(
        CohorteResumen cohorteActual,
        List<AspiranteResumen> aspirantes
) implements OutputResponse {

    @Builder
    public record CohorteResumen(
            Integer id,
            String nombre,
            boolean activa,
            int cuposDisponibles,
            long totalAdmitidos
    ) {}

    @Builder
    public record AspiranteResumen(
            Integer id,
            String nombre,
            String numerodocumento,
            String correo,
            BigDecimal puntaje
    ) {}
}
