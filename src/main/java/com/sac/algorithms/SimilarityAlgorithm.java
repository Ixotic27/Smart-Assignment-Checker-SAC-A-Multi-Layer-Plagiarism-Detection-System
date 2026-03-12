package com.sac.algorithms;
import com.sac.models.SimilarityResult;

public interface SimilarityAlgorithm {
    SimilarityResult calculateSimilarity(String text1, String text2);
}
