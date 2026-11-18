package com.example.auralingo.ui.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.auralingo.ui.components.LanguageDropdownSelector
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Modal Bottom Sheet form for manually adding a new vocabulary word.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordBottomSheet(
    onDismiss: () -> Unit,
    onSave: (originalWord: String, translation: String, contextSentence: String, languageCode: String, partOfSpeech: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    var originalWord by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var contextSentence by remember { mutableStateOf("") }
    var languageCode by remember { mutableStateOf("en") }
    var partOfSpeech by remember { mutableStateOf("noun") }

    val isFormValid = originalWord.isNotBlank() && translation.isNotBlank()

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
            // Header title
            Text(
                text = "Add Vocabulary Word",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
            )

            // Original word input field
            OutlinedTextField(
                value = originalWord,
                onValueChange = { originalWord = it },
                label = { Text("Original Word *", color = TextSecondary) },
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

            // Translation input field
            OutlinedTextField(
                value = translation,
                onValueChange = { translation = it },
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

            // Context sentence input field (optional)
            OutlinedTextField(
                value = contextSentence,
                onValueChange = { contextSentence = it },
                label = { Text("Context Sentence (Optional)", color = TextSecondary) },
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
                maxLines = 2,
            )

            // Language Selector Dropdown and Part of Speech row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Reusable Language Dropdown Component
                LanguageDropdownSelector(
                    selectedLanguageCode = languageCode,
                    onLanguageSelected = { languageCode = it },
                    modifier = Modifier.weight(1f),
                    label = "Language",
                )

                OutlinedTextField(
                    value = partOfSpeech,
                    onValueChange = { partOfSpeech = it },
                    label = { Text("Part of Speech", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save action button
            Button(
                onClick = {
                    if (isFormValid) {
                        onSave(originalWord, translation, contextSentence, languageCode, partOfSpeech)
                    }
                },
                enabled = isFormValid,
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
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = TextPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Word",
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
