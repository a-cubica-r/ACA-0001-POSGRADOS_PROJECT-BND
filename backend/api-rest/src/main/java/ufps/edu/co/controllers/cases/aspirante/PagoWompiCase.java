package ufps.edu.co.controllers.cases.aspirante;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.auth.model.AuthPrincipal;
import ufps.edu.co.processor.crud.PagoProcessor;
import ufps.edu.co.records.output.entity.PagoListadoOutput;
import ufps.edu.co.rest.dto.PagoCheckoutPreviewDTO;
import ufps.edu.co.wompi.model.WompiCheckoutResponse;
import ufps.edu.co.wompi.model.WompiReceiptData;

@RestController
@RequestMapping(value = "/aspirantes/{idAspirante}/pagos", produces = MediaType.APPLICATION_JSON_VALUE)
public class PagoWompiCase {

    private final PagoProcessor pagoProcessor;

    public PagoWompiCase(PagoProcessor pagoProcessor) {
        this.pagoProcessor = pagoProcessor;
    }

    @GetMapping
    public ResponseEntity<List<PagoListadoOutput>> listarPagos(@PathVariable Integer idAspirante) {
        return ResponseEntity.ok(pagoProcessor.findByAspirante(idAspirante));
    }

    @PostMapping(value = "/inscripcion/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WompiCheckoutResponse> iniciarCheckoutInscripcion(@PathVariable Integer idAspirante,
            Authentication authentication) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        return ResponseEntity.ok(pagoProcessor.iniciarCheckoutInscripcion(idAspirante, authenticatedUserId, true));
    }

    @PostMapping(value = "/matricula/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WompiCheckoutResponse> iniciarCheckoutMatricula(@PathVariable Integer idAspirante,
            Authentication authentication,
            @RequestParam("montoelegido") BigDecimal montoElegido) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        return ResponseEntity.ok(pagoProcessor.iniciarCheckoutMatricula(idAspirante, authenticatedUserId, montoElegido,
                true));
    }

    @GetMapping(value = "/inscripcion/resumen", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoCheckoutPreviewDTO> obtenerResumenCheckoutInscripcion(@PathVariable Integer idAspirante,
            Authentication authentication) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        return ResponseEntity.ok(pagoProcessor.obtenerResumenCheckoutInscripcion(idAspirante, authenticatedUserId, true));
    }

    @GetMapping(value = "/matricula/resumen", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoCheckoutPreviewDTO> obtenerResumenCheckoutMatricula(@PathVariable Integer idAspirante,
            Authentication authentication,
            @RequestParam("montoelegido") BigDecimal montoElegido) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        return ResponseEntity.ok(pagoProcessor.obtenerResumenCheckoutMatricula(idAspirante, authenticatedUserId,
                montoElegido, true));
    }

    @GetMapping(value = "/inscripcion/recibo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WompiReceiptData> prepararReciboInscripcion(@PathVariable Integer idAspirante,
            Authentication authentication) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        return ResponseEntity.ok(pagoProcessor.prepararReciboInscripcion(idAspirante, authenticatedUserId,
                true));
    }

        @GetMapping(value = "/matricula/recibo", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<WompiReceiptData> prepararReciboMatricula(@PathVariable Integer idAspirante,
            Authentication authentication,
            @RequestParam("montoelegido") BigDecimal montoElegido) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        return ResponseEntity.ok(pagoProcessor.prepararReciboMatricula(idAspirante, authenticatedUserId, montoElegido,
            true));
        }

    private Integer extractAuthenticatedUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthPrincipal authPrincipal) {
            return authPrincipal.userId();
        }
        return null;
    }
}