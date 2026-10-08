package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.FileResponse;
import com.example.CRUD.DTO.OrderResponse;
import com.example.CRUD.config.PdfProperties;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.service.FileVaultService;
import com.example.CRUD.service.OrderPdfService;
import com.example.CRUD.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OrderPdfServiceImpl implements OrderPdfService {

    private final OrderService orderService;
    private final PdfProperties pdfProperties;
    private final FileVaultService fileVaultService;

    @Override
    public byte[] getStoredPdf(Long orderId) {
        orderService.getById(orderId);   // 404 "Order not found" for a bad id
        return fileVaultService.readLatestByOrder(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PDF for order " + orderId + " has not been created yet"));
    }

    @Override
    public FileResponse generateAndStore(Long orderId) {
        OrderResponse order = orderService.getById(orderId);   // 404 if the order doesn't exist
        byte[] pdf = buildPdf(order);                          // build first, so a failure keeps the old copy
        fileVaultService.deleteByOrder(orderId);
        return fileVaultService.store(pdf, "order-" + orderId + ".pdf", "application/pdf", orderId);
    }

    @Override
    public void deleteStoredPdfs(Long orderId) {
        fileVaultService.deleteByOrder(orderId);
    }

    private byte[] buildPdf(OrderResponse o) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                line(cs, bold, 16, 50, 810, pdfProperties.companyName());

                float y = 760;
                line(cs, bold, 20, 50, y, "Order #" + o.id());                y -= 40;
                line(cs, regular, 12, 50, y, "Type: " + o.type());             y -= 22;
                line(cs, regular, 12, 50, y, "Placed by: " + o.placerName());  y -= 22;
                if (o.supplierName() != null) {
                    line(cs, regular, 12, 50, y, "Supplier: " + o.supplierName()); y -= 22;
                }
                line(cs, regular, 12, 50, y, "Date: " + o.orderDate());         y -= 22;
                line(cs, regular, 12, 50, y, "Status: " + o.status());          y -= 22;
                line(cs, bold, 14, 50, y, "Total: " + o.totalAmount());

                line(cs, regular, 10, 50, 40, pdfProperties.footer());
            }

            String owner = pdfProperties.password();
            if (owner != null && !owner.isBlank()) {
                AccessPermission permissions = new AccessPermission();
                permissions.setCanModify(false);
                StandardProtectionPolicy policy = new StandardProtectionPolicy(owner, "", permissions);
                policy.setEncryptionKeyLength(128);
                doc.protect(policy);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not generate PDF for order " + o.id(), e);
        }
    }

    private void line(PDPageContentStream cs, PDType1Font font, float size, float x, float y, String text)
            throws IOException {
        if (text == null) return;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }
}