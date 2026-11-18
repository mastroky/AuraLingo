package com.example.auralingo.ui.live

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auralingo.ui.speech.SpeechViewModel

/**
 * Main live session screen integrating the neural transcription screen and the interactive word lookup bottom sheet.
 */
@Composable
fun LiveSessionScreen(
    speechViewModel: SpeechViewModel = viewModel(),
    liveSessionViewModel: LiveSessionViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lookupState by liveSessionViewModel.lookupState.collectAsStateWithLifecycle()

    // Observe save success feedback and display toast notifications
    LaunchedEffect(lookupState.saveSuccessMessage) {
        lookupState.saveSuccessMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            liveSessionViewModel.clearMessage()
        }
    }

    // Render TranscriptionScreen and pass the onWordClick callback to handle word tapping
    TranscriptionScreen(
        viewModel = speechViewModel,
        onWordClick = { word, sentence ->
            liveSessionViewModel.onWordSelected(word, sentence)
        }
    )

    // Display the Word Lookup Bottom Sheet when a word is tapped
    if (lookupState.isBottomSheetVisible && lookupState.selectedWord != null) {
        WordLookupBottomSheet(
            selectedWord = lookupState.selectedWord!!,
            translatedWord = lookupState.translatedWord,
            contextSentence = lookupState.contextSentence,
            isSaving = lookupState.isSaving,
            onDismiss = { liveSessionViewModel.dismissBottomSheet() },
            onSaveToVocabulary = {
                liveSessionViewModel.saveWordToVocabulary()
            }
        )
    }
}
