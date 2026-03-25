package com.sac;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.File;

public class DashboardController {

    @FXML
    private Label selectedFileLabel;

    @FXML
    private ComboBox<String> strictnessComboBox;

    @FXML
    private TextArea outputArea;

    private File selectedFile;

    @FXML
    public void initialize() {
        strictnessComboBox.setItems(FXCollections.observableArrayList(
            "Low", "Medium", "High"
        ));
        strictnessComboBox.setValue("Medium");
    }

    @FXML
    public void handleUploadPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            selectedFileLabel.setText("Selected file: " + selectedFile.getName());
            outputArea.setText("File uploaded successfully.\nReady to check similarity.");
        } else {
            selectedFileLabel.setText("No file selected");
        }
    }

    @FXML
    public void handleCheckSimilarity() {
        if (selectedFile == null) {
            outputArea.setText("Please upload a PDF file first.");
            return;
        }
        
        outputArea.setText("Checking similarity for: " + selectedFile.getName() + "...\n");
        outputArea.appendText("Strictness Level: " + strictnessComboBox.getValue() + "\n\n");
        outputArea.appendText("Similarity: 75% (Demo Output)");
    }
}
