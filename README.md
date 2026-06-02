# 🎯 Mamun Bingo App

A modern Android Bingo app with advanced ticket scanning and **QR-based sharing**—plus a unified **CameraX** flow that auto-detects Bingo QR codes and captures full tickets against an on-screen frame for sharper OCR.

---

## 🚀 Features

### 📷 Smart Camera Scanner

- Unified **CameraX** scanner
- Auto-detects **Bingo QR codes** for instant import
- **Frame-based** full-ticket capture (matches on-screen guide)
- Focused capture for **better OCR** on the grid and metadata

### 🔍 OCR Ticket Recognition

- Detects the Bingo **5×5 grid** (25 cells)
- Extracts **LOS** and **SERIAL** where visible
- Works with **camera** and **gallery** images (incl. crop/edit path)

### 🔗 QR Code Sharing

- **Generate a QR** for a ticket from history, live play, or ticket detail
- **Scan a ticket QR** to import—same payload format as the camera pipeline
- Works **offline** between devices (no server required for encode/decode)

### 🧾 Ticket Management

- **History** with saved sessions and detail views
- **Live play** with rooms, calls, and sheet previews
- **Ticket detail** with full grid and room actions

---

## 🧠 Tech Stack

| Area | Technology |
|------|------------|
| UI | Kotlin, **Jetpack Compose** |
| Camera | **CameraX** (preview, capture, analysis) |
| Vision | **ML Kit** (OCR, barcode/QR) |
| QR generation | **ZXing** |
| Image editing | **uCrop** (gallery pipeline) |
| Async | Coroutines, ViewModel |

---

## 📱 Screenshots

> Add your screenshots here (e.g. drop images into `docs/` and link them).

| | |
|--|--|
| Scanner / import camera | *placeholder* |
| Ticket detail | *placeholder* |
| Ticket QR dialog | *placeholder* |
| Live play | *placeholder* |

---

## ⚙️ Setup

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd MamunBingoApp
./gradlew :app:assembleDebug
```

Use **Android Studio** with a recent **AGP** / **compileSdk** as in the project (see `app/build.gradle.kts`).

---

## 📦 Version

**v0.11-camera-unified-scan**

---

## 👨‍💻 Author

**Mamunuzzaman**  
Android Developer · WordPress Engineer

---

## 📌 Notes

This project is tuned for **real-world use**: fast scanning paths, **low friction** in the import flow, and **reliable** OCR + **QR** handoffs (same ticket model whether you scan a QR, use the camera, or pick from gallery).

---

## 🎥 Demo Access

This repository is presented as a portfolio and case study project.

The Android APK is not publicly distributed.

If you would like to review, test, or discuss the application, please contact me directly:

📧 m.mamunuzzaman@gmail.com

Access is provided upon request.

---

## 🤖 AI-Assisted Development

This project was developed using AI-assisted and Vibe Coding workflows.

Tools used during development include:

Cursor AI
ChatGPT
Claude
Google Gemini

My role focused on:

Product planning
Feature design
Workflow architecture
UI/UX decisions
Testing and QA
Prompt engineering
Iterative implementation guidance

This project demonstrates how AI-assisted development can accelerate the creation of production-quality mobile applications.

---

## 📄 License

Source code is provided for portfolio and educational purposes.

All rights reserved unless otherwise stated.
