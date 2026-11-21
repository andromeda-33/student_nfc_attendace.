# TapAttend NFC: 1–2s Classroom Roll Call & Attestation System

> **Eliminating the crowd scramble:** An efficiency Android app designed for university lecturers and class representatives to conduct instantaneous 1–2 second roll calls, exam sign-ins, and assignment drop-offs using phone NFC.

---

## 1. What Can Substitute a Signature in a School Setting?

In traditional university and school settings, a handwritten signature on paper has two main goals:
1. **Verification of Physical Presence** (proving the student was in the room at that moment).
2. **Non-Repudiation** (preventing the student from later claiming *"You marked me absent, but I was there!"*).

However, paper signatures fail catastrophically in large cohorts:
- **Proxy Signing:** Friends sign for 3 or 4 absent friends ("scrambling").
- **Bottlenecks & Time Loss:** 80 students taking 15 seconds each to find their name and sign wastes 20+ minutes of lecture time.
- **Vulnerability:** Sheets get lost, torn, or ruined by spilled coffee.

### The 6 Modern Substitutes for a Handwritten Signature:

| Substitute | Mechanism | Anti-Proxy Strength | Speed |
| :--- | :--- | :--- | :--- |
| **1. Cryptographic UID + Micro-Timestamp (Implemented)** | The student's physical card has a factory-burned hardware UID (e.g. `04:A2:3B:5C:89`). The app records `SHA-256(UID + SessionID + Timestamp + Nonce)` generating an immutable 12-character attestation token. | ⭐⭐⭐⭐ (Requires physical card in hand) | ⚡ **1 second** |
| **2. Burst-Camera "Tap & Snap"** | When the NFC card touches the back of the phone, the phone's camera snaps a silent 0.5s selfie of the person tapping. Stores the thumbnail in the row. | ⭐⭐⭐⭐⭐ (Eliminates card-sharing completely) | ⚡ **1.5 seconds** |
| **3. Lecturer / Invigilator Master Seal** | Dual-attestation model: instead of 100 students scribbling signatures, the lecturer or class rep signs/authenticates once to seal the entire cryptographically hashed cohort list upon closing the session. | ⭐⭐⭐⭐ (Legally accepted in bar exams & civil services) | ⚡ **Instant** |
| **4. Dynamic One-Time Rolling Code (TOTP)** | If students use their own smartphones instead of plastic cards, the student app displays a 4-digit code that changes every 30 seconds, or sends a dynamic BLE/NFC handshake. | ⭐⭐⭐⭐⭐ (Cannot be forwarded or screenshot) | ⚡ **2 seconds** |
| **5. Biometric Fingerprint / Face ID Handshake** | Used in high-stakes testing: student touches their phone's fingerprint sensor before tapping NFC. | ⭐⭐⭐⭐⭐ (Biometric grade) | ⚡ **2–3 seconds** |
| **6. Fast-Canvas Vector Sign (Hybrid)** | If university bylaws strictly mandate an ink-like mark, the app displays a 1-second full-screen scratchpad on tap. The student scribbles a check/signature in 1 second, saved as a PNG attached to their row. | ⭐⭐⭐ (Same as paper, but digital) | ⚡ **3 seconds** |

**Conclusion:** For everyday classes and exams, **Substitute #1 (Cryptographic Card UID + Micro-Timestamp)** backed by **Substitute #3 (Lecturer Digital Seal)** provides far greater legal and administrative integrity than a paper signature while taking only **1–2 seconds per student**.

---

## 2. Supported Sheet Types & Customization

The app features a flexible Session Engine with customizable sheet formats:

### A. Whole Semester Attendance (Multi-Week Matrix)
* **Columns:** `S/N | Reg Number | Full Name | W1 | W2 | W3 ... W14 | Total Attended | Attendance % | Eligibility (>= 75%)`
* **How it works:** Lecturer or Class Rep selects the current week (e.g., Week 4). As students tap, their `W4` column turns to `P` (Present).
* **Deficit Filter:** Instant 1-tap filter reveals students who are below the 75% attendance threshold required to sit for final examinations.

### B. Single Exam / Test Attendance Sheet
* **Columns:** `S/N | Reg Number | Full Name | NFC Card UID | Check-in Time | Digital Attestation Token | Status`
* **Features:** Prevents exam impersonation. Verifies seat and time of entry with cryptographic hash.

