package com.example.auralingo.ui.speech

/**
 * Stato della UI per lo schermo di trascrizione vocale in tempo reale.
 */
data class SpeechState(
    val accumulatedText: String = "",
    val partialText: String = "",
    val isListening: Boolean = false,
    val errorMessage: String? = null,
) {
    val fullText: String
        get() = if (partialText.isNotEmpty()) "$accumulatedText $partialText".trim() else accumulatedText.trim()
}
