package com.sac.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Extracts text and images from PDF files.
// Uses PDFBox for typed PDFs and the PaddleOCR server for handwritten/scanned PDFs.
public class PDFExtractor {

    // Extracts text from a PDF file.
    // First tries direct extraction (fast, works for typed PDFs).
    // If no text is found, falls back to the PaddleOCR server for handwritten content.
    public static String extractText(File pdfFile) throws IOException {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new IOException("PDF file does not exist: " + pdfFile);
        }

        // Try direct text extraction (works for typed/digital PDFs)
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }
        }

        // No selectable text found - use OCR server for scanned/handwritten PDFs
        try {
            String ocrText = OCRClient.extractText(pdfFile);
            if (ocrText != null && !ocrText.trim().isEmpty()) {
                return ocrText.trim();
            }
        } catch (Exception e) {
            System.err.println("OCR failed for " + pdfFile.getName() + ": " + e.getMessage());
        }

        return "";
    }

    // Extracts all embedded images from a PDF as PNG byte arrays
    public static List<byte[]> extractImages(File pdfFile) throws IOException {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new IOException("PDF file does not exist: " + pdfFile);
        }

        List<byte[]> imageBytes = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName cosName : resources.getXObjectNames()) {
                    try {
                        if (resources.isImageXObject(cosName)) {
                            PDImageXObject image = (PDImageXObject) resources.getXObject(cosName);
                            BufferedImage bufferedImage = image.getImage();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(bufferedImage, "png", baos);
                            imageBytes.add(baos.toByteArray());
                        }
                    } catch (Exception e) {
                        System.err.println("Could not extract image: " + e.getMessage());
                    }
                }
            }
        }

        return imageBytes;
    }
}
