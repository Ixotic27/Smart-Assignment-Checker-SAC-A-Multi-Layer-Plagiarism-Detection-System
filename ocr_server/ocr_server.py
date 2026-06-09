# RapidOCR server for extracting text from PDF files.
# Uses ONNX Runtime with PaddleOCR models — fast and accurate on CPU.
# Run this server BEFORE starting the Java application.

from flask import Flask, request
from rapidocr_onnxruntime import RapidOCR
import fitz  # PyMuPDF - renders PDF pages to images
import numpy as np
from PIL import Image
import io
import os
import tempfile

app = Flask(__name__)

# Initialize RapidOCR (uses ONNX Runtime — much faster than PaddlePaddle on CPU)
ocr = RapidOCR()


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
            # Render page as an image (150 DPI — good balance of quality and speed)
            page = doc[page_num]
            pix = page.get_pixmap(dpi=150)
            img_bytes = pix.tobytes("png")

            # Convert to numpy array for RapidOCR
            img = Image.open(io.BytesIO(img_bytes))
            img_array = np.array(img)

            # Run OCR on this page
            page_lines = []
            result, elapsed = ocr(img_array)
            if result:
                for line in result:
                    # Each line is [box_coords, text, confidence]
                    text = line[1]
                    if text and str(text).strip():
                        page_lines.append(str(text).strip())

            all_text.append(" ".join(page_lines))
            print(f"Page {page_num}: {len(page_lines)} lines in {elapsed}s", flush=True)
    finally:
        if doc is not None:
            try:
                doc.close()
            except Exception:
                pass
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
    print("RapidOCR server starting on port 5000...")
    print("Keep this window open while using Smart Assignment Checker.")
    app.run(host='127.0.0.1', port=5000, debug=False)
