package ufps.edu.co.records.output.entity;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record DocumentopersonaOutput(
        Integer id,
        String numerodocumento,
        Integer idTipodocumento,
        Integer idLugarexpedicion) implements OutputResponse {
}
