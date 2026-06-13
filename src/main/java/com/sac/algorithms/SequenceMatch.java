package com.sac.algorithms;

// Hard strictness: checks if student used correct order of words from the answer sheet using LCS
public class SequenceMatch implements SimilarityAlgorithm {

    @Override
    public double calculateSimilarity(String answerText, String studentText) {
        if (answerText == null || studentText == null ||
            answerText.trim().isEmpty() || studentText.trim().isEmpty()) {
            return 0.0;
        }

        // Clean and split words
        String[] answerWords = answerText.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        String[] studentWords = studentText.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");

        int ansLen = answerWords.length;
        int stuLen = studentWords.length;

        if (ansLen == 0) {
            return 100.0;
        }
        if (stuLen == 0) {
            return 0.0;
        }

        // Standard 2D Dynamic Programming table for Longest Common Subsequence (LCS)
        int[][] dp = new int[ansLen + 1][stuLen + 1];

        // Fill the DP table
        for (int i = 1; i <= ansLen; i++) {
            for (int j = 1; j <= stuLen; j++) {
                if (answerWords[i - 1].equals(studentWords[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // The length of the longest common subsequence is in the bottom-right cell
        int lcsLength = dp[ansLen][stuLen];

        // Divide by the answer key length so student is not penalized for extra writing
        double score = ((double) lcsLength / ansLen) * 100.0;
        return score;
    }
}
