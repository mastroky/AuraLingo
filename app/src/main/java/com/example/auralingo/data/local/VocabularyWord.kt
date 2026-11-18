package com.example.auralingo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity che rappresenta una parola salvata nel glossario personale di AuraLingo.
 *
 * @property id Identificatore unico per la parola.
 * @property originalWord Parola originale salvata.
 * @property translatedWord Traduzione della parola.
 * @property contextSentence Frase di contesto in cui compare la parola.
 * @property categoryTag Categoria o tag della parola (es. "Lecture").
 * @property languageCode Codice della lingua ISO (es. "es", "en", "it", "fr", "de").
 * @property partOfSpeech Categoria grammaticale (es. "noun", "verb", "adjective", "other").
 * @property timestamp Data/ora di salvataggio in millisecondi.
 */
@Entity(tableName = "vocabulary_words")
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalWord: String,
    val translatedWord: String,
    val contextSentence: String,
    val categoryTag: String,
    @ColumnInfo(name = "language_code", defaultValue = "es")
    val languageCode: String = "es",
    @ColumnInfo(name = "part_of_speech", defaultValue = "other")
    val partOfSpeech: String = "other",
    val timestamp: Long = System.currentTimeMillis(),
)
