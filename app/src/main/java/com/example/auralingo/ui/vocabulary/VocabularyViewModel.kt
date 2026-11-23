package com.example.auralingo.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auralingo.data.local.AppDatabase
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.data.repository.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing the user's saved vocabulary bank from the Room database with dynamic filtering support.
 */
class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository by lazy {
        val database = AppDatabase.getInstance(application)
        VocabularyRepository(database.vocabularyDao())
    }

    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    private val _selectedPartOfSpeech = MutableStateFlow<String?>(null)
    val selectedPartOfSpeech: StateFlow<String?> = _selectedPartOfSpeech.asStateFlow()

    /**
     * Real-time reactive flow of saved vocabulary words filtered dynamically by language and part of speech.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val savedWords: StateFlow<List<VocabularyWord>> = combine(
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

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Sets the language filter. Pass null or empty for "All".
     */
    fun setLanguageFilter(languageCode: String?) {
        _selectedLanguage.value = languageCode?.ifBlank { null }?.substringBefore("-")?.lowercase()
    }

    /**
     * Sets the part of speech filter. Pass null or empty for "All".
     */
    fun setPartOfSpeechFilter(partOfSpeech: String?) {
        _selectedPartOfSpeech.value = partOfSpeech?.ifBlank { null }
    }

    /**
     * Clears the success message state.
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Saves a manually entered vocabulary word into the Room database using Dispatchers.IO
     * and triggers a success state notification, normalizing the language code.
     */
    fun saveManualWord(
        originalWord: String,
        translatedWord: String,
        contextSentence: String,
        languageCode: String,
        partOfSpeech: String,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val normalizedLang = languageCode.substringBefore("-").lowercase()
            val categoryTag = "$normalizedLang • $partOfSpeech"
            val word = VocabularyWord(
                originalWord = originalWord.trim(),
                translatedWord = translatedWord.trim(),
                contextSentence = contextSentence.trim(),
                categoryTag = categoryTag,
                languageCode = normalizedLang,
                partOfSpeech = partOfSpeech,
                timestamp = System.currentTimeMillis(),
            )
            repository.insertWord(word)
            _successMessage.value = "Successfully added '${word.originalWord}' to vocabulary!"
            onSuccess()
        }
    }

    /**
     * Deletes a vocabulary word from the database.
     */
    fun deleteWord(word: VocabularyWord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWord(word)
        }
    }
}
