package com.example.auralingo.ui.live

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
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
import com.example.auralingo.ui.theme.GlowCyan
import com.example.auralingo.ui.theme.NeonIndigo
import com.example.auralingo.ui.theme.NeonPink
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.ObsidianDark
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Data model for transcription language options.
 */
data class LanguageOption(
    val tag: String,
    val name: String,
    val flag: String,
)

/**
 * Supported transcription languages: Spanish (default), English, Italian, French, German.
 */
val SupportedLanguages = listOf(
    LanguageOption("es-ES", "Español", "🇪🇸"),
    LanguageOption("en-US", "English", "🇺🇸"),
    LanguageOption("it-IT", "Italiano", "🇮🇹"),
    LanguageOption("fr-FR", "Français", "🇫🇷"),
    LanguageOption("de-DE", "Deutsch", "🇩🇪"),
)

/**
 * Full real-time speech transcription screen for AuraLingo following Cyber-Violet dark theme and MVVM.
 */
@Composable
fun TranscriptionScreen(
    viewModel: SpeechViewModel = viewModel(),
    onWordClick: (word: String, contextSentence: String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Selected language state (Default: Spanish es-ES)
    var selectedLanguage by remember { mutableStateOf(SupportedLanguages.first()) }
    var showClearDialog by remember { mutableStateOf(value = false) }

    // Launcher for requesting audio recording permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
        if (isGranted) {
            viewModel.startListening(selectedLanguage.tag)
        }
    }

    // Initial check for audio recording permission
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(hasPermission)
    }

    // Automatically scroll down to the bottom of the transcript upon new text updates
    LaunchedEffect(uiState.accumulatedText, uiState.partialText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // Confirmation dialog for clearing transcript
    if (showClearDialog) {
        ClearConfirmationDialog(
            onConfirm = {
                viewModel.clearTranscript()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Header with title, logo, and language selector
        Column(modifier = Modifier.fillMaxWidth()) {
            LanguageSelectorBar(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it },
                isListening = uiState.isListening,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Listening status indicator card and audio wave visualizer
            PulsingStatusIndicatorCard(
                isListening = uiState.isListening,
                selectedLanguage = selectedLanguage,
            )
        }

        // Main transcript text container with interactive word tapping
        TranscriptContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            accumulatedText = uiState.accumulatedText,
            partialText = uiState.partialText,
            scrollState = scrollState,
            onClearClick = { showClearDialog = true },
            onWordClick = onWordClick,
        )

        // Error notification banner if any
        uiState.error?.let { errorMessage ->
            ErrorBanner(
                errorMessage = errorMessage,
                onDismiss = { viewModel.dismissError() },
            )
        }

        // Main recording control bar
        RecordingControlBar(
            isListening = uiState.isListening,
            onStartRecording = {
                if (uiState.hasAudioPermission) {
                    viewModel.startListening(selectedLanguage.tag)
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onStopRecording = { viewModel.stopListening() },
        )
    }
}

/**
 * Sub-Composable 1: Top bar with AuraLingo logo and compact language selector.
 */
@Composable
fun LanguageSelectorBar(
    selectedLanguage: LanguageOption,
    onLanguageSelected: (LanguageOption) -> Unit,
    isListening: Boolean,
) {
    var dropdownExpanded by remember { mutableStateOf(value = false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Logo and Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NeonViolet, NeonIndigo))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column {
                Text(
                    text = "AuraLingo",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp,
                    ),
                )
                Text(
                    text = "Neural Lecture Engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        // Language Selector Chip / Dropdown
        Box {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = if (isListening) CardBorder else NeonViolet.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = !isListening) { dropdownExpanded = true },
                color = SlateVioletSurface,
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Select Language",
                        tint = if (isListening) TextMuted else NeonViolet,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "${selectedLanguage.flag} ${selectedLanguage.tag}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isListening) TextMuted else TextPrimary,
                        ),
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (isListening) TextMuted else TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier
                    .background(SlateVioletSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
            ) {
                SupportedLanguages.forEach { lang ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${lang.flag}  ${lang.name} (${lang.tag})",
                                color = if (lang.tag == selectedLanguage.tag) NeonViolet else TextPrimary,
                                fontWeight = if (lang.tag == selectedLanguage.tag) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        onClick = {
                            onLanguageSelected(lang)
                            dropdownExpanded = false
                        },
                    )
                }
            }
        }
    }
}

/**
 * Sub-Composable 2: Card showing active listening status and animated audio wave visualizer.
 */
