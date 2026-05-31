package ufps.edu.co.processor.receipt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReciboInscripcionBuildInput(
        String codigoRecibo,
        String periodo,
        String programa,
        String nombreCompleto,
        String tipoPersona,
        String documento,
        String correo,
        LocalDate fechaLimite,
        String concepto,
        BigDecimal valor,
        long valorCentavos,
        LocalDateTime fechaGeneracion,
        String referenciaPago) {
}