package com.example.auralingo.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Main container screen for authentication flows, hosting Login and Registration screens.
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isRegisterMode) {
        RegisterScreen(
            uiState = uiState,
            onNicknameChange = { viewModel.onNicknameChange(it) },
            onEmailChange = { viewModel.onEmailChange(it) },
            onPasswordChange = { viewModel.onPasswordChange(it) },
            onNativeLanguageChange = { viewModel.onNativeLanguageCodeChange(it) },
            onAvatarUriChange = { viewModel.onAvatarUriChange(it) },
            onTogglePasswordVisibility = { viewModel.togglePasswordVisibility() },
            onSubmit = { viewModel.submitForm(onAuthSuccess) },
            onSwitchToLogin = { viewModel.toggleAuthMode() },
            modifier = modifier,
        )
    } else {
        LoginScreen(
            uiState = uiState,
            onEmailChange = { viewModel.onEmailChange(it) },
            onPasswordChange = { viewModel.onPasswordChange(it) },
            onTogglePasswordVisibility = { viewModel.togglePasswordVisibility() },
            onSubmit = { viewModel.submitForm(onAuthSuccess) },
            onSwitchToRegister = { viewModel.toggleAuthMode() },
            modifier = modifier,
        )
    }
}
