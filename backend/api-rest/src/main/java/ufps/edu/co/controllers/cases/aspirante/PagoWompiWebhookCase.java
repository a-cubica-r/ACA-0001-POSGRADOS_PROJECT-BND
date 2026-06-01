package ufps.edu.co.controllers.cases.aspirante;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import ufps.edu.co.processor.crud.PagoProcessor;
import ufps.edu.co.records.output.entity.PagoOutput;
import ufps.edu.co.wompi.model.WompiWebhookRequest;

@RestController
@RequestMapping(value = "/wompi", produces = MediaType.APPLICATION_JSON_VALUE)
public class PagoWompiWebhookCase {

    private final PagoProcessor pagoProcessor;

    public PagoWompiWebhookCase(PagoProcessor pagoProcessor) {
        this.pagoProcessor = pagoProcessor;
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoOutput> recibirWebhook(@RequestBody JsonNode request) {
        return ResponseEntity.ok(pagoProcessor.confirmarWebhook(WompiWebhookRequest.from(request)));
    }

    @PostMapping(value = "/webhook/event", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoOutput> recibirWebhookEvento(@RequestBody JsonNode request) {
        return ResponseEntity.ok(pagoProcessor.confirmarWebhookAutomatico(WompiWebhookRequest.from(request)));
    }

    @PostMapping(value = "/webhook/automatico", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagoOutput> recibirWebhookAutomatico(@RequestBody JsonNode request) {
        return ResponseEntity.ok(pagoProcessor.confirmarWebhookAutomatico(WompiWebhookRequest.from(request)));
    }
}