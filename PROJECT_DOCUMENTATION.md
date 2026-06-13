# Smart Assignment Checker - Comprehensive Project Documentation

## 1. Project Overview
**Smart Assignment Checker (SAC)** is a desktop application designed to grade student assignments by measuring how well each student's work covers the answer sheet's content. A teacher uploads one **answer sheet PDF** and up to **60 student assignment PDFs**, and the system scores each student's work using multiple coverage-based algorithms. It supports both typed and handwritten PDFs through OCR (Optical Character Recognition) and provides results in a table with CSV export.

---

## 2. Technology Stack & Rationale

| Technology | Why it was used | What it does | Where it applies |
| :--- | :--- | :--- | :--- |
| **Java 21** | Core programming language. Chosen for its robust standard library, strong object-oriented design, and excellent performance for algorithmic data processing. | Powers the entire backend logic, algorithms, and application flow. | Across all `.java` files in `src/main/java`. |
| **JavaFX 17** | UI Framework. Chosen because it provides a modern, hardware-accelerated GUI toolkit for Java desktop applications that separates logic from design via FXML. | Renders the login screen, dashboard, file choosers, progress bar, results table, and CSV export. | `GUIApp.java`, `LoginController.java`, `DashboardController.java`, and `.fxml` files. |
| **Maven** | Build and Dependency Management. Chosen to automatically handle external libraries (PDFBox) and build a standalone executable `.jar` file. | Downloads dependencies, compiles code, and packages the application. | `pom.xml` |
| **Apache PDFBox (v3.x)** | PDF Parsing Library. Chosen because it is the industry standard open-source library for extracting text and raw image byte streams from PDF documents. | Reads user-uploaded PDFs, strips text, and isolates embedded PNG/JPG objects. | `utils/PDFExtractor.java` |
| **RapidOCR** | Optical Character Recognition (OCR). Uses ONNX Runtime with PaddleOCR models for high accuracy on both printed and handwritten text. Much faster than full PaddlePaddle on CPU. Runs as a Python microservice. | Converts images of handwritten or scanned text into strings so the algorithms can process them. | `ocr_server/ocr_server.py` + `utils/OCRClient.java` |
| **Flask + PyMuPDF** | Python web framework and PDF renderer. Flask provides the HTTP server for OCR requests. PyMuPDF renders PDF pages into images at 150 DPI for OCR processing. | Powers the OCR microservice that RapidOCR runs inside. | `ocr_server/ocr_server.py` |

---

## 3. Project Architecture & File Breakdown

The project follows the **MVC (Model-View-Controller)** design pattern and the **Strategy Pattern** for its algorithms.

### Directory Structure:
```text
Smart_Assignment_Checker/
├── src/main/
│   ├── java/com/sac/
│   │   ├── algorithms/             (Core Scoring Algorithms)
│   │   │   ├── SimilarityAlgorithm.java
│   │   │   ├── KeywordCoverage.java
│   │   │   ├── ConceptMatch.java
│   │   │   ├── SequenceMatch.java
│   │   │   └── StrictnessController.java
│   │   ├── models/                 (Data Models)
│   │   │   ├── SimilarityResult.java
│   │   │   └── BatchResult.java
│   │   ├── utils/                  (Helper Classes)
│   │   │   ├── PDFExtractor.java
│   │   │   ├── ImageHashDetector.java
│   │   │   ├── OCRClient.java
│   │   │   ├── BatchProcessor.java
│   │   │   └── CSVExporter.java
│   │   ├── DashboardController.java
│   │   ├── LoginController.java
│   │   ├── GUIApp.java
│   │   └── Main.java
│   └── resources/views/           (UI Layouts & Styles)
│       ├── Dashboard.fxml
│       ├── Login.fxml
│       └── styles.css
├── ocr_server/                     (Python OCR Microservice)
│   ├── ocr_server.py
│   ├── requirements.txt
│   └── start_ocr.bat
├── pom.xml                         (Maven Configuration)
└── run.bat                         (Windows Launch Script)
```

### Detailed File Usage & Topics Applied

