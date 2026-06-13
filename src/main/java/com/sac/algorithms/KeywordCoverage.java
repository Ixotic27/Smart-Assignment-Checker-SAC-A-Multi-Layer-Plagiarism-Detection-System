package com.sac.algorithms;

import java.util.*;

// Easy strictness: checks how many keywords from the answer sheet are in student file
public class KeywordCoverage implements SimilarityAlgorithm {

    // Stop words list to ignore common English words
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

        // Get lists of clean words for both texts
        Set<String> answerKeywords = extractKeywords(answerText);
        Set<String> studentKeywords = extractKeywords(studentText);

        if (answerKeywords.isEmpty()) {
            return 100.0;
        }

        // Count how many answer keywords are found in student's keywords
        int matchingCount = 0;
        for (String word : answerKeywords) {
            if (studentKeywords.contains(word)) {
                matchingCount++;
            }
        }

        // Calculate coverage percentage
        double score = ((double) matchingCount / answerKeywords.size()) * 100.0;
        return score;
    }

    // Cleans up the text and extracts keywords that are not stop words
    private Set<String> extractKeywords(String text) {
        String lowercaseText = text.toLowerCase();
        // Remove punctuation and special characters
        String cleanText = lowercaseText.replaceAll("[^a-zA-Z0-9 ]", "");
        // Split by space
        String[] words = cleanText.split("\\s+");

        Set<String> keywordsSet = new HashSet<>();
        for (String word : words) {
            // Only add if word length is 2 or more and is not a stop word
            if (word.length() >= 2) {
                if (!STOP_WORDS.contains(word)) {
                    keywordsSet.add(word);
                }
            }
        }
        return keywordsSet;
    }
}
