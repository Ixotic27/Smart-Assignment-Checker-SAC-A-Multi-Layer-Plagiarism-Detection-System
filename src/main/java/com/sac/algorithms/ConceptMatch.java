package com.sac.algorithms;

import java.util.*;

/**
 * ConceptMatch - Level 2 (Medium Strictness) scoring algorithm.
 *
 * Measures what percentage of the answer sheet's 3-word phrases (concepts)
 * appear in the student's submission.
 *
 * A 3-word phrase (trigram) like "binary search tree" captures a concept
 * better than individual words. If a student mentions the same 3-word
 * sequences as the answer, they likely understood the concepts.
 *
 * Formula:  score = (answer_3grams ∩ student_3grams) / answer_3grams × 100
 *
 * Key Design:
 *   - ONE-DIRECTIONAL: only checks how many answer concepts the student covered
 *   - Uses 3-word sliding window (trigrams) for concept detection
 *   - Normalized to lowercase with punctuation removed
 *
 * Time Complexity: O(n + m)
 * Space Complexity: O(n + m) for trigram sets
 */
public class ConceptMatch implements SimilarityAlgorithm {

    private static final int NGRAM_SIZE = 3;

    @Override
    public double calculateSimilarity(String answerText, String studentText) {
        if (answerText == null || studentText == null ||
            answerText.trim().isEmpty() || studentText.trim().isEmpty()) {
            return 0.0;
        }

        // Generate 3-word phrase sets from both texts
        Set<String> answerNgrams = generateNgrams(answerText);
        Set<String> studentNgrams = generateNgrams(studentText);

        if (answerNgrams.isEmpty()) return 100.0;

        // Count how many answer concepts appear in student's work
        Set<String> covered = new HashSet<>(answerNgrams);
        covered.retainAll(studentNgrams);

        return ((double) covered.size() / answerNgrams.size()) * 100.0;
    }

    /**
     * Generates all 3-word sliding window phrases from the text.
     * Example: "data structures are important" -> {"data structures are", "structures are important"}
     */
    private Set<String> generateNgrams(String text) {
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");

        Set<String> ngrams = new HashSet<>();

        if (words.length < NGRAM_SIZE) {
            // If text is shorter than 3 words, use the whole text as one n-gram
            ngrams.add(String.join(" ", words));
            return ngrams;
        }

        // Slide a window of NGRAM_SIZE words across the text
        for (int i = 0; i <= words.length - NGRAM_SIZE; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < NGRAM_SIZE; j++) {
                if (j > 0) sb.append(" ");
                sb.append(words[i + j]);
            }
            ngrams.add(sb.toString());
        }

        return ngrams;
    }
}
