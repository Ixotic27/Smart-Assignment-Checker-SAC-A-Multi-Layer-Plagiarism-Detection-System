package com.sac;

import com.sac.algorithms.LCS;
import com.sac.utils.PDFExtractor;
import java.io.File;

public class OCRTestHard {
    public static void main(String[] args) throws Exception {
        String downloads = System.getProperty("user.home") + File.separator + "Downloads";
        File f1 = new File(downloads, "Hard_Doc1.pdf");
        File f2 = new File(downloads, "Hard_Doc2.pdf");

        System.out.println("Extracting OCR text from " + f1.getName() + "...");
        String text1 = PDFExtractor.extractText(f1);
        
        System.out.println("Extracting OCR text from " + f2.getName() + "...");
        String text2 = PDFExtractor.extractText(f2);

        LCS lcs = new LCS();
        double score = lcs.calculateSimilarity(text1, text2);
        System.out.println("LCS Score: " + score);

        System.out.println("--- Text 1 First 200 chars ---");
        System.out.println(text1.substring(0, Math.min(200, text1.length())));
        System.out.println("--- Text 2 First 200 chars ---");
        System.out.println(text2.substring(0, Math.min(200, text2.length())));
        
        System.out.println("--- Tokenization test ---");
        String[] w1 = text1.split("\\s+");
        String[] w2 = text2.split("\\s+");
        System.out.println("Words 1: " + w1.length + " | Words 2: " + w2.length);
    }
}
