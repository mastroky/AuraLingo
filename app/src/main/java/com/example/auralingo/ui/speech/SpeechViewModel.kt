package com.example.auralingo.ui.speech

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel che gestisce lo stato dell'interfaccia utente di trascrizione e la comunicazione con SpeechRecognizerManager.
 */
class SpeechViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        SpeechUiState(
            hasAudioPermission = ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED,
        ),
    )
    val uiState: StateFlow<SpeechUiState> = _uiState.asStateFlow()

    private val speechRecognizerManager = SpeechRecognizerManager(
        context = application,
        listener = object : SpeechRecognitionListener {
            override fun onPartialResult(partialText: String) {
                // Aggiorna il testo parziale in tempo reale
                _uiState.update { it.copy(partialText = partialText) }
            }

            override fun onFinalResult(finalText: String) {
                // Formatta e accumula la frase finale come nuovo paragrafo con iniziale maiuscola
                val formattedPhrase = finalText.trim().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
                if (formattedPhrase.isEmpty()) return

                _uiState.update { state ->
                    val updatedAcc = if (state.accumulatedText.isBlank()) {
                        formattedPhrase
                    } else {
                        "${state.accumulatedText}\n\n$formattedPhrase"
                    }
                    state.copy(accumulatedText = updatedAcc, partialText = "")
                }
            }

            override fun onError(errorMessage: String) {
                // Notifica l'errore e aggiorna lo stato di ascolto
                _uiState.update { it.copy(isListening = false, error = errorMessage) }
            }
        },
    )

    /**
     * Aggiorna la lingua di trascrizione selezionata dall'utente (tag BCP-47 es. "es-ES", "en-US", "it-IT").
     */
    fun setLanguage(languageTag: String) {
        _uiState.update { it.copy(selectedLanguageTag = languageTag) }
    }

    /**
     * Aggiorna lo stato del permesso audio concesso dall'utente.
     */
    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasAudioPermission = granted) }
        if (!granted) {
            _uiState.update { it.copy(error = "Il permesso di registrazione audio è necessario per la trascrizione.") }
        }
    }

    /**
     * Avvia il riconoscimento vocale per la lezione nella lingua specificata (default: lingua selezionata in uiState).
     */
    fun startListening(languageTag: String = _uiState.value.selectedLanguageTag) {
        val hasPermission = _uiState.value.hasAudioPermission
        if (!hasPermission) {
            _uiState.update { it.copy(error = "Permesso microfono non concesso.") }
            return
        }

        _uiState.update { it.copy(isListening = true, selectedLanguageTag = languageTag, error = null) }
        speechRecognizerManager.startListening(languageTag)
    }

    /**
     * Interrompe la sessione di ascolto attiva e rimuove eventuali errori residui.
     */
    fun stopListening() {
        _uiState.update { it.copy(isListening = false, partialText = "", error = null) }
        speechRecognizerManager.stopListening()
    }

    /**
     * Pulisce la trascrizione accumulata finora.
     */
    fun clearTranscript() {
        _uiState.update { it.copy(accumulatedText = "", partialText = "") }
    }

    /**
     * Nasconde o chiude il messaggio di errore corrente.
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Distrugge lo SpeechRecognizer per liberare memoria
        speechRecognizerManager.destroy()
    }
}
