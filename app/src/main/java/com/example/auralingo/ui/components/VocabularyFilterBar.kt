package com.example.auralingo.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.GlowCyan
import com.example.auralingo.ui.theme.NeonPink
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Reusable Material 3 Filter Bar component for filtering vocabulary words by Language and Part of Speech.
 * Features high-contrast Cyber-Violet styling, clear checkmark states, and scrollable filter rows.
 */
@Composable
fun VocabularyFilterBar(
    selectedLanguage: String?,
    selectedPartOfSpeech: String?,
    onLanguageSelected: (String?) -> Unit,
    onPartOfSpeechSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    onClearFilters: (() -> Unit)? = null,
) {
    val languages = listOf(
        null to "All",
        "es" to "Español 🇪🇸",
        "en" to "English 🇺🇸",
        "it" to "Italiano 🇮🇹",
        "fr" to "Français 🇫🇷",
        "de" to "Deutsch 🇩🇪",
    )

    val partsOfSpeech = listOf(
        null to "All",
        "noun" to "Nouns",
        "verb" to "Verbs",
        "adjective" to "Adjectives",
        "adverb" to "Adverbs",
        "phrase" to "Phrases",
        "other" to "Other",
    )

    val isAnyFilterActive = (selectedLanguage != null) || (selectedPartOfSpeech != null)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Row 1: Language filter chips with optional Clear action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            languages.forEach { (code, label) ->
                val isSelected = selectedLanguage == code
                FilterChip(
                    selected = isSelected,
                    onClick = { onLanguageSelected(code) },
                    label = {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = GlowCyan,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SlateVioletSurface,
                        selectedLabelColor = TextPrimary,
                        selectedLeadingIconColor = GlowCyan,
                        containerColor = CardElevated,
                        labelColor = TextSecondary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = NeonViolet,
                        selectedBorderWidth = 1.5.dp,
                        borderColor = CardBorder,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            if (isAnyFilterActive && onClearFilters != null) {
                Spacer(modifier = Modifier.width(4.dp))
                InputChip(
                    selected = true,
                    onClick = { onClearFilters() },
                    label = { Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Filters",
                            tint = NeonPink,
                            modifier = Modifier.size(14.dp),
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = SlateVioletSurface,
                        selectedLabelColor = NeonPink,
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        enabled = true,
                        selected = true,
                        selectedBorderColor = NeonPink.copy(alpha = 0.6f),
                        selectedBorderWidth = 1.dp,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }

        // Row 2: Part of speech filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            partsOfSpeech.forEach { (pos, label) ->
                val isSelected = selectedPartOfSpeech == pos
                FilterChip(
                    selected = isSelected,
                    onClick = { onPartOfSpeechSelected(pos) },
                    label = {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = GlowCyan,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SlateVioletSurface,
                        selectedLabelColor = TextPrimary,
                        selectedLeadingIconColor = GlowCyan,
                        containerColor = CardElevated,
                        labelColor = TextSecondary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = NeonViolet,
                        selectedBorderWidth = 1.5.dp,
                        borderColor = CardBorder,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }
    }
}
