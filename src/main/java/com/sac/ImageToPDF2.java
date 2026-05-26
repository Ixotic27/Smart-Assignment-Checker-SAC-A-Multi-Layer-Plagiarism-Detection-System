package com.sac;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;

public class ImageToPDF2 {
    public static void main(String[] args) {
        String downloads = System.getProperty("user.home") + "/Downloads/";

        String[][] files = {
            {"easy 1.png", "New_Image_Easy1.pdf"},
            {"easy 2.png", "New_Image_Easy2.pdf"},
            {"med 1.png", "New_Image_Medium1.pdf"},
            {"med 2.png", "New_Image_Medium2.pdf"},
            {"hard 1.png", "New_Image_Hard1.pdf"},
            {"hard 2.png", "New_Image_Hard2.pdf"}
        };

        for (String[] mapping : files) {
            try {
                File imgFile = new File(downloads + mapping[0]);
                if (!imgFile.exists()) {
                    System.out.println("Could not find image: " + imgFile.getName());
                    continue;
                }

                try (PDDocument doc = new PDDocument()) {
                    PDImageXObject pdImage = PDImageXObject.createFromFile(imgFile.getAbsolutePath(), doc);
                    
                    // Set page size to match image dimensions
                    PDRectangle box = new PDRectangle(pdImage.getWidth(), pdImage.getHeight());
                    PDPage page = new PDPage(box);
                    doc.addPage(page);

                    try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                        contentStream.drawImage(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
                    }
                    
                    String outPath = downloads + mapping[1];
                    doc.save(outPath);
                    System.out.println("Successfully created: " + mapping[1]);
                }
            } catch (Exception e) {
                System.err.println("Failed to process " + mapping[0] + ": " + e.getMessage());
            }
        }
    }
}
