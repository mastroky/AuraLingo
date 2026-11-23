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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State representing the interactive flashcard training session.
 */
data class FlashcardUiState(
    val sessionQueue: List<VocabularyWord> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isReversed: Boolean = false,
    val isLoading: Boolean = false,
    val isSessionComplete: Boolean = false,
    val knownCount: Int = 0,
    val toReviewCount: Int = 0,
    val selectedLanguage: String? = null,
    val selectedPartOfSpeech: String? = null,
) {
    val currentCard: VocabularyWord?
        get() = if (sessionQueue.isNotEmpty() && currentIndex in sessionQueue.indices) sessionQueue[currentIndex] else null

    val totalCount: Int
        get() = sessionQueue.size

    val progress: Float
        get() = if (sessionQueue.isNotEmpty()) (currentIndex + 1).toFloat() / sessionQueue.size else 0f
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

    private val _isReversed = MutableStateFlow(false)
    val isReversed: StateFlow<Boolean> = _isReversed.asStateFlow()

    private val _sessionQueue = MutableStateFlow<List<VocabularyWord>>(emptyList())
    private val _currentIndex = MutableStateFlow(0)
    private val _isFlipped = MutableStateFlow(false)
    private val _isSessionComplete = MutableStateFlow(false)
    private val _knownCount = MutableStateFlow(0)
    private val _toReviewCount = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(true)

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
        // Initialize the first training session deck from repository
        viewModelScope.launch {
            val initialWords = reviewWords.first()
            startNewSession(initialWords)
        }
    }

    private data class SessionMetrics(
        val known: Int,
        val toReview: Int,
        val lang: String?,
        val pos: String?,
        val loading: Boolean,
    )

    /**
     * Combined UI State for the flashcard training session.
     */
    val uiState: StateFlow<FlashcardUiState> = combine(
        _sessionQueue,
        _currentIndex,
        _isFlipped,
        _isSessionComplete,
        combine(_knownCount, _toReviewCount, _selectedLanguage, _selectedPartOfSpeech, _isLoading) { known, review, lang, pos, loading ->
            SessionMetrics(known, review, lang, pos, loading)
        },
    ) { queue, index, flipped, completed, metrics ->
        val safeIndex = if (queue.isNotEmpty()) index.coerceIn(0, queue.size - 1) else 0
        FlashcardUiState(
            sessionQueue = queue,
            currentIndex = safeIndex,
            isFlipped = flipped,
            isLoading = metrics.loading,
            isSessionComplete = completed,
            knownCount = metrics.known,
            toReviewCount = metrics.toReview,
            selectedLanguage = metrics.lang,
            selectedPartOfSpeech = metrics.pos,
        )
    }.combine(_isReversed) { state, reversed ->
        state.copy(isReversed = reversed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FlashcardUiState(isLoading = true),
    )

    /**
     * Toggles the flipped state of the active card.
     */
    fun toggleCardFlip() {
        if (!_isSessionComplete.value) {
            _isFlipped.update { !it }
        }
    }

    /**
     * Toggles flashcard direction (original word first vs translation first).
     * Resets flip state so the new front face is cleanly shown.
     */
    fun toggleReverseDirection(value: Boolean? = null) {
        _isFlipped.value = false
        _isReversed.update { value ?: !it }
    }

    /**
     * Marks the current card as "Known" (or "Got It!"), advancing session progression.
     */
    fun markCardAsKnown() {
        val queue = _sessionQueue.value
        val index = _currentIndex.value
        if (queue.isEmpty() || index >= queue.size) return

        _knownCount.update { it + 1 }
        _isFlipped.value = false

        if (index + 1 < queue.size) {
            _currentIndex.update { it + 1 }
        } else {
            _isSessionComplete.value = true
        }
    }

    /**
     * Marks the current card as "To Review" (or "Review Again"), re-queuing it to the end of the session deck.
     */
    fun markCardForReview() {
        val queue = _sessionQueue.value
        val index = _currentIndex.value
        if (queue.isEmpty() || index >= queue.size) return

        val currentWord = queue[index]
        _toReviewCount.update { it + 1 }
        _isFlipped.value = false

        if (queue.size > 1) {
            _sessionQueue.update { it + currentWord }
            _currentIndex.update { it + 1 }
        }
    }

    /**
     * Restarts the training session cleanly with current filters.
     */
    fun restartSession() {
        viewModelScope.launch {
            val words = repository.getFilteredWords(selectedLanguage.value, selectedPartOfSpeech.value).first()
            startNewSession(words)
        }
    }

    /**
     * Sets the language filter and starts a fresh session.
     */
    fun setLanguageFilter(languageCode: String?) {
        _selectedLanguage.value = languageCode
        applyFiltersAndRestart()
    }

    /**
     * Sets the part of speech filter and starts a fresh session.
     */
    fun setPartOfSpeechFilter(partOfSpeech: String?) {
        _selectedPartOfSpeech.value = partOfSpeech
        applyFiltersAndRestart()
    }

    private fun applyFiltersAndRestart() {
        viewModelScope.launch {
            _isLoading.value = true
            val filteredList = repository.getFilteredWords(selectedLanguage.value, selectedPartOfSpeech.value).first()
            startNewSession(filteredList)
        }
    }

    private fun startNewSession(words: List<VocabularyWord>) {
        _sessionQueue.value = words
        _currentIndex.value = 0
        _isFlipped.value = false
        _isSessionComplete.value = false
        _knownCount.value = 0
        _toReviewCount.value = 0
        _isLoading.value = false
    }
}
