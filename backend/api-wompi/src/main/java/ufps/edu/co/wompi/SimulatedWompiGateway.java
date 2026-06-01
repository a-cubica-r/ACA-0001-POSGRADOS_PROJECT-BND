package ufps.edu.co.wompi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import ufps.edu.co.wompi.config.WompiProperties;
import ufps.edu.co.wompi.model.WompiCheckoutRequest;
import ufps.edu.co.wompi.model.WompiCheckoutResponse;
import ufps.edu.co.wompi.model.WompiWebhookRequest;

@Component
@ConditionalOnProperty(prefix = "wompi", name = "simulated", havingValue = "true", matchIfMissing = true)
public class SimulatedWompiGateway implements WompiGateway {

    private final WompiProperties properties;

    public SimulatedWompiGateway(WompiProperties properties) {
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
                .simulated(true)
                .message("Pago simulado listo para confirmar por webhook")
                .transactionId("SIM-" + request.reference())
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
        String status = request.status() != null ? request.status().toUpperCase() : "APPROVED";
        return WompiCheckoutResponse.builder()
                .paymentId(request.paymentId())
                .reference(request.reference())
                .amount(request.amount())
                .amountInCents(request.amountInCents() != null ? request.amountInCents() : 0L)
                .currency(request.currency() != null ? request.currency() : properties.getCurrency())
                .publicKey(request.publicKey())
                .signatureIntegrity(request.signatureIntegrity())
                .redirectUrl(request.redirectUrl())
                .widgetScriptUrl(request.widgetScriptUrl())
                .checkoutUrl(properties.getCheckoutBaseUrl() + "/" + request.reference())
                .simulated(true)
                .message("Webhook simulado procesado")
                .transactionId(request.transactionId() != null ? request.transactionId() : "SIM-" + request.reference())
                .customerEmail(request.customerEmail())
                .customerName(request.customerName())
                .customerData(request.customerData())
                .receiptData(request.receiptData())
                .pagoreciboinscripcion(null)
                .pagorecibomatricula(null)
                .creationDate(null)
                .status(status)
                .build();
    }

    @Override
    public boolean isSimulated() {
        return true;
    }
}