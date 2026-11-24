package com.example.auralingo.data.repository

import android.content.Context
import com.example.auralingo.data.auth.SessionManager
import com.example.auralingo.data.local.UserDatabaseManager
import com.example.auralingo.data.local.VocabularyDao
import com.example.auralingo.data.local.VocabularyWord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Repository handling per-user vocabulary data operations reactively mapped from active SessionManager user session.
 */
class VocabularyRepository(
    private val context: Context? = null,
    private val sessionManager: SessionManager? = context?.let { SessionManager.getInstance(it) },
    private val userDatabaseManager: UserDatabaseManager = UserDatabaseManager.getInstance(),
    private val directDao: VocabularyDao? = null,
) {

    /**
     * Fallback constructor accepting a direct VocabularyDao for direct testing or legacy DAO usage.
     */
    constructor(vocabularyDao: VocabularyDao) : this(
        context = null,
        sessionManager = null,
        userDatabaseManager = UserDatabaseManager.getInstance(),
        directDao = vocabularyDao,
    )

    /**
     * Reactive stream of all vocabulary words ordered by newest first for active user session.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val allWords: Flow<List<VocabularyWord>> = if (directDao != null) {
        directDao.getAll()
    } else if (context != null && sessionManager != null) {
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId != null) {
                userDatabaseManager.getDatabaseForUser(context, userId).vocabularyDao().getAll()
            } else {
                flowOf(emptyList())
            }
        }
    } else {
        flowOf(emptyList())
    }

    /**
     * Inserts a vocabulary word after normalizing language code and part of speech into active user database.
     */
    suspend fun insertWord(word: VocabularyWord) {
        val normalizedWord = word.copy(
            originalWord = word.originalWord.trim(),
            translatedWord = word.translatedWord.trim(),
            contextSentence = word.contextSentence.trim(),
            languageCode = normalizeLanguageCode(word.languageCode),
            partOfSpeech = normalizePartOfSpeech(word.partOfSpeech),
        )

        if (directDao != null) {
            directDao.insert(normalizedWord)
            return
        }

        if (context != null && sessionManager != null) {
            val userId = sessionManager.currentUserId.firstOrNull() ?: return
            val dao = userDatabaseManager.getDatabaseForUser(context, userId).vocabularyDao()
            dao.insert(normalizedWord)
        }
    }

    /**
     * Deletes a specific vocabulary word from active user database.
     */
    suspend fun deleteWord(word: VocabularyWord) {
        if (directDao != null) {
            directDao.delete(word)
            return
        }

        if (context != null && sessionManager != null) {
            val userId = sessionManager.currentUserId.firstOrNull() ?: return
            val dao = userDatabaseManager.getDatabaseForUser(context, userId).vocabularyDao()
            dao.delete(word)
        }
    }

    /**
     * Retrieves filtered vocabulary words based on language code and part of speech for active user session.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFilteredWords(languageCode: String?, partOfSpeech: String?): Flow<List<VocabularyWord>> {
        val normalizedLang = languageCode?.let { normalizeLanguageCode(it) }
        val normalizedPos = partOfSpeech?.let { normalizePartOfSpeech(it) }

        if (directDao != null) {
            return directDao.getFilteredWords(normalizedLang, normalizedPos)
        }

        if (context == null || sessionManager == null) {
            return flowOf(emptyList())
        }

        return sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId != null) {
                userDatabaseManager.getDatabaseForUser(context, userId).vocabularyDao()
                    .getFilteredWords(normalizedLang, normalizedPos)
            } else {
                flowOf(emptyList())
            }
        }
    }

    /**
     * Returns distinct available languages stored in active user database.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDistinctLanguages(): Flow<List<String>> {
        if (directDao != null) {
            return directDao.getDistinctLanguages()
        }

        if (context == null || sessionManager == null) {
            return flowOf(emptyList())
        }

        return sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId != null) {
                userDatabaseManager.getDatabaseForUser(context, userId).vocabularyDao().getDistinctLanguages()
            } else {
                flowOf(emptyList())
            }
        }
    }

    /**
     * Returns distinct available parts of speech stored in active user database.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDistinctPartsOfSpeech(): Flow<List<String>> {
        if (directDao != null) {
            return directDao.getDistinctPartsOfSpeech()
        }

        if (context == null || sessionManager == null) {
            return flowOf(emptyList())
        }

        return sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId != null) {
                userDatabaseManager.getDatabaseForUser(context, userId).vocabularyDao().getDistinctPartsOfSpeech()
            } else {
                flowOf(emptyList())
            }
        }
    }

    private fun normalizeLanguageCode(tag: String): String {
        val cleaned = tag.substringBefore("-").lowercase().trim()
        val validLanguages = setOf("en", "es", "it", "fr", "de")
        return if (cleaned in validLanguages) cleaned else "en"
    }

    private fun normalizePartOfSpeech(pos: String): String {
        val cleaned = pos.lowercase().trim()
        val validParts = setOf("noun", "verb", "adjective", "adverb", "phrase", "other")
        return if (cleaned in validParts) cleaned else "other"
    }
}
