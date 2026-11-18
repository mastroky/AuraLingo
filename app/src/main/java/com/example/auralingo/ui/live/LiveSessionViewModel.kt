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
 * Stato della UI per il bottom sheet interattivo di ricerca e salvataggio parole.
 */
data class WordLookupUiState(
    val selectedWord: String? = null,
    val translatedWord: String = "",
    val contextSentence: String = "",
    val languageCode: String = "es",
    val isBottomSheetVisible: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessMessage: String? = null,
)

/**
 * ViewModel che gestisce la ricerca delle parole, la traduzione e il salvataggio nel database Room.
 */
class LiveSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository by lazy {
        val database = AppDatabase.getInstance(application)
        VocabularyRepository(database.vocabularyDao())
    }

    private val _lookupState = MutableStateFlow(WordLookupUiState())
    val lookupState: StateFlow<WordLookupUiState> = _lookupState.asStateFlow()

    /**
     * Gestisce la selezione di una parola nel testo: ottiene la traduzione e apre il bottom sheet.
     */
    fun onWordSelected(word: String, contextSentence: String, languageCode: String = "es") {
        val translation = fetchMockTranslation(word)
        _lookupState.update {
            it.copy(
                selectedWord = word,
                translatedWord = translation,
                contextSentence = contextSentence,
                languageCode = languageCode,
                isBottomSheetVisible = true,
                saveSuccessMessage = null,
            )
        }
    }

    /**
     * Chiude il bottom sheet di ricerca della parola.
     */
    fun dismissBottomSheet() {
        _lookupState.update {
            it.copy(
                isBottomSheetVisible = false,
                selectedWord = null,
                translatedWord = "",
                contextSentence = "",
            )
        }
    }

    /**
     * Salva la parola selezionata con la frase di contesto ed il codice lingua nel database Room.
     */
    fun saveWordToVocabulary(categoryTag: String = "Lecture") {
        val currentState = _lookupState.value
        val word = currentState.selectedWord ?: return
        val translation = currentState.translatedWord
        val sentence = currentState.contextSentence
        val langCode = currentState.languageCode

        viewModelScope.launch {
            _lookupState.update { it.copy(isSaving = true) }
            try {
                val vocabularyWord = VocabularyWord(
                    originalWord = word,
                    translatedWord = translation.ifBlank { word },
                    contextSentence = sentence,
                    categoryTag = categoryTag,
                    languageCode = langCode,
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
     * Rimuove i messaggi di conferma di salvataggio attivi.
     */
    fun clearMessage() {
        _lookupState.update { it.copy(saveSuccessMessage = null) }
    }

    /**
     * Dizionario di ricerca mock temporaneo per le traduzioni.
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
