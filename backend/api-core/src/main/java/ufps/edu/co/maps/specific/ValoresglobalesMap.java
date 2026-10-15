package ufps.edu.co.maps.specific;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import ufps.edu.co.enums.UnidadValorGlobal;

@Component
public class ValoresglobalesMap {

    public Long parsePesos(String valor) {
        if (valor == null) {
            return null;
        }

        String normalized = valor.trim().replace(".", "").replace(",", ".");
        if (normalized.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(normalized).longValueExact();
        } catch (NumberFormatException | ArithmeticException ex) {
            return null;
        }
    }

    public BigDecimal parseSmmlvMultiplier(String valor) {
        if (valor == null) {
            return null;
        }

        String normalized = valor.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return null;
        }

        // "SMLMV" es una variante mal escrita que se sigue aceptando al leer valores ya guardados.
        normalized = normalized.replace(UnidadValorGlobal.SMMLV.getValor(), "").replace("SMLMV", "").trim();
        normalized = normalized.replace(",", ".");

        if (normalized.isEmpty()) {
            return BigDecimal.ONE;
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}