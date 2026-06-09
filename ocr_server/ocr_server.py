# PaddleOCR server for extracting text from PDF files.
# Handles both typed and handwritten assignments.
# Run this server BEFORE starting the Java application.

from flask import Flask, request
from paddleocr import PaddleOCR
import fitz  # PyMuPDF - renders PDF pages to images
import numpy as np
from PIL import Image
import io
import tempfile
import os

app = Flask(__name__)

# Initialize PaddleOCR with text orientation detection
ocr = PaddleOCR(use_textline_orientation=True, lang='en')


@app.route('/ocr', methods=['POST'])
def ocr_pdf():
    """Receives PDF bytes, runs OCR on each page, returns extracted text."""
    pdf_bytes = request.data

    # Save to a temp file so PyMuPDF can open it
    temp_file = tempfile.NamedTemporaryFile(suffix='.pdf', delete=False)
    temp_file.write(pdf_bytes)
    temp_file.close()

    try:
        doc = fitz.open(temp_file.name)
        all_text = []

        for page_num in range(len(doc)):
            # Render page as an image at 300 DPI
            page = doc[page_num]
            pix = page.get_pixmap(dpi=300)
            img_bytes = pix.tobytes("png")

            # Convert to numpy array for PaddleOCR
            img = Image.open(io.BytesIO(img_bytes))
            img_array = np.array(img)

            # Run OCR on this page
            result = ocr.ocr(img_array)

            # Collect recognized text from this page
            page_lines = []
            if result:
                for page_result in result:
                    if page_result:
                        for line in page_result:
                            try:
                                text = line[1][0] if isinstance(line[1], (list, tuple)) else str(line[1])
                                page_lines.append(text)
                            except (IndexError, TypeError):
                                continue

            all_text.append(" ".join(page_lines))

        doc.close()
    finally:
        # Clean up temp file
        os.unlink(temp_file.name)

    # Return plain text so Java doesn't need JSON parsing
    return "\n".join(all_text), 200, {'Content-Type': 'text/plain; charset=utf-8'}


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint - Java app calls this to verify server is running."""
    return "OK", 200


if __name__ == '__main__':
    print("PaddleOCR server starting on port 5000...")
    print("Keep this window open while using Smart Assignment Checker.")
    app.run(host='127.0.0.1', port=5000, debug=False)
