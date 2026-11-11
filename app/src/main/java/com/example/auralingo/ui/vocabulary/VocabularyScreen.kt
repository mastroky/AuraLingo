package com.example.auralingo.ui.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auralingo.ui.theme.*

data class VocabularyWordItem(
    val id: Int,
    val originalWord: String,
    val translation: String,
    val contextSentence: String,
    val categoryTag: String,
)

/**
 * Tab 2: Personal Vocabulary Bank & Interactive Dictionary.
 */
@Composable
fun VocabularyScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<Int?>(null) }

    val sampleWords = listOf(
        VocabularyWordItem(1, "estudiando", "studying", "Estoy estudiando para el examen final.", "University"),
        VocabularyWordItem(2, "universidad", "university", "La universidad tiene una biblioteca excelente.", "Campus"),
        VocabularyWordItem(3, "desarrollo", "development", "El desarrollo de software requiere práctica.", "Tech"),
        VocabularyWordItem(4, "imprescindible", "essential", "Este libro es imprescindible para la clase.", "Academics"),
        VocabularyWordItem(5, "investigación", "research", "La investigación científica avanza rápido.", "Science")
    )

    val filteredWords = sampleWords.filter {
        it.originalWord.contains(searchQuery, ignoreCase = true) ||
                it.translation.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Vocabulary Bank",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Tap any word to inspect contextual definitions",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search saved terms...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SlateVioletSurface,
                    unfocusedContainerColor = SlateVioletSurface,
                    focusedBorderColor = NeonViolet,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Word List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredWords) { item ->
                    val isExpanded = expandedId == item.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isExpanded) NeonViolet else CardBorder, RoundedCornerShape(16.dp))
                            .clickable { expandedId = if (isExpanded) null else item.id },
                        colors = CardDefaults.cardColors(containerColor = CardElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.originalWord,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 18.sp
                                        )
                                    )
                                    Text(
                                        text = item.translation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NeonViolet
                                    )
                                }

                                Surface(
                                    color = SlateVioletSurface,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = item.categoryTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    HorizontalDivider(color = CardBorder, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Context Sentence:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "\"${item.contextSentence}\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button with Neon Gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(60.dp)
                .shadow(12.dp, CircleShape, spotColor = NeonViolet)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NeonViolet, NeonIndigo)))
                .clickable { /* Add word dialog action */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Word",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
