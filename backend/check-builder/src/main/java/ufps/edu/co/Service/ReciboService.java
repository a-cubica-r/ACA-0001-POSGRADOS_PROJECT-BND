package ufps.edu.co.Service;

import ufps.edu.co.DTO.ReciboDTO;
import ufps.edu.co.DTO.ReciboInscripcionInputDTO;
import ufps.edu.co.services.S3Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReciboService {

    private final TemplateEngine templateEngine;
    private final S3Service s3Service;

    public ReciboService(TemplateEngine templateEngine, S3Service s3Service) {
        this.templateEngine = templateEngine;
        this.s3Service = s3Service;
    }

    public S3Service.UploadResult construirYSubirRecibo(ReciboInscripcionInputDTO input) throws Exception {
        ReciboDTO dto = input.toReciboDTO();
        byte[] pdf = generarPdf(dto);
        String nombreArchivo = "recibo_inscripcion_" + sanitizeFileName(dto.getCodigoRecibo()) + ".pdf";
        return s3Service.uploadBytes(pdf, "application/pdf", nombreArchivo);
    }

    private byte[] generarPdf(ReciboDTO dto) throws Exception {

        // 1. Generar barcode como base64
        String barcodeBase64 = generarBarcodeBase64(dto.getBarcodeDato());

        // 2. Cargar logos como base64
        String logoUfps        = cargarLogoBase64("pictures/UFPS.jpg", "jpeg");
        String logoBbogota     = cargarLogoBase64("pictures/BancoBogota.png", "png");
        String logoBancolombia = cargarLogoBase64("pictures/Bancolombia.png", "png");
        String logoSupergiros  = cargarLogoBase64("pictures/SuperGiros.png", "png");

        // 3. Armar variables para Thymeleaf
        Map<String, Object> vars = new HashMap<>();
        vars.put("fechaGeneracion",     dto.getFechaGeneracion());
        vars.put("codigoRecibo",        dto.getCodigoRecibo());
        vars.put("periodo",             dto.getPeriodo());
        vars.put("version",             dto.getVersion());
        vars.put("nit",                 dto.getNit());
        vars.put("programa",            dto.getPrograma());
        vars.put("nombre",              dto.getNombre());
        vars.put("tipoPersona",         dto.getTipoPersona());
        vars.put("documento",           dto.getDocumento());
        vars.put("correo",              dto.getCorreo());
        vars.put("tipoPago",            dto.getTipoPago());
        vars.put("fechaLimite",         dto.getFechaLimite());
        vars.put("concepto",            dto.getConcepto());
        vars.put("valor",               dto.getValor());
        vars.put("total",               dto.getTotal());
        vars.put("convenioBancolombia", dto.getConvenioBancolombia());
        vars.put("convenioBbva",        dto.getConvenioBbva());
        vars.put("barcodeDato",         dto.getBarcodeDato());
        vars.put("barcodeBase64",       barcodeBase64);
        vars.put("logoUfps",            logoUfps);
        vars.put("logoBbogota",         logoBbogota);
        vars.put("logoBancolombia",     logoBancolombia);
        vars.put("logoSupergiros",      logoSupergiros);

        // 4. Renderizar HTML con Thymeleaf
        Context ctx = new Context();
        ctx.setVariables(vars);
        String html = templateEngine.process("recibo", ctx);

        // 5. Convertir HTML → PDF con Flying Saucer
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);

        return out.toByteArray();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generarBarcodeBase64(String dato) throws Exception {
        Code128Writer writer = new Code128Writer();
        BitMatrix matrix = writer.encode(dato, BarcodeFormat.CODE_128, 600, 120);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", buf);
        return Base64.getEncoder().encodeToString(buf.toByteArray());
    }

    private String cargarLogoBase64(String resourcePath, String format) throws Exception {
        ClassPathResource res = new ClassPathResource(resourcePath);
        byte[] bytes = res.getInputStream().readAllBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "recibo";
        }
        return value.trim().replaceAll("[^a-zA-Z0-9._-]", "-").toLowerCase(Locale.ROOT);
    }
}