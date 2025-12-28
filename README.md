# 🕋 QiblaPro - Professional Qibla Finder & Compass

**QiblaPro** is a high-precision, modern Android application built with Jetpack Compose. It utilizes advanced sensor fusion algorithms to provide real-time Qibla direction, magnetic field calibration, and integrated map views with a futuristic Neon UI.

---

## ⚖️ License & Legal Notice

**Copyright © 2025 MSA Team. All rights reserved.**

This project is licensed under the **GNU General Public License v3.0 (GPLv3)** - a strong copyleft license.

### 🛑 "Heavy" License Terms:
- **Copyleft:** Any derivative work or modified version of this software must also be licensed under the GPLv3. You cannot make this code part of a closed-source proprietary product.
- **Source Disclosure:** If you distribute a compiled version of this app, you **must** make the full source code available to the users.
- **License and Copyright Notice:** You must include the original copyright notice and a copy of the GPLv3 license in all copies or substantial portions of the software.
- **State Changes:** You must clearly state any changes made to the original code.

For more details, see the [official GPLv3 documentation](https://www.gnu.org/licenses/gpl-3.0.html).

---

## ✨ Key Features

- **🎯 Smart Qibla Engine:** Proprietary `QiblaEngine` with stability scoring and magnetic declination correction.
- **🧭 Responsive Compass:** Intelligent coordinate remapping (`remapCoordinateSystem`) ensuring accurate North/South alignment on all device orientations.
- **🛡️ Adaptive Smoothing:** Dynamic alpha-filtering in `HeadingSmoother` to eliminate needle jitter without compromising speed.
- **🌍 Full Localization:** Complete support for **Persian (FA)**, **Arabic (AR)**, and **English (EN)** with automatic RTL/LTR layout handling.
- **🗺️ Futuristic Maps:** Google Maps integration featuring custom Neon night styles and real-time Qibla path drawing.
- **📐 Calibration Intelligence:** Real-time sensor accuracy monitoring with interactive "Figure-8" calibration guides.
- **🎨 Modern UI:** 100% Jetpack Compose implementation with Material 3, Neon accents, and synchronized haptic feedback.

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Dependency Injection:** Hilt (Dagger)
- **Architecture:** MVVM + Clean Architecture principles
- **Reactive Programming:** Kotlin Coroutines & Flow
- **Local Storage:** Jetpack DataStore (Preferences)
- **Maps:** Google Maps SDK & Maps Compose
- **Sensors:** Android Sensor Framework (Rotation Vector, Accelerometer, Magnetometer)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 35/36 (Compile SDK)
- Min SDK 28 (Android 9.0)

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/QiblaPro.git
   ```
2. Open the project in Android Studio.
3. Add your Google Maps API Key to `local.properties`:
   ```properties
   MAPS_API_KEY=YOUR_KEY_HERE
   ```
4. Sync Gradle and Run the app.

---

## 🏗 Project Structure

- `data/`: Data sources, Repositories (Location, Compass, Settings).
- `domain/`: Business logic, Math utilities, and the Qibla processing engine.
- `ui/`: Compose screens, ViewModels, and UI-specific components.
- `util/`: Hardware helpers (Haptics, GPS), Language management, and Extensions.

---

**Developed with precision by the MSA Team.**
