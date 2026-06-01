package ufps.edu.co.wompi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wompi")
public class WompiProperties {

    private Boolean simulated = true;
    private String apiBaseUrl = "https://sandbox.wompi.co/v1";
    private String checkoutBaseUrl = "https://checkout.wompi.co/p";
    private String widgetScriptUrl = "https://checkout.wompi.co/widget.js";
    private String publicKey;
    private String privateKey;
    private String integritySecret;
    private String currency = "COP";
    private String returnUrl;
    private String webhookUrl;

    public boolean isSimulated() {
        return simulated;
    }

    public void setSimulated(boolean simulated) {
        this.simulated = simulated;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getCheckoutBaseUrl() {
        return checkoutBaseUrl;
    }

    public void setCheckoutBaseUrl(String checkoutBaseUrl) {
        this.checkoutBaseUrl = checkoutBaseUrl;
    }

    public String getWidgetScriptUrl() {
        return widgetScriptUrl;
    }

    public void setWidgetScriptUrl(String widgetScriptUrl) {
        this.widgetScriptUrl = widgetScriptUrl;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getIntegritySecret() {
        return integritySecret;
    }

    public void setIntegritySecret(String integritySecret) {
        this.integritySecret = integritySecret;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}