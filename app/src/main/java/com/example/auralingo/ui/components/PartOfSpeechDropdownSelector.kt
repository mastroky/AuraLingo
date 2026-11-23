package com.example.auralingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Data model representing a standardized part of speech option.
 */
data class PartOfSpeechItem(
    val key: String,
    val label: String,
)

/**
 * Standardized list of part of speech categories stored as lowercase English keys in Room.
 */
val SupportedPartsOfSpeech = listOf(
    PartOfSpeechItem("noun", "Noun"),
    PartOfSpeechItem("verb", "Verb"),
    PartOfSpeechItem("adjective", "Adjective"),
    PartOfSpeechItem("adverb", "Adverb"),
    PartOfSpeechItem("phrase", "Phrase"),
    PartOfSpeechItem("other", "Other"),
)

/**
 * Reusable Material 3 Exposed Dropdown Menu component for selecting part of speech.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartOfSpeechDropdownSelector(
    selectedKey: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Part of Speech",
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelected = SupportedPartsOfSpeech.find { it.key == selectedKey } ?: SupportedPartsOfSpeech.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = currentSelected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = TextSecondary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
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

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(SlateVioletSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
        ) {
            SupportedPartsOfSpeech.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            color = TextPrimary,
                        )
                    },
                    onClick = {
                        onSelected(item.key)
                        expanded = false
                    },
                )
            }
        }
    }
}
