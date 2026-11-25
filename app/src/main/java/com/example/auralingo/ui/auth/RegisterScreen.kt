package com.example.auralingo.ui.auth

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.NeonPink
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.ObsidianDark
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

private data class NativeLanguageOption(
    val code: String,
    val label: String,
)

private val NativeLanguageOptions = listOf(
    NativeLanguageOption("en", "English 🇺🇸"),
    NativeLanguageOption("es", "Español 🇪🇸"),
    NativeLanguageOption("it", "Italiano 🇮🇹"),
    NativeLanguageOption("fr", "Français 🇫🇷"),
    NativeLanguageOption("de", "Deutsch 🇩🇪"),
)

/**
 * Registration screen displaying user avatar picker, credentials form, native language selector, and validation feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onNicknameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNativeLanguageChange: (String) -> Unit,
    onAvatarUriChange: (String?) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (e: Exception) {
                // Ignore if persistable permission cannot be granted for certain URI schemes
            }
            onAvatarUriChange(uri.toString())
        }
    }

    val currentLanguageSelected = NativeLanguageOptions.find { it.code == uiState.nativeLanguageCode }
        ?: NativeLanguageOptions.first()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Title
            Text(
                text = "AuraLingo",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonViolet,
                    letterSpacing = 1.sp,
                ),
            )
            Text(
                text = "Create your account to start neural learning",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Profile Photo Selector Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.avatarUri != null) {
                    AsyncImage(
                        model = uiState.avatarUri,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, NeonViolet, CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(SlateVioletSurface)
                            .border(2.dp, NeonViolet, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            tint = NeonViolet,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }

                // Edit Camera Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(NeonViolet),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Select Avatar",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Text(
                text = "Tap to choose profile photo",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error Message Banner
            if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, NeonPink, RoundedCornerShape(12.dp)),
                    color = NeonPink.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Nickname Input
            OutlinedTextField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                label = { Text("Nickname", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonViolet) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SlateVioletSurface,
                    unfocusedContainerColor = SlateVioletSurface,
                    focusedBorderColor = NeonViolet,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
            )

            // Email Input
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Email Address", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonViolet) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SlateVioletSurface,
                    unfocusedContainerColor = SlateVioletSurface,
                    focusedBorderColor = NeonViolet,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
            )

            // Password Input
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Password (6+ chars)", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonViolet) },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Password Visibility",
                            tint = TextSecondary,
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SlateVioletSurface,
                    unfocusedContainerColor = SlateVioletSurface,
                    focusedBorderColor = NeonViolet,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
            )

            // Native Language Dropdown Selector
            ExposedDropdownMenuBox(
                expanded = isLanguageDropdownExpanded,
                onExpandedChange = { isLanguageDropdownExpanded = !isLanguageDropdownExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = currentLanguageSelected.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Native Language", color = TextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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

                ExposedDropdownMenu(
                    expanded = isLanguageDropdownExpanded,
                    onDismissRequest = { isLanguageDropdownExpanded = false },
                    modifier = Modifier
                        .background(SlateVioletSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                ) {
                    NativeLanguageOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    color = if (option.code == uiState.nativeLanguageCode) NeonViolet else TextPrimary,
                                    fontWeight = if (option.code == uiState.nativeLanguageCode) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            onClick = {
                                onNativeLanguageChange(option.code)
                                isLanguageDropdownExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button CTA
            Button(
                onClick = onSubmit,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonViolet,
                    contentColor = TextPrimary,
                    disabledContainerColor = CardElevated,
                ),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = TextPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            // Bottom Switch to Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                TextButton(onClick = onSwitchToLogin) {
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonViolet,
                        ),
                    )
                }
            }
        }
    }
}
