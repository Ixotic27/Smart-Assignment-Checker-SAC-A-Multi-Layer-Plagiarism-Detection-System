class PlagiarismTask extends Thread {

    private String doc1;
    private String doc2;

    public PlagiarismTask(String d1, String d2){
        doc1=d1;
        doc2=d2;
    }

    public void run(){

        double similarity =
            LCS.getSimilarity(doc1,doc2);

        System.out.println(
           "Similarity: " +
           similarity + "%"
        );
    }
}
