package ufps.edu.co.services;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import ufps.edu.co.records.output.entity.AspiranteOutput;
import ufps.edu.co.wompi.model.WompiReceiptData;

@Service
public class PdfGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(PdfGeneratorService.class);

    // Pantone 18-1664TCX  R:210 G:17 B:22
    private static final Color COLOR_ROJO  = new Color(210, 17, 22);
    // Pantone 6 C          R:0   G:0  B:0
    private static final Color COLOR_NEGRO = new Color(0, 0, 0);

    public byte[] generarListaAdmitidos(
            String cohorteNombre,
            LocalDateTime fechaGeneracion,
            List<AspiranteOutput> aspirantesAdmitidos,
            String directorNombre) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50f, 50f, 60f, 60f);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- Encabezado imagen ---
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("EncabezadoPDF.png")) {
                if (is != null) {
                    Image headerImg = Image.getInstance(is.readAllBytes());
                    headerImg.setAlignment(Element.ALIGN_CENTER);
                    headerImg.scaleToFit(PageSize.A4.getWidth() - 100f, 120f);
                    headerImg.setSpacingAfter(16f);
                    document.add(headerImg);
                } else {
                    logger.warn("EncabezadoPDF.png no encontrado en el classpath");
                }
            }

            // Helvetica es la fuente base PDF más cercana a Helvetica Neue.
            // Para Open Sans se necesitaría embeber el TTF en el classpath.
            Font titleFont     = new Font(Font.HELVETICA, 16, Font.BOLD,   COLOR_ROJO);
            Font subtitleFont  = new Font(Font.HELVETICA, 10, Font.NORMAL, COLOR_NEGRO);
            Font colHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD,   Color.WHITE);
            Font boldFont      = new Font(Font.HELVETICA, 10, Font.BOLD,   COLOR_NEGRO);
            Font normalFont    = new Font(Font.HELVETICA, 10, Font.NORMAL, COLOR_NEGRO);
            Font footerFont    = new Font(Font.HELVETICA,  9, Font.NORMAL, COLOR_NEGRO);
            Font footerBold    = new Font(Font.HELVETICA, 11, Font.BOLD,   COLOR_NEGRO);

            // --- Título ---
            Paragraph title = new Paragraph("Lista de Aspirantes Admitidos", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph cohorte = new Paragraph(cohorteNombre, new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_NEGRO));
            cohorte.setAlignment(Element.ALIGN_CENTER);
            cohorte.setSpacingAfter(4f);
            document.add(cohorte);

            Paragraph fechaPar = new Paragraph(
                    "Fecha de generación: " + fechaGeneracion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    subtitleFont);
            fechaPar.setAlignment(Element.ALIGN_CENTER);
            fechaPar.setSpacingAfter(20f);
            document.add(fechaPar);

            // --- Tabla ---
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 3f, 3f, 1.5f});
            table.setSpacingBefore(5f);
            table.setSpacingAfter(30f);

            for (String col : new String[]{"#", "Nombre Completo", "Correo / Celular", "Puntaje"}) {
                PdfPCell hCell = new PdfPCell(new Phrase(col, colHeaderFont));
                hCell.setBackgroundColor(COLOR_ROJO);
                hCell.setBorderColor(COLOR_NEGRO);
                hCell.setPadding(7f);
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(hCell);
            }

            for (int i = 0; i < aspirantesAdmitidos.size(); i++) {
                AspiranteOutput a = aspirantesAdmitidos.get(i);

                String nombre   = "N/A";
                String contacto = "N/A";
                String puntaje  = a.puntuacion() != null ? a.puntuacion().toPlainString() : "N/A";

                if (a.persona() != null) {
                    String n  = a.persona().nombres()   != null ? a.persona().nombres()   : "";
                    String ap = a.persona().apellidos() != null ? a.persona().apellidos() : "";
                    nombre = (n + " " + ap).trim();

                    String correo  = a.persona().correo()  != null ? a.persona().correo()  : "";
                    String celular = a.persona().celular() != null ? a.persona().celular() : "";
                    contacto = correo + (celular.isBlank() ? "" : "\n" + celular);
                }

                PdfPCell numCell = new PdfPCell(new Phrase(String.valueOf(i + 1), normalFont));
                numCell.setBackgroundColor(Color.WHITE);
                numCell.setBorderColor(COLOR_NEGRO);
                numCell.setPadding(5f);
                numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(numCell);

                PdfPCell nameCell = new PdfPCell(new Phrase(nombre, boldFont));
                nameCell.setBackgroundColor(Color.WHITE);
                nameCell.setBorderColor(COLOR_NEGRO);
                nameCell.setPadding(5f);
                table.addCell(nameCell);

                PdfPCell contactCell = new PdfPCell(new Phrase(contacto, normalFont));
                contactCell.setBackgroundColor(Color.WHITE);
                contactCell.setBorderColor(COLOR_NEGRO);
                contactCell.setPadding(5f);
                table.addCell(contactCell);

                PdfPCell scoreCell = new PdfPCell(new Phrase(puntaje, normalFont));
                scoreCell.setBackgroundColor(Color.WHITE);
                scoreCell.setBorderColor(COLOR_NEGRO);
                scoreCell.setPadding(5f);
                scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(scoreCell);
            }

            document.add(table);

            // --- Línea separadora ---
            PdfPTable linea = new PdfPTable(1);
            linea.setWidthPercentage(100);
            linea.setSpacingAfter(8f);
            PdfPCell lineaCell = new PdfPCell(new Phrase(" "));
            lineaCell.setBorderColorBottom(COLOR_ROJO);
            lineaCell.setBorderWidthBottom(1.5f);
            lineaCell.setBorderWidthTop(0);
            lineaCell.setBorderWidthLeft(0);
            lineaCell.setBorderWidthRight(0);
            linea.addCell(lineaCell);
            document.add(linea);

            // --- Pie de página ---
            Paragraph footerLabel = new Paragraph("Generado y firmado por", footerFont);
            footerLabel.setAlignment(Element.ALIGN_CENTER);
            document.add(footerLabel);

            Paragraph footerName = new Paragraph(directorNombre, footerBold);
            footerName.setAlignment(Element.ALIGN_CENTER);
            document.add(footerName);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Error generando PDF de lista de admitidos para cohorte '{}'", cohorteNombre, e);
            throw new RuntimeException("Error generando PDF de admitidos: " + e.getMessage(), e);
        }
    }

    public byte[] generarReciboInscripcion(WompiReceiptData receiptData) {
        if (receiptData == null) {
            return new byte[0];
        }
        return new byte[0];
    }
}
