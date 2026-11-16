package com.example.auralingo.ui.live

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auralingo.ui.speech.SpeechViewModel

/**
 * Schermata di sessione live per la trascrizione in tempo reale delle lezioni.
 * Delega alla componente modulare TranscriptionScreen per un'esperienza UI completa e moderna.
 */
@Composable
fun LiveSessionScreen(
    viewModel: SpeechViewModel = viewModel(),
) {
    TranscriptionScreen(viewModel = viewModel)
}
