package com.sac;

import com.sac.utils.PDFExtractor;
import com.sac.algorithms.LCS;
import java.io.File;

public class TestNewOCR {
    public static void main(String[] args) throws Exception {
        String downloads = System.getProperty("user.home") + "/Downloads/";
        File f1 = new File(downloads, "New_Image_Hard1.pdf");
        File f2 = new File(downloads, "New_Image_Hard2.pdf");

        String text1 = PDFExtractor.extractText(f1);
        String text2 = PDFExtractor.extractText(f2);
        
        System.out.println("=== DOC 1 OCR (First 300 chars) ===");
        System.out.println(text1.substring(0, Math.min(300, text1.length())));
        System.out.println("Word count: " + text1.split("\\s+").length);
        
        System.out.println("\n=== DOC 2 OCR (First 300 chars) ===");
        System.out.println(text2.substring(0, Math.min(300, text2.length())));
        System.out.println("Word count: " + text2.split("\\s+").length);

        LCS lcs = new LCS();
        System.out.println("\nLCS Score: " + lcs.calculateSimilarity(text1, text2));
    }
}
