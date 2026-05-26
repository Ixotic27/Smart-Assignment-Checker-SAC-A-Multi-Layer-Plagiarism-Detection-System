package com.sac;

import com.sac.utils.ImageHashDetector;
import java.io.File;
import java.nio.file.Files;

public class TestImageSim {
    public static void main(String[] args) throws Exception {
        String dir = System.getProperty("user.home") + "/Downloads/";

        String[][] pairs = {
            {"easy 1.png", "easy 2.png"},
            {"med 1.png", "med 2.png"},
            {"hard 1.png", "hard 2.png"}
        };

        for (String[] pair : pairs) {
            byte[] img1 = Files.readAllBytes(new File(dir + pair[0]).toPath());
            byte[] img2 = Files.readAllBytes(new File(dir + pair[1]).toPath());
            double sim = ImageHashDetector.comparePixels(img1, img2);
            System.out.printf("%s vs %s : %.2f%%\n", pair[0], pair[1], sim);
        }
    }
}
