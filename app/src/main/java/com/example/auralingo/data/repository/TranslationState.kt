package com.example.auralingo.data.repository

/**
 * Sealed hierarchy representing every lifecycle phase of an on-device ML Kit translation request.
 */
sealed interface TranslationState {

    /**
     * Initial loading or initializing phase.
     */
    data object Loading : TranslationState

    /**
     * Model download phase when the requested translation language pack is not yet present on device.
     */
    data object DownloadingModel : TranslationState

    /**
     * Successful translation phase containing the translated text.
     *
     * @property translatedText The resulting translated string.
     */
    data class Success(val translatedText: String) : TranslationState

    /**
     * Error phase when translation or model downloading fails.
     *
     * @property message Human-readable error message.
     * @property cause Optional underlying exception or throwable cause.
     */
    data class Error(val message: String, val cause: Throwable? = null) : TranslationState
}