#### 1. The Application Layer (Controllers & Views)
*   **`GUIApp.java`**: The main entry point.
    *   *Topic:* JavaFX Application Lifecycle. Bootstraps the application and loads the initial `Login.fxml` scene.
*   **`LoginController.java`**: Handles user authentication.
    *   *Topic:* Event Handling, UI state management. Validates `admin` / `1234` credentials before switching scenes.
*   **`DashboardController.java`**: The brain of the UI.
    *   *Topic:* Multithreading & Concurrency. Handles answer sheet upload, batch student file upload (up to 60 PDFs), runs analysis on a background thread using `Platform.runLater()` for UI updates, populates a `TableView` with results, and exports to CSV.

#### 2. The Algorithm Layer (Strategy Pattern)
This package applies advanced Data Structures and Algorithms (DSA) to solve the core problem of assignment grading through coverage-based analysis.

*   **`SimilarityAlgorithm.java`**: Interface defining `calculateSimilarity(answerText, studentText)`.
    *   *Topic:* OOP Abstraction, Strategy Design Pattern.

*   **`StrictnessController.java`**: Routes to grouped algorithms based on strictness.
    *   *Topic:* Factory/Router Pattern. Determines which algorithms run at each level:
        *   **Easy**: KeywordCoverage only (quick keyword check)
        *   **Medium**: KeywordCoverage + ConceptMatch (keywords + phrase matching)
        *   **Hard**: KeywordCoverage + ConceptMatch + SequenceMatch (full deep analysis)
    *   The final score is computed using weighted averaging.

---

##### `KeywordCoverage.java` — Level 1 (Easy Strictness)

**What it checks:** Did the student use the **key words** from the answer sheet?

**Algorithm:** One-directional keyword coverage with stop-word filtering.

**How it works:**
1. Both the answer sheet and student text are split into lowercase words.
2. Common English stop words ("the", "is", "a", "and", etc.) are filtered out, leaving only meaningful content words (e.g., "algorithm", "binary", "tree").
3. The score is calculated as: **`(answer keywords found in student) / (total answer keywords) × 100`**
4. This is **one-directional** — only the answer sheet's word count is the denominator. If a student writes extra content beyond the answer, it does NOT reduce their score.

**Key Data Structure:** `HashSet<String>` for O(1) keyword lookup.

**Time Complexity:** O(n + m) where n = answer words, m = student words
**Space Complexity:** O(n + m) for the word sets

**Example:**
```
Answer sheet keywords:  {binary, search, tree, algorithm, traversal, inorder, data, structure}
Student's keywords:     {binary, search, tree, algorithm, data, structure, example, implementation}
Covered:                {binary, search, tree, algorithm, data, structure} → 6 out of 8
Score:                  6/8 × 100 = 75.00%
```

**Code (core logic):**
```java
Set<String> answerKeywords = extractKeywords(answerText);   // filter stop words
Set<String> studentKeywords = extractKeywords(studentText);
Set<String> covered = new HashSet<>(answerKeywords);
covered.retainAll(studentKeywords);                         // intersection
return ((double) covered.size() / answerKeywords.size()) * 100.0;
```

---

##### `ConceptMatch.java` — Level 2 (Medium Strictness)

**What it checks:** Did the student use similar **phrases** (3-word concepts) from the answer?

**Algorithm:** N-gram (trigram) overlap analysis with one-directional scoring.

**How it works:**
1. Both texts are normalized to lowercase with punctuation removed.
2. A sliding window of 3 consecutive words (trigrams) is generated from each text.
3. The score is calculated as: **`(answer 3-grams found in student) / (total answer 3-grams) × 100`**
4. Trigrams capture **conceptual phrases** — e.g., "binary search tree" is one concept. A student who mentions "binary search tree" understood the concept, while just having the words "binary", "search", and "tree" scattered randomly scores lower at this level.

**Key Data Structure:** `HashSet<String>` for O(1) trigram lookup.

**Time Complexity:** O(n + m) where n = answer words, m = student words
**Space Complexity:** O(n + m) for trigram sets

