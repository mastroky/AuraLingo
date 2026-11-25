package com.example.auralingo.data.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing user authentication and profile operations in AuthDatabase.
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user profile into the auth database, returning the auto-generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserProfile): Long

    /**
     * Finds a user profile by email address (case-insensitive search recommended).
     */
    @Query("SELECT * FROM user_profiles WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfile?

    /**
     * Finds a user profile by unique user ID.
     */
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserProfile?

    /**
     * Observes a user profile reactively by user ID.
     */
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun observeUserById(id: Long): Flow<UserProfile?>

    /**
     * Updates profile fields for the specified user ID.
     */
    @Query("UPDATE user_profiles SET nickname = :nickname, nativeLanguageCode = :nativeLanguageCode, avatarUri = :avatarUri WHERE id = :id")
    suspend fun updateProfile(id: Long, nickname: String, nativeLanguageCode: String, avatarUri: String?)
}