### C. Assignment / Lab Submission Tracker
* **Columns:** `Order # | Reg Number | Full Name | Exact Drop-off Time | Digital Attestation Token | Status`
* **Features:** Automatically tags submission sequence (`#1`, `#2`, `#3`...) to settle disputes about who submitted on time before the deadline.

### D. Single Occasion / Seminar Roll Call
* Quick one-off roll call for guest lectures, faculty meetings, and departmental workshops.

---

## 3. Real-World NFC Card Architecture

University student ID cards fall into two categories:
1. **Hardware UID Only (95% of Universities):**
   * Most cards (Mifare Classic 1K, Mifare Ultralight, DESFire, EMV transit cards) have a unique read-only serial number (e.g., `04A23B5C89`).
   * **How TapAttend handles this:** TapAttend maintains an offline student directory linking each `Card UID` to the student's `Name` and `Registration Number`. When an unmapped card taps, the app asks: *"New Card! Enter Student Name & Reg No"*, saving it permanently for all future classes.
2. **NDEF Formatted Cards:**
   * If cards contain encoded text or JSON payloads, `NfcHelper.kt` decodes the NDEF record directly.

---

## 4. Android Project Structure

```
nfc card reader/
├── app/
│   ├── build.gradle.kts                      # Modern Gradle setup with Jetpack Compose & Material 3
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml           # NFC permissions, tech filters, FileProvider
│           ├── res/                          # Vector icons, colors, themes, tech-lists
│           └── java/com/tapattend/nfc/
│               ├── MainActivity.kt           # Lifecycle, Foreground NFC dispatch, Navigation
│               ├── data/
│               │   ├── Models.kt             # Student, Session, AttendanceRecord, Matrix Row
│               │   ├── AttendanceStorage.kt  # Thread-safe persistent local storage & matrix engine
│               │   └── SheetExporter.kt      # Formatted CSV, HTML & Android Share Sheet
│               ├── nfc/
│               │   └── NfcHelper.kt          # Tag UID extraction, NDEF reader, acoustic/haptic feedback
│               └── ui/
│                   ├── HomeScreen.kt         # Session list, stats cards, fab
│                   ├── CreateSessionDialog.kt# Custom sheet picker (Exam, Submission, Semester)
│                   ├── LiveScanScreen.kt     # 1-2s rapid tap screen, radar pulse, duplicate alerts
│                   ├── SemesterMatrixScreen.kt# Horizontally scrollable W1..W14 matrix with 75% rule
│                   ├── StudentRosterScreen.kt# Student card directory & tap-to-pair
│                   └── Theme.kt              # Clean Material 3 styling
├── .github/workflows/
│   └── build-apk.yml                         # Automated GitHub Actions workflow to build APK
├── web-preview/
│   └── index.html                            # Live interactive simulator with audio & real Web NFC
└── README.md
```

---

## 5. How to Build the APK

### Method 1: Android Studio (Recommended)
1. Open Android Studio.
2. Select **Open** and choose this folder: `c:\Users\nikol\Desktop\nfc card reader`.
3. Wait for Gradle sync to complete.
4. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The generated APK will be in: `app/build/outputs/apk/debug/app-debug.apk`.
6. Transfer to phone or install via ADB: `adb install app-debug.apk`.

### Method 2: Command Line (Gradle)
```bash
./gradlew assembleDebug
```

### Method 3: Cloud Build via GitHub Actions (Zero Local Setup)
1. Push this folder to a GitHub repository.
2. Go to the **Actions** tab on GitHub.
3. The included workflow `.github/workflows/build-apk.yml` will automatically compile and provide a direct download link for `TapAttend-NFC-debug.apk`.

---

## 6. Live Web Simulator & Android Chrome Web NFC

You can test the entire experience right now without building anything:
1. Open [index.html](file:///c:/Users/nikol/Desktop/nfc%20card%20reader/web-preview/index.html) in your browser.
2. Customize sheet types (Semester Matrix, Exam, Submission).
3. Click **Tap Card or Click to Scan** to experience the 1-2 second rapid roll call, acoustic chime, duplicate prevention, and download formatted CSV sheets!
4. On an Android phone with NFC, opening this page in Google Chrome allows testing real physical NFC cards via the **Web NFC API**!

