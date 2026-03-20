package com.sac.algorithms;

public class LCS implements SimilarityAlgorithm {
    @Override
    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.trim().isEmpty() || text2.trim().isEmpty()) {
            return 0.0;
        }
        String[] words1 = text1.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        String[] words2 = text2.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
        int m = words1.length;
        int n = words2.length;
        if (m == 0 || n == 0)
            return 0.0;
        // 1D Array Space Optimization
        int[] dp = new int[n + 1];
        for (int i = 1; i <= m; i++) {
            int prev = 0;
            for (int j = 1; j <= n; j++) {
                int temp = dp[j];
                if (words1[i - 1].equals(words2[j - 1])) {
                    dp[j] = prev + 1;
                } else {
                    dp[j] = Math.max(dp[j], dp[j - 1]);
                }
                prev = temp;
            }
        }
        int lcsL = dp[n];
        int maxL = Math.max(m, n);
        return ((double) lcsL / maxL) * 100.0;
    }
}
