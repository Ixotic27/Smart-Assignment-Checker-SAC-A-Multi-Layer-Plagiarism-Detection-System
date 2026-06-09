package com.sac.algorithms;

import com.sac.models.SimilarityResult;

// Routes the comparison to the right set of algorithms based on strictness level.
// Easy:   Jaccard only
// Medium: Jaccard + Rabin-Karp (average of both)
// Hard:   Jaccard + Rabin-Karp + LCS (average of all three)
public class StrictnessController {

    private final Jacard jaccard = new Jacard();
    private final RabinKarp rabinKarp = new RabinKarp();
    private final LCS lcs = new LCS();

    public SimilarityResult analyze(String text1, String text2, String strictness,
                                     String doc1Name, String doc2Name) {

        SimilarityResult result = new SimilarityResult(doc1Name, doc2Name, strictness);
        double finalScore;

        switch (strictness.toLowerCase()) {
            case "easy":
                // Easy: only Jaccard (word set overlap)
                double jaccardScore = jaccard.calculateSimilarity(text1, text2);
                result.setJaccardSimilarity(jaccardScore);
                finalScore = jaccardScore;
                break;

            case "medium":
                // Medium: Jaccard + Rabin-Karp, final score is their average
                double jScore = jaccard.calculateSimilarity(text1, text2);
                double rkScore = rabinKarp.calculateSimilarity(text1, text2);
                result.setJaccardSimilarity(jScore);
                result.setRabinKarpSimilarity(rkScore);
                finalScore = (jScore + rkScore) / 2.0;
                break;

            case "hard":
                // Hard: all three algorithms, final score is their average
                double jScoreH = jaccard.calculateSimilarity(text1, text2);
                double rkScoreH = rabinKarp.calculateSimilarity(text1, text2);
                double lcsScore = lcs.calculateSimilarity(text1, text2);
                result.setJaccardSimilarity(jScoreH);
                result.setRabinKarpSimilarity(rkScoreH);
                result.setLcsSimilarity(lcsScore);
                finalScore = (jScoreH + rkScoreH + lcsScore) / 3.0;
                break;

            default:
                throw new IllegalArgumentException("Unknown strictness level: " + strictness);
        }

        result.setFinalSimilarity(finalScore);
        return result;
    }
}
