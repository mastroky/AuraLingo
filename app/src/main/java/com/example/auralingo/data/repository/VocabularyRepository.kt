package com.example.auralingo.data.repository

import com.example.auralingo.data.local.VocabularyDao
import com.example.auralingo.data.local.VocabularyWord
import kotlinx.coroutines.flow.Flow

/**
 * Repository handling all vocabulary data operations, centralizing data normalization
 * and abstracting the Room Database data source from the ViewModels.
 */
class VocabularyRepository(
    private val vocabularyDao: VocabularyDao,
) {

    /**
     * Reactive stream of all vocabulary words ordered by newest first.
     */
    val allWords: Flow<List<VocabularyWord>> = vocabularyDao.getAll()

    /**
     * Inserts a vocabulary word after normalizing language code and part of speech.
     */
    suspend fun insertWord(word: VocabularyWord) {
        val normalizedWord = word.copy(
            originalWord = word.originalWord.trim(),
            translatedWord = word.translatedWord.trim(),
            contextSentence = word.contextSentence.trim(),
            languageCode = normalizeLanguageCode(word.languageCode),
            partOfSpeech = normalizePartOfSpeech(word.partOfSpeech),
        )
        vocabularyDao.insert(normalizedWord)
    }

    /**
     * Deletes a specific vocabulary word from the database.
     */
    suspend fun deleteWord(word: VocabularyWord) {
        vocabularyDao.delete(word)
    }

    /**
     * Retrieves filtered vocabulary words based on language code and part of speech.
     */
    fun getFilteredWords(languageCode: String?, partOfSpeech: String?): Flow<List<VocabularyWord>> {
        val normalizedLang = languageCode?.let { normalizeLanguageCode(it) }
        val normalizedPos = partOfSpeech?.let { normalizePartOfSpeech(it) }
        return vocabularyDao.getFilteredWords(normalizedLang, normalizedPos)
    }

    /**
     * Returns distinct available languages stored in the database.
     */
    fun getDistinctLanguages(): Flow<List<String>> {
        return vocabularyDao.getDistinctLanguages()
    }

    /**
     * Returns distinct available parts of speech stored in the database.
     */
    fun getDistinctPartsOfSpeech(): Flow<List<String>> {
        return vocabularyDao.getDistinctPartsOfSpeech()
    }

    /**
     * Normalizes a language tag (e.g. "es-ES" -> "es", defaults to "en" if blank).
     */
    private fun normalizeLanguageCode(tag: String): String {
        val cleaned = tag.substringBefore("-").lowercase().trim()
        val validLanguages = setOf("en", "es", "it", "fr", "de")
        return if (cleaned in validLanguages) cleaned else "en"
    }

    /**
     * Normalizes a part of speech key (defaults to "other" if invalid).
     */
    private fun normalizePartOfSpeech(pos: String): String {
        val cleaned = pos.lowercase().trim()
        val validParts = setOf("noun", "verb", "adjective", "adverb", "phrase", "other")
        return if (cleaned in validParts) cleaned else "other"
    }
}
