package com.sac.algorithms;

import com.sac.models.SimilarityResult;

/**
 * StrictnessController - Routes plagiarism checks to a SINGLE algorithm
 * based on the faculty-selected strictness level.
 *
 * Each level uses one distinct algorithm:
 *   EASY   → Jaccard Similarity (Set-based word overlap)     [O(n+m)]
 *   MEDIUM → Rabin-Karp (Rolling hash chunk matching)        [O(n+m)]
 *   HARD   → LCS (Dynamic Programming subsequence analysis)  [O(n×m)]
 */
public class StrictnessController {

    private final Jacard jaccard = new Jacard();
    private final RabinKarp rabinKarp = new RabinKarp();
    private final LCS lcs = new LCS();

    /**
     * Runs the appropriate single algorithm based on the selected strictness level.
     */
    public SimilarityResult analyze(String text1, String text2, String strictness,
                                     String doc1Name, String doc2Name) {
        SimilarityResult result = new SimilarityResult(doc1Name, doc2Name, strictness);

        double finalScore;

        switch (strictness.toLowerCase()) {
            case "easy":
                // Level 1 — Jaccard Similarity — O(n+m)
                double jaccardScore = jaccard.calculateSimilarity(text1, text2);
                result.setJaccardSimilarity(jaccardScore);
                finalScore = jaccardScore;
                break;

            case "medium":
                // Level 2 — Rabin-Karp Rolling Hash — O(n+m)
                double rkScore = rabinKarp.calculateSimilarity(text1, text2);
                result.setRabinKarpSimilarity(rkScore);
                finalScore = rkScore;
                break;

            case "hard":
                // Level 3 — LCS via Dynamic Programming — O(n×m)
                double lcsScore = lcs.calculateSimilarity(text1, text2);
                result.setLcsSimilarity(lcsScore);
                finalScore = lcsScore;
                break;

            default:
                throw new IllegalArgumentException("Unknown strictness level: " + strictness);
        }

        result.setFinalSimilarity(finalScore);
        return result;
    }
}
