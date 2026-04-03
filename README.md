🟠 Bingo Scanner App (Android)

A smart Bingo ticket scanner built with Jetpack Compose that extracts the full 5×5 grid, LOS number, and serial number from ticket images using on-device OCR.

✨ Features
🎯 Accurate Bingo Grid Detection
Extracts all 25 numbers (5×5 grid)
Handles highlighted / marked tickets
Column validation (B/I/N/G/O ranges)
🧠 Smart OCR Pipeline
Multi-stage processing (base, deskew, perspective, contrast)
Consensus-based result stabilization
Highlight (marker) suppression
🔍 Metadata Extraction
Reads LOS number
Reads Serial number
Works from ticket image (no server needed)
⚡ On-Device Processing
Powered by ML Kit Text Recognition
Fast, private, offline-first
🧩 Tech Stack
Kotlin
Jetpack Compose
CameraX (optional scanning)
ML Kit OCR
Clean modular architecture
🚀 Status

Actively in development
Grid detection is stable (~25/25)
Ongoing improvements for LOS & serial extraction

📸 Supported Inputs
Cropped ticket images
Photos with perspective
Tickets with marker highlights
🎯 Goal

To build a reliable real-world Bingo scanner that works even with imperfect images — highlights, angles, and noise included.
