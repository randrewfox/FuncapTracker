# FUNCAP Tracker — Native Android App

A native Kotlin + Jetpack Compose Android application designed to take, score, and track **FUNCAP55** and **FUNCAP27** functional capacity assessments over time.

## Key Features
1. **Complete Official FUNCAP55 & FUNCAP27 Questionnaires**:
   - Includes all **55 questions** of FUNCAP55 and all **27 questions** of FUNCAP27 across the 8 validated functional domains (`A` through `H`), scored on the official `0–6` activity consequence scale (Sommerfelt et al., *J. Clin. Med.* 2024;13(12):3486).
   - Automatically calculates domain averages (`A–H`), total score (mean of the 8 domain means), and—whenever FUNCAP55 is completed—also calculates the equivalent FUNCAP27 subset score so both tests can be compared longitudinally.
2. **Longitudinal Score Log & Trend Chart**:
   - Local SQLite persistence powered by **Jetpack Room** (`funcap_tracker.db`).
   - Interactive Material 3 dashboard with a longitudinal score trend line chart, filterable by `All`, `FUNCAP27`, or `FUNCAP55`, plus subscale comparison bars against validation study benchmarks.
3. **Customizable Periodic Reminders**:
   - Uses **Android WorkManager** (`FuncapReminderWorker`) to schedule repeating notifications at user-configurable intervals (`Weekly`, `Bi-weekly`, `Monthly (30 days)`, `Quarterly`, or any custom number of days `1–365`).
4. **Zero-Setup Cloud Backup & Google Drive Integration**:
   - **Automatic Cloud Sync**: Configured with Android Auto Backup (`backup_rules.xml` and `data_extraction_rules.xml`) so your device automatically backs up `funcap_tracker.db` and `funcap_settings.preferences_pb` to your Google Account / Google Drive App Data.
   - **Direct Google Drive Export & Restore**: Uses the Android Storage Access Framework (`ActivityResultContracts.CreateDocument` & `OpenDocument`) to save and restore lossless JSON backups (`funcap_backup_YYYYMMDD.json`) and clinician-ready CSV spreadsheets (`funcap_scores_YYYYMMDD.csv`) directly to your Google Drive folder without requiring OAuth client IDs or Google Cloud console setup.
5. **Light Mode & Dark Mode Theme Toggle**:
   - Persisted via **Jetpack DataStore** (`UserPreferencesRepository`), featuring both a top-bar quick toggle and a dedicated Settings toggle (`Light Mode`, `Dark Mode`, `System Default`, plus a **Material You Dynamic Colors** switch).

## How to Open & Run
1. Unzip `FuncapTracker_Android_Studio_Project.zip` on your computer.
2. Open **Android Studio** (Ladybug 2024.2.1 or newer) and select **Open** -> choose the `FuncapTracker` folder.
3. Let Android Studio sync Gradle and download the dependencies.
4. Connect your device via USB debugging or Wi-Fi pairing and click **Run ('app')**.
