package com.sac.algorithms;

import java.util.*;

/**
 * KeywordCoverage - Level 1 (Easy Strictness) scoring algorithm.
 *
 * Measures what percentage of the answer sheet's meaningful keywords
 * appear in the student's submission.
 *
 * Formula:  score = (answer_keywords ∩ student_keywords) / answer_keywords × 100
 *
 * Key Design:
 *   - ONE-DIRECTIONAL: only checks coverage of the answer sheet's words
 *   - Filters out common stop words (the, is, a, etc.) to focus on content words
 *   - Student writing extra content does NOT reduce the score
 *
 * Time Complexity: O(n + m) where n, m = word counts
 * Space Complexity: O(n + m) for word sets
 */
public class KeywordCoverage implements SimilarityAlgorithm {

    // Common English stop words that don't carry meaningful content
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "could",
        "should", "may", "might", "shall", "can", "need", "dare", "ought",
        "used", "to", "of", "in", "for", "on", "with", "at", "by", "from",
        "as", "into", "through", "during", "before", "after", "above", "below",
        "between", "out", "off", "over", "under", "again", "further", "then",
        "once", "here", "there", "when", "where", "why", "how", "all", "both",
        "each", "few", "more", "most", "other", "some", "such", "no", "nor",
        "not", "only", "own", "same", "so", "than", "too", "very", "just",
        "because", "but", "and", "or", "if", "while", "about", "against",
        "this", "that", "these", "those", "it", "its", "they", "them", "their",
        "we", "our", "you", "your", "he", "she", "him", "her", "his",
        "i", "me", "my", "which", "who", "whom", "what", "also"
    ));

    @Override
    public double calculateSimilarity(String answerText, String studentText) {
        if (answerText == null || studentText == null ||
            answerText.trim().isEmpty() || studentText.trim().isEmpty()) {
            return 0.0;
        }

        // Extract meaningful keywords (excluding stop words)
        Set<String> answerKeywords = extractKeywords(answerText);
        Set<String> studentKeywords = extractKeywords(studentText);

        if (answerKeywords.isEmpty()) return 100.0;

        // Count how many answer keywords appear in the student's work
        Set<String> covered = new HashSet<>(answerKeywords);
        covered.retainAll(studentKeywords);

        return ((double) covered.size() / answerKeywords.size()) * 100.0;
    }

    /**
     * Extracts meaningful keywords from text by:
     * 1. Converting to lowercase
     * 2. Removing punctuation
     * 3. Splitting into words
     * 4. Filtering out stop words and very short words
     */
    private Set<String> extractKeywords(String text) {
        String[] words = text.toLowerCase()
                            .replaceAll("[^a-zA-Z0-9 ]", "")
                            .split("\\s+");
        Set<String> keywords = new HashSet<>();
        for (String word : words) {
            // Keep words that are meaningful (not stop words, at least 2 chars)
            if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                keywords.add(word);
            }
        }
        return keywords;
    }
}
