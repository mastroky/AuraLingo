package com.example.auralingo.ui.live

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auralingo.ui.speech.SpeechViewModel

@Composable
fun LiveSessionScreen(
    speechViewModel: SpeechViewModel = viewModel(),
    liveSessionViewModel: LiveSessionViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lookupState by liveSessionViewModel.lookupState.collectAsStateWithLifecycle()

    LaunchedEffect(lookupState.saveSuccessMessage) {
        lookupState.saveSuccessMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            liveSessionViewModel.clearMessage()
        }
    }

    TranscriptionScreen(viewModel = speechViewModel)

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