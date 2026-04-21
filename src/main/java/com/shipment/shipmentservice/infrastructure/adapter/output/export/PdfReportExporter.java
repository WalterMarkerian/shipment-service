package com.shipment.shipmentservice.infrastructure.adapter.output.export;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shipment.shipmentservice.application.service.ReportExporter;
import com.shipment.shipmentservice.domain.model.ShipmentStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class PdfReportExporter implements ReportExporter {

    private static final String LOGO_PATH = "/static/images/logo-urbano.png";

    @Override
    public byte[] exportToPdf(Map<ShipmentStatus, Long> data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 2f});

            try {
                URL logoUrl = getClass().getResource(LOGO_PATH);
                if (logoUrl != null) {
                    Image logo = Image.getInstance(logoUrl);
                    logo.scaleToFit(100, 100);
                    PdfPCell logoCell = new PdfPCell(logo);
                    logoCell.setBorder(Rectangle.NO_BORDER);
                    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    headerTable.addCell(logoCell);
                } else {
                    headerTable.addCell(new PdfPCell(new Phrase(" ")));
                }
            } catch (Exception e) {
                headerTable.addCell(new PdfPCell(new Phrase(" ")));
            }

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            PdfPCell titleCell = new PdfPCell(new Paragraph("REPORTE ESTADÍSTICO DE ENVÍOS", fontTitle));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(titleCell);

            document.add(headerTable);
            document.add(new Paragraph(" ")); // Espaciador

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            Font fontHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

            PdfPCell h1 = new PdfPCell(new Phrase("ESTADO", fontHead));
            h1.setPadding(8);
            h1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(h1);

            PdfPCell h2 = new PdfPCell(new Phrase("CANTIDAD", fontHead));
            h2.setPadding(8);
            h2.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(h2);

            data.forEach((status, count) -> {
                table.addCell(new PdfPCell(new Phrase(status.name())));
                PdfPCell cCount = new PdfPCell(new Phrase(count.toString()));
                cCount.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cCount);
            });

            document.add(table);

            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph footer = new Paragraph("\nGenerado el: " + date, FontFactory.getFont(FontFactory.HELVETICA, 9));
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al construir el PDF", e);
        }
        return out.toByteArray();
    }
}