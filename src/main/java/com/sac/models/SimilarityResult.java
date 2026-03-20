package com.sac.models;

public class SimilarityResult {
    private String doc1;
    private String doc2;
    private String strictness;

    private double jaccardSimilarity;
    private double rabinKarpSimilarity;
    private double lcsSimilarity;
    private double finalSimilarity;

    public SimilarityResult(String doc1, String doc2, String strictness) {
        this.doc1 = doc1;
        this.doc2 = doc2;
        this.strictness = strictness;
    }

    // Setters and Getters

    public String getDoc1() {
        return doc1;
    }

    public String getDoc2() {
        return doc2;
    }

    public String getStrictness() {
        return strictness;
    }

    public double getSimilarityScore() {
        return finalSimilarity;
    }

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
}
