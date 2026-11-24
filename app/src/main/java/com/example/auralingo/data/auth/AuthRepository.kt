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
 * Repository for user authentication, registration, session tracking, and profile management.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val userDatabaseManager: UserDatabaseManager = UserDatabaseManager.getInstance(),
) {

    /**
     * Registers a new user account in AuthDatabase and persists active session.
     */
    suspend fun registerUser(
        nickname: String,
        email: String,
        password: String,
        nativeLanguageCode: String,
        avatarUri: String? = null,
    ): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank() || nickname.isBlank()) {
            return Result.failure(IllegalArgumentException("Nickname, email, and password cannot be empty."))
        }

        // Reject duplicate email registration cleanly
        val existingUser = userDao.getUserByEmail(cleanEmail)
        if (existingUser != null) {
            return Result.failure(IllegalArgumentException("An account with email '$cleanEmail' already exists."))
        }

        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        val passwordHash = "$salt:$hash"

        val newUser = UserProfile(
            nickname = nickname.trim(),
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
     * Authenticates an existing user by email and password, updating session DataStore.
     */
    suspend fun loginUser(email: String, password: String): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Invalid email or password."))

        val isPasswordValid = verifyPassword(password, user.passwordHash)
        if (!isPasswordValid) {
            return Result.failure(IllegalArgumentException("Invalid email or password."))
        }

        sessionManager.setCurrentUserId(user.id)
        return Result.success(user)
    }

    /**
     * Updates user profile attributes (nickname, native language, avatar URI).
     */
    suspend fun updateProfile(
        userId: Long,
        nickname: String,
        nativeLanguageCode: String,
        avatarUri: String?,
    ): Result<UserProfile> {
        val existingUser = userDao.getUserById(userId)
            ?: return Result.failure(IllegalArgumentException("User profile with ID $userId not found."))

        return try {
            userDao.updateProfile(
                id = userId,
                nickname = nickname.trim(),
                nativeLanguageCode = nativeLanguageCode.trim().lowercase(),
                avatarUri = avatarUri,
            )
            val updated = existingUser.copy(
                nickname = nickname.trim(),
                nativeLanguageCode = nativeLanguageCode.trim().lowercase(),
                avatarUri = avatarUri,
            )
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reactively observes the currently logged-in UserProfile based on active session ID.
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
     * Logs out the current user, clearing session DataStore and closing isolated user database.
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
         * Returns singleton instance of AuthRepository.
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
