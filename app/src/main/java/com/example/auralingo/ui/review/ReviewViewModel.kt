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
import kotlinx.coroutines.flow.update

/**
 * UI State representing the interactive flashcard training session.
 */
data class FlashcardUiState(
    val cards: List<VocabularyWord> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = false,
    val selectedLanguage: String? = null,
    val selectedPartOfSpeech: String? = null,
) {
    val currentCard: VocabularyWord?
        get() = if (cards.isNotEmpty() && currentIndex in cards.indices) cards[currentIndex] else null

    val totalCount: Int
        get() = cards.size

    val progress: Float
        get() = if (cards.isNotEmpty()) (currentIndex + 1).toFloat() / cards.size else 0f
}

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

    private val _currentIndex = MutableStateFlow(0)
    private val _isFlipped = MutableStateFlow(false)

    /**
     * Reactively filtered list of vocabulary words for active recall training.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val reviewWords: StateFlow<List<VocabularyWord>> = combine(
        _selectedLanguage,
        _selectedPartOfSpeech,
    ) { lang, pos -> Pair(lang, pos) }
        .flatMapLatest { (lang, pos) ->
            repository.getFilteredWords(lang, pos)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    /**
     * Combined UI State for the flashcard training session.
     */
    val uiState: StateFlow<FlashcardUiState> = combine(
        reviewWords,
        _currentIndex,
        _isFlipped,
        _selectedLanguage,
        _selectedPartOfSpeech,
    ) { words, index, flipped, lang, pos ->
        val safeIndex = if (words.isNotEmpty()) index.coerceIn(0, words.size - 1) else 0
        FlashcardUiState(
            cards = words,
            currentIndex = safeIndex,
            isFlipped = flipped,
            isLoading = false,
            selectedLanguage = lang,
            selectedPartOfSpeech = pos,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FlashcardUiState(isLoading = true),
    )

    /**
     * Toggles the flipped state of the active card.
     */
    fun toggleCardFlip() {
        _isFlipped.update { !it }
    }

    /**
     * Moves to the next card and resets the flip state.
     */
    fun nextCard() {
        val total = reviewWords.value.size
        if (total > 0) {
            _isFlipped.value = false
            _currentIndex.update { (it + 1) % total }
        }
    }

    /**
     * Moves to the previous card and resets the flip state.
     */
    fun previousCard() {
        val total = reviewWords.value.size
        if (total > 0) {
            _isFlipped.value = false
            _currentIndex.update { if (it > 0) it - 1 else total - 1 }
        }
    }

    /**
     * Sets the language filter for review flashcards and resets session progress.
     */
    fun setLanguageFilter(languageCode: String?) {
        _isFlipped.value = false
        _currentIndex.value = 0
        _selectedLanguage.value = languageCode
    }

    /**
     * Sets the part of speech filter for review flashcards and resets session progress.
     */
    fun setPartOfSpeechFilter(partOfSpeech: String?) {
        _isFlipped.value = false
        _currentIndex.value = 0
        _selectedPartOfSpeech.value = partOfSpeech
    }
}
