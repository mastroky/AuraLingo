package com.example.auralingo.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auralingo.data.local.AppDatabase
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.data.repository.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing the user's saved vocabulary bank from the Room database.
 */
class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabularyRepository by lazy {
        val database = AppDatabase.getInstance(application)
        VocabularyRepository(database.vocabularyDao())
    }

    /**
     * Real-time reactive flow of all saved vocabulary words, shared with the UI layer.
     */
    val savedWords: StateFlow<List<VocabularyWord>> = repository.allWords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Saves a manually entered vocabulary word into the Room database using Dispatchers.IO.
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
            val categoryTag = "$languageCode • $partOfSpeech"
            val word = VocabularyWord(
                originalWord = originalWord.trim(),
                translatedWord = translatedWord.trim(),
                contextSentence = contextSentence.trim(),
                categoryTag = categoryTag,
                timestamp = System.currentTimeMillis(),
            )
            repository.insertWord(word)
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
