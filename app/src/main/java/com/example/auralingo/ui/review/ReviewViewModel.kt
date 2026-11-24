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
 * UI State representing the interactive flashcard training session and active filter state.
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
    val availableDeckCount: Int = 0,
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
     * Reactively filtered list of vocabulary words for active recall training matching current filters.
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
        // Collect reactive review words flow and initialize session deck when data arrives from Room DB
        viewModelScope.launch {
            reviewWords.collect { words ->
                if (_sessionQueue.value.isEmpty() && !_isSessionComplete.value && words.isNotEmpty()) {
                    startNewSession(words)
                } else if (words.isEmpty()) {
                    _isLoading.value = false
                }
            }
        }
    }

    private data class FilterMetrics(
        val lang: String?,
        val pos: String?,
        val availableCount: Int,
    )

    private data class SessionMetrics(
        val known: Int,
        val toReview: Int,
        val loading: Boolean,
    )

    private data class BaseSessionState(
        val queue: List<VocabularyWord>,
        val index: Int,
        val flipped: Boolean,
        val completed: Boolean,
    )

    private val filterMetrics = combine(_selectedLanguage, _selectedPartOfSpeech, reviewWords) { lang, pos, words ->
        FilterMetrics(lang, pos, words.size)
    }

    private val sessionMetrics = combine(_knownCount, _toReviewCount, _isLoading) { known, review, loading ->
        SessionMetrics(known, review, loading)
    }

    private val baseSessionState = combine(_sessionQueue, _currentIndex, _isFlipped, _isSessionComplete) { queue, index, flipped, completed ->
        BaseSessionState(queue, index, flipped, completed)
    }

    /**
     * Combined UI State for the flashcard training session and active filters.
     */
    val uiState: StateFlow<FlashcardUiState> = combine(
        baseSessionState,
        filterMetrics,
        sessionMetrics,
    ) { base, filter, session ->
        val safeIndex = if (base.queue.isNotEmpty()) base.index.coerceIn(0, base.queue.size - 1) else 0
        FlashcardUiState(
            sessionQueue = base.queue,
            currentIndex = safeIndex,
            isFlipped = base.flipped,
            isLoading = session.loading,
            isSessionComplete = base.completed,
            knownCount = session.known,
            toReviewCount = session.toReview,
            selectedLanguage = filter.lang,
            selectedPartOfSpeech = filter.pos,
            availableDeckCount = filter.availableCount,
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
            val words = repository.getFilteredWords(_selectedLanguage.value, _selectedPartOfSpeech.value).first()
            startNewSession(words)
        }
    }

    /**
     * Reactively updates language filter and reloads deck.
     */
    fun onLanguageFilterSelected(languageCode: String?) {
        val normalized = languageCode?.ifBlank { null }?.substringBefore("-")?.lowercase()
        _selectedLanguage.value = normalized
        applyFiltersAndRestart()
    }

    /**
     * Reactively updates part of speech filter and reloads deck.
     */
    fun onPartOfSpeechFilterSelected(partOfSpeech: String?) {
        val normalized = partOfSpeech?.ifBlank { null }
        _selectedPartOfSpeech.value = normalized
        applyFiltersAndRestart()
    }

    /**
     * Clears all active filters and reloads deck with all saved words.
     */
    fun clearFilters() {
        _selectedLanguage.value = null
        _selectedPartOfSpeech.value = null
        applyFiltersAndRestart()
    }

    /**
     * Alias for setting language filter for backward compatibility.
     */
    fun setLanguageFilter(languageCode: String?) {
        onLanguageFilterSelected(languageCode)
    }

    /**
     * Alias for setting part of speech filter for backward compatibility.
     */
    fun setPartOfSpeechFilter(partOfSpeech: String?) {
        onPartOfSpeechFilterSelected(partOfSpeech)
    }

    private fun applyFiltersAndRestart() {
        viewModelScope.launch {
            _isLoading.value = true
            val filteredList = repository.getFilteredWords(_selectedLanguage.value, _selectedPartOfSpeech.value).first()
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
