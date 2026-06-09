# Smart Assignment Checker - Comprehensive Project Documentation

## 1. Project Overview
**Smart Assignment Checker (SAC)** is a desktop application designed to detect plagiarism in student assignments. A teacher uploads one **answer sheet PDF** and up to **60 student assignment PDFs**, and the system compares each student's work against the answer sheet using multiple algorithms. It supports both typed and handwritten PDFs through OCR (Optical Character Recognition) and provides results in a table with CSV export.

---

## 2. Technology Stack & Rationale

| Technology | Why it was used | What it does | Where it applies |
| :--- | :--- | :--- | :--- |
| **Java 21** | Core programming language. Chosen for its robust standard library, strong object-oriented design, and excellent performance for algorithmic data processing. | Powers the entire backend logic, algorithms, and application flow. | Across all `.java` files in `src/main/java`. |
| **JavaFX 17** | UI Framework. Chosen because it provides a modern, hardware-accelerated GUI toolkit for Java desktop applications that separates logic from design via FXML. | Renders the login screen, dashboard, file choosers, progress bar, results table, and CSV export. | `GUIApp.java`, `LoginController.java`, `DashboardController.java`, and `.fxml` files. |
| **Maven** | Build and Dependency Management. Chosen to automatically handle external libraries (PDFBox) and build a standalone executable `.jar` file. | Downloads dependencies, compiles code, and packages the application. | `pom.xml` |
| **Apache PDFBox (v3.x)** | PDF Parsing Library. Chosen because it is the industry standard open-source library for extracting text and raw image byte streams from PDF documents. | Reads user-uploaded PDFs, strips text, and isolates embedded PNG/JPG objects. | `utils/PDFExtractor.java` |
| **PaddleOCR** | Optical Character Recognition (OCR). Chosen for its high accuracy on both printed and handwritten text. Runs as a Python microservice that the Java app communicates with over HTTP. | Converts images of handwritten or scanned text into strings so the algorithms can process them. | `ocr_server/ocr_server.py` + `utils/OCRClient.java` |
| **Flask + PyMuPDF** | Python web framework and PDF renderer. Flask provides the HTTP server for OCR requests. PyMuPDF renders PDF pages into images at 300 DPI for OCR processing. | Powers the OCR microservice that PaddleOCR runs inside. | `ocr_server/ocr_server.py` |

---

## 3. Project Architecture & File Breakdown

The project follows the **MVC (Model-View-Controller)** design pattern and the **Strategy Pattern** for its algorithms.

