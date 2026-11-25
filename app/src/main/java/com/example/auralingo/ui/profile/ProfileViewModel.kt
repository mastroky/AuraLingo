package com.example.auralingo.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auralingo.data.auth.AuthRepository
import com.example.auralingo.data.auth.UserProfile
import com.example.auralingo.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stato immutabile dell'interfaccia utente per la schermata di gestione del profilo utente.
 */
data class ProfileUiState(
    val userId: Long = 0L,
    val email: String = "",
    val nickname: String = "",
    val nativeLanguageCode: String = "en",
    val avatarUri: String? = null,
    val totalSavedWords: Int = 0,
    val distinctLanguagesCount: Int = 0,
    val hasUnsavedChanges: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

/**
 * ViewModel che gestisce le informazioni del profilo utente, le statistiche del vocabolario e il salvataggio dei cambiamenti.
 */
class ProfileViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val vocabularyRepository: VocabularyRepository,
) : AndroidViewModel(application) {

    /**
     * Costruttore secondario per la compatibilità con AndroidViewModelFactory predefinito.
     */
    constructor(application: Application) : this(
        application = application,
        authRepository = AuthRepository.getInstance(application),
        vocabularyRepository = VocabularyRepository(application),
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    @Volatile
    private var persistedProfile: UserProfile? = null

    init {
        // Osserva reattivamente sia il profilo utente corrente che le parole salvate nel vocabolario isolato dell'utente
        viewModelScope.launch {
            combine(
                authRepository.getCurrentUserFlow(),
                vocabularyRepository.allWords,
            ) { user, words ->
                Pair(user, words)
            }.collect { (user, words) ->
                if (user != null) {
                    val initialLoad = persistedProfile == null || persistedProfile?.id != user.id
                    persistedProfile = user

                    val totalWords = words.size
                    val distinctLangs = words.map { it.languageCode }.distinct().size

                    _uiState.update { currentState ->
                        if (initialLoad) {
                            currentState.copy(
                                userId = user.id,
                                email = user.email,
                                nickname = user.nickname,
                                nativeLanguageCode = user.nativeLanguageCode,
                                avatarUri = user.avatarUri,
                                totalSavedWords = totalWords,
                                distinctLanguagesCount = distinctLangs,
                                hasUnsavedChanges = false,
                            )
                        } else {
                            val currentNick = currentState.nickname.trim()
                            val currentLang = currentState.nativeLanguageCode.trim().lowercase()
                            val currentAvatar = currentState.avatarUri
                            val hasChanges = currentNick != user.nickname ||
                                    currentLang != user.nativeLanguageCode ||
                                    currentAvatar != user.avatarUri

                            currentState.copy(
                                totalSavedWords = totalWords,
                                distinctLanguagesCount = distinctLangs,
                                hasUnsavedChanges = hasChanges,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onNicknameChange(value: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                nickname = value,
                errorMessage = null,
                successMessage = null,
            )
            val persisted = persistedProfile
            val hasChanges = if (persisted != null) {
                value.trim() != persisted.nickname ||
                        updatedState.nativeLanguageCode.trim().lowercase() != persisted.nativeLanguageCode ||
                        updatedState.avatarUri != persisted.avatarUri
            } else {
                false
            }
            updatedState.copy(hasUnsavedChanges = hasChanges)
        }
    }

    fun onNativeLanguageSelected(code: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                nativeLanguageCode = code,
                errorMessage = null,
                successMessage = null,
            )
            val persisted = persistedProfile
            val hasChanges = if (persisted != null) {
                updatedState.nickname.trim() != persisted.nickname ||
                        code.trim().lowercase() != persisted.nativeLanguageCode ||
                        updatedState.avatarUri != persisted.avatarUri
            } else {
                false
            }
            updatedState.copy(hasUnsavedChanges = hasChanges)
        }
    }

    fun onAvatarSelected(uri: String?) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                avatarUri = uri,
                errorMessage = null,
                successMessage = null,
            )
            val persisted = persistedProfile
            val hasChanges = if (persisted != null) {
                updatedState.nickname.trim() != persisted.nickname ||
                        updatedState.nativeLanguageCode.trim().lowercase() != persisted.nativeLanguageCode ||
                        uri != persisted.avatarUri
            } else {
                false
            }
            updatedState.copy(hasUnsavedChanges = hasChanges)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    /**
     * Valida ed esegue il salvataggio delle modifiche al profilo tramite AuthRepository.
     */
    fun saveChanges() {
        val currentState = _uiState.value
        val cleanNickname = currentState.nickname.trim()
        val langCode = currentState.nativeLanguageCode.trim().lowercase()

        if (cleanNickname.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nickname cannot be empty.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.updateProfile(
                userId = currentState.userId,
                nickname = cleanNickname,
                nativeLanguageCode = langCode,
                avatarUri = currentState.avatarUri,
            )

            result.fold(
                onSuccess = { updatedProfile ->
                    persistedProfile = updatedProfile
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            nickname = updatedProfile.nickname,
                            nativeLanguageCode = updatedProfile.nativeLanguageCode,
                            avatarUri = updatedProfile.avatarUri,
                            hasUnsavedChanges = false,
                            successMessage = "Profile updated successfully!",
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Failed to update profile. Please try again.",
                            successMessage = null,
                        )
                    }
                },
            )
        }
    }

    /**
     * Esegue il logout dell'utente disconnettendo la sessione e chiudendo il database utente.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
