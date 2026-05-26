package com.sac;

import com.sac.algorithms.Jacard;
import com.sac.algorithms.RabinKarp;
import com.sac.algorithms.LCS;
import java.io.File;
import java.nio.file.Files;

public class RunTests {
    public static void main(String[] args) throws Exception {
        String dir = System.getProperty("user.home") + "/Downloads/";

        // 1. EASY (Jaccard)
        String easy1 = Files.readString(new File(dir + "SAC_Easy_Doc1.txt").toPath());
        String easy2 = Files.readString(new File(dir + "SAC_Easy_Doc2.txt").toPath());
        Jacard jaccard = new Jacard();
        double easyScore = jaccard.calculateSimilarity(easy1, easy2);

        // 2. MEDIUM (Rabin-Karp)
        String med1 = Files.readString(new File(dir + "SAC_Medium_Doc1.txt").toPath());
        String med2 = Files.readString(new File(dir + "SAC_Medium_Doc2.txt").toPath());
        RabinKarp rk = new RabinKarp();
        double medScore = rk.calculateSimilarity(med1, med2);

        // 3. HARD (LCS)
        String hard1 = Files.readString(new File(dir + "SAC_Hard_Doc1.txt").toPath());
        String hard2 = Files.readString(new File(dir + "SAC_Hard_Doc2.txt").toPath());
        LCS lcs = new LCS();
        double hardScore = lcs.calculateSimilarity(hard1, hard2);

        System.out.println("=========================================");
        System.out.println("           ACTUAL TEST RESULTS           ");
        System.out.println("=========================================");
        System.out.printf("EASY   (Jaccard)    : %.2f%%\n", easyScore);
        System.out.printf("MEDIUM (Rabin-Karp) : %.2f%%\n", medScore);
        System.out.printf("HARD   (LCS)        : %.2f%%\n", hardScore);
        System.out.println("=========================================");
    }
}
