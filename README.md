# FUNCAP Tracker — Native Android App

> If you have concerns that this app used AI to develop it, we understand and apologize for not being able to provide an entirely human-built app. We mainly wanted to use this app for ourselves and felt that opening it to more people would help offset the cost of using AI. Thank you for your consideration.

A native Kotlin + Jetpack Compose Android application designed to take, score, and track **FUNCAP55** and **FUNCAP27** functional capacity assessments over time.

## Request Access & Download
FUNCAP Tracker is available on the Google Play Store.

- **Request Access**: To request access to testing and use the app, please fill out the [Access Request Form](https://forms.gle/2tzaVuC8jJSwMXMk6).
- **Google Play Store**: Once granted access, you can download the app from Google Play:

<a href="https://play.google.com/store/apps/details?id=com.funcap.tracker">
  <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="55"/>
</a>

*(Direct link: [https://play.google.com/store/apps/details?id=com.funcap.tracker](https://play.google.com/store/apps/details?id=com.funcap.tracker))*

## What is a FUNCAP Test?
**FUNCAP** (*FUNctional CAPacity*) is a validated clinical questionnaire developed specifically to assess functional capacity and disability in individuals with Myalgic Encephalomyelitis / Chronic Fatigue Syndrome (**ME/CFS**) and similar energy-limiting or exertion-intolerant conditions (Sommerfelt et al., *J. Clin. Med.* 2024).

Unlike traditional disability assessments that only ask whether an activity can be performed at a single moment, FUNCAP evaluates the **consequences and post-exertional malaise (PEM)** of performing everyday activities. Each question is scored on an official **0 to 6** scale measuring how severely an activity impacts a person's remaining capacity on that day and over subsequent days.

The assessment covers **8 core functional domains**:
- **Domain A**: Personal hygiene & basic functions
- **Domain B**: Walking & moving around
- **Domain C**: Being upright (*orthostatic tolerance*)
- **Domain D**: Activities in the home
- **Domain E**: Communication
- **Domain F**: Activities outside the home
- **Domain G**: Reactions to light & sound
- **Domain H**: Concentration & cognitive stamina

The assessment comes in two validated variants:
- **FUNCAP55**: A comprehensive 55-item questionnaire for detailed diagnostic and baseline evaluation.
- **FUNCAP27**: A shortened 27-item subset designed for routine monthly check-ins and longitudinal tracking.

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
