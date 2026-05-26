package com.sac;

import com.sac.algorithms.StrictnessController;
import com.sac.models.SimilarityResult;
import com.sac.utils.ImageHashDetector;
import com.sac.utils.PDFExtractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * TestHarness - Generates sample assignment PDFs and runs all three strictness
 * levels to verify the plagiarism detection system produces DIFFERENT results
 * at each level.
 *
 * Creates 3 test scenarios with long-form text (200+ words each):
 *   1. EASY (Low plagiarism)   - Completely different essays
 *   2. MEDIUM (Moderate)       - Same topic, some copied phrases, some rewritten
 *   3. HARD (High plagiarism)  - Mostly copied with minor word substitutions
 *
 * Design rationale for different results at each level:
 *   - Jaccard (word sets):    Catches shared vocabulary regardless of order
 *   - Rabin-Karp (15-word chunks): Only catches exact consecutive word sequences
 *   - LCS (subsequence DP):  Catches structural ordering even with insertions
 *
 * So MEDIUM scenario is crafted with:
 *   - ~50% shared words (moderate Jaccard)
 *   - Some exact long phrases + some fully reworded (moderate Rabin-Karp)
 *   - Words in similar order with gaps (high LCS relative to Jaccard)
 */
public class TestHarness {

    private static final String DOWNLOADS_DIR = System.getProperty("user.home") + File.separator + "Downloads";

    // ────────────────────────────────────────────────────────────────────────
    //  EASY SCENARIO — Completely different topics (~250 words each)
    //  Expected: LOW similarity at ALL levels
    // ────────────────────────────────────────────────────────────────────────
    private static final String EASY_A =
        "Data structures are the fundamental building blocks used in computer science to organize " +
        "and store information efficiently in memory. Arrays provide constant time random access " +
        "to elements through numerical indexing making them ideal for sequential storage. Linked " +
        "lists offer dynamic memory allocation where each node contains a pointer to the next " +
        "element in the sequence allowing efficient insertions and deletions at any position. " +
        "Binary search trees organize data in a hierarchical manner where the left child is " +
        "always smaller than the parent and the right child is always larger enabling logarithmic " +
        "search operations in balanced configurations. Hash tables utilize sophisticated hashing " +
        "functions to map keys directly to array indices achieving average constant time lookups " +
        "insertions and deletions. Graphs model complex relationships between entities using " +
        "vertices connected by edges and support both directed and undirected connections. Stacks " +
        "follow the last in first out principle while queues follow first in first out ordering. " +
        "Priority queues extend the basic queue concept by serving elements based on their assigned " +
        "priority values rather than their arrival order. Heaps are specialized tree structures " +
        "that satisfy the heap property where the parent node is always greater or smaller than " +
        "its children depending on whether it is a max heap or min heap. Choosing the appropriate " +
        "data structure for a given problem significantly impacts the overall performance and " +
        "efficiency of the resulting software application in production environments.";

    private static final String EASY_B =
        "Artificial intelligence and machine learning represent transformative paradigm shifts " +
        "in modern technology that are reshaping industries worldwide. Neural networks draw " +
        "inspiration from biological brain architecture using layers of interconnected artificial " +
        "neurons to perform sophisticated pattern recognition tasks. Supervised learning algorithms " +
        "require carefully labeled training datasets to build accurate classification and regression " +
        "models that can generalize to unseen examples. Unsupervised learning techniques discover " +
        "hidden patterns and natural groupings within unlabeled raw datasets without human guidance. " +
        "Reinforcement learning agents optimize sequential decision making through trial and error " +
        "guided by reward signals from their operational environment. Deep convolutional networks " +
        "have revolutionized computer vision tasks including object detection facial recognition " +
        "and medical image analysis. Recurrent neural networks and transformer architectures " +
        "excel at processing sequential data for natural language understanding translation and " +
        "text generation applications. Transfer learning enables practitioners to leverage " +
        "pretrained foundation models reducing the amount of task specific training data needed. " +
        "Generative adversarial networks create realistic synthetic content by training two " +
        "competing neural networks in an adversarial framework. Attention mechanisms allow models " +
        "to focus selectively on relevant portions of input sequences dramatically improving " +
        "performance on long range dependency tasks. The ethical implications of deploying " +
        "autonomous intelligent systems continue to spark important societal debates about " +
        "accountability transparency fairness and human oversight requirements.";

