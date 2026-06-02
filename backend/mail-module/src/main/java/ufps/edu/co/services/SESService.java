package ufps.edu.co.services;

import java.nio.charset.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import ufps.edu.co.exception.SesEmailException;

@Service
public class SESService {

        private static final Logger logger = LoggerFactory.getLogger(SESService.class);

        private final SesClient sesClient;
        private final String fromEmail;

        public SESService(
                        @Value("${AWS_ACCESS_KEY_ID}") String accessKeyId,
                        @Value("${AWS_SECRET_KEY_SES}") String secretKey,
                        @Value("${AWS_SES_FROM_EMAIL}") String fromEmail,
                        @Value("${AWS_SES_REGION}") String region) {
                this.fromEmail = fromEmail;
                this.sesClient = SesClient.builder()
                                .region(Region.of(region))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKeyId, secretKey)))
                                .build();
        }

        public void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {
                SendEmailRequest request = SendEmailRequest.builder()
                                .source(fromEmail)
                                .destination(Destination.builder()
                                                .toAddresses(destinatario)
                                                .build())
                                .message(Message.builder()
                                                .subject(Content.builder()
                                                                .data(asunto)
                                                                .charset("UTF-8")
                                                                .build())
                                                .body(Body.builder()
                                                                .html(Content.builder()
                                                                                .data(cuerpoHtml)
                                                                                .charset("UTF-8")
                                                                                .build())
                                                                .build())
                                                .build())
                                .build();

                try {
                        sesClient.sendEmail(request);
                } catch (SesException e) {
                        throw new SesEmailException(
                                        "No se pudo enviar el correo a " + destinatario + ": " + e.awsErrorDetails().errorMessage(), e);
                } catch (Exception e) {
                        throw new SesEmailException(
                                        "Error inesperado al enviar el correo a " + destinatario + ": " + e.getMessage(), e);
                }
        }

        @Async
        public void enviarCorreoAsync(String destinatario, String asunto, String cuerpoHtml) {
                try {
                        enviarCorreo(destinatario, asunto, cuerpoHtml);
                } catch (Exception e) {
                        logger.error("[EMAIL_ASYNC] Fallo al enviar correo a '{}': {}", destinatario, e.getMessage(), e);
                }
        }

        public void sendPdfToDirector(String directorEmail, String directorNombre, String cohorteNombre, byte[] pdfBytes) {
                String boundary = "----=_Part_" + System.currentTimeMillis();
                String filename = "lista_admitidos_" + cohorteNombre.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
                String encodedPdf = Base64.getMimeEncoder().encodeToString(pdfBytes);
                String rawBody = "From: " + fromEmail + "\r\n"
                        + "To: " + directorEmail + "\r\n"
                        + "Subject: Lista de Admitidos - " + cohorteNombre + "\r\n"
                        + "MIME-Version: 1.0\r\n"
                        + "Content-Type: multipart/mixed; boundary=\"" + boundary + "\"\r\n\r\n"
                        + "--" + boundary + "\r\n"
                        + "Content-Type: text/html; charset=UTF-8\r\n\r\n"
                        + "<p>Estimado/a <strong>" + directorNombre + "</strong>,</p>"
                        + "<p>Se adjunta la lista de admitidos para la cohorte <strong>" + cohorteNombre + "</strong>.</p>"
                        + "\r\n"
                        + "--" + boundary + "\r\n"
                        + "Content-Type: application/pdf\r\n"
                        + "Content-Disposition: attachment; filename=\"" + filename + "\"\r\n"
                        + "Content-Transfer-Encoding: base64\r\n\r\n"
                        + encodedPdf + "\r\n"
                        + "--" + boundary + "--";
                RawMessage rawMessage = RawMessage.builder()
                        .data(SdkBytes.fromByteArray(rawBody.getBytes(StandardCharsets.UTF_8)))
                        .build();
                try {
                        sesClient.sendRawEmail(SendRawEmailRequest.builder().rawMessage(rawMessage).build());
                } catch (SesException e) {
                        throw new SesEmailException(
                                "No se pudo enviar el PDF al director " + directorEmail + ": " + e.awsErrorDetails().errorMessage(), e);
                } catch (Exception e) {
                        throw new SesEmailException(
                                "Error inesperado al enviar el PDF al director: " + e.getMessage(), e);
                }
        }
}