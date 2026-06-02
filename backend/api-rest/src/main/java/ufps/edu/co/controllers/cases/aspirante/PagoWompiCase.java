package ufps.edu.co.controllers.cases.aspirante;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ufps.edu.co.services.S3Service s3Service;

    @Autowired
    private ufps.edu.co.rest.services.PagoreciboinscripcionService pagoreciboinscripcionService;

    @Autowired
    private ufps.edu.co.rest.services.PagorecibomatriculaService pagorecibomatriculaService;

    public PagoWompiCase(PagoProcessor pagoProcessor) {
        this.pagoProcessor = pagoProcessor;
    }

    @PostMapping(value = "/inscripcion/factura", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> subirFacturaInscripcion(@PathVariable Integer idAspirante,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Archivo inválido");
            }
            var recibo = pagoreciboinscripcionService.findCurrentByIdAspirante(idAspirante);
            if (recibo == null) {
                throw new ufps.edu.co.domain.exceptions.DomainException(
                        ufps.edu.co.domain.exceptions.errorcodes.PagoErrorCode.PAGO_NOT_FOUND, idAspirante);
            }
            if (recibo.getUrlfactura() != null && !recibo.getUrlfactura().isBlank()) {
                throw new ufps.edu.co.domain.exceptions.DomainException(
                        ufps.edu.co.domain.exceptions.errorcodes.PagoErrorCode.FACTURA_YA_EXISTE_CONFLICT,
                        recibo.getId());
            }

            var upload = s3Service.uploadFile(file);
            recibo.setUrlfactura(upload.enlaceurl());
            pagoreciboinscripcionService.update(recibo.getId(), recibo);
            return ResponseEntity.ok().build();
        } catch (ufps.edu.co.domain.exceptions.DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error subiendo factura de inscripción: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/matricula/factura", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> subirFacturaMatricula(@PathVariable Integer idAspirante,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Archivo inválido");
            }
            var recibo = pagorecibomatriculaService.findCurrentByIdAspirante(idAspirante);
            if (recibo == null) {
                throw new ufps.edu.co.domain.exceptions.DomainException(
                        ufps.edu.co.domain.exceptions.errorcodes.PagoErrorCode.PAGO_NOT_FOUND, idAspirante);
            }
            if (recibo.getUrlfactura() != null && !recibo.getUrlfactura().isBlank()) {
                throw new ufps.edu.co.domain.exceptions.DomainException(
                        ufps.edu.co.domain.exceptions.errorcodes.PagoErrorCode.FACTURA_YA_EXISTE_CONFLICT,
                        recibo.getId());
            }

            var upload = s3Service.uploadFile(file);
            recibo.setUrlfactura(upload.enlaceurl());
            pagorecibomatriculaService.update(recibo.getId(), recibo);
            return ResponseEntity.ok().build();
        } catch (ufps.edu.co.domain.exceptions.DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error subiendo factura de matrícula: " + e.getMessage(), e);
        }
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
            @RequestParam("montoelegido") Long montoElegidoCentavos) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        BigDecimal montoElegido = montoElegidoCentavos != null ? BigDecimal.valueOf(montoElegidoCentavos).movePointLeft(2)
            : null;
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
            Authentication authentication) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        return ResponseEntity.ok(pagoProcessor.obtenerResumenCheckoutMatricula(idAspirante, authenticatedUserId,
            null, true));
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
            Authentication authentication) {
        Integer authenticatedUserId = extractAuthenticatedUserId(authentication);
        return ResponseEntity.ok(pagoProcessor.prepararReciboMatricula(idAspirante, authenticatedUserId, null,
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