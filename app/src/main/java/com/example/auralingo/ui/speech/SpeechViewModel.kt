package com.example.auralingo.ui.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

/**
 * ViewModel che gestisce lo SpeechRecognizer ed espone lo stato tramite StateFlow.
 */
class SpeechViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SpeechState())
    val uiState: StateFlow<SpeechState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    private fun getOrCreateSpeechRecognizer(context: Context): SpeechRecognizer {
        return speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(context.applicationContext).also {
            it.setRecognitionListener(createRecognitionListener(context))
            speechRecognizer = it
        }
    }

    private fun createRecognitionListener(context: Context) = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            val isListening = _uiState.value.isListening
            if (isListening && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                startListening(context)
            } else {
                _uiState.update {
                    it.copy(isListening = false, errorMessage = getErrorMessage(error))
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull().orEmpty()

            _uiState.update { state ->
                val updatedAcc = if (state.accumulatedText.isEmpty()) text else "${state.accumulatedText} $text"
                state.copy(accumulatedText = updatedAcc, partialText = "")
            }

            if (_uiState.value.isListening) {
                startListening(context)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull().orEmpty()
            _uiState.update { it.copy(partialText = text) }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening(context: Context) {
        val recognizer = getOrCreateSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ITALIAN.language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        _uiState.update { it.copy(isListening = true, errorMessage = null) }
        recognizer.startListening(intent)
    }

    fun stopListening() {
        _uiState.update { it.copy(isListening = false, partialText = "") }
        speechRecognizer?.stopListening()
    }

    fun clearText() {
        _uiState.update { it.copy(accumulatedText = "", partialText = "") }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Errore registrazione audio."
            SpeechRecognizer.ERROR_CLIENT -> "Errore lato client."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permesso microfono mancante."
            SpeechRecognizer.ERROR_NETWORK -> "Errore di connessione di rete."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout di rete."
            SpeechRecognizer.ERROR_NO_MATCH -> "Nessun discorso rilevato."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Servizio di riconoscimento occupato."
            SpeechRecognizer.ERROR_SERVER -> "Errore del server."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nessun input vocale rilevato."
            else -> "Errore sconosciuto ($errorCode)."
        }
    }
}
