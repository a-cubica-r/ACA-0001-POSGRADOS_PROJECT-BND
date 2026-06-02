package ufps.edu.co.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoreciboDirectorProjectionDTO {
    private Integer id;
    private Integer idPago;
    private Integer idAspirante;
    private String aspirante;
    private LocalDate fechavencimiento;
    private String urlrecibo;
    private String urlfactura;
    private String referenciapago;
    private BigDecimal valorpago;
    private Integer idEstado;
    private String estado;
}
