package com.sac.algorithms;

/**
 * SequenceMatch - Level 3 (Hard Strictness) scoring algorithm.
 *
 * Uses Longest Common Subsequence (LCS) to measure how well the student's
 * answer preserves the ORDER of words from the answer sheet.
 *
 * Unlike the old LCS that divided by max(m,n), this version divides by
 * the ANSWER sheet's word count. This means:
 *   - Student covers all content in order → 100%
 *   - Student adds extra content → still ~100% (not penalized)
 *   - Student rearranges content → lower score
 *
 * Formula:  score = LCS(answer_words, student_words) / answer_word_count × 100
 *
 * Key Design:
 *   - ONE-DIRECTIONAL: denominator is answer length, not max
 *   - Measures sequence preservation (word ORDER matters)
 *   - Space-optimized 1D DP array: O(min(n,m)) space
 *
 * Time Complexity: O(n × m) where n, m = word counts
 * Space Complexity: O(min(n, m)) with 1D array optimization
 */
public class SequenceMatch implements SimilarityAlgorithm {

    @Override
    public double calculateSimilarity(String answerText, String studentText) {
        if (answerText == null || studentText == null ||
            answerText.trim().isEmpty() || studentText.trim().isEmpty()) {
            return 0.0;
        }

        String[] answerWords = answerText.toLowerCase()
                                        .replaceAll("[^a-zA-Z0-9 ]", "")
                                        .split("\\s+");
        String[] studentWords = studentText.toLowerCase()
                                          .replaceAll("[^a-zA-Z0-9 ]", "")
                                          .split("\\s+");

        int ansLen = answerWords.length;
        int stuLen = studentWords.length;

        if (ansLen == 0) return 100.0;
        if (stuLen == 0) return 0.0;

        // Space-optimized LCS using 1D DP array
        int[] dp = new int[stuLen + 1];

        for (int i = 1; i <= ansLen; i++) {
            int prev = 0;
            for (int j = 1; j <= stuLen; j++) {
                int temp = dp[j];
                if (answerWords[i - 1].equals(studentWords[j - 1])) {
                    dp[j] = prev + 1;
                } else {
                    dp[j] = Math.max(dp[j], dp[j - 1]);
                }
                prev = temp;
            }
        }

        int lcsLength = dp[stuLen];

        // Divide by ANSWER length (not max) — student extra content doesn't penalize
        return ((double) lcsLength / ansLen) * 100.0;
    }
}
