# 🟠 Bingo Scanner App (Android)

A smart **Bingo ticket scanner** built with **Jetpack Compose** that extracts the full 5×5 grid, **LOS number**, and **serial number** from ticket images using on-device OCR.

## ✨ Features

- 🎯 Accurate Bingo Grid Detection (5×5)
- 🧠 Multi-stage OCR pipeline (deskew, perspective, contrast)
- 🟡 Highlight/marker suppression
- 🔍 LOS number detection
- 🔍 Serial number detection
- ⚡ On-device processing (ML Kit)

## 🧩 Tech Stack

- Kotlin
- Jetpack Compose
- ML Kit Text Recognition
- CameraX

## 🚀 Status

> Active development  
> Grid detection stable (~25/25)  
> Improving LOS & Serial detection

## 📸 Supported Inputs

- Cropped ticket images  
- Perspective images  
- Highlighted tickets  

## 🎯 Goal

Reliable real-world Bingo ticket scanning, even with imperfect images.
