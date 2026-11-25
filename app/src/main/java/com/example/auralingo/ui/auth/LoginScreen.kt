package com.example.auralingo.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auralingo.ui.theme.CardBorder
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.NeonPink
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.ObsidianDark
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextPrimary
import com.example.auralingo.ui.theme.TextSecondary

/**
 * Schermata di Login con supporto per l'autenticazione tramite Email o Username.
 */
@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            // Icona Logo AuraLingo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SlateVioletSurface)
                    .border(2.dp, NeonViolet, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AuraLingo Logo",
                    tint = NeonViolet,
                    modifier = Modifier.size(42.dp),
                )
            }

            // Titolo della schermata
            Text(
                text = "AuraLingo",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonViolet,
                    letterSpacing = 1.sp,
                ),
            )
            Text(
                text = "Welcome back! Log in to access your glossary.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Banner per i messaggi di errore di autenticazione
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

            // Campo di input Email o Username
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Email or Username", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonViolet) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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

            // Campo di input Password
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Password", color = TextSecondary) },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Pulsante di conferma Login
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
                        text = "Log In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            // Pulsante per passare alla schermata di Registrazione
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                TextButton(onClick = onSwitchToRegister) {
                    Text(
                        text = "Sign Up",
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
