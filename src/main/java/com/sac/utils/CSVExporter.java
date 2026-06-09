package com.sac.utils;

import com.sac.models.BatchResult;
import com.sac.models.SimilarityResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// Exports batch results to a CSV file for teachers to review
public class CSVExporter {

    public static void export(BatchResult batch, File outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

            // Write the header row
            writer.println("S.No,Student File,Jaccard %,Rabin-Karp %,LCS %,Final Score %,Verdict");

            List<SimilarityResult> results = batch.getResults();

            for (int i = 0; i < results.size(); i++) {
                SimilarityResult r = results.get(i);

                // Determine verdict based on final score
                String verdict;
                if (r.getSimilarityScore() >= 75) {
                    verdict = "HIGH";
                } else if (r.getSimilarityScore() >= 40) {
                    verdict = "MODERATE";
                } else {
                    verdict = "LOW";
                }

                // Format each score, show 0 if algorithm was not run
                double jaccard = r.getJaccardSimilarity() >= 0 ? r.getJaccardSimilarity() : 0;
                double rabinKarp = r.getRabinKarpSimilarity() >= 0 ? r.getRabinKarpSimilarity() : 0;
                double lcs = r.getLcsSimilarity() >= 0 ? r.getLcsSimilarity() : 0;

                writer.printf("%d,%s,%.2f,%.2f,%.2f,%.2f,%s%n",
                    i + 1,
                    r.getDoc2(),
                    jaccard,
                    rabinKarp,
                    lcs,
                    r.getSimilarityScore(),
                    verdict
                );
            }
        }
    }
}