### Directory Structure:
```text
Smart_Assignment_Checker/
├── src/main/
│   ├── java/com/sac/
│   │   ├── algorithms/             (Core Similarity Algorithms)
│   │   │   ├── SimilarityAlgorithm.java
│   │   │   ├── Jacard.java
│   │   │   ├── RabinKarp.java
│   │   │   ├── LCS.java
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
This package applies advanced Data Structures and Algorithms (DSA) to solve the core problem of plagiarism detection.

*   **`SimilarityAlgorithm.java`**: Interface defining `calculateSimilarity(text1, text2)`.
    *   *Topic:* OOP Abstraction, Strategy Design Pattern.
*   **`StrictnessController.java`**: Routes to grouped algorithms based on strictness.
    *   *Topic:* Factory/Router Pattern. Determines which algorithms run at each level:
        *   **Easy**: Jaccard only (fast screening)
        *   **Medium**: Jaccard + Rabin-Karp (adds structural matching)
        *   **Hard**: Jaccard + Rabin-Karp + LCS (full deep analysis)
    *   The final score is the average of all algorithms run at that level.
*   **`Jacard.java` (Easy Strictness)**:
    *   *Topic:* Set Theory, HashSets.
    *   *How it works:* Splits text into words, puts them in a `HashSet`, and calculates the Intersection over Union. (O(n+m) complexity). Excellent for finding vocabulary overlap without caring about sequence.
*   **`RabinKarp.java` (Medium Strictness)**:
    *   *Topic:* String Matching, Rolling Hash.
    *   *How it works:* Groups words into 5-word chunks. Computes a rolling hash for each chunk. If the hashes match, a plagiarism block is found. (O(n+m) complexity). Excellent for finding exact copy-pasted sentences hidden inside rewritten essays.
*   **`LCS.java` (Hard Strictness)**:
    *   *Topic:* Dynamic Programming (DP), Longest Common Subsequence.
    *   *How it works:* Builds a 1D optimized DP array to track the longest sequential chain of matching words between two documents. (O(n * m) complexity). Excellent for defeating "clever" plagiarism where a student deletes or changes every 5th word to trick basic checkers.

#### 3. The Utilities Layer (Data Extraction, OCR & Batch Processing)
*   **`PDFExtractor.java`**:
    *   *Topic:* File I/O, Document Parsing.
    *   *How it works:* First tries to extract selectable text directly using PDFBox (fast path for typed PDFs). If no text is found (scanned or handwritten PDF), it sends the PDF to the PaddleOCR server via `OCRClient` for recognition.
*   **`OCRClient.java`**:
    *   *Topic:* HTTP Client, Microservice Communication.
    *   *How it works:* Sends PDF file bytes to the Python OCR server at `localhost:5000/ocr` using Java's built-in `HttpClient`. Receives plain text back. Also has a health check method to verify the server is running.
*   **`BatchProcessor.java`**:
    *   *Topic:* Batch Processing, Callback Pattern.
    *   *How it works:* Takes one answer sheet and a list of student files. Extracts text from the answer sheet first, then processes each student file sequentially. Reports progress to the UI through a `ProgressCallback` interface.
*   **`ImageHashDetector.java`**:
    *   *Topic:* Cryptography (SHA-256), Matrix Traversal (Image Processing).
    *   *How it works (Mode 1):* Creates a SHA-256 cryptographic hash of image bytes. Uses a `HashSet` to detect exact duplicate images in O(1) lookup time.
    *   *How it works (Mode 2):* Performs a 256x256 grayscale pixel-by-pixel intensity comparison to find visual similarity.
*   **`CSVExporter.java`**:
    *   *Topic:* File I/O, Data Formatting.
    *   *How it works:* Takes a `BatchResult` and writes all scores to a CSV file with columns: S.No, Student File, Jaccard %, Rabin-Karp %, LCS %, Final Score %, Verdict.

#### 4. The Data Models
*   **`SimilarityResult.java`**:
    *   *Topic:* Encapsulation. Stores the algorithm used, the strictness level, individual algorithm scores, and the final combined score.
*   **`BatchResult.java`**:
    *   *Topic:* Collections, Aggregation. Holds a list of `SimilarityResult` objects for the entire batch plus helper methods for average score and flagged count.

#### 5. The OCR Microservice (Python)
*   **`ocr_server/ocr_server.py`**:
    *   *Topic:* REST API, Image Processing, OCR.
    *   *How it works:* A Flask server that receives PDF bytes via HTTP POST. Uses PyMuPDF to render each page as an image at 300 DPI, then runs PaddleOCR to extract text (supports both typed and handwritten content). Returns plain text.
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
9. **`ocr_server.py`** — Receives the PDF bytes, renders each page as a 300 DPI image using PyMuPDF, runs PaddleOCR on each page image, and returns the extracted text as plain text.
10. **`BatchProcessor.java`** — For each student file, repeats steps 7-9 to extract student text, then passes both texts to `StrictnessController`.
11. **`StrictnessController.java`** — Based on the selected strictness, runs the appropriate algorithms:
    - **Easy**: `Jacard.java` only
    - **Medium**: `Jacard.java` + `RabinKarp.java`
    - **Hard**: `Jacard.java` + `RabinKarp.java` + `LCS.java`
12. **`SimilarityResult.java`** — Stores the individual algorithm scores and the computed final score (average of all algorithms run).
13. **`BatchProcessor.java`** — Calls the `ProgressCallback` to report progress back to the UI. Adds the result to `BatchResult.java`.
14. **`DashboardController.java`** — Receives each result via `Platform.runLater()` and adds it to the `TableView`. Progress bar updates with each file processed.
15. **`DashboardController.java`** — When all files are done, displays the summary: total files analyzed and number flagged as high similarity.
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
                                                     |            [PaddleOCR extracts text]
                                                     |                 |
                                                     └───────┬────────┘
                                                              |
                                                              ▼
                                                     StrictnessController.java
                                                     [Run algorithm(s) based on level]
                                                              |
                                              ┌───────────────┼───────────────┐
                                              |               |               |
                                         Jacard.java    RabinKarp.java    LCS.java
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

| Level | Algorithm | Time Complexity | Space Complexity | Detection Approach |
|-------|-----------|----------------|-----------------|-------------------|
| Easy | Jaccard Similarity | O(n+m) | O(n+m) | Word-set overlap (unordered) |
| Medium | Rabin-Karp | O(n+m) | O(n+m) | Rolling hash chunk matching |
| Hard | LCS (DP) | O(n*m) | O(m) | Structural subsequence analysis |

### Strictness Grouping

| Strictness | Algorithms Run | Final Score Calculation |
|------------|---------------|----------------------|
| Easy | Jaccard only | Final = Jaccard |
| Medium | Jaccard + Rabin-Karp | Final = Average of both |
| Hard | Jaccard + Rabin-Karp + LCS | Final = Average of all three |

---

## 7. How to Run

### Step 1: Start the OCR Server (required for handwritten PDFs)
```bash
cd ocr_server
start_ocr.bat
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
