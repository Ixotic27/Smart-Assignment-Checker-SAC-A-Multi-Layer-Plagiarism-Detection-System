# Smart Assignment Checker (SAC) - A Multi-Layer Plagiarism Detection System

## Overview
The **Smart Assignment Checker (SAC)** is a Java-based multi-layer plagiarism detection system that applies different Design and Analysis of Algorithms (DAA) techniques to detect duplicate content in student assignment PDFs. By combining hashing, string matching, set operations, and dynamic programming, the system demonstrates practical real-world applications of DAA concepts.

## Login Credentials
- **Username:** `admin`
- **Password:** `1234`

## Core Features
- **PDF Extraction:** Extracts text and embedded images from assignment PDFs using Apache PDFBox 3.x.
- **Image Duplicate Detection:** Converts images to byte arrays and generates SHA-256 hashes, stored in a HashSet for O(1) duplicate lookup time.
- **Three Strictness Levels (each uses a different algorithm):**
  - **Easy — Jaccard Similarity:** Converts text into word sets and computes intersection/union. Time: O(n+m)
  - **Medium — Rabin-Karp Algorithm:** Uses a rolling hash for pattern matches on 5-word chunks. Time: O(n+m)
  - **Hard — Longest Common Subsequence (LCS):** Applies Dynamic Programming to analyze structural similarity. Time: O(n × m)

## Project Structure
```
Smart_Assignment_Checker/
├── src/main/java/com/sac/
│   ├── GUIApp.java                    # JavaFX Application entry point
│   ├── LoginController.java           # Login screen with credential validation
│   ├── DashboardController.java       # Dashboard: 2-file upload + analysis
│   ├── Main.java                      # CLI entry point
│   ├── TestHarness.java               # Test PDF generator + validation
│   ├── algorithms/
│   │   ├── SimilarityAlgorithm.java   # Algorithm interface
│   │   ├── Jacard.java                # Level 1: Jaccard Similarity
│   │   ├── RabinKarp.java             # Level 2: Rabin-Karp Rolling Hash
│   │   ├── LCS.java                   # Level 3: LCS Dynamic Programming
│   │   └── StrictnessController.java  # Routes Easy/Medium/Hard to algorithm
│   ├── models/
│   │   └── SimilarityResult.java      # Result model with report formatter
│   └── utils/
│       ├── PDFExtractor.java          # PDF text + image extraction (PDFBox 3.x)
│       └── ImageHashDetector.java     # SHA-256 image hash + HashSet dedup
├── src/main/resources/views/
│   ├── Login.fxml                     # Login screen UI
│   └── Dashboard.fxml                 # Dashboard UI (2 uploads + results)
├── pom.xml                            # Maven config with all dependencies
└── run.bat                            # Windows launch script
```

## Technologies Used
- **Java 17+** (Core System)
- **Apache PDFBox 3.0.1** (PDF Processing)
- **JavaFX 17** (GUI)
- **Maven** (Build System)

## How to Build
```bash
# Set JAVA_HOME
set JAVA_HOME=D:\Program Files\Java\jdk-23

# Build fat JAR with all dependencies
.\mvnw.cmd clean package -DskipTests
```

## How to Run

### Option 1: Using the launch script
```
run.bat
```

### Option 2: Using Maven
```bash
set JAVA_HOME=D:\Program Files\Java\jdk-23
.\mvnw.cmd javafx:run
```

### Option 3: Using Java directly
```bash
# Build classpath
.\mvnw.cmd dependency:build-classpath -Dmdep.outputFile=target/cp.txt

# Run GUI
java --module-path "C:\javafx\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes;%CP%" com.sac.GUIApp

# Run Test Harness (CLI)
java -cp "target\classes;%CP%" com.sac.TestHarness
```

## Algorithm Complexity Comparison

| Level | Algorithm | Time Complexity | Space Complexity | Detection Approach |
|-------|-----------|----------------|-----------------|-------------------|
| Easy | Jaccard Similarity | O(n+m) | O(n+m) | Word-set overlap (unordered) |
| Medium | Rabin-Karp | O(n+m) | O(n+m) | Rolling hash chunk matching |
| Hard | LCS (DP) | O(n×m) | O(m) | Structural subsequence analysis |
