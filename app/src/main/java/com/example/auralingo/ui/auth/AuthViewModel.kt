package com.example.auralingo.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auralingo.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stato immutabile dell'interfaccia utente per l'autenticazione dell'utente (Login e Registrazione).
 */
data class AuthUiState(
    val isRegisterMode: Boolean = false,
    val nickname: String = "",
    val email: String = "",
    val password: String = "",
    val nativeLanguageCode: String = "en",
    val avatarUri: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
)

/**
 * ViewModel che gestisce lo stato di autenticazione, la validazione dei moduli e la comunicazione con AuthRepository.
 */
class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository,
) : AndroidViewModel(application) {

    /**
     * Costruttore secondario per la compatibilità con AndroidViewModelFactory predefinito.
     */
    constructor(application: Application) : this(
        application = application,
        authRepository = AuthRepository.getInstance(application),
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onNicknameChange(value: String) {
        _uiState.update { it.copy(nickname = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onNativeLanguageCodeChange(value: String) {
        _uiState.update { it.copy(nativeLanguageCode = value, errorMessage = null) }
    }

    fun onAvatarUriChange(value: String?) {
        _uiState.update { it.copy(avatarUri = value, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null,
                password = "",
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Valida i campi del modulo ed esegue il login o la registrazione tramite AuthRepository.
     */
    fun submitForm(onSuccess: () -> Unit = {}) {
        val currentState = _uiState.value
        val cleanEmailOrIdentifier = currentState.email.trim()
        val cleanNickname = currentState.nickname.trim()
        val cleanPassword = currentState.password
        val langCode = currentState.nativeLanguageCode.trim().lowercase()

        if (currentState.isRegisterMode) {
            // Validazione modulo di registrazione
            if (cleanNickname.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Nickname cannot be empty.") }
                return
            }
            if (!isValidEmail(cleanEmailOrIdentifier)) {
                _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
                return
            }
            if (cleanPassword.length < 6) {
                _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters long.") }
                return
            }
            if (langCode !in VALID_LANGUAGE_CODES) {
                _uiState.update { it.copy(errorMessage = "Please select a valid native language.") }
                return
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch {
                val result = authRepository.registerUser(
                    nickname = cleanNickname,
                    email = cleanEmailOrIdentifier,
                    password = cleanPassword,
                    nativeLanguageCode = langCode,
                    avatarUri = currentState.avatarUri,
                )
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                        onSuccess()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Registration failed. Please try again.",
                            )
                        }
                    },
                )
            }
        } else {
            // Validazione modulo di login (accetta sia Email che Username/Nickname)
            if (cleanEmailOrIdentifier.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Please enter your email or username.") }
                return
            }
            if (cleanPassword.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Password cannot be empty.") }
                return
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch {
                val result = authRepository.loginUser(
                    identifier = cleanEmailOrIdentifier,
                    password = cleanPassword,
                )
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                        onSuccess()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Invalid email/username or password.",
                            )
                        }
                    },
                )
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    companion object {
        private val VALID_LANGUAGE_CODES = setOf("en", "es", "it", "fr", "de")
    }
}
