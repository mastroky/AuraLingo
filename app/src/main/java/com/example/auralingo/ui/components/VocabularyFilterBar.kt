package com.example.auralingo.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Reusable Material 3 Filter Bar component for filtering vocabulary words by Language and Part of Speech.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyFilterBar(
    selectedLanguage: String?,
    selectedPartOfSpeech: String?,
    onLanguageSelected: (String?) -> Unit,
    onPartOfSpeechSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val languages = listOf(
        null to "All Languages",
        "en" to "English 🇬🇧",
        "es" to "Español 🇪🇸",
        "it" to "Italiano 🇮🇹",
        "fr" to "Français 🇫🇷",
        "de" to "Deutsch 🇩🇪",
    )

    val partsOfSpeech = listOf(
        null to "All Types",
        "noun" to "Noun",
        "verb" to "Verb",
        "adjective" to "Adjective",
        "other" to "Other",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Language filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            languages.forEach { (code, label) ->
                val isSelected = selectedLanguage == code
                FilterChip(
                    selected = isSelected,
                    onClick = { onLanguageSelected(code) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet,
                        selectedLabelColor = TextPrimary,
                        containerColor = CardElevated,
                        labelColor = TextSecondary,
                    ),
                )
            }
        }

        // Part of speech filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            partsOfSpeech.forEach { (pos, label) ->
                val isSelected = selectedPartOfSpeech == pos
                FilterChip(
                    selected = isSelected,
                    onClick = { onPartOfSpeechSelected(pos) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonViolet,
                        selectedLabelColor = TextPrimary,
                        containerColor = CardElevated,
                        labelColor = TextSecondary,
                    ),
                )
            }
        }
    }
}
