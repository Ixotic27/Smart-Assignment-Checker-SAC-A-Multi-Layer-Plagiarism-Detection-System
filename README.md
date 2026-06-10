# Smart Assignment Checker (SAC) - A Multi-Layer Grading & Plagiarism Detection System

## Overview
The **Smart Assignment Checker (SAC)** is a JavaFX-based desktop application designed to streamline the grading of student assignments. By comparing student submissions against an official answer sheet, the system calculates strictness-based coverage scores. 

SAC supports both **typed PDFs** (via direct text extraction using Apache PDFBox 3.x) and **scanned or handwritten PDFs** (via a lightweight Python OCR microservice powered by RapidOCR and PyMuPDF). 

Additionally, the system performs **image duplicate detection** using SHA-256 cryptographic hashing to identify copy-pasted diagrams/images across assignments.

---

## Login Credentials
Access to the dashboard is protected by a login screen:
*   **Username:** `admin`
*   **Password:** `1234`

---

## Core Features
1. **Direct PDF Parsing**: Extracts text directly from digitally generated PDFs.
2. **Handwriting & Scan OCR Fallback**: Automatically invokes a local Python OCR microservice if no selectable text is found in the PDF.
3. **Image Deduplication**: Extracts embedded images, generates SHA-256 hashes, and detects exact copy-pasted diagrams in $O(1)$ time.
4. **Three Strictness Levels (Coverage-based algorithms)**:
    *   **Easy — Keyword Coverage**: Checks if meaningful key terms from the answer sheet are present in the student's submission (filtering out common stop words).
    *   **Medium — Concept Match**: Checks keywords and captures conceptual phrases using a 3-word sliding window (trigrams) comparison.
    *   **Hard — Sequence Match**: Checks keywords, phrases, and correct chronological order using a space-optimized 1D array Dynamic Programming implementation of the Longest Common Subsequence (LCS).
5. **Batch Processing**: Grades up to 60 student assignments in a single batch on a background thread to keep the JavaFX UI fully responsive.
6. **Detailed Reporting**: Displays individual algorithm scores, final scores, and visual verdicts (Excellent, Good, Needs Work) with full CSV export support.

---

## Project Structure
```text
Smart_Assignment_Checker/
├── src/main/java/com/sac/
│   ├── GUIApp.java                    # JavaFX Application entry point
│   ├── LoginController.java           # Login screen validation logic
│   ├── DashboardController.java       # Dashboard UI event handling & background threads
│   ├── Main.java                      # CLI/GUI entry point router
│   ├── algorithms/
│   │   ├── SimilarityAlgorithm.java   # Common interface for algorithms
│   │   ├── KeywordCoverage.java       # Level 1: One-directional keyword coverage (Easy)
│   │   ├── ConceptMatch.java          # Level 2: 3-word trigram concept overlap (Medium)
│   │   ├── SequenceMatch.java         # Level 3: Space-optimized LCS sequence ordering (Hard)
│   │   └── StrictnessController.java  # Strictness level router & weighted scoring logic
│   ├── models/
│   │   ├── SimilarityResult.java      # Model containing score breakdowns for one student
│   │   └── BatchResult.java           # Model for aggregate stats and verdicts
│   └── utils/
│       ├── PDFExtractor.java          # Parser attempting direct text extraction before OCR
│       ├── ImageHashDetector.java     # SHA-256 image hashes & pixel similarity checker
│       ├── OCRClient.java             # HTTP client connecting Java backend to Python OCR
│       ├── BatchProcessor.java        # Sequential batch execution manager
│       └── CSVExporter.java           # CSV report generator
├── src/main/resources/views/
│   ├── Login.fxml                     # Login view FXML layout
│   ├── Dashboard.fxml                 # Dashboard view FXML layout
│   └── styles.css                     # Premium styling with standard property equivalents
├── ocr_server/                        # Python OCR Microservice
│   ├── ocr_server.py                  # Flask web service rendering PDFs and running RapidOCR
│   ├── requirements.txt               # Python package list (Flask, rapidocr-onnxruntime, fitz)
│   └── start_ocr.bat                  # Batch script to setup environment and start OCR
├── pom.xml                            # Maven project build configuration
├── PROJECT_DOCUMENTATION.md           # Extensive system design & algorithm documentation
└── run.bat                            # Windows launch script for the application
```