**Example:**
```
Answer text: "binary search tree traversal uses inorder method"
Answer 3-grams: {"binary search tree", "search tree traversal", "tree traversal uses", "traversal uses inorder", "uses inorder method"}
Student text: "binary search tree is efficient and search tree traversal works well"
Student 3-grams: {"binary search tree", "search tree is", "tree is efficient", "is efficient and", "efficient and search", "and search tree", "search tree traversal", "tree traversal works", "traversal works well"}
Covered: {"binary search tree", "search tree traversal"} → 2 out of 5
Score: 2/5 × 100 = 40.00%
```

**Code (core logic):**
```java
Set<String> answerNgrams = generateNgrams(answerText);   // 3-word sliding window
Set<String> studentNgrams = generateNgrams(studentText);
Set<String> covered = new HashSet<>(answerNgrams);
covered.retainAll(studentNgrams);                        // intersection
return ((double) covered.size() / answerNgrams.size()) * 100.0;
```

---

##### `SequenceMatch.java` — Level 3 (Hard Strictness)

**What it checks:** Did the student preserve the **correct order** of content from the answer?

**Algorithm:** Longest Common Subsequence (LCS) with one-directional scoring using a standard 2D Dynamic Programming table.

**How it works:**
1. Both text files are split into word arrays.
2. A 2D Dynamic Programming array `dp[ansLen + 1][stuLen + 1]` computes the Longest Common Subsequence (LCS) — the longest chain of words that appear in the same order in both documents.
3. The score is calculated as: **`LCS(answer, student) / answer_word_count × 100`**
4. Unlike traditional LCS that divides by `max(m, n)`, this divides by the **answer's word count** only. This means if a student writes the correct answer plus extra explanation, they still get ~100%.
5. This algorithm catches students who have the right keywords and concepts but arranged in a completely wrong order.

**Key Data Structure:** `int[][] dp` — 2D array representing the dynamic programming lookup grid.

**Time Complexity:** O(n × m) where n = answer words, m = student words
**Space Complexity:** O(n × m) for the 2D DP lookup table

**Example:**
```
Answer words:  ["data", "structures", "enable", "efficient", "algorithms"]
Student words: ["data", "structures", "are", "important", "for", "efficient", "algorithms"]
LCS: ["data", "structures", "efficient", "algorithms"] → length 4
Score: 4/5 × 100 = 80.00%
```

**Code (core logic):**
```java
int[][] dp = new int[ansLen + 1][stuLen + 1];
for (int i = 1; i <= ansLen; i++) {
    for (int j = 1; j <= stuLen; j++) {
        if (answerWords[i - 1].equals(studentWords[j - 1])) {
            dp[i][j] = dp[i - 1][j - 1] + 1;
        } else {
            dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
        }
    }
}
int lcsLength = dp[ansLen][stuLen];
return ((double) lcsLength / ansLen) * 100.0;  // divide by ANSWER length
```

---

#### 3. The Utilities Layer (Data Extraction, OCR & Batch Processing)
*   **`PDFExtractor.java`**:
    *   *Topic:* File I/O, Document Parsing.
    *   *How it works:* First tries to extract selectable text directly using PDFBox (fast path for typed PDFs). If no text is found (scanned or handwritten PDF), it sends the PDF to the RapidOCR server via `OCRClient` for recognition.
*   **`OCRClient.java`**:
    *   *Topic:* HTTP Client, Microservice Communication.
    *   *How it works:* Sends PDF file bytes to the Python OCR server at `localhost:5000/ocr` using Java's built-in `HttpClient`. Receives plain text back. Also has a health check method to verify the server is running. Timeout set to 10 minutes for large image PDFs.
*   **`BatchProcessor.java`**:
    *   *Topic:* Batch Processing, Callback Pattern.
    *   *How it works:* Takes one answer sheet and a list of student files. Extracts text from the answer sheet first, then processes each student file sequentially. Reports progress to the UI through a `ProgressCallback` interface.
*   **`ImageHashDetector.java`**:
    *   *Topic:* Cryptography (SHA-256), Matrix Traversal (Image Processing).
    *   *How it works (Mode 1):* Creates a SHA-256 cryptographic hash of image bytes. Uses a `HashSet` to detect exact duplicate images in O(1) lookup time.
    *   *How it works (Mode 2):* Performs a 256x256 grayscale pixel-by-pixel intensity comparison to find visual similarity.