@Composable
fun PulsingStatusIndicatorCard(
    isListening: Boolean,
    selectedLanguage: LanguageOption,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .border(
                width = 1.dp,
                color = if (isListening) NeonViolet else CardBorder,
                shape = RoundedCornerShape(16.dp),
            ),
        colors = CardDefaults.cardColors(containerColor = SlateVioletSurface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Pulsing dot for active state
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "pulseAlpha",
                    )

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) NeonPink.copy(alpha = pulseAlpha) else TextMuted,
                            ),
                    )

                    Text(
                        text = if (isListening) "NEURAL STREAM ACTIVE" else "READY TO LISTEN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isListening) NeonViolet else TextMuted,
                            letterSpacing = 0.8.sp,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isListening) {
                        "Transcribing live in ${selectedLanguage.name} (${selectedLanguage.tag})"
                    } else {
                        "Select language and tap Start Recording"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            // Dynamic moving audio waveform when listening is active
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "waveBars")
                repeat(5) { index ->
                    val height by infiniteTransition.animateFloat(
                        initialValue = 10f,
                        targetValue = if (isListening) (18f + (index * 7f)) else 10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(350 + (index * 90), easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "waveBar_$index",
                    )
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(height.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isListening) GlowCyan else TextMuted),
                    )
                }
            }
        }
    }
}

/**
 * Sub-Composable 3: Scrollable container for live transcription.
 * Renders accumulated words as individually tappable while preserving paragraph line breaks (\n\n) and partial streaming text.
 */
@Composable
fun TranscriptContainer(
    modifier: Modifier = Modifier,
    accumulatedText: String,
    partialText: String,
    scrollState: androidx.compose.foundation.ScrollState,
    onClearClick: () -> Unit,
    onWordClick: (word: String, contextSentence: String) -> Unit = { _, _ -> },
) {
    val isHasContent = accumulatedText.isNotEmpty() || partialText.isNotEmpty()

    Card(
        modifier = modifier
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CardElevated),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // Header with title and clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Lecture Transcript (Tap any word for lookup)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                    ),
                )

                IconButton(
                    onClick = onClearClick,
                    enabled = isHasContent,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Transcript",
                        tint = if (isHasContent) NeonPink else TextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable text area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                if (!isHasContent) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Real-time lecture transcription will appear here as soon as you start recording...",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Completed and accumulated paragraphs with tappable words
                        if (accumulatedText.isNotEmpty()) {
                            val paragraphs = accumulatedText.split(Regex("\n+"))
                            val annotatedString = buildAnnotatedString {
                                paragraphs.forEachIndexed { pIndex, paragraph ->
                                    val words = paragraph.split(Regex("\\s+"))
                                    words.forEachIndexed { wIndex, word ->
                                        if (word.isNotBlank()) {
                                            pushStringAnnotation(tag = "WORD_TAG", annotation = word)
                                            withStyle(
                                                style = SpanStyle(
                                                    color = TextPrimary,
                                                    fontSize = 17.sp
                                                )
                                            ) {
                                                append(word)
                                            }
                                            pop()
                                            if (wIndex < words.size - 1) {
                                                append(" ")
                                            }
                                        }
                                    }
                                    if (pIndex < paragraphs.size - 1) {
                                        append("\n\n")
                                    }
                                }
                            }

                            ClickableText(
                                text = annotatedString,
                                style = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    lineHeight = 28.sp
                                ),
                                onClick = { offset ->
                                    annotatedString.getStringAnnotations(tag = "WORD_TAG", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            val cleanedWord = annotation.item.trim { it.isLetterOrDigit().not() }
                                            if (cleanedWord.isNotEmpty()) {
                                                val contextSentence = extractSurroundingSentence(accumulatedText, cleanedWord)
                                                onWordClick(cleanedWord, contextSentence)
                                            }
                                        }
                                }
                            )
                        }

                        // Partial streaming text in progress (highlighted in italic with neon cyan)
                        if (partialText.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = partialText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = GlowCyan,
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 28.sp,
                                        fontSize = 17.sp,
                                    ),
                                )
                                Text(
                                    text = "▌",
                                    color = NeonViolet,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Extracts the surrounding sentence from the transcript containing the target word.
 */
private fun extractSurroundingSentence(text: String, targetWord: String): String {
    val sentences = text.split(Regex("(?<=[.!?])\\s+"))
    return sentences.firstOrNull { it.contains(targetWord, ignoreCase = true) } ?: text
}

/**
 * Sub-Composable 4: Bottom control bar with primary start/stop recording button.
 */
@Composable
fun RecordingControlBar(
    isListening: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
) {
    Button(
        onClick = {
            if (isListening) onStopRecording() else onStartRecording()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = if (isListening) NeonPink else NeonViolet),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isListening) NeonPink else NeonViolet,
            contentColor = TextPrimary,
        ),
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (isListening) "Stop Recording" else "Start Recording",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            ),
        )
    }
}

/**
 * Sub-Composable 5: Non-blocking error notification banner with dismiss button.
 */
@Composable
fun ErrorBanner(
    errorMessage: String,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .border(1.dp, NeonPink, RoundedCornerShape(12.dp)),
            color = CardElevated,
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = errorMessage,
                    color = NeonPink,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss error",
                        tint = NeonPink,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Confirmation dialog before clearing the accumulated transcript.
 */
@Composable
fun ClearConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Clear Transcript?",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "This action will remove the entire transcribed text for this session.",
                color = TextSecondary,
            )
        },
        containerColor = SlateVioletSurface,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Clear All", color = NeonPink, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextSecondary)
            }
        },
    )
}
