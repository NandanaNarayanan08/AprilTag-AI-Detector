# AprilTag AI Detector - Android Project

Welcome to the official, play-store ready Android Studio project repository for the **AprilTag AI Detector**. 
This is a modern Android application designed for high-precision real-time AprilTag fiducial marker tracking, 3D Pose/homography estimation, local scan history storage, and deep Gemini AI learning integration.

This project works on APIs 31 to 35+ (Android 12, 13, 14, 15), fully matching modern Android Architecture Guidelines.

---

## Technical Stack & Architecture

- **UI Layer**: Jetpack Compose designed with high-end Material Design 3 guidelines, containing a dark cyber-tech theme, smooth spring animations (`androidx.compose.animation`), and single-activity navigation.
- **Hardware Integrations**: CameraX (`androidx.camera`) for high-frame-rate real-time frame analysis.
- **Engine Core**: JNI (Java Native Interface) bridge linking the Kotlin analyzer to native **OpenCV SDK (v4.9.0+)** and the C-based **AprilTag core library (v3.3.0)** for heavy matrix math calculations.
- **Pose Estimation**: Perspective-n-Point (PnP) solver using camera intrinsic calibration parameters to yield translation vector coordinates (X, Y, Z coordinates) and orientation vectors (pitch, roll, yaw angles).
- **AI Core**: Google GenAI SDK (`com.google.genai`) server-proxying integration to dynamically translate detected Tag contexts into educational explanations using Gemini 3.5.
- **Local Database**: Room SQL Database with offline-first repositories for scan logging, querying, and exporting.

---

## Directory Layout & Source Tree

This repository houses the complete Android Studio code. If you have exported this workspace as a ZIP file, you can immediately find these files structured perfectly:

```text
android-project/
├── build.gradle.kts                 # Root Gradle project configuration
├── settings.gradle.kts              # Root Project name and module mappings
├── gradle/                          # Gradle properties and wrappers
│   └── wrapper/gradle-wrapper.properties
├── android-project-privacy-policy.html # Google Play Store Privacy template
└── app/
    ├── build.gradle.kts             # App-level builds, package settings, and signatures
    ├── proguard-rules.pro           # ProGuard optimizations for JNI C++ and Room
    └── src/
        └── main/
            ├── AndroidManifest.xml   # Camera, Audio, Internet, and system declarations
            ├── cpp/
            │   ├── CMakeLists.txt   # CMake compiler links to OpenCV/AprilTag
            │   └── apriltag_native.cpp # Native JNI C++ bridge implementation
            ├── java/com/apriltag/aidetector/
            │   ├── MainActivity.kt  # Root Navigator and System Layout control
            │   ├── data/
            │   │   └── ScanHistory.kt # Room Entity, DAO definitions, and Repository pattern
            │   ├── utils/
            │   │   └── AprilTagImageAnalyzer.kt # CameraX analyzer interfacing JNI OpenCV
            │   └── ui/
            │       ├── theme/
            │       │   └── Theme.kt # Cyber-tech Dark M3 Color-scheme variables
            │       └── screens/
            │           ├── ScannerScreen.kt # Camera feed renderer, HUD, overlay canvas
            │           ├── HistoryScreen.kt # History tracking, searching, deletion and CSV export
            │           └── LearningScreen.kt # Educational modules, CV formulas
            └── res/                 # Vector launcher icons, splash screen, and color definitions
```

---

## How to Set Up & Build in Android Studio

To compile this project into a production-ready **APK or Android App Bundle (AAB)** directly suitable for Play Store publication, follow these precise implementation steps:

### Prerequisites
1. **Android Studio**: Ensure you have Android Studio (Ladybug 2024.1 or higher) installed.
2. **Android NDK**: Open SDK Manager, go to SDK Tools, and install **NDK (Side by side)** and **CMake**.
3. **OpenCV Android SDK**: 
   - Download the precompiled OpenCV Android release (v4.9.0 or later) from [opencv.org](https://opencv.org/releases/).
   - Unzip the SDK. In Android Studio, go to `File -> New -> Import Module` and select the directory `/OpenCV-android-sdk/sdk`. Name the module `:opencv`.
4. **AprilTag Core C-Library**:
   - The native C++ wrapper uses the official AprilTag library. It is directly linked via the `CMakeLists.txt` file located in `app/src/main/cpp`. These source codes are included inside this package structure.

### Development Setup
1. **Set Gemini Secret Key**:
   - Go to your system environment variables OR configure your project `local.properties` file:
     ```properties
     GEMINI_API_KEY="your_actual_play_store_gemini_api_key_here"
     ```
   - The build configuration file reads this variable safely and registers it inside your `BuildConfig` class, preventing any hardcoded secrets in version control.

2. **Open and Sync Project**:
   - Open Android Studio, click **Open**, and select the `/android-project` folder.
   - Wait for Gradle to build dependencies and run the JNI native sync.

3. **Running the Application**:
   - Connect a real physical Android 12+ device with USB debugging enabled. Real terminal Camera frame analysis requires a hardware camera (the emulator camera might lack sufficient auto-focus for fiducial markers).
   - Press **Run (Shift + F10)**.

---

## Preparing for Production Google Play Store Release

To publish **AprilTag AI Detector** onto the Google Play Console, follow these steps:

### 1. Generating Release Signing Keys
To compile a secure AAB, you must sign the release using a Keystore:
1. In Android Studio, select `Build -> Generate Signed Bundle / APK`.
2. Choose **Android App Bundle** and click Next.
3. Click **Create New** under Key store path. Specify a secure local folder, enter a robust password, and fill in the certificate organization details.
4. Set the alias to `release_key` and proceed. Save these passwords securely.

### 2. ProGuard & Code Obfuscation
The application is pre-configured with `proguard-rules.pro` optimization configurations. This guarantees:
- Keeps JNI functions (`Java_com_apriltag_aidetector_utils_AprilTagImageAnalyzer_...`) untouched so the native loader locates them.
- Obfuscates database schemas and internal helper modules of Room SQL and Google GenAI, reducing reverse-engineering risk and compiling a lightweight APK payload.

### 3. Creating App Store Assets & Privacy Policy
- **Privacy Policy**: Before uploading to the Play Console, you must host a Privacy Policy. We have pre-composed a compliant `android-project-privacy-policy.html` in this repo. Host this file on GitHub Pages, Firebase Hosting, or your personal server, and paste the URL in the Console.
- **Store Graphics**: Capture screenshots of the Scanner Screen bounding-boxes, History charts, and Gemini AI details.

---

## License & Citation
This project uses the official AprilTag visual reference code:
- **AprilTag 3**: Developed by the APRIL Lab at the University of Michigan (licensed under LGPLv2.1).
- **OpenCV**: Released under the Apache 2 License.
- **Gemini**: Provided by Google. Code assets produced herein are licensed under **Apache-2.0**.
