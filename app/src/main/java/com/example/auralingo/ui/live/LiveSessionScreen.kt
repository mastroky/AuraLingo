package com.example.auralingo.ui.live

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auralingo.ui.speech.SpeechViewModel
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.NeonIndigo
import com.example.auralingo.ui.theme.NeonPink
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.ObsidianDark
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Main live session screen handling real-time speech transcription, audio wave visualization,
 * and interactive word lookup tapping connected to LiveSessionViewModel and Room persistence.
 */
@Composable
fun LiveSessionScreen(
    speechViewModel: SpeechViewModel = viewModel(),
    liveSessionViewModel: LiveSessionViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by speechViewModel.uiState.collectAsStateWithLifecycle()
    val lookupState by liveSessionViewModel.lookupState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        speechViewModel.onPermissionResult(isGranted)
        if (isGranted) {
            speechViewModel.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for live transcription", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        speechViewModel.onPermissionResult(hasPermission)
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Observe save success messages and display toast notifications
    LaunchedEffect(lookupState.saveSuccessMessage) {
        lookupState.saveSuccessMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            liveSessionViewModel.clearMessage()
        }
    }

    // Automatically scroll down when new transcript text is added
    LaunchedEffect(uiState.fullText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Futuristic Logo Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonViolet, NeonIndigo))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "AuraLingo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "Real-Time Neural Vocabulary",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Wave Visualizer Placeholder Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateVioletSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (uiState.isListening) "NEURAL STREAM ACTIVE" else "READY TO LISTEN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isListening) NeonViolet else TextMuted
                            )
                        )
                        Text(
                            text = if (uiState.isListening) "Listening in Italian/Foreign..." else "Tap start to capture lecture audio",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Simulated Waveform Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "wave")
                        repeat(5) { index ->
                            val height by infiniteTransition.animateFloat(
                                initialValue = 12f,
                                targetValue = if (uiState.isListening) (20f + (index * 8f)) else 12f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(400 + (index * 100), easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bar_$index"
                            )
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(height.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (uiState.isListening) NeonViolet else TextMuted)
                            )
                        }
                    }
                }
            }
        }

        // Live Transcription Feed Box with Tappable Words
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = CardElevated),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Transcript (Tap any word for lookup)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    )

                    if (uiState.fullText.isNotEmpty()) {
                        IconButton(
                            onClick = { speechViewModel.clearTranscript() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear transcript",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (uiState.fullText.isEmpty()) {
                        Text(
                            text = "Your real-time neural transcript will appear here instantly as speech is detected...",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        // Build interactive annotated string where each word is individually tappable
                        val transcript = uiState.fullText
                        val annotatedString = buildAnnotatedString {
                            val words = transcript.split(Regex("\\s+"))
                            words.forEachIndexed { index, word ->
                                pushStringAnnotation(tag = "WORD_TAG", annotation = word)
                                withStyle(
                                    style = SpanStyle(
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                ) {
                                    append(word)
                                }
                                pop()
                                if (index < words.size - 1) {
                                    append(" ")
                                }
                            }
                        }

                        ClickableText(
                            text = annotatedString,
                            onClick = { offset ->
                                annotatedString.getStringAnnotations(tag = "WORD_TAG", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        val clickedWord = annotation.item.trim { it.isLetterOrDigit().not() }
                                        if (clickedWord.isNotEmpty()) {
                                            val contextSentence = extractSurroundingSentence(transcript, clickedWord)
                                            liveSessionViewModel.onWordSelected(clickedWord, contextSentence)
                                        }
                                    }
                            }
                        )
                    }
                }
            }
        }

        // Error message banner if any
        uiState.error?.let { error ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, NeonPink, RoundedCornerShape(12.dp)),
                color = CardElevated,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = error,
                        color = NeonPink,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { speechViewModel.dismissError() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss error",
                            tint = NeonPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Glowing Neon Action Button
        Button(
            onClick = {
                if (uiState.isListening) {
                    speechViewModel.stopListening()
                } else {
                    if (uiState.hasAudioPermission) {
                        speechViewModel.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = NeonViolet),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isListening) NeonPink else NeonViolet,
                contentColor = TextPrimary
            )
        ) {
            Icon(
                imageVector = if (uiState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (uiState.isListening) "Stop Live Session" else "Start Live Session",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }

    // Modal Bottom Sheet for Interactive Word Lookup & Persistence
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

/**
 * Extracts the surrounding sentence from the full transcript containing the target word.
 */
private fun extractSurroundingSentence(text: String, targetWord: String): String {
    val sentences = text.split(Regex("(?<=[.!?])\\s+"))
    return sentences.firstOrNull { it.contains(targetWord, ignoreCase = true) } ?: text
}