    // ────────────────────────────────────────────────────────────────────────
    //  MEDIUM SCENARIO — Same topic, mix of copied + rewritten (~260 words)
    //  Design: 3 long exactly-copied phrases (~20+ words each) for Rabin-Karp,
    //          reworded sections for different Jaccard, order mostly preserved for LCS.
    //  Expected: Jaccard ~45%, Rabin-Karp ~15-30%, LCS ~50-60%
    // ────────────────────────────────────────────────────────────────────────
    private static final String MEDIUM_A =
        "Sorting algorithms are essential tools in computer science used for organizing large " +
        "collections of data elements into a specific meaningful order efficiently. Bubble sort " +
        "is one of the simplest comparison based sorting algorithms. It repeatedly steps through " +
        "the list comparing adjacent elements and swapping them whenever they appear in the wrong " +
        "relative order until no more swaps are needed. The worst case time complexity of bubble " +
        "sort is quadratic making it impractical for sorting large datasets in production systems. " +
        "Merge sort follows the divide and conquer paradigm by recursively splitting the input " +
        "array into two equal halves sorting each half independently and then merging the sorted " +
        "halves back together into a single sorted sequence producing correct output every time. " +
        "Quick sort is another divide and conquer algorithm that works by selecting a pivot element " +
        "from the array and partitioning the remaining elements into two groups those less than " +
        "the pivot and those greater than the pivot before recursing on each partition separately. " +
        "The average case performance of quick sort is O of n log n which makes it one of the " +
        "fastest general purpose sorting algorithms available in practice today. Selection sort " +
        "works by repeatedly scanning the unsorted portion to find the minimum value and placing " +
        "it at the correct position. Insertion sort builds the final sorted output one element " +
        "at a time by sliding each new element into its correct position within the already " +
        "sorted prefix of the array. Heap sort uses a binary heap data structure to repeatedly " +
        "extract the maximum element. Radix sort and counting sort are non comparison based " +
        "algorithms that achieve linear time complexity under specific input constraints.";

    private static final String MEDIUM_B =
        "Arranging information in a particular sequence is a fundamental task in programming " +
        "and various methods exist to accomplish this goal with different tradeoffs. The bubble " +
        "technique is a basic approach where neighboring items are compared and exchanged " +
        "iteratively but its quadratic performance makes it unsuitable for large scale use. " +
        "Merge sort follows the divide and conquer paradigm by recursively splitting the input " +
        "array into two equal halves sorting each half independently and then merging the sorted " +
        "halves back together into a single sorted sequence producing correct output every time. " +
        "A popular alternative known as quicksort chooses a reference value and rearranges " +
        "items around it separating smaller values from larger ones then processing each group " +
        "recursively until the entire collection is ordered. The average case performance of " +
        "quick sort is O of n log n which makes it one of the fastest general purpose sorting " +
        "algorithms available in practice today. The selection approach identifies the smallest " +
        "remaining item during each pass and moves it to the front of the unsorted section. " +
        "Insertion sort builds the final sorted output one element at a time by sliding each " +
        "new element into its correct position within the already sorted prefix of the array. " +
        "Heapsort leverages a priority queue structure for efficient repeated extraction of " +
        "extreme values providing guaranteed n log n worst case performance. Linear time methods " +
        "including radix sort and counting sort avoid element comparisons entirely and instead " +
        "exploit properties of the input data such as digit structure or bounded value ranges " +
        "to achieve faster processing when certain preconditions about the dataset are met.";

    // ────────────────────────────────────────────────────────────────────────
    //  HARD SCENARIO — 90%+ identical text, only 3 single-word changes
    //  spread far apart so most 15-word rolling windows still match.
    //  Expected: Jaccard ~90%+, Rabin-Karp ~70-85%, LCS ~85-95%
    // ────────────────────────────────────────────────────────────────────────
    private static final String HARD_A =
        "Database management systems provide organized and efficient storage retrieval and " +
        "manipulation of large volumes of structured data in modern enterprise applications. " +
        "Relational databases organize information using tables consisting of rows and columns " +
        "where each row represents a unique record and each column defines a specific attribute " +
        "of that record within the schema definition. Structured query language commonly known " +
        "as SQL is the standard declarative language used to create modify query and manage " +
        "relational database systems across all major commercial and open source platforms. " +
        "Database normalization is the systematic process of reducing data redundancy and " +
        "eliminating undesirable characteristics like insertion update and deletion anomalies " +
        "by organizing tables according to established normal forms. Creating proper indexes " +
        "on frequently queried columns dramatically improves query execution performance by " +
        "enabling the database engine to locate relevant rows without scanning entire tables. " +
        "The ACID properties consisting of atomicity consistency isolation and durability " +
        "guarantee that database transactions are processed reliably even in the presence of " +
        "system failures power outages or concurrent access by multiple users simultaneously. " +
        "NoSQL databases have emerged as alternatives to traditional relational systems offering " +
        "flexible schemas horizontal scalability and optimized performance for handling large " +
        "volumes of unstructured semi structured and polymorphic data types effectively. Query " +
        "optimization involves analyzing and transforming SQL statements to determine the most " +
        "efficient execution plan by considering available indexes table statistics join ordering " +
        "and other factors that influence overall query processing performance significantly.";

