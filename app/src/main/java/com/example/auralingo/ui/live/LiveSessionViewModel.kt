package com.example.auralingo.ui.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auralingo.data.auth.AuthRepository
import com.example.auralingo.data.auth.SessionManager
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.data.repository.TranslationRepository
import com.example.auralingo.data.repository.TranslationState
import com.example.auralingo.data.repository.VocabularyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the interactive word lookup bottom sheet with real-time ML Kit translation support.
 */
data class WordLookupUiState(
    val selectedWord: String? = null,
    val translatedWord: String = "",
    val editableTranslation: String = "",
    val contextSentence: String = "",
    val sourceLanguageCode: String = "es",
    val targetLanguageCode: String = "en",
    val partOfSpeech: String = "noun",
    val isBottomSheetVisible: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null,
    val translationState: TranslationState = TranslationState.Loading,
)

/**
 * ViewModel managing real-time ML Kit translation, word lookup, heuristics, and Room persistence.
 */
class LiveSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository by lazy {
        VocabularyRepository(application)
    }

    private val authRepository: AuthRepository by lazy {
        AuthRepository.getInstance(application)
    }

    private val sessionManager: SessionManager by lazy {
        SessionManager.getInstance(application)
    }

    private val translationRepository: TranslationRepository by lazy {
        TranslationRepository.getInstance()
    }

    private val _lookupState = MutableStateFlow(WordLookupUiState())
    val lookupState: StateFlow<WordLookupUiState> = _lookupState.asStateFlow()

    private val _userNativeLanguage = MutableStateFlow("en")

    private var translationJob: Job? = null

    init {
        // Observe active user profile for native target language code
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { profile ->
                val nativeLang = profile?.nativeLanguageCode?.ifBlank { "en" } ?: "en"
                _userNativeLanguage.value = nativeLang.substringBefore("-").lowercase()
            }
        }

        // Observe session user ID changes to clear lookup state
        viewModelScope.launch {
            sessionManager.currentUserId.collect { _ ->
                dismissBottomSheet()
                clearMessage()
            }
        }
    }

    /**
     * Handles word selection from transcript: cancels any in-flight translation job, triggers ML Kit translation flow, and opens bottom sheet.
     */
    fun onWordSelected(word: String, contextSentence: String, languageCode: String = "es") {
        translationJob?.cancel()
        val guessedPos = guessPartOfSpeech(word)
        val normalizedSrc = languageCode.substringBefore("-").lowercase()
        val normalizedTgt = _userNativeLanguage.value.substringBefore("-").lowercase()

        _lookupState.update {
            it.copy(
                selectedWord = word,
                translatedWord = "",
                editableTranslation = "",
                contextSentence = contextSentence,
                sourceLanguageCode = normalizedSrc,
                targetLanguageCode = normalizedTgt,
                partOfSpeech = guessedPos,
                isBottomSheetVisible = true,
                saveSuccessMessage = null,
                translationState = TranslationState.Loading,
            )
        }

        translationJob = viewModelScope.launch {
            translationRepository.translate(word, normalizedSrc, normalizedTgt).collect { state ->
                _lookupState.update { current ->
                    current.copy(
                        translationState = state,
                        translatedWord = if (state is TranslationState.Success) state.translatedText else current.translatedWord,
                        editableTranslation = if (state is TranslationState.Success && current.editableTranslation.isBlank()) state.translatedText else current.editableTranslation,
                    )
                }
            }
        }
    }

    /**
     * Updates editable translation text when user makes manual tweaks.
     */
    fun onTranslationChanged(newText: String) {
        _lookupState.update { it.copy(editableTranslation = newText) }
    }

    /**
     * Updates the part of speech category based on user selection in the bottom sheet dropdown.
     */
    fun updatePartOfSpeech(newPos: String) {
        _lookupState.update { it.copy(partOfSpeech = newPos) }
    }

    /**
     * Dismisses the word lookup bottom sheet and cancels any active translation.
     */
    fun dismissBottomSheet() {
        translationJob?.cancel()
        _lookupState.update {
            it.copy(
                isBottomSheetVisible = false,
                selectedWord = null,
                translatedWord = "",
                editableTranslation = "",
                contextSentence = "",
                partOfSpeech = "noun",
                translationState = TranslationState.Loading,
            )
        }
    }

    /**
     * Persists the selected word, editable translation, context, and part of speech to the Room database.
     */
    fun saveWordToVocabulary(categoryTag: String = "Lecture") {
        val currentState = _lookupState.value
        val word = currentState.selectedWord ?: return
        val translation = currentState.editableTranslation.ifBlank { currentState.translatedWord.ifBlank { word } }
        val sentence = currentState.contextSentence
        val langCode = currentState.sourceLanguageCode
        val pos = currentState.partOfSpeech
        val resolvedCategoryTag = "$langCode • $pos"

        viewModelScope.launch {
            _lookupState.update { it.copy(isSaving = true) }
            try {
                val vocabularyWord = VocabularyWord(
                    originalWord = word,
                    translatedWord = translation,
                    contextSentence = sentence,
                    categoryTag = resolvedCategoryTag,
                    languageCode = langCode,
                    partOfSpeech = pos,
                    timestamp = System.currentTimeMillis(),
                )
                repository.insertWord(vocabularyWord)
                _lookupState.update {
                    it.copy(
                        isSaving = false,
                        isBottomSheetVisible = false,
                        saveSuccessMessage = "Successfully saved '$word' ($langCode) to vocabulary!",
                    )
                }
            } catch (_: Exception) {
                _lookupState.update { it.copy(isSaving = false) }
            }
        }
    }

    /**
     * Clears active success messages.
     */
    fun clearMessage() {
        _lookupState.update { it.copy(saveSuccessMessage = null) }
    }

    /**
     * Lightweight heuristic guessing mechanism for part of speech categorization.
     */
    private fun guessPartOfSpeech(word: String): String {
        val trimmed = word.trim()
        if (trimmed.contains(" ")) return "phrase"
        val lower = trimmed.lowercase()
        return when {
            lower.endsWith("ar") || lower.endsWith("er") || lower.endsWith("ir") || lower.endsWith("ing") || lower.endsWith("ed") -> "verb"
            lower.endsWith("ly") || lower.endsWith("mente") -> "adverb"
            lower.endsWith("able") || lower.endsWith("ible") || lower.endsWith("al") || lower.endsWith("ous") || lower.endsWith("ivo") || lower.endsWith("ivo/a") -> "adjective"
            else -> "noun"
        }
    }

    override fun onCleared() {
        super.onCleared()
        translationJob?.cancel()
        try {
            translationRepository.close()
        } catch (_: Exception) {
        }
    }
}
