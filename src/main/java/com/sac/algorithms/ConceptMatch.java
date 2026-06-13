package com.sac.algorithms;

import java.util.*;

// Medium strictness: checks if student used similar 3-word phrases (concepts) from the answer sheet
public class ConceptMatch implements SimilarityAlgorithm {

    private static final int NGRAM_SIZE = 3;

    @Override
    public double calculateSimilarity(String answerText, String studentText) {
        if (answerText == null || studentText == null ||
            answerText.trim().isEmpty() || studentText.trim().isEmpty()) {
            return 0.0;
        }

        // Get 3-word concepts for both texts
        Set<String> answerNgrams = generateNgrams(answerText);
        Set<String> studentNgrams = generateNgrams(studentText);

        if (answerNgrams.isEmpty()) {
            return 100.0;
        }

        // Count how many answer concepts are in the student's concepts
        int matchCount = 0;
        for (String concept : answerNgrams) {
            if (studentNgrams.contains(concept)) {
                matchCount++;
            }
        }

        // Calculate score
        double score = ((double) matchCount / answerNgrams.size()) * 100.0;
        return score;
    }

    // Generates 3-word phrases from text
    private Set<String> generateNgrams(String text) {
        String lowercaseText = text.toLowerCase();
        String cleanText = lowercaseText.replaceAll("[^a-zA-Z0-9 ]", "");
        String[] words = cleanText.split("\\s+");

        Set<String> ngrams = new HashSet<>();

        // If text is too short, just put the whole text as one item
        if (words.length < NGRAM_SIZE) {
            String shortText = "";
            for (int i = 0; i < words.length; i++) {
                if (i > 0) {
                    shortText = shortText + " ";
                }
                shortText = shortText + words[i];
            }
            ngrams.add(shortText);
            return ngrams;
        }

        // Loop and concatenate 3 words at a time
        for (int i = 0; i <= words.length - NGRAM_SIZE; i++) {
            String phrase = words[i] + " " + words[i + 1] + " " + words[i + 2];
            ngrams.add(phrase);
        }

        return ngrams;
    }
}
