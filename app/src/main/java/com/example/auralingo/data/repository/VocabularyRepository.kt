package com.example.auralingo.data.repository

import com.example.auralingo.data.local.VocabularyDao
import com.example.auralingo.data.local.VocabularyWord
import kotlinx.coroutines.flow.Flow

/**
 * Repository per la gestione delle operazioni sui dati del vocabolario.
 * Agisce come livello di astrazione tra le sorgenti dati (Room Database) e i ViewModel.
 */
class VocabularyRepository(
    private val vocabularyDao: VocabularyDao,
) {

    /**
     * Flusso reattivo di tutte le parole salvate, ordinate dalla più recente.
     */
    val allWords: Flow<List<VocabularyWord>> = vocabularyDao.getAll()

    /**
     * Inserisce una nuova parola nel database locale Room.
     */
    suspend fun insertWord(word: VocabularyWord) {
        vocabularyDao.insert(word)
    }

    /**
     * Elimina una specifica parola dal database locale Room.
     */
    suspend fun deleteWord(word: VocabularyWord) {
        vocabularyDao.delete(word)
    }

    /**
     * Filtra le parole del vocabolario in base alla lingua specificata.
     */
    fun getWordsByLanguage(languageCode: String): Flow<List<VocabularyWord>> {
        return vocabularyDao.getWordsByLanguage(languageCode)
    }

    /**
     * Filtra le parole del vocabolario in base alla categoria grammaticale (Part of Speech).
     */
    fun getWordsByPartOfSpeech(partOfSpeech: String): Flow<List<VocabularyWord>> {
        return vocabularyDao.getWordsByPartOfSpeech(partOfSpeech)
    }

    /**
     * Filtra le parole del vocabolario combinando lingua e/o categoria grammaticale.
     */
    fun getFilteredWords(languageCode: String?, partOfSpeech: String?): Flow<List<VocabularyWord>> {
        return vocabularyDao.getFilteredWords(languageCode, partOfSpeech)
    }
}
