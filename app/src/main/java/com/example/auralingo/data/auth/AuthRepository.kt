package com.example.auralingo.data.auth

import android.content.Context
import com.example.auralingo.data.local.UserDatabaseManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Repository per la gestione dell'autenticazione, registrazione, tracciamento sessioni e profilo utente.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val userDatabaseManager: UserDatabaseManager = UserDatabaseManager.getInstance(),
) {

    /**
     * Registra un nuovo account utente in AuthDatabase e salva la sessione attiva.
     * Verifica l'univocità sia dell'email che del nickname.
     */
    suspend fun registerUser(
        nickname: String,
        email: String,
        password: String,
        nativeLanguageCode: String,
        avatarUri: String? = null,
    ): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        val cleanNickname = nickname.trim()

        if (cleanEmail.isBlank() || password.isBlank() || cleanNickname.isBlank()) {
            return Result.failure(IllegalArgumentException("Nickname, email, and password cannot be empty."))
        }

        // Rifiuta la registrazione se l'email è già in uso
        val existingUserByEmail = userDao.getUserByEmail(cleanEmail)
        if (existingUserByEmail != null) {
            return Result.failure(IllegalArgumentException("An account with email '$cleanEmail' already exists."))
        }

        // Rifiuta la registrazione se il nickname è già in uso
        val existingUserByNickname = userDao.getUserByNickname(cleanNickname)
        if (existingUserByNickname != null) {
            return Result.failure(IllegalArgumentException("Nickname '$cleanNickname' is already taken."))
        }

        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        val passwordHash = "$salt:$hash"

        val newUser = UserProfile(
            nickname = cleanNickname,
            email = cleanEmail,
            passwordHash = passwordHash,
            nativeLanguageCode = nativeLanguageCode.trim().lowercase(),
            avatarUri = avatarUri,
        )

        return try {
            val generatedId = userDao.insertUser(newUser)
            sessionManager.setCurrentUserId(generatedId)
            Result.success(newUser.copy(id = generatedId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Autentica un utente esistente tramite email oppure nickname e password, aggiornando la sessione DataStore.
     */
    suspend fun loginUser(identifier: String, password: String): Result<UserProfile> {
        val cleanIdentifier = identifier.trim()
        if (cleanIdentifier.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email/username and password cannot be empty."))
        }

        // Se l'identificatore contiene '@', effettua la ricerca per email, altrimenti per nickname
        val user = if (cleanIdentifier.contains("@")) {
            userDao.getUserByEmail(cleanIdentifier.lowercase())
        } else {
            userDao.getUserByNickname(cleanIdentifier)
        } ?: return Result.failure(IllegalArgumentException("Invalid email/username or password."))

        val isPasswordValid = verifyPassword(password, user.passwordHash)
        if (!isPasswordValid) {
            return Result.failure(IllegalArgumentException("Invalid email/username or password."))
        }

        sessionManager.setCurrentUserId(user.id)
        return Result.success(user)
    }

    /**
     * Aggiorna gli attributi del profilo utente (nickname, lingua madre, URI avatar), verificando l'univocità del nickname.
     */
    suspend fun updateProfile(
        userId: Long,
        nickname: String,
        nativeLanguageCode: String,
        avatarUri: String?,
    ): Result<UserProfile> {
        val cleanNickname = nickname.trim()
        val existingUser = userDao.getUserById(userId)
            ?: return Result.failure(IllegalArgumentException("User profile with ID $userId not found."))

        // Verifica che il nuovo nickname non sia già utilizzato da un altro utente
        val existingUserByNickname = userDao.getUserByNickname(cleanNickname)
        if (existingUserByNickname != null && existingUserByNickname.id != userId) {
            return Result.failure(IllegalArgumentException("Nickname '$cleanNickname' is already taken."))
        }

        return try {
            userDao.updateProfile(
                id = userId,
                nickname = cleanNickname,
                nativeLanguageCode = nativeLanguageCode.trim().lowercase(),
                avatarUri = avatarUri,
            )
            val updated = existingUser.copy(
                nickname = cleanNickname,
                nativeLanguageCode = nativeLanguageCode.trim().lowercase(),
                avatarUri = avatarUri,
            )
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Osserva in modo reattivo il UserProfile dell'utente attualmente connesso in base all'ID sessione attivo.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentUserFlow(): Flow<UserProfile?> {
        return sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId != null) {
                userDao.observeUserById(userId)
            } else {
                flowOf(null)
            }
        }
    }

    /**
     * Disconnette l'utente corrente, cancellando la sessione DataStore e chiudendo il database isolato dell'utente.
     */
    suspend fun logout() {
        sessionManager.clearSession()
        userDatabaseManager.closeAll()
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        val salt = parts[0]
        val expectedHash = parts[1]
        val computedHash = hashPassword(password, salt)
        return computedHash == expectedHash
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        /**
         * Restituisce l'istanza singleton di AuthRepository.
         */
        fun getInstance(context: Context): AuthRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository(
                    userDao = AuthDatabase.getInstance(context).userDao(),
                    sessionManager = SessionManager.getInstance(context),
                ).also { INSTANCE = it }
            }
    }
}
