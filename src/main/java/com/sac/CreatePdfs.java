package com.sac;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

public class CreatePdfs {
    public static void main(String[] args) {
        String downloads = System.getProperty("user.home") + "/Downloads/";
        String desktop = System.getProperty("user.home") + "/Desktop/SAC_Test_PDFs/";
        new File(desktop).mkdirs();

        String[][] files = {
            {"SAC_Easy_Doc1.txt", "Easy_Doc1.pdf"},
            {"SAC_Easy_Doc2.txt", "Easy_Doc2.pdf"},
            {"SAC_Medium_Doc1.txt", "Medium_Doc1.pdf"},
            {"SAC_Medium_Doc2.txt", "Medium_Doc2.pdf"},
            {"SAC_Hard_Doc1.txt", "Hard_Doc1.pdf"},
            {"SAC_Hard_Doc2.txt", "Hard_Doc2.pdf"}
        };

        for (String[] mapping : files) {
            try {
                File txtFile = new File(downloads + mapping[0]);
                if (!txtFile.exists()) {
                    System.out.println("Missing: " + txtFile.getName());
                    continue;
                }
                
                String content = Files.readString(txtFile.toPath());
                createPdf(content, desktop + mapping[1]);
                System.out.println("Created " + mapping[1] + " in SAC_Test_PDFs folder on Desktop");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createPdf(String text, String outputPath) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(FontName.HELVETICA), 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 750);
                
                // Handle basic word wrap and newlines so it fits on page
                String[] lines = text.split("\n");
                for (String line : lines) {
                    line = line.replace("\r", "").replace("\t", "    ");
                    List<String> wrapped = wrapLine(line, 80);
                    for (String w : wrapped) {
                        // PDFBox strictly requires valid characters
                        w = w.replaceAll("[^\\x00-\\x7F]", "");
                        contentStream.showText(w);
                        contentStream.newLine();
                    }
                }
                contentStream.endText();
            }
            document.save(outputPath);
        }
    }
    
    private static List<String> wrapLine(String line, int maxChars) {
        List<String> result = new ArrayList<>();
        if (line.trim().isEmpty()) {
            result.add(" ");
            return result;
        }
        while (line.length() > maxChars) {
            int spaceIndex = line.lastIndexOf(' ', maxChars);
            if (spaceIndex == -1) spaceIndex = maxChars;
            result.add(line.substring(0, spaceIndex));
            line = line.substring(spaceIndex).trim();
        }
        result.add(line);
        return result;
    }
}
