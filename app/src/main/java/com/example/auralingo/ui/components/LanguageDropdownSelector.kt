package com.example.auralingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Modello dati per le opzioni di lingua supportate dal selettore.
 */
data class LanguageItem(
    val code: String,
    val displayName: String,
    val flag: String,
)

/**
 * Elenco predefinito delle lingue supportate da AuraLingo con codici BCP-47.
 */
val SupportedLanguageItems = listOf(
    LanguageItem("es-ES", "Español", "🇪🇸"),
    LanguageItem("en-US", "English", "🇺🇸"),
    LanguageItem("it-IT", "Italiano", "🇮🇹"),
    LanguageItem("fr-FR", "Français", "🇫🇷"),
    LanguageItem("de-DE", "Deutsch", "🇩🇪"),
)

/**
 * Componente M3 Dropdown Selettore Lingua riutilizzabile.
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

    // Trova l'elemento della lingua corrente in base al codice BCP-47 (es. "es-ES")
    val currentSelected = SupportedLanguageItems.find { it.code.equals(selectedLanguageCode, ignoreCase = true) }
        ?: SupportedLanguageItems.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = "${currentSelected.flag} ${currentSelected.displayName}",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = TextSecondary, fontSize = 11.sp) },
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
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(SlateVioletSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
        ) {
            SupportedLanguageItems.forEach { language ->
                val isSelected = language.code.equals(currentSelected.code, ignoreCase = true)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${language.flag}  ${language.displayName} (${language.code})",
                            color = if (isSelected) NeonViolet else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
