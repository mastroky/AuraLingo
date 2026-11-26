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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository handling thread-safe on-device ML Kit translation with caching, automatic language pack downloading,
 * edge-case handling, and reactive state emissions.
 */
class TranslationRepository private constructor() {

    private val translatorCache = ConcurrentHashMap<String, Translator>()
    private val cacheLock = Any()

    /**
     * Translates the given text from a source language code to a target language code, returning a reactive Flow of TranslationState on Dispatchers.IO.
     */
    fun translate(text: String, sourceLangCode: String, targetLangCode: String): Flow<TranslationState> = callbackFlow {
        try {
            send(TranslationState.Loading)

            val trimmedText = text.trim()
            if (trimmedText.isBlank() || trimmedText.all { !it.isLetterOrDigit() }) {
                send(TranslationState.Success(""))
                close()
                return@callbackFlow
            }

            val srcMlKit = try {
                normalizeLanguageToMlKit(sourceLangCode)
            } catch (e: Exception) {
                send(TranslationState.Error(message = "Unsupported source language: $sourceLangCode", cause = e))
                close()
                return@callbackFlow
            }

            val tgtMlKit = try {
                normalizeLanguageToMlKit(targetLangCode)
            } catch (e: Exception) {
                send(TranslationState.Error(message = "Unsupported target language: $targetLangCode", cause = e))
                close()
                return@callbackFlow
            }

            // Same-language optimization
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
            // Keep translators cached in map, callbackFlow terminates
        }
        awaitClose {
            // No-op
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Thread-safely retrieves or creates a Translator instance cached by source->target key.
     */
    private fun getOrCreateTranslator(sourceLang: String, targetLang: String): Translator {
        val key = "$sourceLang->$targetLang"
        translatorCache[key]?.let { return it }

        synchronized(cacheLock) {
            translatorCache[key]?.let { return it }

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()

            val newTranslator = Translation.getClient(options)
            translatorCache[key] = newTranslator
            return newTranslator
        }
    }

    /**
     * Normalizes various language representations (ISO codes, BCP-47 tags, and display names) to ML Kit TranslateLanguage constants.
     */
    private fun normalizeLanguageToMlKit(langInput: String): String {
        val cleaned = langInput.trim()
        if (cleaned.isBlank()) throw IllegalArgumentException("Language code cannot be blank.")
        val lower = cleaned.lowercase()
        val iso = lower.substringBefore("-")

        return when (iso) {
            "es", "spanish", "español" -> TranslateLanguage.SPANISH
            "en", "english" -> TranslateLanguage.ENGLISH
            "it", "italian", "italiano" -> TranslateLanguage.ITALIAN
            "fr", "french", "français" -> TranslateLanguage.FRENCH
            "de", "german", "deutsch" -> TranslateLanguage.GERMAN
            else -> {
                try {
                    TranslateLanguage.getAllLanguages().find {
                        it.equals(cleaned, ignoreCase = true) || it.equals(iso, ignoreCase = true)
                    } ?: throw IllegalArgumentException("Unsupported language code: $langInput")
                } catch (e: Exception) {
                    throw IllegalArgumentException("Unsupported language code: $langInput", e)
                }
            }
        }
    }

    /**
     * Safely closes and releases all cached native ML Kit translator instances and clears the cache.
     */
    fun close() {
        synchronized(cacheLock) {
            translatorCache.values.forEach { translator ->
                try {
                    translator.close()
                } catch (_: Exception) {
                }
            }
            translatorCache.clear()
        }
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

    companion object {
        @Volatile
        private var INSTANCE: TranslationRepository? = null

        /**
         * Returns singleton instance of TranslationRepository.
         */
        fun getInstance(): TranslationRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TranslationRepository().also { INSTANCE = it }
            }
    }
}
