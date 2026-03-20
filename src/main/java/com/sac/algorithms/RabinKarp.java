package com.sac.algorithms;

import java.util.*;

public class RabinKarp implements SimilarityAlgorithm {
    private static final int CHUNK_SIZE = 15;
    private static final int BASE = 256;
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

    private Set<Long> generateHashes(String[] words) {
        Set<Long> hashes = new HashSet<>();
        if (words.length < CHUNK_SIZE)
            return hashes;
        long currentHash = 0;
        long h = 1;
        for (int i = 0; i < CHUNK_SIZE - 1; i++) {
            h = (h * BASE) % PRIME;
        }
        for (int i = 0; i < CHUNK_SIZE; i++) {
            currentHash = (currentHash * BASE + words[i].charAt(0)) % PRIME;
        }
        hashes.add(currentHash);
        for (int i = CHUNK_SIZE; i < words.length; i++) {
            currentHash = (currentHash - words[i - CHUNK_SIZE].charAt(0) * h) % PRIME;
            currentHash = (currentHash * BASE + words[i].charAt(0)) % PRIME;
            if (currentHash < 0)
                currentHash += PRIME;
            hashes.add(currentHash);
        }
        return hashes;
    }

}
