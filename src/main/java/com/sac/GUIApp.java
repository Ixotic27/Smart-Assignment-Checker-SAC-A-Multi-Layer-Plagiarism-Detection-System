package com.sac;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * GUIApp - Main JavaFX Application entry point.
 * Loads the Login screen from classpath resources.
 */
public class GUIApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML from classpath (works regardless of working directory)
        URL fxmlUrl = getClass().getResource("/views/Login.fxml");
        if (fxmlUrl == null) {
            throw new RuntimeException("Cannot find /views/Login.fxml on classpath");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 600, 450);

        stage.setTitle("Smart Assignment Checker");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
