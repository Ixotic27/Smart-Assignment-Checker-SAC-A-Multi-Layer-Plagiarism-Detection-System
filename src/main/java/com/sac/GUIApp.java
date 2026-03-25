package com.sac;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

public class GUIApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the FXML file
        File fxmlFile = new File("src/main/resources/views/Login.fxml");
        URL fxmlUrl = fxmlFile.toURI().toURL();
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
