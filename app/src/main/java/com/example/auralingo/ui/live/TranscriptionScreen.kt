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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
 * Modello dati per le opzioni di lingua supportate dal motore di trascrizione.
 */
data class LanguageOption(
    val tag: String,
    val name: String,
    val flag: String,
)

/**
 * Lingue predefinite supportate: Spagnolo (default), Inglese, Italiano, Francese, Tedesco.
 */
val SupportedLanguages = listOf(
    LanguageOption("es-ES", "Español", "🇪🇸"),
    LanguageOption("en-US", "English", "🇺🇸"),
    LanguageOption("it-IT", "Italiano", "🇮🇹"),
    LanguageOption("fr-FR", "Français", "🇫🇷"),
    LanguageOption("de-DE", "Deutsch", "🇩🇪"),
)

/**
 * Schermata di trascrizione vocale completa in tempo reale per AuraLingo.
 * Segue il tema scuro Cyber-Violet e l'architettura MVVM.
 */
@Composable
fun TranscriptionScreen(
    viewModel: SpeechViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Stato per la lingua di trascrizione selezionata (Default: Spagnolo es-ES)
    var selectedLanguage by remember { mutableStateOf(SupportedLanguages.first()) }
    var showClearDialog by remember { mutableStateOf(value = false) }

    // Launcher per richiedere il permesso di registrazione audio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
        if (isGranted) {
            viewModel.startListening(selectedLanguage.tag)
        }
    }

    // Verifica iniziale del permesso di registrazione audio
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(hasPermission)
    }

    // Scorri automaticamente in fondo al testo della trascrizione ad ogni aggiornamento
    LaunchedEffect(uiState.accumulatedText, uiState.partialText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // Dialogo di conferma cancellazione trascrizione
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
        // Intestazione con titolo, logo e selettore della lingua
        Column(modifier = Modifier.fillMaxWidth()) {
            LanguageSelectorBar(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it },
                isListening = uiState.isListening,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Indicatore pulsante dello stato di ascolto e delle onde audio
            PulsingStatusIndicatorCard(
                isListening = uiState.isListening,
                selectedLanguage = selectedLanguage,
            )
        }

        // Area principale di testo trascritto (paragrafi accumulati + parole parziali in corso)
        TranscriptContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            accumulatedText = uiState.accumulatedText,
            partialText = uiState.partialText,
            scrollState = scrollState,
            onClearClick = { showClearDialog = true },
        )

        // Banner per la visualizzazione di eventuali errori
        uiState.error?.let { errorMessage ->
            ErrorBanner(
                errorMessage = errorMessage,
                onDismiss = { viewModel.dismissError() },
            )
        }

        // Pulsante principale di avvio/interruzione registrazione
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
 * Sub-Composable 1: Barra superiore con logo AuraLingo e selettore di lingua compatto.
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
        // Logo e Titolo
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

        // Chip / Dropdown Selettore Lingua
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
                        contentDescription = "Seleziona Lingua",
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
 * Sub-Composable 2: Card con stato di ascolto attivo e visualizzatore ad onde audio animate.
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
                    // Punto pulsante per lo stato attivo
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
                        text = if (isListening) "STREAM NEURALE ATTIVO" else "PRONTO ALL'ASCOLTO",
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
                        "Trascrizione in corso in ${selectedLanguage.name} (${selectedLanguage.tag})"
                    } else {
                        "Seleziona lingua e tocca Avvia Registrazione"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            // Onde audio dinamiche in movimento quando l'ascolto è attivo
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
 * Sub-Composable 3: Contenitore scrollabile per la trascrizione live.
 * Distingue visivamente i paragrafi completati (accumulatedText) dalle parole in corso (partialText).
 */
@Composable
fun TranscriptContainer(
    modifier: Modifier = Modifier,
    accumulatedText: String,
    partialText: String,
    scrollState: androidx.compose.foundation.ScrollState,
    onClearClick: () -> Unit,
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
            // Intestazione con titolo ed eventuale pulsante Cancella
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Trascrizione della Lezione",
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
                        contentDescription = "Pulisci Trascrizione",
                        tint = if (isHasContent) NeonPink else TextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Area scrollabile del testo
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
                            text = "La trascrizione della lezione in tempo reale apparirà qui non appena avvieri la registrazione...",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Paragrafi completati ed accumulati
                        if (accumulatedText.isNotEmpty()) {
                            Text(
                                text = accumulatedText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextPrimary,
                                    lineHeight = 28.sp,
                                    fontSize = 17.sp,
                                ),
                            )
                        }

                        // Testo parziale in corso di riconoscimento (evidenziato in corsivo con colore ciano/neon)
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
 * Sub-Composable 4: Barra di controllo inferiore con pulsante principale di avvio/interruzione registrazione.
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
            text = if (isListening) "Interrompi Registrazione" else "Avvia Registrazione",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            ),
        )
    }
}

/**
 * Sub-Composable 5: Banner di notifica errori non bloccante con pulsante di chiusura.
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
                        contentDescription = "Chiudi errore",
                        tint = NeonPink,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Dialogo di conferma prima di cancellare la trascrizione accumulata.
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
                text = "Cancellare la trascrizione?",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "Questa azione rimuoverà l'intero testo trascritto durante questa sessione.",
                color = TextSecondary,
            )
        },
        containerColor = SlateVioletSurface,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Cancella Tutto", color = NeonPink, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Annulla", color = TextSecondary)
            }
        },
    )
}
