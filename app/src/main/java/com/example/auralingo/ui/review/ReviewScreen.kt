package com.example.auralingo.ui.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auralingo.ui.theme.*

data class Flashcard(
    val term: String,
    val definition: String,
    val example: String,
)

/**
 * Tab 3: Interactive Flashcards & Active Recall Training.
 */
@Composable
fun ReviewScreen() {
    val flashcards = listOf(
        Flashcard("estudiando", "studying", "Estoy estudiando para el examen final."),
        Flashcard("universidad", "university", "La universidad tiene una biblioteca excelente."),
        Flashcard("desarrollo", "development", "El desarrollo de software requiere práctica.")
    )

    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    val currentCard = flashcards[currentIndex]

    // Card Flip Animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "cardFlip"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header & Progress
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Active Recall Review",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / flashcards.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonViolet,
                    trackColor = CardBorder,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Card ${currentIndex + 1} of ${flashcards.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            // Flashcard Box with Flip Effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 8f * density
                    }
                    .clickable { isFlipped = !isFlipped }
                    .border(1.dp, NeonViolet, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardElevated),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front Side: Term
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Tap to Flip",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonIndigo
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = currentCard.term,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 36.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Back Side: Definition & Example
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        Text(
                            text = "Translation",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonViolet
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentCard.definition,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "\"${currentCard.example}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Action Buttons (Got it / Review Again)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        isFlipped = false
                        currentIndex = (currentIndex + 1) % flashcards.size
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlateVioletSurface)
                ) {
                    Text("Review Again", color = TextSecondary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        score++
                        isFlipped = false
                        currentIndex = (currentIndex + 1) % flashcards.size
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonViolet,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Got It!", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
