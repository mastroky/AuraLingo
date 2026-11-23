package com.example.auralingo.ui.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.auralingo.ui.theme.GlowCyan
import com.example.auralingo.ui.theme.NeonPink
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.ObsidianDark
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Tab 3: Interactive Flashcards & Active Recall Training backed by Room Database with 3D Flip animation, clean filter setup, and reverse direction support.
 */
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isFilterExpanded by remember { mutableStateOf(false) }

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
            // Header & Collapsible Filter Setup
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

                // Collapsible Filter Setup Header Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .clickable { isFilterExpanded = !isFilterExpanded },
                    color = SlateVioletSurface,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "SESSION FILTERS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                ),
                                color = NeonViolet,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = buildFilterSummary(uiState.selectedLanguage, uiState.selectedPartOfSpeech, uiState.isReversed),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                            )
                        }

                        IconButton(
                            onClick = { isFilterExpanded = !isFilterExpanded },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Toggle Filters",
                                tint = NeonViolet,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // Collapsible Filter Selectors & Reverse Switch
                AnimatedVisibility(
                    visible = isFilterExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        VocabularyFilterBar(
                            selectedLanguage = uiState.selectedLanguage,
                            selectedPartOfSpeech = uiState.selectedPartOfSpeech,
                            onLanguageSelected = { viewModel.setLanguageFilter(it) },
                            onPartOfSpeechSelected = { viewModel.setPartOfSpeechFilter(it) },
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Translation First (Reverse Cards)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )

                            Switch(
                                checked = uiState.isReversed,
                                onCheckedChange = { viewModel.toggleReverseDirection(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextPrimary,
                                    checkedTrackColor = NeonViolet,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = SlateVioletSurface,
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.sessionQueue.isNotEmpty() && !uiState.isSessionComplete) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Card ${uiState.currentIndex + 1} of ${uiState.totalCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                        Text(
                            text = "Mastered: ${uiState.knownCount} • Re-queued: ${uiState.toReviewCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlowCyan,
                        )
                    }
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
            } else if (uiState.sessionQueue.isEmpty()) {
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
            } else if (uiState.isSessionComplete) {
                // Session Completed Summary Screen
                SessionCompletedCard(
                    knownCount = uiState.knownCount,
                    reviewCount = uiState.toReviewCount,
                    totalPracticed = uiState.knownCount + uiState.toReviewCount,
                    onRestart = { viewModel.restartSession() },
                )
            } else {
                uiState.currentCard?.let { word ->
                    Interactive3DFlashcard(
                        word = word,
                        isFlipped = uiState.isFlipped,
                        isReversed = uiState.isReversed,
                        onCardClick = { viewModel.toggleCardFlip() },
                    )
                }
            }

            // Action Feedback Buttons ("Review Again" / "Got It!") without emojis
            if (uiState.sessionQueue.isNotEmpty() && !uiState.isSessionComplete) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(
                        onClick = { viewModel.markCardForReview() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlateVioletSurface,
                            contentColor = NeonPink,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review Again", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.markCardAsKnown() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonViolet,
                            contentColor = TextPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Got It!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 3D Interactive Flashcard with horizontal rotation animation, crisp perspective formatting, and optional reverse direction support.
 */
@Composable
fun Interactive3DFlashcard(
    word: VocabularyWord,
    isFlipped: Boolean,
    isReversed: Boolean = false,
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
                cameraDistance = 12f * density
            }
            .clickable { onCardClick() }
            .border(1.dp, NeonViolet, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(CardElevated),
        contentAlignment = Alignment.Center,
    ) {
        if (rotation <= 90f) {
            // Front Face: Shows original word (normal) or translation (reversed)
            val mainText = if (!isReversed) word.originalWord else word.translatedWord
            val headerTag = if (!isReversed) {
                "${word.languageCode.uppercase()} • ${word.partOfSpeech.capitalize()}"
            } else {
                "TRANSLATION (${word.languageCode.uppercase()})"
            }

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
                            text = headerTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonViolet,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    Text(
                        text = "Tap to Flip",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mainText,
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
            // Back Face: Shows translation (normal) or original word (reversed) + context sentence (Un-mirrored text)
            val backHeader = if (!isReversed) "TRANSLATION" else "ORIGINAL TERM"
            val backMainText = if (!isReversed) word.translatedWord else word.originalWord

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .graphicsLayer { rotationY = 180f },
            ) {
                Text(
                    text = backHeader,
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
                        text = backMainText,
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
                    text = "Tap to Flip Back",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        }
    }
}

/**
 * Summary Card displayed when the flashcard training session is completed (no emojis).
 */
@Composable
fun SessionCompletedCard(
    knownCount: Int,
    reviewCount: Int,
    totalPracticed: Int,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .border(1.dp, GlowCyan, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CardElevated),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GlowCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GlowCyan,
                    modifier = Modifier.size(36.dp),
                )
            }

            Text(
                text = "Session Completed",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
            )

            Text(
                text = "Great job reviewing your vocabulary deck!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            HorizontalDivider(color = CardBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$knownCount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = GlowCyan))
                    Text(text = "Mastered", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$reviewCount", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = NeonPink))
                    Text(text = "Re-queued", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$totalPracticed", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = NeonViolet))
                    Text(text = "Total Reviews", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonViolet,
                    contentColor = TextPrimary,
                ),
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Restart Session", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Builds a friendly summary string of active session filters.
 */
private fun buildFilterSummary(language: String?, partOfSpeech: String?, isReversed: Boolean): String {
    val langLabel = language?.uppercase() ?: "All Languages"
    val posLabel = partOfSpeech?.capitalize() ?: "All Types"
    val modeLabel = if (isReversed) "Translation First" else "Word First"
    return "$langLabel • $posLabel • $modeLabel"
}

/**
 * Extension helper to capitalize strings cleanly.
 */
private fun String.capitalize(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}
