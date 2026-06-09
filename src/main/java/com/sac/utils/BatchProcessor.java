package com.sac.utils;

import com.sac.algorithms.StrictnessController;
import com.sac.models.BatchResult;
import com.sac.models.SimilarityResult;

import java.io.File;
import java.util.List;

// Processes all student PDFs against one answer sheet and reports progress
public class BatchProcessor {

    private final StrictnessController controller = new StrictnessController();

    // Callback interface so the UI can update progress as each file is processed
    public interface ProgressCallback {
        void onProgress(int current, int total, String currentFileName);
        void onFileComplete(SimilarityResult result);
        void onError(String fileName, String errorMessage);
    }

    // Runs the full batch comparison
    public BatchResult processAll(File answerSheet, List<File> studentFiles,
                                   String strictness, ProgressCallback callback) {

        BatchResult batchResult = new BatchResult(answerSheet.getName(), strictness);

        // First extract the answer sheet text
        String answerText;
        try {
            answerText = PDFExtractor.extractText(answerSheet);
            if (answerText.isEmpty()) {
                callback.onError(answerSheet.getName(),
                    "Could not extract text from answer sheet. Make sure OCR server is running.");
                return batchResult;
            }
        } catch (Exception e) {
            callback.onError(answerSheet.getName(), "Failed to read answer sheet: " + e.getMessage());
            return batchResult;
        }

        int total = studentFiles.size();

        // Process each student file one by one
        for (int i = 0; i < total; i++) {
            File studentFile = studentFiles.get(i);
            callback.onProgress(i + 1, total, studentFile.getName());

            try {
                // Extract text from student PDF
                String studentText = PDFExtractor.extractText(studentFile);

                // Compare student text against answer sheet
                SimilarityResult result = controller.analyze(
                    answerText, studentText, strictness,
                    answerSheet.getName(), studentFile.getName()
                );

                batchResult.addResult(result);
                callback.onFileComplete(result);

            } catch (Exception e) {
                callback.onError(studentFile.getName(), e.getMessage());
            }
        }

        return batchResult;
    }
}
