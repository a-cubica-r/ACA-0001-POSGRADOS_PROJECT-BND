package ufps.edu.co.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

class SESServiceTest {

    private SesClient sesClient;
    private SESService service;

    @BeforeEach
    void setUp() {
        sesClient = mock(SesClient.class);
        service = new SESService("access", "secret", "from@example.com", "us-east-1");
        ReflectionTestUtils.setField(service, "sesClient", sesClient);
    }

    @Test
    void enviarCorreoNormalizaElDestinatario() {
        service.enviarCorreo("  ADOLFO@OUTLOOK.COM ", "Asunto", "<p>Cuerpo</p>");

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());
        assertEquals("adolfo@outlook.com", captor.getValue().destination().toAddresses().get(0));
    }

    @Test
    void enviarCorreoAsyncNormalizaElDestinatario() {
        service.enviarCorreoAsync("  ADOLFO@OUTLOOK.COM ", "Asunto", "<p>Cuerpo</p>");

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());
        assertEquals("adolfo@outlook.com", captor.getValue().destination().toAddresses().get(0));
    }
}