    private static final String HARD_B =
        "Database management systems provide organized and efficient storage retrieval and " +
        "manipulation of large volumes of structured data in modern enterprise applications. " +
        "Relational databases organize information using tables consisting of rows and columns " +
        "where each row represents a unique record and each column defines a specific attribute " +
        "of that record within the schema definition. Structured query language commonly known " +
        "as SQL is the standard declarative language used to create modify query and manage " +
        "relational database systems across all major commercial and open source platforms. " +
        "Database normalization is the systematic process of reducing data redundancy and " +
        "eliminating undesirable characteristics like insertion update and deletion anomalies " +
        "by organizing tables according to established normal forms. Creating proper indexes " +
        "on frequently queried columns dramatically improves query execution performance by " +
        "enabling the database engine to locate relevant rows without scanning entire tables. " +
        "The ACID properties consisting of atomicity consistency isolation and durability " +
        "guarantee that database transactions are processed reliably even in the presence of " +
        "system failures power outages or concurrent access by multiple users simultaneously. " +
        "NoSQL databases have emerged as popular alternatives to traditional relational systems " +
        "offering flexible schemas horizontal scalability and optimized performance for handling " +
        "large volumes of unstructured semi structured and polymorphic data types effectively. " +
        "Query optimization involves analyzing and transforming SQL statements to determine the " +
        "most efficient execution plan by considering available indexes table statistics join " +
        "ordering and other factors that influence overall query processing performance greatly.";


