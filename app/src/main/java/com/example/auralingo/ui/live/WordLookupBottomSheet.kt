package com.example.auralingo.ui.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auralingo.data.repository.TranslationState
import com.example.auralingo.ui.components.PartOfSpeechDropdownSelector
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.NeonPink
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Modal Bottom Sheet that opens when a word is tapped in the live transcription.
 * Displays the selected word, real-time ML Kit translation, editable translation field, context sentence, part of speech selector, and save action button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordLookupBottomSheet(
    selectedWord: String,
    editableTranslation: String,
    contextSentence: String,
    partOfSpeech: String,
    translationState: TranslationState,
    isSaving: Boolean,
    onTranslationChanged: (String) -> Unit,
    onPartOfSpeechChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSaveToVocabulary: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SlateVioletSurface,
        contentColor = TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header label
            Text(
                text = "SELECTED TERM",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = NeonViolet,
            )

            // Selected word display
            Text(
                text = selectedWord,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
            )

            // Translation status indicator / description
            when (translationState) {
                is TranslationState.Loading -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonViolet, strokeWidth = 2.dp)
                        Text(text = "Translating on-device...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                is TranslationState.DownloadingModel -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonViolet, strokeWidth = 2.dp)
                        Text(text = "Downloading language pack model...", style = MaterialTheme.typography.bodySmall, color = NeonViolet)
                    }
                }
                is TranslationState.Error -> {
                    Text(
                        text = "Translation notice: ${translationState.message}. You may edit the translation manually below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonPink,
                    )
                }
                is TranslationState.Success -> {
                    // Ready
                }
            }

            // Editable translation field
            OutlinedTextField(
                value = editableTranslation,
                onValueChange = onTranslationChanged,
                label = { Text("Translation *", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonViolet,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SlateVioletSurface,
                    unfocusedContainerColor = SlateVioletSurface,
                ),
                singleLine = true,
            )

            // Standardized Part of Speech Dropdown Selector
            PartOfSpeechDropdownSelector(
                selectedKey = partOfSpeech,
                onSelected = onPartOfSpeechChanged,
                modifier = Modifier.fillMaxWidth(),
                label = "Category / Part of Speech",
            )

            // Context label
            Text(
                text = "SURROUNDING CONTEXT",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = TextSecondary,
            )

            // Surrounding sentence context card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "\"$contextSentence\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        lineHeight = 22.sp,
                    ),
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save to Vocabulary action button
            Button(
                onClick = onSaveToVocabulary,
                enabled = !isSaving && editableTranslation.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonViolet,
                    disabledContainerColor = TextMuted.copy(alpha = 0.3f),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    tint = TextPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaving) "Saving to Vocabulary..." else "Save to Vocabulary",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
