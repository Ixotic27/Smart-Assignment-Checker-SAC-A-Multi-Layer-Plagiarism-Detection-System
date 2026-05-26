package com.sac.algorithms;

import java.util.*;

/**
 * RabinKarp - Level 2 (Medium Strictness) text similarity algorithm.
 *
 * Uses a rolling hash over consecutive word chunks (n-grams) to detect
 * exact structural pattern matches between two documents.
 *
 * Algorithm:
 *   1. Normalize both texts to lowercase word arrays
 *   2. Generate rolling hashes over sliding windows of CHUNK_SIZE words
 *   3. Compare hash sets using Jaccard-style intersection/union
 *
 * The rolling hash uses the full hashCode of each word (not just first char)
 * for better accuracy, combined with polynomial rolling to maintain O(n) generation.
 *
 * Time Complexity: O(n + m) average where n, m = word counts
 * Space Complexity: O(n + m) for hash sets
 */
public class RabinKarp implements SimilarityAlgorithm {
    private static final int CHUNK_SIZE = 5;  // 5-word sliding window (n-gram)
    private static final long BASE = 31L;
    private static final long PRIME = 1000000007L;

    @Override
    public double calculateSimilarity(String t1, String t2) {
        if (t1 == null || t2 == null || t1.trim().isEmpty() || t2.trim().isEmpty()) {
            return 0.0;
        }
        String[] words1 = t1.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        String[] words2 = t2.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        Set<Long> hashes1 = generateHashes(words1);
        Set<Long> hashes2 = generateHashes(words2);
        if (hashes1.isEmpty() && hashes2.isEmpty())
            return 100.0;
        if (hashes1.isEmpty() || hashes2.isEmpty())
            return 0.0;
        Set<Long> intersection = new HashSet<>(hashes1);
        intersection.retainAll(hashes2);
        Set<Long> union = new HashSet<>(hashes1);
        union.addAll(hashes2);
        return ((double) intersection.size() / union.size()) * 100.0;
    }

    /**
     * Generates rolling hashes for all CHUNK_SIZE-word windows in the word array.
     * Uses word hashCode combined with polynomial rolling for O(1) per window.
     */
    private Set<Long> generateHashes(String[] words) {
        Set<Long> hashes = new HashSet<>();
        if (words.length < CHUNK_SIZE)
            return hashes;

        // Precompute BASE^(CHUNK_SIZE-1) mod PRIME
        long h = 1;
        for (int i = 0; i < CHUNK_SIZE - 1; i++) {
            h = (h * BASE) % PRIME;
        }

        // Compute hash for the first window using full word hashCodes
        long currentHash = 0;
        for (int i = 0; i < CHUNK_SIZE; i++) {
            currentHash = (currentHash * BASE + wordHash(words[i])) % PRIME;
        }
        hashes.add(currentHash);

        // Roll the window across the rest of the array
        for (int i = CHUNK_SIZE; i < words.length; i++) {
            currentHash = (currentHash - wordHash(words[i - CHUNK_SIZE]) * h % PRIME + PRIME) % PRIME;
            currentHash = (currentHash * BASE + wordHash(words[i])) % PRIME;
            hashes.add(currentHash);
        }
        return hashes;
    }

    /**
     * Generates a positive hash value for a word.
     * Uses Java's String.hashCode() mapped to positive long range.
     */
    private long wordHash(String word) {
        return (Math.abs((long) word.hashCode()) % PRIME);
    }
}
