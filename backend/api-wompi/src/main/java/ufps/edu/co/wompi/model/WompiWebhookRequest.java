package ufps.edu.co.wompi.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;

@Builder
public record WompiWebhookRequest(
        String event,
        Integer paymentId,
        Integer aspiranteId,
        Integer pagoconceptoId,
        String concepto,
        String reference,
        BigDecimal amount,
        Long amountInCents,
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
        String status,
        Map<String, String> metadata) {

        public static WompiWebhookRequest from(JsonNode payload) {
                JsonNode transaction = payload.path("data").path("transaction");
                if (transaction.isMissingNode() || transaction.isNull()) {
                        transaction = payload.path("transaction");
                }
                if (transaction.isMissingNode() || transaction.isNull()) {
                        transaction = payload.path("data");
                }
                if (transaction.isMissingNode() || transaction.isNull()) {
                        transaction = payload;
                }

                JsonNode metadataNode = transaction.path("metadata");
                if (metadataNode.isMissingNode() || metadataNode.isNull()) {
                        metadataNode = payload.path("data").path("metadata");
                }
                if (metadataNode.isMissingNode() || metadataNode.isNull()) {
                        metadataNode = payload.path("metadata");
                }

                return WompiWebhookRequest.builder()
                                .event(textValue(payload, "event"))
                                .paymentId(resolvePaymentId(metadataNode, payload, transaction))
                                .aspiranteId(intValue(metadataNode, "aspiranteId"))
                                .pagoconceptoId(intValue(metadataNode, "pagoconceptoId"))
                                .concepto(textValue(metadataNode, "concepto"))
                                .reference(firstText(transaction, "reference", payload, "reference"))
                                .amount(firstDecimal(transaction, "amount", payload, "amount"))
                                .amountInCents(firstLong(transaction, "amount_in_cents", payload, "amount_in_cents"))
                                .currency(firstText(transaction, "currency", payload, "currency"))
                                .transactionId(firstText(transaction, "id", payload, "id"))
                                .customerEmail(textValue(transaction.path("customer_data"), "email"))
                                .customerName(textValue(transaction.path("customer_data"), "full_name"))
                                .customerData(WompiCustomerData.builder()
                                                .email(textValue(transaction.path("customer_data"), "email"))
                                                .fullName(textValue(transaction.path("customer_data"), "full_name"))
                                                .phoneNumber(textValue(transaction.path("customer_data"), "phone_number"))
                                                .phoneNumberPrefix(
                                                                textValue(transaction.path("customer_data"), "phone_number_prefix"))
                                                .legalId(textValue(transaction.path("customer_data"), "legal_id"))
                                                .legalIdType(textValue(transaction.path("customer_data"), "legal_id_type"))
                                                .build())
                                .receiptData(WompiReceiptData.builder()
                                                .paymentId(intValue(metadataNode, "paymentId"))
                                                .aspiranteId(intValue(metadataNode, "aspiranteId"))
                                                .pagoconceptoId(intValue(metadataNode, "pagoconceptoId"))
                                                .concepto(textValue(metadataNode, "concepto"))
                                                .reference(firstText(transaction, "reference", payload, "reference"))
                                                .amount(firstDecimal(transaction, "amount", payload, "amount"))
                                                .amountInCents(firstLong(transaction, "amount_in_cents", payload, "amount_in_cents"))
                                                .currency(firstText(transaction, "currency", payload, "currency"))
                                                .fullName(textValue(transaction.path("customer_data"), "full_name"))
                                                .email(textValue(transaction.path("customer_data"), "email"))
                                                .phoneNumber(textValue(transaction.path("customer_data"), "phone_number"))
                                                .legalId(textValue(transaction.path("customer_data"), "legal_id"))
                                                .legalIdType(textValue(transaction.path("customer_data"), "legal_id_type"))
                                                .build())
                                .status(firstText(transaction, "status", payload, "status"))
                                .metadata(extractMetadata(metadataNode))
                                .build();
        }

        private static String textValue(JsonNode node, String fieldName) {
                return node != null && node.hasNonNull(fieldName) ? node.get(fieldName).asText() : null;
        }

        private static Integer intValue(JsonNode node, String fieldName) {
                return node != null && node.hasNonNull(fieldName) ? node.get(fieldName).asInt() : null;
        }

        private static Integer resolvePaymentId(JsonNode metadataNode, JsonNode payload, JsonNode transaction) {
                Integer paymentId = intValue(metadataNode, "paymentId");
                if (paymentId != null) {
                        return paymentId;
                }

                String reference = firstText(transaction, "reference", payload, "reference");
                if (reference == null || !reference.startsWith("PAGO-")) {
                        return null;
                }

                String[] parts = reference.split("-");
                if (parts.length < 2) {
                        return null;
                }

                try {
                        return Integer.valueOf(parts[1]);
                } catch (NumberFormatException ex) {
                        return null;
                }
        }

        private static Long firstLong(JsonNode firstNode, String firstField, JsonNode secondNode, String secondField) {
                if (firstNode != null && firstNode.hasNonNull(firstField)) {
                        return firstNode.get(firstField).asLong();
                }
                if (secondNode != null && secondNode.hasNonNull(secondField)) {
                        return secondNode.get(secondField).asLong();
                }
                return null;
        }

        private static String firstText(JsonNode firstNode, String firstField, JsonNode secondNode, String secondField) {
                String value = textValue(firstNode, firstField);
                return value != null ? value : textValue(secondNode, secondField);
        }

        private static BigDecimal firstDecimal(JsonNode firstNode, String firstField, JsonNode secondNode, String secondField) {
                Long cents = firstLong(firstNode, firstField + "_in_cents", secondNode, secondField + "_in_cents");
                if (cents != null) {
                        return BigDecimal.valueOf(cents).movePointLeft(2);
                }
                String value = firstText(firstNode, firstField, secondNode, secondField);
                if (value == null) {
                        return null;
                }
                try {
                        return new BigDecimal(value);
                } catch (NumberFormatException ex) {
                        return null;
                }
        }

        @SuppressWarnings("deprecation")
        private static Map<String, String> extractMetadata(JsonNode metadataNode) {
                if (metadataNode == null || metadataNode.isMissingNode() || metadataNode.isNull()) {
                        return Map.of();
                }

                Map<String, String> metadata = new LinkedHashMap<>();
                metadataNode.fields().forEachRemaining(entry -> metadata.put(entry.getKey(), entry.getValue().isNull() ? null : entry.getValue().asText()));
                return metadata;
        }
}