---

## Technologies Used
*   **Java 17 / 21** (Core System)
*   **JavaFX 17** (Desktop Graphical User Interface)
*   **Apache PDFBox 3.0.1** (PDF Parsing and image stream extraction)
*   **Maven** (Dependency and build management)
*   **Python 3.10+** (OCR Microservice runtime)
*   **RapidOCR (ONNX Runtime)** (Lightweight, high-accuracy printed/handwritten text recognition)
*   **PyMuPDF / Flask** (PDF page rendering at 150 DPI and HTTP web routing)

---

## How to Set Up and Run

> [!IMPORTANT]
> Because OCR runs on a local microservice, you **must** start the OCR server first if you plan to analyze scanned or handwritten PDFs.

### Step 1: Start the OCR Microservice
Open a terminal in the project directory and run:
```bash
cd ocr_server
start_ocr.bat
```
*(This script will set up a virtual environment, install dependencies from `requirements.txt`, and start the server on `http://localhost:5000`)*

### Step 2: Build and Run the Desktop App
Open another terminal in the root of the project directory and run:
```powershell
# Set JDK Path (Adjust if your JDK is installed elsewhere)
$env:JAVA_HOME="D:\Program Files\Java\jdk-23"

# Compile and package the Java project
.\mvnw.cmd clean package -DskipTests

# Run the JavaFX Application
.\mvnw.cmd javafx:run
```

Alternatively, you can run the pre-built launch script on Windows:
```cmd
run.bat
```

---

## Algorithm & Grading Reference

### Complexity & Scoring Matrix
All algorithms utilize a **one-directional coverage metric** (dividing by the size of the answer sheet). This prevents students from being penalized for writing extra context or descriptive explanations.

| Strictness Level | Algorithm | Logic Focus | Time Complexity | Space Complexity |
| :--- | :--- | :--- | :--- | :--- |
| **Easy** | Keyword Coverage | Key content words found (excluding stop words like *the, is, an*). | $O(n + m)$ | $O(n + m)$ |
| **Medium** | Concept Match | Key phrases and vocabulary combinations (3-word trigrams). | $O(n + m)$ | $O(n + m)$ |
| **Hard** | Sequence Match | Ordered content alignment using space-optimized LCS. | $O(n \times m)$ | $O(\min(n, m))$ |

### Score Calculation Formulas
Based on the strictness slider in the UI, SAC computes the final assignment score:
*   **Easy Level**:
    $$\text{Final Score} = \text{Keyword Coverage}$$
*   **Medium Level**:
    $$\text{Final Score} = \frac{\text{Keyword Coverage} + \text{Concept Match}}{2}$$
*   **Hard Level**:
    $$\text{Final Score} = (0.3 \times \text{Keyword Coverage}) + (0.3 \times \text{Concept Match}) + (0.4 \times \text{Sequence Match})$$

### Verdict Thresholds
Each assignment is categorized into one of three verdicts depending on the final score:

| Score Range | Verdict | Actionable Meaning |
| :--- | :--- | :--- |
| **$\ge 80\%$** | <span style="color:#2ecc71; font-weight:bold;">EXCELLENT</span> | Strong understanding; comprehensive coverage of the answer sheet. |
| **$50\% \text{ to } 79\%$** | <span style="color:#f1c40f; font-weight:bold;">GOOD</span> | Covered most core concepts but missed secondary details. |
| **$< 50\%$** | <span style="color:#e74c3c; font-weight:bold;">NEEDS WORK</span> | Significant portions of the answer sheet are missing or wrong. |

---

## CSV Export Structure
When batch checking completes, teachers can export all metrics into a spreadsheet. The CSV layout includes:
1. `S.No` - Serial number of the student file.
2. `Student File` - File name of the graded PDF.
3. `Keyword Coverage %` - Level 1 score.
4. `Concept Match %` - Level 2 score.
5. `Sequence Match %` - Level 3 score (blank if Easy/Medium check was chosen).
6. `Assignment Score %` - Final weighted score.
7. `Verdict` - Final categorical verdict based on score thresholds.
