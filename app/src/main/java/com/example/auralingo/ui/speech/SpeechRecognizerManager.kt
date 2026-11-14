package com.example.auralingo.ui.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Listener di callback per gli eventi del riconoscimento vocale.
 */
interface SpeechRecognitionListener {
    fun onPartialResult(partialText: String)
    fun onFinalResult(finalText: String)
    fun onError(errorMessage: String)
}

/**
 * Wrapper per Android SpeechRecognizer che gestisce il riconoscimento vocale continuo per le lezioni.
 * Esegue le operazioni sul thread principale (Main thread) e gestisce il riavvio automatico in caso di timeout o no-match.
 */
class SpeechRecognizerManager(
    private val context: Context,
    private val listener: SpeechRecognitionListener
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var isListeningActive = false

    private var currentLanguageTag: String = Locale.ITALIAN.toLanguageTag()
    private var currentIntent: Intent? = null

    /**
     * Inizializza o restituisce la funzione SpeechRecognizer sul thread principale.
     */
    private fun ensureRecognizerCreated() {
        if (speechRecognizer == null) {
            // Crea lo SpeechRecognizer nel thread principale
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }
    }

    /**
     * Avvia il riconoscimento vocale continuo con la lingua specificata.
     */
    fun startListening(languageTag: String = Locale.ITALIAN.toLanguageTag()) {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                listener.onError("Il servizio di riconoscimento vocale non è disponibile su questo dispositivo.")
                return@post
            }

            currentLanguageTag = languageTag
            isListeningActive = true

            // Configurazione dell'Intent per il riconoscimento vocale continuo e risultati parziali
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            currentIntent = intent

            ensureRecognizerCreated()

            try {
                speechRecognizer?.cancel()
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isListeningActive = false
                listener.onError("Impossibile avviare il riconoscimento vocale: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Interrompe la sessione di ascolto attiva.
     */
    fun stopListening() {
        mainHandler.post {
            isListeningActive = false
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {
                speechRecognizer?.cancel()
            }
        }
    }

    /**
     * Rilascia le risorse e distrugge lo SpeechRecognizer per evitare memory leak.
     */
    fun destroy() {
        mainHandler.post {
            isListeningActive = false
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {
                // Ignora eventuali eccezioni durante la distruzione
            } finally {
                speechRecognizer = null
            }
        }
    }

    /**
     * Riavvia silenziosamente l'ascolto continuo sul thread principale.
     */
    private fun restartListeningSilently() {
        if (!isListeningActive) return

        mainHandler.post {
            if (!isListeningActive) return@post
            try {
                speechRecognizer?.cancel()
                currentIntent?.let { intent ->
                    speechRecognizer?.startListening(intent)
                }
            } catch (_: Exception) {
                // In caso di errore di riavvio, interrompe l'ascolto e notifica l'errore
                isListeningActive = false
                listener.onError("Errore durante il riavvio del riconoscimento vocale.")
            }
        }
    }

    /**
     * Crea il RecognitionListener che riceve le callback di sistema da SpeechRecognizer.
     */
    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onPartialResults(partialResults: Bundle?) {
            // Aggiorna il testo parziale in tempo reale
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull().orEmpty()
            if (partialText.isNotEmpty()) {
                listener.onPartialResult(partialText)
            }
        }

        override fun onResults(results: Bundle?) {
            // Estrae il testo finale riconosciuto
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val finalText = matches?.firstOrNull().orEmpty()

            if (finalText.isNotEmpty()) {
                listener.onFinalResult(finalText)
            }

            // Se l'ascolto è ancora attivo, riavvia continuamente per trascrivere la lezione
            if (isListeningActive) {
                restartListeningSilently()
            }
        }

        override fun onError(error: Int) {
            // Gestione silenziosa dei casi NO_MATCH e SPEECH_TIMEOUT per garantire ascolto continuo senza crash
            if (isListeningActive && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                restartListeningSilently()
            } else if (isListeningActive && error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                // Se il servizio è temporaneamente occupato, esegue un riavvio posticipato
                mainHandler.postDelayed({ restartListeningSilently() }, 300)
            } else {
                // Notifica errori reali e ferma lo stato di ascolto
                isListeningActive = false
                listener.onError(getErrorMessage(error))
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    /**
     * Mappa i codici di errore di SpeechRecognizer in messaggi leggibili in italiano.
     */
    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Errore nella registrazione dell'audio."
            SpeechRecognizer.ERROR_CLIENT -> "Errore lato client del riconoscimento vocale."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permesso per l'uso del microfono non concesso."
            SpeechRecognizer.ERROR_NETWORK -> "Errore di connessione alla rete."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout della connessione di rete."
            SpeechRecognizer.ERROR_NO_MATCH -> "Nessun discorso rilevato."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Servizio di riconoscimento vocale occupato."
            SpeechRecognizer.ERROR_SERVER -> "Errore del server di riconoscimento."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nessun input vocale rilevato."
            else -> "Errore sconosciuto nel riconoscimento vocale (Codice $errorCode)."
        }
    }
}
