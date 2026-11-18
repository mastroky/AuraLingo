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
 * Data model representing a supported language option.
 */
data class LanguageItem(
    val code: String,
    val displayName: String,
    val flag: String,
)

/**
 * Predefined list of common languages supported by AuraLingo.
 */
val SupportedLanguageItems = listOf(
    LanguageItem("en", "English", "🇬🇧"),
    LanguageItem("es", "Español", "🇪🇸"),
    LanguageItem("it", "Italiano", "🇮🇹"),
    LanguageItem("fr", "Français", "🇫🇷"),
    LanguageItem("de", "Deutsch", "🇩🇪"),
)

/**
 * Reusable Material 3 Exposed Dropdown Menu component for selecting a language.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdownSelector(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Language",
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelected = SupportedLanguageItems.find { it.code == selectedLanguageCode } ?: SupportedLanguageItems.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = "${currentSelected.flag}  ${currentSelected.displayName} (${currentSelected.code})",
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
            SupportedLanguageItems.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${language.flag}  ${language.displayName} (${language.code})",
                            color = TextPrimary,
                        )
                    },
                    onClick = {
                        onLanguageSelected(language.code)
                        expanded = false
                    },
                )
            }
        }
    }
}
