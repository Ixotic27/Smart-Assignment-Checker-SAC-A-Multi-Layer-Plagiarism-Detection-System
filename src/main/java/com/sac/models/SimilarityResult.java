package com.sac.models;

/**
 * SimilarityResult - Stores the results of a multi-layer plagiarism analysis.
 * Holds individual scores from each algorithm layer plus the computed final score.
 */
public class SimilarityResult {
    private String doc1;
    private String doc2;
    private String strictness;

    private double jaccardSimilarity = -1;
    private double rabinKarpSimilarity = -1;
    private double lcsSimilarity = -1;
    private double finalSimilarity;

    public SimilarityResult(String doc1, String doc2, String strictness) {
        this.doc1 = doc1;
        this.doc2 = doc2;
        this.strictness = strictness;
    }

    // Getters
    public String getDoc1() { return doc1; }
    public String getDoc2() { return doc2; }
    public String getStrictness() { return strictness; }
    public double getSimilarityScore() { return finalSimilarity; }
    public double getJaccardSimilarity() { return jaccardSimilarity; }
    public double getRabinKarpSimilarity() { return rabinKarpSimilarity; }
    public double getLcsSimilarity() { return lcsSimilarity; }

    // Setters
    public void setJaccardSimilarity(double jaccardSimilarity) {
        this.jaccardSimilarity = jaccardSimilarity;
    }

    public void setRabinKarpSimilarity(double rabinKarpSimilarity) {
        this.rabinKarpSimilarity = rabinKarpSimilarity;
    }

    public void setLcsSimilarity(double lcsSimilarity) {
        this.lcsSimilarity = lcsSimilarity;
    }

    public void setFinalSimilarity(double finalSimilarity) {
        this.finalSimilarity = finalSimilarity;
    }

    /**
     * Generates a formatted report string for display in the UI.
     */
    public String toReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("  PLAGIARISM ANALYSIS REPORT\n");
        sb.append("═══════════════════════════════════════════════\n\n");
        sb.append("  Document 1: ").append(doc1).append("\n");
        sb.append("  Document 2: ").append(doc2).append("\n");
        sb.append("  Strictness: ").append(strictness).append("\n\n");
        sb.append("───────────────────────────────────────────────\n");
        sb.append("  ALGORITHM RESULTS\n");
        sb.append("───────────────────────────────────────────────\n\n");

        if (jaccardSimilarity >= 0) {
            sb.append(String.format("  ▸ Level 1 — Jaccard Similarity:    %6.2f%%\n", jaccardSimilarity));
            sb.append("    Algorithm: Set Intersection/Union | O(n+m)\n\n");
        }
        if (rabinKarpSimilarity >= 0) {
            sb.append(String.format("  ▸ Level 2 — Rabin-Karp:            %6.2f%%\n", rabinKarpSimilarity));
            sb.append("    Algorithm: Rolling Hash Matching  | O(n+m)\n\n");
        }
        if (lcsSimilarity >= 0) {
            sb.append(String.format("  ▸ Level 3 — LCS (Dynamic Prog.):   %6.2f%%\n", lcsSimilarity));
            sb.append("    Algorithm: DP LCS Table          | O(n×m)\n\n");
        }

        sb.append("───────────────────────────────────────────────\n");
        sb.append(String.format("  ★ FINAL SIMILARITY SCORE:          %6.2f%%\n", finalSimilarity));
        sb.append("───────────────────────────────────────────────\n\n");

        if (finalSimilarity >= 75) {
            sb.append("  ⚠ VERDICT: HIGH PLAGIARISM DETECTED\n");
        } else if (finalSimilarity >= 40) {
            sb.append("  ⚠ VERDICT: MODERATE SIMILARITY — Manual review advised\n");
        } else {
            sb.append("  ✓ VERDICT: LOW SIMILARITY — Likely original work\n");
        }
        sb.append("═══════════════════════════════════════════════\n");

        return sb.toString();
    }
}
