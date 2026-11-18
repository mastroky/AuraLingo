package com.example.auralingo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) per gestire le operazioni di lettura e scrittura sulla tabella vocabulary_words.
 */
@Dao
interface VocabularyDao {

    /**
     * Inserisce una nuova parola nel database.
     */
    @Insert
    suspend fun insert(word: VocabularyWord)

    /**
     * Elimina una parola dal database.
     */
    @Delete
    suspend fun delete(word: VocabularyWord)

    /**
     * Restituisce tutte le parole salvate, ordinate dalla più recente alla più datata.
     */
    @Query("SELECT * FROM vocabulary_words ORDER BY timestamp DESC")
    fun getAll(): Flow<List<VocabularyWord>>

    /**
     * Filtra le parole in base al codice della lingua (es. "es", "en", "it").
     */
    @Query("SELECT * FROM vocabulary_words WHERE language_code = :languageCode ORDER BY timestamp DESC")
    fun getWordsByLanguage(languageCode: String): Flow<List<VocabularyWord>>

    /**
     * Filtra le parole in base alla categoria grammaticale (es. "noun", "verb", "adjective").
     */
    @Query("SELECT * FROM vocabulary_words WHERE part_of_speech = :partOfSpeech ORDER BY timestamp DESC")
    fun getWordsByPartOfSpeech(partOfSpeech: String): Flow<List<VocabularyWord>>

    /**
     * Filtra le parole combinando opzionalmente lingua e/o categoria grammaticale.
     */
    @Query("SELECT * FROM vocabulary_words WHERE (:languageCode IS NULL OR language_code = :languageCode) AND (:partOfSpeech IS NULL OR part_of_speech = :partOfSpeech) ORDER BY timestamp DESC")
    fun getFilteredWords(languageCode: String?, partOfSpeech: String?): Flow<List<VocabularyWord>>
}
