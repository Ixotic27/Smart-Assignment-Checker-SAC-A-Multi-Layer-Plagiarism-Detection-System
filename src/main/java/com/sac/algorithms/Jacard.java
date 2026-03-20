package com.sac.algorithms;

import java.util.*;

public class Jacard implements SimilarityAlgorithm {
    @Override
    public double calculateSimilarity(String t1, String t2) {
        if (t1 == null || t2 == null || t1.trim().isEmpty() || t2.trim().isEmpty()) {
            return 0.0;
        }
        String[] words1 = t1.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        String[] words2 = t2.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        if (set1.isEmpty() && set2.isEmpty())
            return 100.0;
        if (set1.isEmpty() || set2.isEmpty())
            return 0.0;
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return ((double) intersection.size() / union.size()) * 100.0;
    }
}
