package com.sac.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * ImageHashDetector - Detects duplicate and similar images across two PDFs.
 *
 * Two detection modes:
 *   1. SHA-256 Hash (exact match): O(1) lookup via HashSet for identical images
 *   2. Pixel Comparison (visual similarity): Resizes images to a standard
 *      dimension, converts to grayscale, and compares pixel intensities
 *      to produce a percentage similarity score.
 *
 * Time Complexity:
 *   - SHA-256: O(n * b_avg) where n = total images, b_avg = avg image size
 *   - Pixel:   O(W * H) where W, H = comparison dimensions (256x256)
 *
 * Space Complexity: O(n) for hashes, O(W * H) for pixel arrays
 */
public class ImageHashDetector {

    // Standard size for pixel comparison (larger = more accurate but slower)
    private static final int COMPARE_SIZE = 256;

    /**
     * Generates a SHA-256 hash string for a byte array (image data).
     */
    public static String computeSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Detects exact duplicate images using SHA-256 hash comparison.
     */
    public static ImageDuplicateResult detectDuplicates(List<byte[]> images1, List<byte[]> images2) {
        Set<String> hashes1 = new HashSet<>();
        for (byte[] img : images1) {
            hashes1.add(computeSHA256(img));
        }

        Set<String> hashes2 = new HashSet<>();
        for (byte[] img : images2) {
            hashes2.add(computeSHA256(img));
        }

        Set<String> duplicateHashes = new HashSet<>(hashes1);
        duplicateHashes.retainAll(hashes2);

        return new ImageDuplicateResult(hashes1.size(), hashes2.size(), duplicateHashes.size());
    }

    /**
     * Compares two images visually using pixel-level grayscale comparison.
     *
     * Algorithm:
     *   1. Decode both byte arrays into BufferedImages
     *   2. Resize both to COMPARE_SIZE x COMPARE_SIZE
     *   3. Convert to grayscale pixel arrays
     *   4. Compare each pixel pair — if difference is within threshold, count as matching
     *   5. Return (matching pixels / total pixels) * 100
     *
     * @param imageData1 raw bytes of image 1
     * @param imageData2 raw bytes of image 2
     * @return similarity percentage (0-100), or -1 if images cannot be decoded
     */
    public static double comparePixels(byte[] imageData1, byte[] imageData2) {
        try {
            BufferedImage img1 = ImageIO.read(new ByteArrayInputStream(imageData1));
            BufferedImage img2 = ImageIO.read(new ByteArrayInputStream(imageData2));

            if (img1 == null || img2 == null) return -1;

            // Resize both to standard dimensions
            BufferedImage resized1 = resizeToGrayscale(img1, COMPARE_SIZE, COMPARE_SIZE);
            BufferedImage resized2 = resizeToGrayscale(img2, COMPARE_SIZE, COMPARE_SIZE);

            // Compare pixel intensities
            int totalPixels = COMPARE_SIZE * COMPARE_SIZE;
            int matchingPixels = 0;
            int threshold = 30; // Tolerance for minor rendering differences (0-255 scale)

            for (int y = 0; y < COMPARE_SIZE; y++) {
                for (int x = 0; x < COMPARE_SIZE; x++) {
                    int gray1 = resized1.getRGB(x, y) & 0xFF; // Extract blue channel (grayscale)
                    int gray2 = resized2.getRGB(x, y) & 0xFF;
                    if (Math.abs(gray1 - gray2) <= threshold) {
                        matchingPixels++;
                    }
                }
            }

            return ((double) matchingPixels / totalPixels) * 100.0;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Compares all images from two documents pairwise and returns the
     * highest similarity found (best match).
     */
    public static double compareBestMatch(List<byte[]> images1, List<byte[]> images2) {
        if (images1.isEmpty() || images2.isEmpty()) return 0.0;

        double bestMatch = 0.0;
        for (byte[] img1 : images1) {
            for (byte[] img2 : images2) {
                double similarity = comparePixels(img1, img2);
                if (similarity > bestMatch) {
                    bestMatch = similarity;
                }
            }
        }
        return bestMatch;
    }

    /**
     * Resizes an image to the target dimensions and converts to grayscale.
     */
    private static BufferedImage resizeToGrayscale(BufferedImage original, int width, int height) {
        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return grayscale;
    }

    /**
     * Result record for image duplicate detection.
     */
    public static class ImageDuplicateResult {
        private final int uniqueImagesDoc1;
        private final int uniqueImagesDoc2;
        private final int duplicateCount;

        public ImageDuplicateResult(int uniqueImagesDoc1, int uniqueImagesDoc2, int duplicateCount) {
            this.uniqueImagesDoc1 = uniqueImagesDoc1;
            this.uniqueImagesDoc2 = uniqueImagesDoc2;
            this.duplicateCount = duplicateCount;
        }

        public int getUniqueImagesDoc1() { return uniqueImagesDoc1; }
        public int getUniqueImagesDoc2() { return uniqueImagesDoc2; }
        public int getDuplicateCount() { return duplicateCount; }

        public double getDuplicatePercentage() {
            int total = uniqueImagesDoc1 + uniqueImagesDoc2;
            if (total == 0) return 0.0;
            return ((double) duplicateCount * 2 / total) * 100.0;
        }

        @Override
        public String toString() {
            return String.format("Images in Doc1: %d unique | Images in Doc2: %d unique | Duplicates: %d (%.1f%%)",
                uniqueImagesDoc1, uniqueImagesDoc2, duplicateCount, getDuplicatePercentage());
        }
    }
}
