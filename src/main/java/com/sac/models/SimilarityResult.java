package com.sac.models;

// Class to store scores of one student file compared to answer sheet
public class SimilarityResult {
    private String doc1;
    private String doc2;
    private String strictness;

    private double keywordCoverage = -1.0;
    private double conceptMatch = -1.0;
    private double sequenceMatch = -1.0;
    private double finalScore = 0.0;

    // Constructor
    public SimilarityResult(String doc1, String doc2, String strictness) {
        this.doc1 = doc1;
        this.doc2 = doc2;
        this.strictness = strictness;
    }

    // Getters
    public String getDoc1() {
        return this.doc1;
    }

    public String getDoc2() {
        return this.doc2;
    }

    public String getStrictness() {
        return this.strictness;
    }

    public double getSimilarityScore() {
        return this.finalScore;
    }

    public double getKeywordCoverage() {
        return this.keywordCoverage;
    }

    public double getConceptMatch() {
        return this.conceptMatch;
    }

    public double getSequenceMatch() {
        return this.sequenceMatch;
    }

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
}
