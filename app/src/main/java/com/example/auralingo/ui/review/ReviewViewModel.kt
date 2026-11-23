package com.example.auralingo.ui.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auralingo.data.local.AppDatabase
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.data.repository.VocabularyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel managing flashcards and active recall training pool reactively from the Room database.
 */
class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository by lazy {
        val database = AppDatabase.getInstance(application)
        VocabularyRepository(database.vocabularyDao())
    }

    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    private val _selectedPartOfSpeech = MutableStateFlow<String?>(null)
    val selectedPartOfSpeech: StateFlow<String?> = _selectedPartOfSpeech.asStateFlow()

    /**
     * Reactively filtered list of vocabulary words for active recall training.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val reviewWords: StateFlow<List<VocabularyWord>> = combine(
        _selectedLanguage,
        _selectedPartOfSpeech
    ) { lang, pos -> Pair(lang, pos) }
        .flatMapLatest { (lang, pos) ->
            repository.getFilteredWords(lang, pos)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Sets the language filter for review flashcards.
     */
    fun setLanguageFilter(languageCode: String?) {
        _selectedLanguage.value = languageCode
    }

    /**
     * Sets the part of speech filter for review flashcards.
     */
    fun setPartOfSpeechFilter(partOfSpeech: String?) {
        _selectedPartOfSpeech.value = partOfSpeech
    }
}
