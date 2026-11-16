package com.example.auralingo.ui.speech

/**
 * Stato immutabile dell'interfaccia utente per il motore di trascrizione vocale in tempo reale.
 *
 * @property isListening In dica se il riconoscimento vocale è attualmente attivo.
 * @property partialText Testo parziale riconosciuto in tempo reale durante l'acquisizione corrente.
 * @property accumulatedText Testo finale accumulato durante l'intera sessione di trascrizione.
 * @property error Eventuale messaggio di errore da mostrare all'utente (null se nessun errore).
 * @property hasAudioPermission Indica se il permesso di registrazione audio (RECORD_AUDIO) è stato concesso.
 */
data class SpeechUiState(
    val isListening: Boolean = false,
    val partialText: String = "",
    val accumulatedText: String = "",
    val error: String? = null,
    val hasAudioPermission: Boolean = false,
) {
    /**
     * Calcola la trascrizione completa unendo il testo accumulato e il testo parziale corrente.
     */
    val fullText: String
        get() = when {
            accumulatedText.isEmpty() -> partialText
            partialText.isEmpty() -> accumulatedText
            else -> "$accumulatedText\n\n$partialText"
        }.trim()
}
