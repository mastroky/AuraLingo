package com.example.auralingo.data.translation

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository handling on-device ML Kit translation with automatic language pack model downloading and reactive state emission.
 */
class TranslationRepository {

    private var activeTranslator: Translator? = null
    private var activeSourceLang: String? = null
    private var activeTargetLang: String? = null

    /**
     * Translates the given text from a source language code to a target language code, returning a reactive Flow of TranslationState.
     */
    fun translate(text: String, sourceLangCode: String, targetLangCode: String): Flow<TranslationState> = callbackFlow {
        try {
            send(TranslationState.Loading)

            val trimmedText = text.trim()
            if (trimmedText.isEmpty()) {
                send(TranslationState.Success(""))
                close()
                return@callbackFlow
            }

            val srcMlKit = normalizeLanguageToMlKit(sourceLangCode)
            val tgtMlKit = normalizeLanguageToMlKit(targetLangCode)

            if (srcMlKit == tgtMlKit) {
                send(TranslationState.Success(trimmedText))
                close()
                return@callbackFlow
            }

            val translator = getOrCreateTranslator(srcMlKit, tgtMlKit)

            // Check if models are downloaded
            val remoteModelManager = RemoteModelManager.getInstance()
            val srcModel = TranslateRemoteModel.Builder(srcMlKit).build()
            val tgtModel = TranslateRemoteModel.Builder(tgtMlKit).build()

            val srcDownloaded = remoteModelManager.isModelDownloaded(srcModel).safeAwait()
            val tgtDownloaded = remoteModelManager.isModelDownloaded(tgtModel).safeAwait()

            if (!srcDownloaded || !tgtDownloaded) {
                send(TranslationState.DownloadingModel)
            }

            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()

            val translatedText = translator.translate(trimmedText).await()
            send(TranslationState.Success(translatedText))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            send(TranslationState.Error(message = e.localizedMessage ?: "Translation failed", cause = e))
        } finally {
            close()
        }
        awaitClose {
            close()
        }
    }

    private fun getOrCreateTranslator(sourceLang: String, targetLang: String): Translator {
        if (activeTranslator != null && activeSourceLang == sourceLang && activeTargetLang == targetLang) {
            return activeTranslator!!
        }

        close()

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

        val newTranslator = Translation.getClient(options)
        activeTranslator = newTranslator
        activeSourceLang = sourceLang
        activeTargetLang = targetLang
        return newTranslator
    }

    /**
     * Normalizes various language representations (ISO codes, BCP-47 tags, and display names) to ML Kit TranslateLanguage constants.
     */
    private fun normalizeLanguageToMlKit(langInput: String): String {
        val cleaned = langInput.trim()
        val lower = cleaned.lowercase()
        val iso = lower.substringBefore("-")

        return when (iso) {
            "es", "spanish" -> TranslateLanguage.SPANISH
            "en", "english" -> TranslateLanguage.ENGLISH
            "it", "italian", "italiano" -> TranslateLanguage.ITALIAN
            "fr", "french", "francais" -> TranslateLanguage.FRENCH
            "de", "german", "deutsch" -> TranslateLanguage.GERMAN
            else -> {
                try {
                    TranslateLanguage.getAllLanguages().find { it.equals(cleaned, ignoreCase = true) || it.equals(iso, ignoreCase = true) }
                        ?: TranslateLanguage.ENGLISH
                } catch (_: Exception) {
                    TranslateLanguage.ENGLISH
                }
            }
        }
    }

    /**
     * Closes and releases native ML Kit translator resources.
     */
    fun close() {
        try {
            activeTranslator?.close()
        } catch (_: Exception) {
        }
        activeTranslator = null
        activeSourceLang = null
        activeTargetLang = null
    }

    /**
     * Lightweight coroutine await extension for Google Play Services Task<T>.
     */
    private suspend inline fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

    /**
     * Safe non-throwing await extension for model download checks.
     */
    private suspend fun Task<Boolean>.safeAwait(): Boolean {
        return try {
            this.await()
        } catch (_: Exception) {
            false
        }
    }
}
