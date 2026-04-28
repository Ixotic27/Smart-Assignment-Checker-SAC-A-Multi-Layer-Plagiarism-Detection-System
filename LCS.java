public class LCS {

    // Find LCS length using Dynamic Programming
    public static int findLCS(String s1, String s2) {

        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m+1][n+1];

        for(int i=1; i<=m; i++) {
            for(int j=1; j<=n; j++) {

                if(s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1] + 1;
                }

                else {
                    dp[i][j] = Math.max(
                        dp[i-1][j],
                        dp[i][j-1]
                    );
                }
            }
        }

        return dp[m][n];
    }


    // Similarity percentage
    public static double getSimilarity(String s1, String s2){

        int lcs = findLCS(s1,s2);

        int maxLength = Math.max(
                s1.length(),
                s2.length()
        );

        return ((double)lcs/maxLength)*100;
    }


    public static void main(String args[]){

        String a="Java Programming Language";
        String b="Java Programming Concepts";

        System.out.println(
            "Similarity = " +
            getSimilarity(a,b) + "%"
        );
    }
}
