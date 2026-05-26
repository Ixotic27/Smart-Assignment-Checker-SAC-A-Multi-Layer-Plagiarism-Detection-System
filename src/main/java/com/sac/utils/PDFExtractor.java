package com.sac.utils;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PDFExtractor - Extracts text and embedded images from PDF assignment files.
 * Uses Apache PDFBox 3.x API with Tesseract OCR fallback for image-based PDFs.
 *
 * Two extraction modes:
 *   1. Direct text extraction via PDFTextStripper (for text-based PDFs)
 *   2. OCR via Tesseract (for scanned/image-based PDFs)
 *
 * Time Complexity:
 *   - Text extraction: O(n) where n = number of characters in the PDF
 *   - OCR extraction: O(p * w * h) where p = pages, w*h = image dimensions
 *   - Image extraction: O(p * r) where p = pages, r = resources per page
 */
public class PDFExtractor {

    /**
     * Extracts text from a PDF file. If no selectable text is found,
     * automatically falls back to OCR (Tesseract) to extract text from
     * rendered page images.
     *
     * @param pdfFile the PDF file to extract text from
     * @return the extracted text as a String
     * @throws IOException if the file cannot be read or parsed
     */
    public static String extractText(File pdfFile) throws IOException {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new IOException("PDF file does not exist: " + pdfFile);
        }

        // Try direct text extraction first (fast path)
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }
        }

        // Fallback: OCR extraction for image-based PDFs
        return extractTextWithOCR(pdfFile);
    }

    /**
     * Extracts text from image-based PDFs using Tesseract OCR.
     * Renders each page as an image at 300 DPI and runs OCR on it.
     *
     * @param pdfFile the PDF file to OCR
     * @return the extracted text, or empty string if OCR fails
     */
    public static String extractTextWithOCR(File pdfFile) {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            Tesseract tesseract = new Tesseract();

            // Find tessdata directory — check project root, then working dir
            String tessDataPath = findTessDataPath();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");

            // Render each page and OCR it
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder allText = new StringBuilder();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                // Render page at 300 DPI for good OCR quality
                BufferedImage pageImage = renderer.renderImageWithDPI(i, 300);

                try {
                    String pageText = tesseract.doOCR(pageImage);
                    if (pageText != null && !pageText.trim().isEmpty()) {
                        allText.append(pageText.trim()).append("\n");
                    }
                } catch (TesseractException e) {
                    System.err.println("OCR failed on page " + (i + 1) + ": " + e.getMessage());
                }
            }

            return allText.toString().trim();
        } catch (Exception e) {
            System.err.println("OCR extraction failed: " + e.getMessage());
            return "";
        }
    }

    /**
     * Locates the tessdata directory by checking multiple possible locations.
     */
    private static String findTessDataPath() {
        // Check these locations in order
        String[] possiblePaths = {
            "tessdata",                                    // Project root (relative)
            System.getProperty("user.dir") + File.separator + "tessdata",
            "d:\\Smart_Assignment_Checker\\tessdata",      // Absolute project path
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File engData = new File(dir, "eng.traineddata");
                if (engData.exists()) {
                    return dir.getAbsolutePath();
                }
            }
        }

        // Default — let Tesseract try its own default path
        return "tessdata";
    }

    /**
     * Extracts all embedded images from a PDF file as byte arrays.
     * Each byte array represents a PNG-encoded image.
     */
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
                        // Skip images that can't be decoded
                        System.err.println("Warning: Could not extract image '" + cosName.getName() + "': " + e.getMessage());
                    }
                }
            }
        }
        return imageBytes;
    }
}
