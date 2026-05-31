package ufps.edu.co.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import ufps.edu.co.DTO.ReciboInscripcionInputDTO;
import ufps.edu.co.processor.receipt.ReciboInscripcionBuildInput;
import ufps.edu.co.processor.receipt.ReciboInscripcionBuilderPort;
import ufps.edu.co.services.S3Service;

@Service
public class ReciboInscripcionBuilderAdapter implements ReciboInscripcionBuilderPort {

    private static final Locale LOCALE_ES_CO = Locale.forLanguageTag("es-CO");
    private static final String VERSION = "V. 1.0";
    private static final String NIT = "Nit. 890500622-6";
    private static final String CONVENIO_BANCOLOMBIA = "Convenio 61003";
    private static final String CONVENIO_BBVA = "Convenio 4668";

    private final ReciboService reciboService;

    public ReciboInscripcionBuilderAdapter(ReciboService reciboService) {
        this.reciboService = reciboService;
    }

    @Override
    public String construirYSubirRecibo(ReciboInscripcionBuildInput input) {
        try {
            ReciboInscripcionInputDTO payload = ReciboInscripcionInputDTO.builder()
                    .fechaGeneracion(formatearFechaLarga(input.fechaGeneracion()))
                    .codigoRecibo(input.codigoRecibo())
                    .periodo(valorConFallback(input.periodo(), String.valueOf(LocalDate.now().getYear())))
                    .version(VERSION)
                    .nit(NIT)
                    .programa(valorConFallback(input.programa(), "POSGRADOS"))
                    .nombre(valorConFallback(input.nombreCompleto(), "ASPIRANTE"))
                    .tipoPersona(valorConFallback(input.tipoPersona(), "Aspirante"))
                    .documento(valorConFallback(input.documento(), "N/A"))
                    .correo(valorConFallback(input.correo(), "N/A"))
                    .tipoPago("Inscripción a Posgrados")
                    .fechaLimite(formatearFechaLarga(input.fechaLimite()))
                    .concepto(valorConFallback(input.concepto(), "Derechos de inscripción posgrado"))
                    .valor(formatearMoneda(input.valor()))
                    .total(formatearMoneda(input.valor()))
                    .convenioBancolombia(CONVENIO_BANCOLOMBIA)
                    .convenioBbva(CONVENIO_BBVA)
                    .barcodeDato(construirBarcode(input))
                    .build();

            S3Service.UploadResult result = reciboService.construirYSubirRecibo(payload);
            return result != null ? result.enlaceurl() : null;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo construir/subir el recibo de inscripción", e);
        }
    }

    private String construirBarcode(ReciboInscripcionBuildInput input) {
        long centavos = Math.max(input.valorCentavos(), 0L);
        String fecha = input.fechaLimite() != null
                ? input.fechaLimite().format(DateTimeFormatter.BASIC_ISO_DATE)
                : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String referencia = valorConFallback(input.referenciaPago(), input.codigoRecibo());
        return "(8020)" + referencia + "(3900)" + centavos + "(96)" + fecha;
    }

    private String formatearFechaLarga(LocalDateTime fecha) {
        if (fecha == null) {
            return formatearFechaLarga(LocalDate.now());
        }
        return formatearFechaLarga(fecha.toLocalDate());
    }

    private String formatearFechaLarga(LocalDate fecha) {
        LocalDate value = fecha != null ? fecha : LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", LOCALE_ES_CO);
        return value.format(formatter);
    }

    private String formatearMoneda(BigDecimal valor) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(LOCALE_ES_CO);
        BigDecimal value = valor != null ? valor : BigDecimal.ZERO;
        return currency.format(value);
    }

    private String valorConFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}