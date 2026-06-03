package ufps.edu.co.records.output.entity;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record DocumentosrequisitoconsejoOutput(
                Integer id,
                String nombre,
                Integer tamanomaximo,
                String urlformato) implements OutputResponse {
}