*   **`CSVExporter.java`**:
    *   *Topic:* File I/O, Data Formatting.
    *   *How it works:* Takes a `BatchResult` and writes all scores to a CSV file with columns: S.No, Student File, Keyword Coverage %, Concept Match %, Sequence Match %, Assignment Score %, Verdict.

#### 4. The Data Models
*   **`SimilarityResult.java`**:
    *   *Topic:* Encapsulation. Stores the strictness level, individual algorithm scores (keywordCoverage, conceptMatch, sequenceMatch), and the final combined assignment score.
*   **`BatchResult.java`**:
    *   *Topic:* Collections, Aggregation. Holds a list of `SimilarityResult` objects for the entire batch plus helper methods for average score and excellent count.

#### 5. The OCR Microservice (Python)
*   **`ocr_server/ocr_server.py`**:
    *   *Topic:* REST API, Image Processing, OCR.
    *   *How it works:* A Flask server that receives PDF bytes via HTTP POST. Uses PyMuPDF to render each page as an image at 150 DPI, then runs RapidOCR (ONNX Runtime) to extract text (supports both typed and handwritten content). Returns plain text.
*   **`ocr_server/start_ocr.bat`**: Installs Python dependencies and starts the OCR server.

---

## 4. How the Data Flows (Execution Pipeline)

### Step-by-Step Workflow

1. **`GUIApp.java`** — Application starts and loads the Login screen (`Login.fxml`).
2. **`LoginController.java`** — Teacher enters credentials (`admin` / `1234`). On success, loads the Dashboard screen (`Dashboard.fxml`).
3. **`DashboardController.java`** — Teacher uploads one Answer Sheet PDF using the file chooser (`showOpenDialog`).
4. **`DashboardController.java`** — Teacher batch-selects up to 60 Student Assignment PDFs using the multi-file chooser (`showOpenMultipleDialog`).
5. **`DashboardController.java`** — Teacher selects a strictness level (Easy / Medium / Hard) and clicks "Check All Assignments". A background thread is started to keep the UI responsive.
6. **`BatchProcessor.java`** — Receives the answer sheet + student file list. Extracts text from the answer sheet first by calling `PDFExtractor`.
7. **`PDFExtractor.java`** — Tries to extract selectable text from the PDF using Apache PDFBox (`PDFTextStripper`). If text is found (typed PDF), returns it immediately.
8. **`OCRClient.java`** — If `PDFExtractor` finds no selectable text (scanned or handwritten PDF), it calls `OCRClient` which sends the PDF bytes over HTTP to the Python server at `localhost:5000/ocr`.
9. **`ocr_server.py`** — Receives the PDF bytes, renders each page as a 150 DPI image using PyMuPDF, runs RapidOCR on each page image, and returns the extracted text as plain text.
10. **`BatchProcessor.java`** — For each student file, repeats steps 7-9 to extract student text, then passes both texts to `StrictnessController`.
11. **`StrictnessController.java`** — Based on the selected strictness, runs the appropriate algorithms:
    - **Easy**: `KeywordCoverage.java` only
    - **Medium**: `KeywordCoverage.java` + `ConceptMatch.java`
    - **Hard**: `KeywordCoverage.java` + `ConceptMatch.java` + `SequenceMatch.java`
12. **`SimilarityResult.java`** — Stores the individual algorithm scores and the computed final assignment score.
13. **`BatchProcessor.java`** — Calls the `ProgressCallback` to report progress back to the UI. Adds the result to `BatchResult.java`.
14. **`DashboardController.java`** — Receives each result via `Platform.runLater()` and adds it to the `TableView`. Progress bar updates with each file processed.
15. **`DashboardController.java`** — When all files are done, displays the summary: total files graded and breakdown of Excellent/Good/Needs Work verdicts.
16. **`CSVExporter.java`** — When teacher clicks "Export CSV", writes all results to a CSV file at the location they choose in the Save dialog.

### Visual Workflow Diagram

