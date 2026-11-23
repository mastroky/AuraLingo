package com.example.auralingo.ui.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auralingo.data.local.AppDatabase
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the interactive word lookup bottom sheet.
 */
data class WordLookupUiState(
    val selectedWord: String? = null,
    val translatedWord: String = "",
    val contextSentence: String = "",
    val languageCode: String = "es",
    val partOfSpeech: String = "noun",
    val isBottomSheetVisible: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null,
)

/**
 * ViewModel managing word lookup, translation lookup, part-of-speech heuristics, and Room persistence.
 */
class LiveSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository by lazy {
        val database = AppDatabase.getInstance(application)
        VocabularyRepository(database.vocabularyDao())
    }

    private val _lookupState = MutableStateFlow(WordLookupUiState())
    val lookupState: StateFlow<WordLookupUiState> = _lookupState.asStateFlow()

    /**
     * Handles word selection from the transcript: translates the word, detects part of speech, and opens the bottom sheet.
     */
    fun onWordSelected(word: String, contextSentence: String, languageCode: String = "es") {
        val translation = fetchMockTranslation(word)
        val guessedPos = guessPartOfSpeech(word)
        _lookupState.update {
            it.copy(
                selectedWord = word,
                translatedWord = translation,
                contextSentence = contextSentence,
                languageCode = languageCode,
                partOfSpeech = guessedPos,
                isBottomSheetVisible = true,
                saveSuccessMessage = null,
            )
        }
    }

    /**
     * Updates the part of speech category based on user selection in the bottom sheet dropdown.
     */
    fun updatePartOfSpeech(newPos: String) {
        _lookupState.update { it.copy(partOfSpeech = newPos) }
    }

    /**
     * Dismisses the word lookup bottom sheet.
     */
    fun dismissBottomSheet() {
        _lookupState.update {
            it.copy(
                isBottomSheetVisible = false,
                selectedWord = null,
                translatedWord = "",
                contextSentence = "",
                partOfSpeech = "noun",
            )
        }
    }

    /**
     * Persists the selected word, translation, context, and standardized part of speech to the Room database.
     */
    fun saveWordToVocabulary(categoryTag: String = "Lecture") {
        val currentState = _lookupState.value
        val word = currentState.selectedWord ?: return
        val translation = currentState.translatedWord
        val sentence = currentState.contextSentence
        val langCode = currentState.languageCode
        val pos = currentState.partOfSpeech

        viewModelScope.launch {
            _lookupState.update { it.copy(isSaving = true) }
            try {
                val vocabularyWord = VocabularyWord(
                    originalWord = word,
                    translatedWord = translation.ifBlank { word },
                    contextSentence = sentence,
                    categoryTag = categoryTag,
                    languageCode = langCode,
                    partOfSpeech = pos,
                    timestamp = System.currentTimeMillis(),
                )
                repository.insertWord(vocabularyWord)
                _lookupState.update {
                    it.copy(
                        isSaving = false,
                        isBottomSheetVisible = false,
                        saveSuccessMessage = "Successfully saved '$word' to vocabulary!",
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

    /**
     * Temporary translation helper providing dictionary mock lookup.
     */
    private fun fetchMockTranslation(word: String): String {
        val dictionary = mapOf(
            "estudiando" to "studying",
            "universidad" to "university",
            "desarrollo" to "development",
            "imprescindible" to "essential",
            "investigación" to "research",
            "hola" to "hello",
            "ciao" to "hello / goodbye",
            "profesor" to "professor",
            "lección" to "lecture",
            "aprender" to "to learn",
        )
        return dictionary[word.lowercase()] ?: "(Translation: $word)"
    }
}
