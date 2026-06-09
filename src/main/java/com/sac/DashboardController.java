package com.sac;

import com.sac.models.BatchResult;
import com.sac.models.SimilarityResult;
import com.sac.utils.BatchProcessor;
import com.sac.utils.CSVExporter;
import com.sac.utils.OCRClient;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

// Main controller for the Dashboard screen.
// Handles file uploads, batch processing, table display, and CSV export.
public class DashboardController {

    // UI elements linked to Dashboard.fxml
    @FXML private Label answerSheetLabel;
    @FXML private Label studentFilesLabel;
    @FXML private ComboBox<String> strictnessComboBox;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Button checkButton;
    @FXML private Button exportButton;

    // Table and its columns
    @FXML private TableView<SimilarityResult> resultsTable;
    @FXML private TableColumn<SimilarityResult, String> colIndex;
    @FXML private TableColumn<SimilarityResult, String> colFileName;
    @FXML private TableColumn<SimilarityResult, String> colJaccard;
    @FXML private TableColumn<SimilarityResult, String> colRabinKarp;
    @FXML private TableColumn<SimilarityResult, String> colLCS;
    @FXML private TableColumn<SimilarityResult, String> colFinal;
    @FXML private TableColumn<SimilarityResult, String> colVerdict;

    // Uploaded files
    private File answerSheetFile;
    private List<File> studentFiles;

    // Stores the last batch result for CSV export
    private BatchResult lastBatchResult;

    // Remembers the last folder the user browsed
    private static File lastDirectory;

    // Observable list backing the table
    private ObservableList<SimilarityResult> tableData = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        // Set up the strictness dropdown options
        strictnessComboBox.setItems(FXCollections.observableArrayList("Easy", "Medium", "Hard"));
        strictnessComboBox.setValue("Easy");

        // Connect the table to its data list
        resultsTable.setItems(tableData);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();

        // Disable export until there are results
        exportButton.setDisable(true);
        progressBar.setVisible(false);

        // Warn if the OCR server is not running
        if (!OCRClient.isServerRunning()) {
            statusLabel.setText("Note: OCR server not running. Start it for handwritten PDF support.");
        }
    }


    // Tells each table column where to get its data from SimilarityResult
    private void setupTableColumns() {

        // Row number column
        colIndex.setCellValueFactory(data -> {
            int index = tableData.indexOf(data.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });

        // Student file name column
        colFileName.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDoc2())
        );

        // Jaccard score column - shows "-" if algorithm was not run
        colJaccard.setCellValueFactory(data -> {
            double val = data.getValue().getJaccardSimilarity();
            String display = val >= 0 ? String.format("%.2f%%", val) : "-";
            return new SimpleStringProperty(display);
        });

        // Rabin-Karp score column
        colRabinKarp.setCellValueFactory(data -> {
            double val = data.getValue().getRabinKarpSimilarity();
            String display = val >= 0 ? String.format("%.2f%%", val) : "-";
            return new SimpleStringProperty(display);
        });

        // LCS score column
        colLCS.setCellValueFactory(data -> {
            double val = data.getValue().getLcsSimilarity();
            String display = val >= 0 ? String.format("%.2f%%", val) : "-";
            return new SimpleStringProperty(display);
        });

        // Final combined score column
        colFinal.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("%.2f%%", data.getValue().getSimilarityScore()))
        );

        // Verdict column based on final score thresholds
        colVerdict.setCellValueFactory(data -> {
            double score = data.getValue().getSimilarityScore();
            if (score >= 75) return new SimpleStringProperty("HIGH");
            if (score >= 40) return new SimpleStringProperty("MODERATE");
            return new SimpleStringProperty("LOW");
        });
    }


    // Opens a file chooser to select the answer sheet PDF
    @FXML
    public void handleUploadAnswerSheet(ActionEvent event) {
        FileChooser chooser = createPdfChooser("Select Answer Sheet PDF");
        Stage stage = getStageFromEvent(event);
        File file = chooser.showOpenDialog(stage);

        if (file != null) {
            answerSheetFile = file;
            lastDirectory = file.getParentFile();
            answerSheetLabel.setText(file.getName());
            answerSheetLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }


    // Opens a multi-file chooser to select student assignment PDFs (up to 60)
    @FXML
    public void handleUploadStudentFiles(ActionEvent event) {
        FileChooser chooser = createPdfChooser("Select Student Assignment PDFs (max 60)");
        Stage stage = getStageFromEvent(event);
        List<File> files = chooser.showOpenMultipleDialog(stage);

        if (files != null && !files.isEmpty()) {
            if (files.size() > 60) {
                files = files.subList(0, 60);
            }
            studentFiles = files;
            lastDirectory = files.get(0).getParentFile();
            studentFilesLabel.setText(files.size() + " files selected");
            studentFilesLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }


    // Starts the batch comparison on a background thread
    @FXML
    public void handleCheckAll(ActionEvent event) {
        // Validate inputs
        if (answerSheetFile == null) {
            statusLabel.setText("Please upload an answer sheet first.");
            return;
        }
        if (studentFiles == null || studentFiles.isEmpty()) {
            statusLabel.setText("Please upload student assignment PDFs.");
            return;
        }

        String strictness = strictnessComboBox.getValue();

        // Reset UI for new run
        tableData.clear();
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        checkButton.setDisable(true);
        exportButton.setDisable(true);
        statusLabel.setText("Starting analysis...");

        BatchProcessor processor = new BatchProcessor();

        // Run on background thread so the UI stays responsive
        new Thread(() -> {
            BatchResult result = processor.processAll(
                answerSheetFile, studentFiles, strictness,
                new BatchProcessor.ProgressCallback() {

                    @Override
                    public void onProgress(int current, int total, String fileName) {
                        Platform.runLater(() -> {
                            progressBar.setProgress((double) current / total);
                            statusLabel.setText("Processing " + current + "/" + total + ": " + fileName);
                        });
                    }

                    @Override
                    public void onFileComplete(SimilarityResult r) {
                        Platform.runLater(() -> tableData.add(r));
                    }

                    @Override
                    public void onError(String fileName, String error) {
                        Platform.runLater(() ->
                            statusLabel.setText("Error on " + fileName + ": " + error)
                        );
                    }
                }
            );

            // Update UI when all files are done
            Platform.runLater(() -> {
                lastBatchResult = result;
                progressBar.setProgress(1.0);
                progressBar.setVisible(false);
                checkButton.setDisable(false);
                exportButton.setDisable(false);
                statusLabel.setText("Done! " + result.getResults().size() + " files analyzed. "
                    + result.getFlaggedCount() + " flagged as high similarity.");
            });
        }).start();
    }


    // Saves the results to a CSV file
    @FXML
    public void handleExportCSV(ActionEvent event) {
        if (lastBatchResult == null || lastBatchResult.getResults().isEmpty()) {
            statusLabel.setText("No results to export. Run analysis first.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Results as CSV");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        chooser.setInitialFileName("sac_results.csv");

        Stage stage = getStageFromEvent(event);
        File file = chooser.showSaveDialog(stage);

        if (file != null) {
            try {
                CSVExporter.export(lastBatchResult, file);
                statusLabel.setText("Results exported to " + file.getName());
            } catch (Exception e) {
                statusLabel.setText("Export failed: " + e.getMessage());
            }
        }
    }


    // Helper: creates a FileChooser configured for PDF files
    private FileChooser createPdfChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        if (lastDirectory != null && lastDirectory.exists()) {
            chooser.setInitialDirectory(lastDirectory);
        }
        return chooser;
    }

    // Helper: gets the Stage from an ActionEvent
    private Stage getStageFromEvent(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}
