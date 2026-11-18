package com.example.auralingo.ui.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auralingo.ui.speech.SpeechViewModel
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.GlowCyan
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.theme.TextPrimary
import kotlinx.coroutines.delay

/**
 * Schermata di sessione live principale che integra la trascrizione neurale e il bottom sheet di ricerca parole.
 */
@Composable
fun LiveSessionScreen(
    speechViewModel: SpeechViewModel = viewModel(),
    liveSessionViewModel: LiveSessionViewModel = viewModel(),
) {
    val lookupState by liveSessionViewModel.lookupState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // Schermata principale di trascrizione
        TranscriptionScreen(
            viewModel = speechViewModel,
            onWordClick = { word, sentence, languageCode ->
                liveSessionViewModel.onWordSelected(word, sentence, languageCode)
            },
        )

        // Banner personalizzato Cyber-Violet di notifica successo salvataggio
        lookupState.saveSuccessMessage?.let { message ->
            SaveSuccessNotificationBanner(
                message = message,
                onDismiss = { liveSessionViewModel.clearMessage() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
            )
        }

        // Visualizzazione del Bottom Sheet di ricerca parola quando una parola viene toccata
        if (lookupState.isBottomSheetVisible && (lookupState.selectedWord != null)) {
            WordLookupBottomSheet(
                selectedWord = lookupState.selectedWord!!,
                translatedWord = lookupState.translatedWord,
                contextSentence = lookupState.contextSentence,
                isSaving = lookupState.isSaving,
                onDismiss = { liveSessionViewModel.dismissBottomSheet() },
                onSaveToVocabulary = {
                    liveSessionViewModel.saveWordToVocabulary()
                },
            )
        }
    }
}

/**
 * Banner personalizzato di notifica con stile Cyber-Violet per la conferma di salvataggio.
 * Sostituisce il Toast nativo di Android con transizioni fluide di scorrimento e dissolvenza.
 */
@Composable
fun SaveSuccessNotificationBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Auto-dismiss dopo 2.5 secondi
    LaunchedEffect(message) {
        delay(2500)
        onDismiss()
    }

    AnimatedVisibility(
        visible = message.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = NeonViolet)
                .border(1.dp, NeonViolet, RoundedCornerShape(14.dp))
                .clickable { onDismiss() },
            color = CardElevated,
            shape = RoundedCornerShape(14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GlowCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Successo",
                        tint = GlowCyan,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    ),
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Chiudi",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