```
  GUIApp.java                LoginController.java           DashboardController.java
  [App Start] ──────────────▶ [Login Screen] ──────────────▶ [Dashboard Screen]
                               admin / 1234                   |
                                                              |  Upload Answer Sheet (1 PDF)
                                                              |  Upload Student Files (up to 60 PDFs)
                                                              |  Select Strictness Level
                                                              |  Click "Check All"
                                                              ▼
                                                         BatchProcessor.java
                                                         [For each student file:]
                                                              |
                                                              ▼
                                                         PDFExtractor.java
                                                         [Try direct text extraction]
                                                              |
                                                     ┌───────┴────────┐
                                                     |                 |
                                                 Text found?       No text?
                                                 (typed PDF)     (scanned/handwritten)
                                                     |                 |
                                                     |            OCRClient.java
                                                     |            [Send PDF to server]
                                                     |                 |
                                                     |            ocr_server.py
                                                     |            [RapidOCR extracts text]
                                                     |                 |
                                                     └───────┬────────┘
                                                              |
                                                              ▼
                                                     StrictnessController.java
                                                     [Run algorithm(s) based on level]
                                                              |
                                              ┌───────────────┼───────────────┐
                                              |               |               |
                                       KeywordCoverage  ConceptMatch    SequenceMatch
                                       (Easy+Med+Hard) (Med+Hard only) (Hard only)
                                              |               |               |
                                              └───────────────┼───────────────┘
                                                              |
                                                              ▼
                                                     SimilarityResult.java
                                                     [Store scores + compute final]
                                                              |
                                                              ▼
                                                     DashboardController.java
                                                     [Add row to TableView]
                                                     [Update progress bar]
                                                              |
                                                              ▼
                                                     CSVExporter.java
                                                     [Export results to CSV file]
```

---

## 5. Where Files Are Stored
- **PDFs are NOT stored by the application.** They are read from wherever the user selects them on disk, processed in memory, and never copied or saved elsewhere.
- **CSV exports** are saved to whichever location the teacher chooses in the "Save As" dialog when clicking "Export CSV".
- **OCR temp files** are created in the system temp directory during processing and deleted immediately after OCR completes.

---

## 6. Algorithm Complexity Comparison

| Level | Algorithm | What It Checks | Time Complexity | Space Complexity |
|-------|-----------|---------------|----------------|-----------------|
| Easy | Keyword Coverage | Key terms from answer found in student's work | O(n+m) | O(n+m) |
| Medium | Concept Match | 3-word phrases from answer found in student's work | O(n+m) | O(n+m) |
| Hard | Sequence Match (LCS DP) | Answer word order preserved in student's work | O(n×m) | O(m) |

### Strictness Grouping & Scoring Formula

| Strictness | Algorithms Run | Final Score Calculation |
|------------|---------------|----------------------|
| Easy | KeywordCoverage only | Final = KeywordCoverage |
| Medium | KeywordCoverage + ConceptMatch | Final = Average of both (50/50) |
| Hard | KeywordCoverage + ConceptMatch + SequenceMatch | Final = 0.3×Keyword + 0.3×Concept + 0.4×Sequence |

### Why Coverage-Based (Not Plagiarism-Style)

All algorithms use a **one-directional** formula: they divide by the **answer sheet's** size, not the union or maximum of both documents. This ensures:
- ✅ Student covers all answer content → ~100%
- ✅ Student writes correct answer + extra detail → still ~100% (not penalized)
- ✅ Student covers half the answer → ~50%
- ✅ Student writes unrelated content → ~0%

### Verdict Thresholds

| Score Range | Verdict | Meaning |
|-------------|---------|---------|
| ≥ 80% | EXCELLENT | Student demonstrated strong understanding |
| 50% – 79% | GOOD | Student covered most key concepts |
| < 50% | NEEDS WORK | Student missed significant content |

---

## 7. How to Run

### Step 1: Start the OCR Server (required for handwritten PDFs)
```bash
cd Smart_Assignment_Checker
python ocr_server/ocr_server.py
```
Keep this terminal window open while using the application.

### Step 2: Build and Run the Java Application
```bash
set JAVA_HOME=D:\Program Files\Java\jdk-23
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd javafx:run
```

### Step 3: Login
- **Username:** `admin`
- **Password:** `1234`
