package ufps.edu.co.rest.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record PagoCheckoutPreviewDTO(
        String programa,
        String periodo,
        String aspirante,
        String documento,
        String facultad,
        String tipo,
        BigDecimal valor,
        BigDecimal valormatricula,
        BigDecimal valorminimo,
        String urlrecibo,
        String urlfactura,
        String estado) implements Serializable {
}