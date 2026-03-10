# Smart Assignment Checker (SAC) - A Multi-Layer Plagiarism Detection System

## Overview
Plagiarism in student assignments has become a major academic concern. Traditional plagiarism detection systems are either too strict (high computation cost) or too weak (low detection accuracy). Many rely on basic keyword matching that fails against structural similarity or paraphrased content.

The **Smart Assignment Checker (SAC)** is a Java-based multi-layer plagiarism detection system that applies different Design and Analysis of Algorithms (DAA) techniques to detect duplicate content in student assignment PDFs. By combining hashing, string matching, set operations, and dynamic programming, the system demonstrates practical real-world applications of DAA concepts.

## Core Features
- **PDF Extraction:** Extracts text and embedded images from assignment PDFs using Apache PDFBox.
- **Image Duplicate Detection:** Converts images to byte arrays and generates SHA-256 hashes, stored in a HashSet for $O(1)$ duplicate lookup time.
- **Multi-Level Text Similarity Layers:**
  - **Level 1 (Low Strictness) - Jaccard Similarity:** Converts text into word sets and computes intersection/union. Average $O(n+m)$ time complexity.
  - **Level 2 (Medium Strictness) - Rabin-Karp Algorithm:** Uses a rolling hash for pattern matches on chunked text. Average $O(n+m)$ time complexity.
  - **Level 3 (High Strictness) - Longest Common Subsequence (LCS):** Applies Dynamic Programming to deeply analyze exact sequence structural similarity. $O(n \times m)$ time complexity.
- **Strictness Control System:** Allows faculty members to select the algorithm strictness, ensuring computationally expensive algorithms like DP are executed only when necessary.

## Deliverables
1. Fully working Java application for detecting plagiarised student submissions.
2. Implementation of 3 advanced text similarity algorithms.
3. Complexity comparison analysis report between layers.

## Technologies Used
- **Java** (Core System)
- **Apache PDFBox** (Document Processing)

## Setup
*(Instructions to follow once core implementation is ready)*
