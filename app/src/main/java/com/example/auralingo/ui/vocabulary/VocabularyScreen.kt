package com.example.auralingo.ui.vocabulary

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auralingo.ui.components.VocabularyFilterBar
import com.example.auralingo.ui.theme.*

/**
 * Tab 2: Personal Vocabulary Bank & Interactive Dictionary backed by Room Database with dynamic filtering.
 */
@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel = viewModel(),
) {
    val context = LocalContext.current
    val savedWords by viewModel.savedWords.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedPartOfSpeech by viewModel.selectedPartOfSpeech.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<Int?>(null) }
    var showAddWordSheet by remember { mutableStateOf(false) }

    // Observe success message and display Material 3 Snackbar notification
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    // Filter words locally by search query as well
    val filteredWords = savedWords.filter {
        it.originalWord.contains(searchQuery, ignoreCase = true) ||
                it.translatedWord.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // Header
            Text(
                text = "Vocabulary Bank",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
            )
            Text(
                text = "Saved words from live sessions and manual entries",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Bar for Language and Part of Speech
            VocabularyFilterBar(
                selectedLanguage = selectedLanguage,
                selectedPartOfSpeech = selectedPartOfSpeech,
                onLanguageSelected = { viewModel.setLanguageFilter(it) },
                onPartOfSpeechSelected = { viewModel.setPartOfSpeechFilter(it) },
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    unfocusedTextColor = TextPrimary,
                ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredWords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) {
                            "No vocabulary matches this filter combination.\nTry selecting 'All' or add a new word!"
                        } else {
                            "No matching words found for '$searchQuery'."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            } else {
                // Word List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredWords, key = { it.id }) { item ->
                        val isExpanded = expandedId == item.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (isExpanded) NeonViolet else CardBorder, RoundedCornerShape(16.dp))
                                .clickable { expandedId = if (isExpanded) null else item.id },
                            colors = CardDefaults.cardColors(containerColor = CardElevated),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(
                                            text = item.originalWord,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 18.sp,
                                            ),
                                        )
                                        Text(
                                            text = item.translatedWord,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = NeonViolet,
                                        )
                                    }

                                    Surface(
                                        color = SlateVioletSurface,
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(
                                            text = item.categoryTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                    ) {
                                        HorizontalDivider(color = CardBorder, thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Context Sentence:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "\"${item.contextSentence}\"",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = TextSecondary,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button with Neon Gradient positioned at bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(60.dp)
                .shadow(12.dp, CircleShape, spotColor = NeonViolet)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NeonViolet, NeonIndigo)))
                .clickable { showAddWordSheet = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Word",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp),
            )
        }

        // Material 3 Snackbar Host for success visual feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
        )
    }

    // Modal Bottom Sheet for manual vocabulary entry
    if (showAddWordSheet) {
        AddWordBottomSheet(
            onDismiss = { showAddWordSheet = false },
            onSave = { orig, trans, ctx, lang, pos ->
                viewModel.saveManualWord(
                    originalWord = orig,
                    translatedWord = trans,
                    contextSentence = ctx,
                    languageCode = lang,
                    partOfSpeech = pos,
                    onSuccess = {
                        showAddWordSheet = false
                    },
                )
            },
        )
    }
}
