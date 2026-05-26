package com.sac;

import com.sac.algorithms.RabinKarp;
import com.sac.utils.PDFExtractor;
import java.io.File;

public class OCRTest {
    public static void main(String[] args) throws Exception {
        String downloads = System.getProperty("user.home") + File.separator + "Downloads";
        File f1 = new File(downloads, "Medium_Doc1.pdf");
        File f2 = new File(downloads, "Medium_Doc2.pdf");

        System.out.println("Extracting OCR text from " + f1.getName() + "...");
        String text1 = PDFExtractor.extractText(f1);
        System.out.println("Text 1 length: " + text1.length() + " chars, " + text1.split("\\s+").length + " words");
        
        System.out.println("Extracting OCR text from " + f2.getName() + "...");
        String text2 = PDFExtractor.extractText(f2);
        System.out.println("Text 2 length: " + text2.length() + " chars, " + text2.split("\\s+").length + " words");

        RabinKarp rk = new RabinKarp();
        double score = rk.calculateSimilarity(text1, text2);
        System.out.println("Rabin-Karp Score: " + score);
        
        if (score == 0.0) {
            System.out.println("Showing first 100 chars of text1: " + text1.substring(0, Math.min(100, text1.length())));
            System.out.println("Showing first 100 chars of text2: " + text2.substring(0, Math.min(100, text2.length())));
        }
    }
}
