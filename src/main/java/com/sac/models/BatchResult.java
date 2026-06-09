package com.sac.models;

import java.util.ArrayList;
import java.util.List;

// Stores results for a batch of student files compared against one answer sheet
public class BatchResult {

    private String answerSheetName;
    private String strictness;
    private List<SimilarityResult> results;

    public BatchResult(String answerSheetName, String strictness) {
        this.answerSheetName = answerSheetName;
        this.strictness = strictness;
        this.results = new ArrayList<>();
    }

    public void addResult(SimilarityResult result) {
        results.add(result);
    }

    public List<SimilarityResult> getResults() {
        return results;
    }

    public String getAnswerSheetName() {
        return answerSheetName;
    }

    public String getStrictness() {
        return strictness;
    }

    // Returns the average final score across all student files
    public double getAverageScore() {
        if (results.isEmpty()) return 0;
        double sum = 0;
        for (SimilarityResult r : results) {
            sum += r.getSimilarityScore();
        }
        return sum / results.size();
    }

    // Counts how many students are flagged as high similarity (75% or above)
    public int getFlaggedCount() {
        int count = 0;
        for (SimilarityResult r : results) {
            if (r.getSimilarityScore() >= 75) {
                count++;
            }
        }
        return count;
    }
}
