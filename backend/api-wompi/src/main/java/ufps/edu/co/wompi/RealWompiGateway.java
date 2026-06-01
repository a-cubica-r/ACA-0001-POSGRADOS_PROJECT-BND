package ufps.edu.co.wompi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ufps.edu.co.wompi.config.WompiProperties;
import ufps.edu.co.wompi.model.WompiCheckoutRequest;
import ufps.edu.co.wompi.model.WompiCheckoutResponse;
import ufps.edu.co.wompi.model.WompiCustomerData;
import ufps.edu.co.wompi.model.WompiWebhookRequest;

@Component
@ConditionalOnProperty(prefix = "wompi", name = "simulated", havingValue = "false")
public class RealWompiGateway implements WompiGateway {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WompiProperties properties;

    public RealWompiGateway(WompiProperties properties) {
        this.properties = properties;
    }

    @Override
    public WompiCheckoutResponse createCheckout(WompiCheckoutRequest request) {
        return WompiCheckoutResponse.builder()
                .paymentId(request.paymentId())
                .aspiranteId(request.aspiranteId())
                .pagoconceptoId(request.pagoconceptoId())
                .concepto(request.concepto())
                .reference(request.reference())
                .amount(request.amount())
                .amountInCents(request.amountInCents())
                .currency(request.currency())
                .publicKey(request.publicKey())
                .signatureIntegrity(request.signatureIntegrity())
                .redirectUrl(request.redirectUrl())
                .widgetScriptUrl(request.widgetScriptUrl())
                .checkoutUrl(properties.getCheckoutBaseUrl() + "/" + request.reference())
                .simulated(false)
                .message("Checkout listo para renderizar en el widget de Wompi")
                .transactionId(null)
                .customerEmail(request.customerEmail())
                .customerName(request.customerName())
                .customerData(request.customerData())
                .receiptData(request.receiptData())
                .pagoreciboinscripcion(request.pagoreciboinscripcion())
                .pagorecibomatricula(request.pagorecibomatricula())
                .creationDate(request.creationDate())
                .build();
    }

    @Override
    public WompiCheckoutResponse confirmPayment(WompiWebhookRequest request) {
        JsonNode transaction = fetchTransaction(request.transactionId());
        String transactionId = firstText(transaction, "id", request.transactionId());
        String status = firstText(transaction, "status", request.status());
        String reference = firstText(transaction, "reference", request.reference());
        long amountInCents = firstLong(transaction, "amount_in_cents", request.amountInCents() != null ? request.amountInCents() : 0L);
        String currency = firstText(transaction, "currency", request.currency() != null ? request.currency() : properties.getCurrency());
        JsonNode customerData = transaction != null ? transaction.path("customer_data") : null;

        return WompiCheckoutResponse.builder()
                .paymentId(request.paymentId())
                .aspiranteId(request.aspiranteId())
                .pagoconceptoId(request.pagoconceptoId())
                .concepto(request.concepto())
                .reference(reference)
                .amount(request.amount() != null ? request.amount() : java.math.BigDecimal.valueOf(amountInCents).movePointLeft(2))
                .amountInCents(amountInCents)
                .currency(currency)
                .publicKey(properties.getPublicKey())
                .signatureIntegrity(request.signatureIntegrity())
                .redirectUrl(request.redirectUrl())
                .widgetScriptUrl(request.widgetScriptUrl())
                .checkoutUrl(properties.getCheckoutBaseUrl() + "/" + reference)
                .simulated(false)
                .message(transaction != null && !transaction.isMissingNode()
                        ? "Transacción consultada en Wompi"
                        : "Webhook recibido sin consulta adicional a Wompi")
                .transactionId(transactionId)
                .customerEmail(firstText(customerData, "email", request.customerEmail()))
                .customerName(firstText(customerData, "full_name", request.customerName()))
                .customerData(WompiCustomerData.builder()
                        .email(firstText(customerData, "email", request.customerEmail()))
                        .fullName(firstText(customerData, "full_name", request.customerName()))
                        .phoneNumber(firstText(customerData, "phone_number", null))
                        .phoneNumberPrefix(firstText(customerData, "phone_number_prefix", null))
                        .legalId(firstText(customerData, "legal_id", null))
                        .legalIdType(firstText(customerData, "legal_id_type", null))
                        .build())
                .receiptData(request.receiptData())
                .pagoreciboinscripcion(null)
                .pagorecibomatricula(null)
                .creationDate(null)
                .status(status)
                .build();
    }

    @Override
    public boolean isSimulated() {
        return false;
    }

    private JsonNode fetchTransaction(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            return null;
        }

        String baseUrl = normalizeUrl(properties.getApiBaseUrl());
        String url = baseUrl + "/transactions/" + transactionId;
        String privateKey = properties.getPrivateKey();
        if (privateKey == null || privateKey.isBlank()) {
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", privateKey.startsWith("Bearer ") ? privateKey : "Bearer " + privateKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }

            return objectMapper.readTree(response.body()).path("data");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    private String firstText(JsonNode node, String fieldName, String fallback) {
        if (node != null && node.hasNonNull(fieldName)) {
            return node.get(fieldName).asText();
        }
        return fallback;
    }

    private long firstLong(JsonNode node, String fieldName, long fallback) {
        if (node != null && node.hasNonNull(fieldName)) {
            return node.get(fieldName).asLong();
        }
        return fallback;
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "https://sandbox.wompi.co/v1";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}