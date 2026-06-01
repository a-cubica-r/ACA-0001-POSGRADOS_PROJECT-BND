package ufps.edu.co.wompi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

import ufps.edu.co.rest.dto.PagoreciboinscripcionDTO;
import ufps.edu.co.rest.dto.PagorecibomatriculaDTO;

@Builder
public record WompiCheckoutResponse(
                Integer paymentId,
                Integer aspiranteId,
                Integer pagoconceptoId,
                String concepto,
                String reference,
                BigDecimal amount,
                long amountInCents,
                String currency,
                String publicKey,
                String signatureIntegrity,
                String redirectUrl,
                String widgetScriptUrl,
                String checkoutUrl,
                boolean simulated,
                String message,
                String transactionId,
                String customerEmail,
                String customerName,
                WompiCustomerData customerData,
                WompiReceiptData receiptData,
                PagoreciboinscripcionDTO pagoreciboinscripcion,
                PagorecibomatriculaDTO pagorecibomatricula,
                LocalDateTime creationDate,
                String status) {
}