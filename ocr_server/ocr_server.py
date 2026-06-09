# PaddleOCR server for extracting text from PDF files.
# Handles both typed and handwritten assignments.
# Run this server BEFORE starting the Java application.

import os
# Disable oneDNN to avoid NotImplementedError on CPU execution
os.environ['FLAGS_use_onednn'] = '0'
os.environ['PADDLE_PDX_ENABLE_MKLDNN_BYDEFAULT'] = '0'
os.environ['FLAGS_enable_pir_in_executor'] = '0'
os.environ['FLAGS_enable_pir_api'] = '0'
os.environ['FLAGS_allocator_strategy'] = 'naive_best_fit'

from flask import Flask, request
from paddleocr import PaddleOCR
import fitz  # PyMuPDF - renders PDF pages to images
import numpy as np
from PIL import Image
import io
import tempfile

app = Flask(__name__)

# Initialize PaddleOCR with text orientation detection and disabled mkldnn + limited threads for stability on CPU
ocr = PaddleOCR(use_textline_orientation=True, lang='en', enable_mkldnn=False, cpu_threads=2)


@app.route('/ocr', methods=['POST'])
def ocr_pdf():
    """Receives PDF bytes, runs OCR on each page, returns extracted text."""
    pdf_bytes = request.data

    # Save to a temp file so PyMuPDF can open it
    temp_file = tempfile.NamedTemporaryFile(suffix='.pdf', delete=False)
    temp_file.write(pdf_bytes)
    temp_file.close()

    doc = None
    try:
        doc = fitz.open(temp_file.name)
        all_text = []

        for page_num in range(len(doc)):
            # Render page as an image at 150 DPI to save memory and process faster
            page = doc[page_num]
            pix = page.get_pixmap(dpi=150)
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
                    if not page_result:
                        continue
                    if isinstance(page_result, dict):
                        # PaddleOCR v3.x dict format
                        if 'rec_texts' in page_result:
                            for text in page_result['rec_texts']:
                                if text:
                                    page_lines.append(str(text))
                    elif isinstance(page_result, list):
                        # PaddleOCR v2.x nested list format
                        for line in page_result:
                            try:
                                if line and isinstance(line, list) and len(line) > 1:
                                    text = line[1][0] if isinstance(line[1], (list, tuple)) else str(line[1])
                                    page_lines.append(text)
                            except (IndexError, TypeError):
                                continue

            all_text.append(" ".join(page_lines))
    finally:
        if doc is not None:
            try:
                doc.close()
            except Exception:
                pass
        # Clean up temp file safely
        try:
            os.unlink(temp_file.name)
        except Exception:
            pass

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
