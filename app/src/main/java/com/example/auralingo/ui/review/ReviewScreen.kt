package com.example.auralingo.ui.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.ui.components.VocabularyFilterBar
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.ObsidianDark
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Tab 3: Interactive Flashcards & Active Recall Training backed by Room Database with 3D Flip animation.
 */
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header & Filters
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Active Recall Review",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    ),
                )
                Text(
                    text = "Practice saved vocabulary with 3D smart flashcards",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Bar
                VocabularyFilterBar(
                    selectedLanguage = uiState.selectedLanguage,
                    selectedPartOfSpeech = uiState.selectedPartOfSpeech,
                    onLanguageSelected = { viewModel.setLanguageFilter(it) },
                    onPartOfSpeechSelected = { viewModel.setPartOfSpeechFilter(it) },
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.cards.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonViolet,
                        trackColor = CardBorder,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Card ${uiState.currentIndex + 1} of ${uiState.totalCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = NeonViolet)
                }
            } else if (uiState.cards.isEmpty()) {
                // Empty state handling when filter matches 0 words
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No flashcards match this filter combination.\nTap words during live sessions or add words manually to start training!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                uiState.currentCard?.let { word ->
                    Interactive3DFlashcard(
                        word = word,
                        isFlipped = uiState.isFlipped,
                        onCardClick = { viewModel.toggleCardFlip() },
                    )
                }
            }

            // Action Buttons (Review Again / Got It!)
            if (uiState.cards.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(
                        onClick = { viewModel.previousCard() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateVioletSurface),
                    ) {
                        Text("Previous", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.nextCard() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonViolet,
                            contentColor = TextPrimary,
                        ),
                    ) {
                        Text("Next Card", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 3D Interactive Flashcard with horizontal rotation animation and crisp perspective formatting.
 */
@Composable
fun Interactive3DFlashcard(
    word: VocabularyWord,
    isFlipped: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "cardFlipAnimation",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density // Crisp perspective depth without distortion
            }
            .clickable { onCardClick() }
            .border(1.dp, NeonViolet, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(CardElevated),
        contentAlignment = Alignment.Center,
    ) {
        if (rotation <= 90f) {
            // Front Face: Target Original Word & Metadata
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = SlateVioletSurface,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "${word.languageCode.uppercase()} • ${word.partOfSpeech.capitalize()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonViolet,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    Text(
                        text = "Tap to Flip 🔄",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = word.originalWord,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 36.sp,
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Category: ${word.categoryTag}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        } else {
            // Back Face: Translation & Context Sentence (Un-mirrored text)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .graphicsLayer { rotationY = 180f }, // Corrects text orientation
            ) {
                Text(
                    text = "TRANSLATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                    color = NeonViolet,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = word.translatedWord,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    if (word.contextSentence.isNotBlank()) {
                        Text(
                            text = "\"${word.contextSentence}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontStyle = FontStyle.Italic,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }

                Text(
                    text = "Tap to Flip Back 🔄",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        }
    }
}

/**
 * Extension helper to capitalize strings cleanly.
 */
private fun String.capitalize(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}
