package com.example.auralingo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a saved vocabulary word in AuraLingo's personal glossary.
 * Supports multi-language partitioning and grammatical categories (Part of Speech).
 *
 * @property id Unique identifier for the vocabulary word.
 * @property originalWord Original word saved.
 * @property translatedWord Translation of the word.
 * @property contextSentence Surrounding context sentence where the word appears.
 * @property categoryTag Category tag or topic (e.g. "Lecture").
 * @property languageCode ISO 639-1 two-letter lowercase language code (e.g. "en", "es", "it", "fr", "de").
 * @property partOfSpeech Normalized grammatical category ("noun", "verb", "adjective", "adverb", "phrase", "other").
 * @property timestamp Epoch timestamp in milliseconds when saved.
 */
@Entity(
    tableName = "vocabulary_words",
    indices = [
        Index(value = ["language_code"]),
        Index(value = ["part_of_speech"]),
    ],
)
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalWord: String,
    val translatedWord: String,
    val contextSentence: String,
    val categoryTag: String,
    @ColumnInfo(name = "language_code")
    val languageCode: String,
    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,
    val timestamp: Long = System.currentTimeMillis(),
)
