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
import kotlinx.coroutines.launch

/**
 * UI State representing the interactive flashcard training session.
 */
data class FlashcardUiState(
    val activeDeck: List<VocabularyWord> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
    val selectedLanguage: String? = null,
    val selectedPartOfSpeech: String? = null,
) {
    val currentCard: VocabularyWord?
        get() = if (activeDeck.isNotEmpty() && currentIndex in activeDeck.indices) activeDeck[currentIndex] else null

    val totalCount: Int
        get() = activeDeck.size

    val progress: Float
        get() = if (activeDeck.isNotEmpty()) (currentIndex + 1).toFloat() / activeDeck.size else 0f
}

/**
 * ViewModel managing flashcards and active recall training session progression reactively from the Room database.
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

    private val _activeDeck = MutableStateFlow<List<VocabularyWord>>(emptyList())
    private val _currentIndex = MutableStateFlow(0)
    private val _isFlipped = MutableStateFlow(false)
    private val _isCompleted = MutableStateFlow(false)
    private val _knownCount = MutableStateFlow(0)
    private val _reviewCount = MutableStateFlow(0)

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

    init {
        // Automatically sync active deck when underlying database reviewWords change
        viewModelScope.launch {
            reviewWords.collect { words ->
                restartSessionWithWords(words)
            }
        }
    }

    private data class MetaState(
        val known: Int,
        val review: Int,
        val lang: String?,
        val pos: String?,
    )

    /**
     * Combined UI State for the flashcard training session.
     */
    val uiState: StateFlow<FlashcardUiState> = combine(
        _activeDeck,
        _currentIndex,
        _isFlipped,
        _isCompleted,
        combine(_knownCount, _reviewCount, _selectedLanguage, _selectedPartOfSpeech) { known, review, lang, pos ->
            MetaState(known, review, lang, pos)
        },
    ) { deck, index, flipped, completed, meta ->
        val safeIndex = if (deck.isNotEmpty()) index.coerceIn(0, deck.size - 1) else 0
        FlashcardUiState(
            activeDeck = deck,
            currentIndex = safeIndex,
            isFlipped = flipped,
            isLoading = false,
            isCompleted = completed,
            knownCount = meta.known,
            reviewCount = meta.review,
            selectedLanguage = meta.lang,
            selectedPartOfSpeech = meta.pos,
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
        if (!_isCompleted.value) {
            _isFlipped.update { !it }
        }
    }

    /**
     * Marks the current card as "Known" (or "Got It!"), advancing session progression.
     */
    fun markCardAsKnown() {
        val deck = _activeDeck.value
        val index = _currentIndex.value
        if (deck.isEmpty() || index >= deck.size) return

        _knownCount.update { it + 1 }
        _isFlipped.value = false

        if (index + 1 < deck.size) {
            _currentIndex.update { it + 1 }
        } else {
            _isCompleted.value = true
        }
    }

    /**
     * Marks the current card as "To Review" (or "Review Again"), re-queuing it to the end of the deck.
     */
    fun markCardForReview() {
        val deck = _activeDeck.value
        val index = _currentIndex.value
        if (deck.isEmpty() || index >= deck.size) return

        val currentWord = deck[index]
        _reviewCount.update { it + 1 }
        _isFlipped.value = false

        // Re-queue card to the end of the deck
        _activeDeck.update { it + currentWord }
        _currentIndex.update { it + 1 }
    }

    /**
     * Restarts the training session with the current deck pool.
     */
    fun restartSession() {
        restartSessionWithWords(reviewWords.value)
    }

    private fun restartSessionWithWords(words: List<VocabularyWord>) {
        _activeDeck.value = words
        _currentIndex.value = 0
        _isFlipped.value = false
        _isCompleted.value = false
        _knownCount.value = 0
        _reviewCount.value = 0
    }

    /**
     * Sets the language filter for review flashcards and resets session progress.
     */
    fun setLanguageFilter(languageCode: String?) {
        _selectedLanguage.value = languageCode
    }

    /**
     * Sets the part of speech filter for review flashcards and resets session progress.
     */
    fun setPartOfSpeechFilter(partOfSpeech: String?) {
        _selectedPartOfSpeech.value = partOfSpeech
    }
}
