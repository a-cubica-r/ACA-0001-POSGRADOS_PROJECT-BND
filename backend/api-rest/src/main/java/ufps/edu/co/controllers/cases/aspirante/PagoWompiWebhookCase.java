package ufps.edu.co.controllers.cases.aspirante;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ufps.edu.co.processor.crud.PagoProcessor;
import ufps.edu.co.records.output.entity.PagoOutput;
import ufps.edu.co.wompi.model.WompiWebhookRequest;

@RestController
@RequestMapping(value = "/wompi", produces = MediaType.APPLICATION_JSON_VALUE)
public class PagoWompiWebhookCase {

    private final PagoProcessor pagoProcessor;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(PagoWompiWebhookCase.class);

    public PagoWompiWebhookCase(PagoProcessor pagoProcessor, ObjectMapper objectMapper) {
        this.pagoProcessor = pagoProcessor;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoOutput> recibirWebhook(@RequestBody String request) {
        try {
            WompiWebhookRequest wompi = WompiWebhookRequest.from(objectMapper.readTree(request));
            PagoOutput output = pagoProcessor.confirmarWebhook(wompi);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            log.error("Error procesando webhook Wompi: {}", e.getMessage(), e);
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping(value = "/webhook/event", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoOutput> recibirWebhookEvento(@RequestBody String request) {
        try {
            WompiWebhookRequest wompi = WompiWebhookRequest.from(objectMapper.readTree(request));
            PagoOutput output = pagoProcessor.confirmarWebhookAutomatico(wompi);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            log.error("Error procesando webhook Wompi (event): {}", e.getMessage(), e);
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping(value = "/webhook/automatico", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoOutput> recibirWebhookAutomatico(@RequestBody String request) {
        try {
            WompiWebhookRequest wompi = WompiWebhookRequest.from(objectMapper.readTree(request));
            PagoOutput output = pagoProcessor.confirmarWebhookAutomatico(wompi);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            log.error("Error procesando webhook Wompi (automatico): {}", e.getMessage(), e);
            return ResponseEntity.ok().build();
        }
    }
}