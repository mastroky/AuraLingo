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
    val isBottomSheetVisible: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null
)

/**
 * ViewModel managing interactive word lookup, translation lookup, and persistence to Room database.
 */
class LiveSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository by lazy {
        val database = AppDatabase.getInstance(application)
        VocabularyRepository(database.vocabularyDao())
    }

    private val _lookupState = MutableStateFlow(WordLookupUiState())
    val lookupState: StateFlow<WordLookupUiState> = _lookupState.asStateFlow()

    /**
     * Handles word selection from the transcript: translates the word and opens the lookup bottom sheet.
     */
    fun onWordSelected(word: String, contextSentence: String) {
        val translation = fetchMockTranslation(word)
        _lookupState.update {
            it.copy(
                selectedWord = word,
                translatedWord = translation,
                contextSentence = contextSentence,
                isBottomSheetVisible = true,
                saveSuccessMessage = null
            )
        }
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
                contextSentence = ""
            )
        }
    }

    /**
     * Persists the selected word and its surrounding sentence to the Room database via VocabularyRepository.
     */
    fun saveWordToVocabulary(categoryTag: String = "Lecture") {
        val currentState = _lookupState.value
        val word = currentState.selectedWord ?: return
        val translation = currentState.translatedWord
        val sentence = currentState.contextSentence

        viewModelScope.launch {
            _lookupState.update { it.copy(isSaving = true) }
            try {
                val vocabularyWord = VocabularyWord(
                    originalWord = word,
                    translatedWord = translation.ifBlank { word },
                    contextSentence = sentence,
                    categoryTag = categoryTag,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertWord(vocabularyWord)
                _lookupState.update {
                    it.copy(
                        isSaving = false,
                        isBottomSheetVisible = false,
                        saveSuccessMessage = "Successfully saved '$word' to vocabulary!"
                    )
                }
            } catch (e: Exception) {
                _lookupState.update { it.copy(isSaving = false) }
            }
        }
    }

    /**
     * Clears any active success messages.
     */
    fun clearMessage() {
        _lookupState.update { it.copy(saveSuccessMessage = null) }
    }

    /**
     * Temporary translation helper providing intelligent dictionary mock lookup.
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
            "aprender" to "to learn"
        )
        return dictionary[word.lowercase()] ?: "(Translation: $word)"
    }
}
