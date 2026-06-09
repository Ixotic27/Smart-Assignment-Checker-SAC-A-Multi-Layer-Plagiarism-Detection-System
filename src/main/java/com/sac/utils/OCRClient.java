package com.sac.utils;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

// Sends PDF files to the Python OCR server and gets back extracted text.
// The server must be running on localhost:5000 before using this.
public class OCRClient {

    private static final String SERVER_URL = "http://localhost:5000";

    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    // Sends a PDF to the OCR server and returns the recognized text
    public static String extractText(File pdfFile) throws Exception {
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER_URL + "/ocr"))
            .header("Content-Type", "application/pdf")
            .timeout(Duration.ofMinutes(10))
            .POST(HttpRequest.BodyPublishers.ofByteArray(pdfBytes))
            .build();

        HttpResponse<String> response = client.send(
            request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new Exception("OCR server returned error code: " + response.statusCode());
        }

        return response.body().trim();
    }

    // Checks if the OCR server is up and running
    public static boolean isServerRunning() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/health"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
            );
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
