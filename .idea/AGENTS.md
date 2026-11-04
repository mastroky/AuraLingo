# AGENTS.md - AuraLingo (LingoLecture) Project Guidelines

## Project Context
AuraLingo (LingoLecture) is an Android application for university and Erasmus students.
Its main goal is to transcribe lectures in real-time, enable quick search/translation of complex words, and save them into a personal vocabulary list.

## Technical Stack & Architecture
- **Language:** Kotlin
- **Minimum SDK:** API 29 (Android 10.0)
- **UI Framework:** Jetpack Compose + Material Design 3 (strictly no XML layouts)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Local Database:** Room Database
- **Asynchronous Code:** Kotlin Coroutines & Flow (StateFlow / SharedFlow)

## Mandatory Coding Rules for AI Agents
1. **Jetpack Compose Only:** Always use Jetpack Compose for UI screens and components. Do not generate XML layouts.
2. **Architecture Decoupling:** Keep Composable functions state-less where possible. Expose UI state from ViewModels using `StateFlow` and collect it in Compose using `collectAsStateWithLifecycle()` or `collectAsState()`.
3. **Design System:** Use Material Design 3 components (`androidx.compose.material3.*`). Keep the UI minimal, modern, and intuitive.
4. **Language & Comments:** Write code in Kotlin. Include short, clear inline comments in **Italian** explaining key steps, architectural choices, and complex logic.
5. **Clean Code:** Separate layers clearly into `data`, `domain` (if applicable), and `ui` packages.