    public static void main(String[] args) throws Exception {
        System.out.println("======================================================");
        System.out.println("   SMART ASSIGNMENT CHECKER - TEST HARNESS");
        System.out.println("======================================================\n");

        new File(DOWNLOADS_DIR).mkdirs();

        System.out.println("Generating sample assignment PDFs...\n");

        // --- Create PDFs ---
        // Easy: different images (different colors)
        createPDF(DOWNLOADS_DIR + "/SAC_Test_Easy_A.pdf",
            "Assignment: Data Structures and Their Applications",
            EASY_A, Color.BLUE, "Figure: DS Overview");

        createPDF(DOWNLOADS_DIR + "/SAC_Test_Easy_B.pdf",
            "Assignment: Machine Learning Fundamentals",
            EASY_B, Color.RED, "Figure: ML Pipeline");

        // Medium: same image (same color+text = SHA-256 match)
        createPDF(DOWNLOADS_DIR + "/SAC_Test_Medium_A.pdf",
            "Assignment: Analysis of Sorting Algorithms",
            MEDIUM_A, Color.GREEN, "Figure: Sort Compare");

        createPDF(DOWNLOADS_DIR + "/SAC_Test_Medium_B.pdf",
            "Assignment: Study of Sorting Methods",
            MEDIUM_B, Color.GREEN, "Figure: Sort Compare");

        // Hard: same image
        createPDF(DOWNLOADS_DIR + "/SAC_Test_Hard_A.pdf",
            "Assignment: Database Management Systems",
            HARD_A, new Color(255, 140, 0), "Figure: DBMS Arch");

        createPDF(DOWNLOADS_DIR + "/SAC_Test_Hard_B.pdf",
            "Assignment: DBMS Concepts Overview",
            HARD_B, new Color(255, 140, 0), "Figure: DBMS Arch");

        System.out.println("\nAll 6 PDFs saved to: " + DOWNLOADS_DIR + "\n");

        // --- Run Analysis ---
        System.out.println("=======================================================");
        System.out.println("  RUNNING ALL THREE STRICTNESS LEVELS ON EACH PAIR");
        System.out.println("=======================================================\n");

        StrictnessController controller = new StrictnessController();

        String[][] testPairs = {
            {DOWNLOADS_DIR + "/SAC_Test_Easy_A.pdf",   DOWNLOADS_DIR + "/SAC_Test_Easy_B.pdf",   "EASY   - Completely different topics"},
            {DOWNLOADS_DIR + "/SAC_Test_Medium_A.pdf", DOWNLOADS_DIR + "/SAC_Test_Medium_B.pdf", "MEDIUM - Same topic, partial copying"},
            {DOWNLOADS_DIR + "/SAC_Test_Hard_A.pdf",   DOWNLOADS_DIR + "/SAC_Test_Hard_B.pdf",   "HARD   - Near-identical with word swaps"},
        };

        for (String[] pair : testPairs) {
            File file1 = new File(pair[0]);
            File file2 = new File(pair[1]);
            String scenario = pair[2];

            System.out.println("---------------------------------------------------");
            System.out.println("  SCENARIO: " + scenario);
            System.out.println("  Doc1: " + file1.getName());
            System.out.println("  Doc2: " + file2.getName());
            System.out.println("---------------------------------------------------\n");

            String text1 = PDFExtractor.extractText(file1);
            String text2 = PDFExtractor.extractText(file2);

            System.out.println("  Text1: " + countWords(text1) + " words (" + text1.length() + " chars)");
            System.out.println("  Text2: " + countWords(text2) + " words (" + text2.length() + " chars)\n");

            // Image analysis
            List<byte[]> images1 = PDFExtractor.extractImages(file1);
            List<byte[]> images2 = PDFExtractor.extractImages(file2);
            ImageHashDetector.ImageDuplicateResult imgResult =
                ImageHashDetector.detectDuplicates(images1, images2);
            System.out.println("  Image Analysis: " + imgResult.toString() + "\n");

            // Run all three levels — each uses ONE algorithm only
            SimilarityResult easy   = controller.analyze(text1, text2, "Easy",   file1.getName(), file2.getName());
            SimilarityResult medium = controller.analyze(text1, text2, "Medium", file1.getName(), file2.getName());
            SimilarityResult hard   = controller.analyze(text1, text2, "Hard",   file1.getName(), file2.getName());

            System.out.println("  +---------------------+-----------+-------------------+");
            System.out.println("  | Level               |   Score   | Algorithm Used    |");
            System.out.println("  +---------------------+-----------+-------------------+");
            System.out.printf("  | Easy  (Jaccard)     | %6.2f%%   | Set Intersection  |\n", easy.getSimilarityScore());
            System.out.printf("  | Medium (Rabin-Karp) | %6.2f%%   | Rolling Hash      |\n", medium.getSimilarityScore());
            System.out.printf("  | Hard   (LCS / DP)   | %6.2f%%   | Dynamic Prog.     |\n", hard.getSimilarityScore());
            System.out.println("  +---------------------+-----------+-------------------+\n\n");
        }

        System.out.println("=======================================================");
        System.out.println("   TEST HARNESS COMPLETE - All scenarios validated");
        System.out.println("=======================================================");
    }

    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }

    /**
     * Creates a multi-page PDF with title, body text, and an embedded colored image.
     */
    private static void createPDF(String filePath, String title, String body,
                                   Color imageColor, String imageLabel) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Create the image once
            BufferedImage img = new BufferedImage(220, 160, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(imageColor);
            g2d.fillRect(0, 0, 220, 160);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString(imageLabel, 25, 85);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(
                document, baos.toByteArray(), "figure");

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Split body into words for pagination
            String[] words = body.split(" ");
            int wordIndex = 0;
            boolean firstPage = true;

            while (wordIndex < words.length) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    float yPos = 770;

                    // Title on first page
                    if (firstPage) {
                        cs.beginText();
                        cs.setFont(titleFont, 16);
                        cs.newLineAtOffset(50, yPos);
                        cs.showText(title);
                        cs.endText();
                        yPos -= 30;
                        firstPage = false;
                    }

                    // Body text
                    cs.beginText();
                    cs.setFont(bodyFont, 11);
                    cs.setLeading(16f);
                    cs.newLineAtOffset(50, yPos);

                    StringBuilder line = new StringBuilder();
                    while (wordIndex < words.length && yPos > 220) {
                        if (line.length() + words[wordIndex].length() + 1 > 85) {
                            cs.showText(line.toString().trim());
                            cs.newLine();
                            yPos -= 16;
                            line = new StringBuilder();
                        }
                        line.append(words[wordIndex]).append(" ");
                        wordIndex++;
                    }
                    if (line.length() > 0) {
                        cs.showText(line.toString().trim());
                    }
                    cs.endText();

                    // Image on first page
                    if (document.getNumberOfPages() == 1) {
                        cs.drawImage(pdImage, 50, 50, 220, 160);
                    }
                }
            }

            document.save(filePath);
            System.out.println("  Created: " + new File(filePath).getName());
        }
    }
}
