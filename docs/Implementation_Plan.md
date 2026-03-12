# Smart Assignment Checker - Implementation Plan

## Team Structure
To ensure efficient execution and to divide the algorithm complexities logically, the project follows this responsibility matrix:
*   **Member 1**: PDF Processing & Core Setup
*   **Member 2**: Image Extraction, Hashing & Duplicate Detection
*   **Member 3**: Text Similarity Analytics (Level 1 & 2 Algorithms)
*   **Member 4**: Dynamic Programming (Level 3 Algorithm) & Strictness Controller

---

## Step-by-Step Execution Plan

**Objective Milestone:** Achieve at least 40% project completion **before the 25th**.

### Phase 1: Foundation & Base Features (Target: 40% Completion before 25th)

*   **Step 1: Setup & Project Initialization (All Members / Lead)**
    *   Initialize the Java structured project using Maven.
    *   Include and test the `Apache PDFBox` dependency.
    *   Establish basic project architecture (`utils`, `models`, `algorithms`).
    *   *Project Completion Status:* **5%**

*   **Step 2: Member 1 - PDF Processing Core**
    *   Implement `PDFExtractor` class to parse raw text from PDF assignment submissions.
    *   Implement logic to isolate and extract embedded images from the PDFs.
    *   Validate text output strings and stored image arrays.
    *   *Project Completion Status:* **20%**

*   **Step 3: Member 2 - Image Duplicate Detection**
    *   Convert extracted embedded images into Byte Arrays.in th
    *   Implement `SHA-256` hashing on the arrays.
    *   Construct a `HashSet` integration to achieve $O(1)$ duplicate image lookups (If hash repeats -> Duplicate detected).
    *   *Project Completion Status:* **30%**

*   **Step 4: Member 4 - JavaFX GUI Setup**
    *   Initialize JavaFX dependency in `pom.xml`.
    *   Design the main application window (File upload, Output fields).
    *   Build the interactive UI to accept strictness drop-downs from the user.
    *   *Project Completion Status:* **40% (MILESTONE MET!)**

*(Checkpoint: By the 25th, Steps 1-4 must be fully merged, ensuring 40% overall project functionality with a working UI base.)*

---

### Phase 2: Core Algorithmic Analytical Engines

*   **Step 5: Member 3 - Level 1 Text Similarity (Low Strictness)**
    *   Design and implement `Jaccard Similarity` analysis.
    *   Extract clean text into word sets and compute similarity ($Similarity = Intersection / Union$).
    *   *Average Time Complexity:* $O(n+m)$
    *   *Project Completion Status:* **55%**

*   **Step 6: Member 3 - Level 2 Text Similarity (Medium Strictness)**
    *   Design and implement the `Rabin-Karp Algorithm`.
    *   Divide text into chunks and apply rolling hash mechanisms to detect exact structural pattern matches.
    *   *Average Time Complexity:* $O(n+m)$
    *   *Project Completion Status:* **70%**

---

### Phase 3: Advanced Depth Analytics & Controller Integration

*   **Step 7: Member 4 - Level 3 Text Similarity (High Strictness)**
    *   Design and implement the `Longest Common Subsequence (LCS)`.
    *   Apply Dynamic Programming techniques.
    *   Compute longest matching subsequences between document texts iteratively.
    *   *Average Time Complexity:* $O(n \times m)$
    *   *Project Completion Status:* **85%**

*   **Step 8: Member 4 - Strictness Controller**
    *   Build the main faculty control execution logic:
        *   **Low** -> Triggers Jaccard only.
        *   **Medium** -> Triggers Jaccard AND Rabin-Karp.
        *   **High** -> Triggers Jaccard AND Rabin-Karp AND LCS.
    *   This strictly ensures that $O(n \times m)$ costly algorithms are only executed when requested by the faculty, thus optimizing performance.
    *   *Project Completion Status:* **95%**

---

### Phase 4: Finalization & Presentation

*   **Step 9: All Members - Testing, Optimization & Report generation**
    *   Run diverse test assignments to evaluate edge cases.
    *   Compile the "Complexity Comparison Analysis Report" matching standard DAA expectations.
    *   Finalize inline code documentation.
    *   *Project Completion Status:* **100%**
