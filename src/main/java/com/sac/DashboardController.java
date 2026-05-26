package com.sac;

import com.sac.algorithms.StrictnessController;
import com.sac.models.SimilarityResult;
import com.sac.utils.ImageHashDetector;
import com.sac.utils.PDFExtractor;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.File;
import java.util.List;

/**
 * DashboardController - Connects the UI to the plagiarism detection engine.
 * Handles two PDF uploads, invokes text extraction, image dedup, and
 * routes through the StrictnessController for multi-layer analysis.
 */
public class DashboardController {

    @FXML private Label selectedFile1Label;
    @FXML private Label selectedFile2Label;
    @FXML private ComboBox<String> strictnessComboBox;
    @FXML private TextArea outputArea;
    @FXML private ProgressBar progressBar;

    private File selectedFile1;
    private File selectedFile2;
    private static File lastVisitedDirectory;

    private final StrictnessController controller = new StrictnessController();

    @FXML
    public void initialize() {
        strictnessComboBox.setItems(FXCollections.observableArrayList(
            "Easy", "Medium", "Hard"
        ));
        strictnessComboBox.setValue("Easy");
    }

    @FXML
    public void handleUploadPDF1(ActionEvent event) {
        selectedFile1 = chooseFile(event);
        if (selectedFile1 != null) {
            selectedFile1Label.setText("✓ " + selectedFile1.getName());
            selectedFile1Label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            selectedFile1Label.setText("No file selected");
            selectedFile1Label.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    @FXML
    public void handleUploadPDF2(ActionEvent event) {
        selectedFile2 = chooseFile(event);
        if (selectedFile2 != null) {
            selectedFile2Label.setText("✓ " + selectedFile2.getName());
            selectedFile2Label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            selectedFile2Label.setText("No file selected");
            selectedFile2Label.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    private File chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        if (lastVisitedDirectory != null && lastVisitedDirectory.exists()) {
            fileChooser.setInitialDirectory(lastVisitedDirectory);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            lastVisitedDirectory = file.getParentFile();
        }
        
        return file;
    }

    @FXML
    public void handleCheckSimilarity() {
        // Validate both files are uploaded
        if (selectedFile1 == null || selectedFile2 == null) {
            outputArea.setText("⚠ Please upload BOTH PDF files before checking similarity.");
            return;
        }

        String strictness = strictnessComboBox.getValue();
        outputArea.setText("⏳ Analyzing documents...\n");
        progressBar.setVisible(true);
        progressBar.setProgress(0.0);

        // Run analysis on a background thread to keep UI responsive
        new Thread(() -> {
            try {
                // Step 1: Extract text (with OCR fallback)
                updateUI("  → Step 1/4: Extracting text from PDFs...\n", 0.10);
                Thread.sleep(500);
                String text1 = PDFExtractor.extractText(selectedFile1);
                if (text1.isEmpty()) {
                    updateUI("    ⤷ No selectable text in Doc1 — running OCR...\n", 0.15);
                }
                String text2 = PDFExtractor.extractText(selectedFile2);
                if (text2.isEmpty()) {
                    updateUI("    ⤷ No selectable text in Doc2 — running OCR...\n", 0.20);
                }
                if (!text1.isEmpty() || !text2.isEmpty()) {
                    updateUI("    ✓ Text extracted successfully (" + countWords(text1) + " + " + countWords(text2) + " words)\n", 0.25);
                }

                // Step 2: Extract images
                updateUI("  → Step 2/4: Extracting embedded images...\n", 0.30);
                Thread.sleep(600);
                List<byte[]> images1 = PDFExtractor.extractImages(selectedFile1);
                List<byte[]> images2 = PDFExtractor.extractImages(selectedFile2);

                // Step 3: Run comparison
                updateUI("  → Step 3/4: Running " + strictness + " analysis...\n", 0.50);
                Thread.sleep(800);

                ImageHashDetector.ImageDuplicateResult hashResult =
                    ImageHashDetector.detectDuplicates(images1, images2);

                // Handle image-only PDFs (no extractable text)
                if (text1.isEmpty() && text2.isEmpty()) {
                    updateUI("  → Step 3/4: No text found — switching to visual image comparison...\n", 0.60);
                    Thread.sleep(600);

                    updateUI("  → Step 4/4: Comparing images pixel-by-pixel (256×256 grayscale)...\n", 0.75);
                    Thread.sleep(500);

                    double pixelSimilarity = ImageHashDetector.compareBestMatch(images1, images2);

                    updateUI("  → Generating report...\n", 0.90);
                    Thread.sleep(400);

                    final double finalPixelSim = pixelSimilarity;
                    Platform.runLater(() -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append("═══════════════════════════════════════════════\n");
                        sb.append("  PLAGIARISM ANALYSIS REPORT\n");
                        sb.append("═══════════════════════════════════════════════\n\n");
                        sb.append("  Document 1: ").append(selectedFile1.getName()).append("\n");
                        sb.append("  Document 2: ").append(selectedFile2.getName()).append("\n");
                        sb.append("  Strictness: ").append(strictness).append("\n\n");
                        sb.append("───────────────────────────────────────────────\n");
                        sb.append("  ℹ PDFs contain images only (no selectable text).\n");
                        sb.append("  → Analysis Mode: Visual Image Comparison\n");
                        sb.append("───────────────────────────────────────────────\n\n");

                        sb.append("  ALGORITHM RESULTS\n");
                        sb.append("───────────────────────────────────────────────\n\n");

                        if (!images1.isEmpty() && !images2.isEmpty()) {
                            // SHA-256 exact match
                            sb.append(String.format("  ▸ SHA-256 Hash Match:          %6.2f%%\n", hashResult.getDuplicatePercentage()));
                            sb.append("    Method: Cryptographic hash comparison\n");
                            sb.append(String.format("    (Exact duplicates: %d of %d images)\n\n",
                                hashResult.getDuplicateCount(),
                                Math.max(images1.size(), images2.size())));

                            // Pixel comparison
                            sb.append(String.format("  ▸ Pixel Visual Similarity:     %6.2f%%\n", finalPixelSim));
                            sb.append("    Method: 256×256 grayscale pixel comparison\n");
                            sb.append("    (Tolerance threshold: 30/255 per pixel)\n\n");

                            // Final score = weighted: 40% hash + 60% pixel
                            double finalScore = (hashResult.getDuplicatePercentage() * 0.4) + (finalPixelSim * 0.6);
                            sb.append("───────────────────────────────────────────────\n");
                            sb.append(String.format("  ★ FINAL SIMILARITY SCORE:      %6.2f%%\n", finalScore));
                            sb.append("    (40%% Hash Match + 60%% Pixel Similarity)\n");
                            sb.append("───────────────────────────────────────────────\n\n");

                            if (finalScore >= 75) {
                                sb.append("  ⚠ VERDICT: HIGH PLAGIARISM DETECTED\n");
                            } else if (finalScore >= 40) {
                                sb.append("  ⚠ VERDICT: MODERATE SIMILARITY — Manual review advised\n");
                            } else {
                                sb.append("  ✓ VERDICT: LOW SIMILARITY — Likely original work\n");
                            }
                        } else {
                            sb.append("  ⚠ No images could be extracted from one or both PDFs.\n");
                        }

                        sb.append("═══════════════════════════════════════════════\n");
                        outputArea.setText(sb.toString());
                        progressBar.setProgress(1.0);
                        progressBar.setVisible(false);
                    });
                    return;
                }

                // Text-based PDFs — run algorithm via StrictnessController
                updateUI("  → Step 4/4: Computing " + strictness + " algorithm scores...\n", 0.70);
                Thread.sleep(600);

                SimilarityResult result = controller.analyze(
                    text1, text2, strictness,
                    selectedFile1.getName(), selectedFile2.getName()
                );

                updateUI("  → Generating report...\n", 0.90);
                Thread.sleep(400);

                // Display results
                Platform.runLater(() -> {
                    outputArea.setText(result.toReport());

                    // Append image analysis if images exist
                    if (images1.size() + images2.size() > 0) {
                        outputArea.appendText("\n───────────────────────────────────────────────\n");
                        outputArea.appendText("  IMAGE DUPLICATE ANALYSIS (SHA-256)\n");
                        outputArea.appendText("───────────────────────────────────────────────\n");
                        outputArea.appendText("  " + hashResult.toString() + "\n");
                    } else {
                        outputArea.appendText("\n  ℹ No embedded images found in either document.\n");
                    }

                    progressBar.setProgress(1.0);
                    progressBar.setVisible(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    outputArea.setText("❌ Error during analysis:\n" + e.getMessage());
                    progressBar.setVisible(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /**
     * Helper to update both the output area and progress bar from a background thread.
     */
    private void updateUI(String message, double progress) {
        Platform.runLater(() -> {
            outputArea.appendText(message);
            progressBar.setProgress(progress);
        });
    }

    /**
     * Counts the number of words in a string.
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }
}
