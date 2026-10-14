# 🎧 AuraLingo — Real-Time Speech Transcription & Personal Vocabulary Builder

[![Android Minimum SDK](https://img.shields.io/badge/Android-API%2029%2B%20%28Android%2010%29-brightgreen?logo=android)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-orange?logo=kotlin)](https://kotlinlang.org/)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-purple)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**AuraLingo** is a native Android application designed to eliminate language barriers in real-time conversations, lectures, and international meetings. Whether you are an exchange student attending lectures in a foreign language, an expat navigating daily life, or a professional participating in global meetings, **AuraLingo listens, transcribes, and helps you learn complex vocabulary on the fly.**

---

## 💡 Problem & Solution

* **The Problem:** Following fast-paced spoken conversations, academic lectures, or business meetings in a foreign language is exhausting. Looking up unknown words mid-conversation distracts from listening, leading to missed information.
* **The Solution:** AuraLingo offers a seamless real-time speech-to-text engine where **every transcribed word is interactive**. Tap any unfamiliar term to get an instant contextual translation and definition, and save it to your personal vocabulary bank in one tap.

---

## ✨ Key Features

- 🎙️ **Live Speech-to-Text Stream:** Real-time speech transcription powered by Android's native speech recognition APIs.
- 👆 **Interactive Word Lookup:** Tap any word in the live transcript to trigger a contextual translation sheet with definitions and audio pronunciation.
- ⭐ **Personal Vocabulary Bank:** Save key terms with a single tap. Organize your vocabulary by context tags (e.g., *University*, *Business*, *Daily Life*, *Slang*).
- 🗂️ **Lecture & Session Archive:** Automatically record, save, and review full transcripts from past lectures or meetings.
- 🧠 **Smart Flashcard Review:** Practice saved terms using spaced-repetition flashcards to retain vocabulary long-term.
- 🌙 **Material Design 3 & Dark Theme:** Built with modern Android UI standards for a smooth, battery-efficient dark/light mode experience.

---
## 🛠️ Architecture & Tech Stack

AuraLingo follows Google's recommended **Clean Architecture** principles paired with the **MVVM (Model-View-ViewModel)** design pattern for maximum scalability, testability, and code separation.

| Component | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | [Kotlin](https://kotlinlang.org/) | First-class language for modern Android development |
| **Minimum SDK** | **API 29 (Android 10.0)** | Ensures wide hardware coverage and modern API compatibility |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | Declarative UI toolkit with Material Design 3 |
| **Local Database** | [Room Database](https://developer.android.com/training/data-storage/room) | Offline storage for saved terms and transcript archives |
| **Async & Flow** | Kotlin Coroutines & `StateFlow` | Asynchronous audio stream processing and state management |
| **Speech Engine** | Android `SpeechRecognizer` API | On-device / cloud live speech processing |
| **Dependency Injection** | Hilt / Koin | Modular architecture and clean component testing |

### Architecture Diagram
┌──────────────────────────────────────────────────────────┐
│                      UI LAYER                            │
│           (Jetpack Compose + Material 3)                 │
└───────────────────────────┬──────────────────────────────┘
│
▼
┌──────────────────────────────────────────────────────────┐
│                    VIEWMODEL LAYER                       │
│              (StateFlow & UI State Management)           │
└───────────────┬───────────────────────────┬──────────────┘
│                           │
▼                           ▼
┌───────────────────────────────┐ ┌────────────────────────┐
│      SPEECH ENGINE API        │ │     LOCAL STORAGE      │
│  (Real-Time Audio Stream)     │ │    (Room Database)     │
└───────────────────────────────┘ └────────────────────────┘

---

## 📊 Database Schema (Room)

```kotlin
// Vocabulary Word Entity
@Entity(tableName = "vocabulary_words")
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalWord: String,
    val translatedWord: String,
    val contextSentence: String,
    val categoryTag: String, // e.g., "Lecture", "Daily", "Business"
    val timestamp: Long = System.currentTimeMillis()
)

// Lecture / Session Archive Entity
@Entity(tableName = "session_archives")
data class SessionArchive(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val fullTranscript: String,
    val dateTimestamp: Long
)
