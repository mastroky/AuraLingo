package com.example.auralingo.data.repository

import com.example.auralingo.data.local.VocabularyDao
import com.example.auralingo.data.local.VocabularyWord
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing vocabulary data operations, acting as a clean abstraction layer
 * between the data sources (Room Database) and the ViewModel.
 */
class VocabularyRepository(
    private val vocabularyDao: VocabularyDao
) {

    /**
     * Emits a stream of all saved vocabulary words ordered by timestamp descending.
     */
    val allWords: Flow<List<VocabularyWord>> = vocabularyDao.getAll()

    /**
     * Inserts a new vocabulary word into the local Room database.
     */
    suspend fun insertWord(word: VocabularyWord) {
        vocabularyDao.insert(word)
    }

    /**
     * Deletes a specific vocabulary word from the local Room database.
     */
    suspend fun deleteWord(word: VocabularyWord) {
        vocabularyDao.delete(word)
    }
}
