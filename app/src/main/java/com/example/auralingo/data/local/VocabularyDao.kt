package com.example.auralingo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing read and write operations on the vocabulary_words table.
 */
@Dao
interface VocabularyDao {

    /**
     * Inserts a new vocabulary word into the database.
     */
    @Insert
    suspend fun insert(word: VocabularyWord)

    /**
     * Deletes a vocabulary word from the database.
     */
    @Delete
    suspend fun delete(word: VocabularyWord)

    /**
     * Returns all saved vocabulary words ordered by newest first.
     */
    @Query("SELECT * FROM vocabulary_words ORDER BY timestamp DESC")
    fun getAll(): Flow<List<VocabularyWord>>

    /**
     * Filters words dynamically by language code and/or part of speech.
     */
    @Query(
        "SELECT * FROM vocabulary_words WHERE (:languageCode IS NULL OR language_code = :languageCode) AND (:partOfSpeech IS NULL OR part_of_speech = :partOfSpeech) ORDER BY timestamp DESC",
    )
    fun getFilteredWords(languageCode: String?, partOfSpeech: String?): Flow<List<VocabularyWord>>

    /**
     * Returns a distinct list of language codes stored in the database.
     */
    @Query("SELECT DISTINCT language_code FROM vocabulary_words ORDER BY language_code ASC")
    fun getDistinctLanguages(): Flow<List<String>>

    /**
     * Returns a distinct list of parts of speech stored in the database.
     */
    @Query("SELECT DISTINCT part_of_speech FROM vocabulary_words ORDER BY part_of_speech ASC")
    fun getDistinctPartsOfSpeech(): Flow<List<String>>
}
