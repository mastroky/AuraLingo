## Project Context
LingoLecture is an Android application designed for university and Erasmus students.
Its main goal is to transcribe lectures in real-time, enable quick search/translation of complex words, and save them into a personal vocabulary list.

## Technical Requirements
- Language: Kotlin
- Minimum SDK: API 29 (Android 10.0)
- UI Framework: Jetpack Compose + Material Design 3
- Architecture: MVVM (Model-View-ViewModel)
- Local Database: Room Database
- Asynchronous Code: Coroutines & Kotlin Flow

## Code Style & Rules
1. Use **Jetpack Compose** exclusively for the UI (no XML layouts).
2. Keep the UI clean and decoupled from business logic (use ViewModels).
3. Keep screen designs minimal, modern, and intuitive.
4. When writing Kotlin code, include short comments in Italian to explain key steps.