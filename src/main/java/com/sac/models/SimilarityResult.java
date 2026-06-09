package com.sac.models;

/**
 * SimilarityResult - Stores the results of an assignment grading analysis.
 * Holds individual scores from each algorithm layer plus the computed final score.
 *
 * The three scoring layers:
 *   Level 1 - Keyword Coverage:  Did the student use the key terms from the answer?
 *   Level 2 - Concept Match:     Did the student use similar 3-word phrases?
 *   Level 3 - Sequence Match:    Did the student preserve the answer's word order?
 */
public class SimilarityResult {
    private String doc1;
    private String doc2;
    private String strictness;

    private double keywordCoverage = -1;
    private double conceptMatch = -1;
    private double sequenceMatch = -1;
    private double finalScore;

    public SimilarityResult(String doc1, String doc2, String strictness) {
        this.doc1 = doc1;
        this.doc2 = doc2;
        this.strictness = strictness;
    }

    // Getters
    public String getDoc1() { return doc1; }
    public String getDoc2() { return doc2; }
    public String getStrictness() { return strictness; }
    public double getSimilarityScore() { return finalScore; }
    public double getKeywordCoverage() { return keywordCoverage; }
    public double getConceptMatch() { return conceptMatch; }
    public double getSequenceMatch() { return sequenceMatch; }

    // Setters
    public void setKeywordCoverage(double keywordCoverage) {
        this.keywordCoverage = keywordCoverage;
    }

    public void setConceptMatch(double conceptMatch) {
        this.conceptMatch = conceptMatch;
    }

    public void setSequenceMatch(double sequenceMatch) {
        this.sequenceMatch = sequenceMatch;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    /**
     * Generates a formatted report string for display in the UI.
     */
    public String toReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("  ASSIGNMENT GRADING REPORT\n");
        sb.append("═══════════════════════════════════════════════\n\n");
        sb.append("  Answer Sheet:  ").append(doc1).append("\n");
        sb.append("  Student File:  ").append(doc2).append("\n");
        sb.append("  Strictness:    ").append(strictness).append("\n\n");
        sb.append("───────────────────────────────────────────────\n");
        sb.append("  ALGORITHM RESULTS\n");
        sb.append("───────────────────────────────────────────────\n\n");

        if (keywordCoverage >= 0) {
            sb.append(String.format("  ▸ Level 1 — Keyword Coverage:      %6.2f%%\n", keywordCoverage));
            sb.append("    Checks: Key terms from answer found in student's work\n\n");
        }
        if (conceptMatch >= 0) {
            sb.append(String.format("  ▸ Level 2 — Concept Match:         %6.2f%%\n", conceptMatch));
            sb.append("    Checks: 3-word phrases from answer found in student's work\n\n");
        }
        if (sequenceMatch >= 0) {
            sb.append(String.format("  ▸ Level 3 — Sequence Match:        %6.2f%%\n", sequenceMatch));
            sb.append("    Checks: Answer word order preserved in student's work\n\n");
        }

        sb.append("───────────────────────────────────────────────\n");
        sb.append(String.format("  ★ ASSIGNMENT SCORE:                %6.2f%%\n", finalScore));
        sb.append("───────────────────────────────────────────────\n\n");

        if (finalScore >= 80) {
            sb.append("  ✓ VERDICT: EXCELLENT — Student demonstrated strong understanding\n");
        } else if (finalScore >= 50) {
            sb.append("  ⚠ VERDICT: GOOD — Student covered most key concepts\n");
        } else {
            sb.append("  ✗ VERDICT: NEEDS IMPROVEMENT — Student missed significant content\n");
        }
        sb.append("═══════════════════════════════════════════════\n");

        return sb.toString();
    }
}
