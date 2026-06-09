@echo off
echo ====================================
echo  PaddleOCR Server Setup
echo ====================================
echo.
echo Installing dependencies (first run may take a few minutes)...
pip install -r requirements.txt
echo.
echo Starting OCR server on port 5000...
echo Keep this window open while using Smart Assignment Checker.
echo.
python ocr_server.py
pause
