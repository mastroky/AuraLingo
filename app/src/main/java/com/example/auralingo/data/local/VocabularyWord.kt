package com.example.auralingo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a saved vocabulary word in the personal glossary.
 */
@Entity(tableName = "vocabulary_words")
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalWord: String,
    val translatedWord: String,
    val contextSentence: String,
    val categoryTag: String,
    val timestamp: Long = System.currentTimeMillis()
)
