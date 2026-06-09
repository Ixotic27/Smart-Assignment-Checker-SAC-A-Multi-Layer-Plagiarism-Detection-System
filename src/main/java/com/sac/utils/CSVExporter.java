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
            writer.println("S.No,Student File,Keyword Coverage %,Concept Match %,Sequence Match %,Assignment Score %,Verdict");

            List<SimilarityResult> results = batch.getResults();

            for (int i = 0; i < results.size(); i++) {
                SimilarityResult r = results.get(i);

                // Determine verdict based on assignment score
                String verdict;
                if (r.getSimilarityScore() >= 80) {
                    verdict = "EXCELLENT";
                } else if (r.getSimilarityScore() >= 50) {
                    verdict = "GOOD";
                } else {
                    verdict = "NEEDS WORK";
                }

                // Format each score, show 0 if algorithm was not run at this level
                double keyword = r.getKeywordCoverage() >= 0 ? r.getKeywordCoverage() : 0;
                double concept = r.getConceptMatch() >= 0 ? r.getConceptMatch() : 0;
                double sequence = r.getSequenceMatch() >= 0 ? r.getSequenceMatch() : 0;

                writer.printf("%d,%s,%.2f,%.2f,%.2f,%.2f,%s%n",
                    i + 1,
                    r.getDoc2(),
                    keyword,
                    concept,
                    sequence,
                    r.getSimilarityScore(),
                    verdict
                );
            }
        }
    }
}
