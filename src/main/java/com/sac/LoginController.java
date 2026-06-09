package com.sac;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;

/**
 * LoginController - Handles user authentication.
 * Validates credentials before navigating to the Dashboard.
 */
public class LoginController {

    // Default credentials for demonstration
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "1234";

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please enter both username and password.");
            return;
        }

        // Check credentials
        if (!username.equals(DEFAULT_USERNAME) || !password.equals(DEFAULT_PASSWORD)) {
            showAlert("Login Failed", "Invalid username or password.\n\nDefault: admin / 1234");
            return;
        }

        try {
            // Load the Dashboard FXML from classpath
            URL fxmlUrl = getClass().getResource("/views/Dashboard.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Cannot find /views/Dashboard.fxml on classpath");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            // Get current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(loader.load(), 900, 700);

            stage.setTitle("Dashboard - Smart Assignment Checker");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to load Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
