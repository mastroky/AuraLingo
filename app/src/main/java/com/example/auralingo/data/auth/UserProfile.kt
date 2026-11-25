package com.example.auralingo.data.auth

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a registered user profile in AuraLingo.
 *
 * @property id Generated unique row identifier.
 * @property nickname User's display nickname.
 * @property email User's registered email address (unique index).
 * @property passwordHash SHA-256 salted hash of the user's password.
 * @property nativeLanguageCode Two-letter ISO language code for the user's native language (e.g. "en", "es", "it", "fr", "de").
 * @property avatarUri Optional URI string referencing the user's profile avatar picture.
 */
@Entity(
    tableName = "user_profiles",
    indices = [
        Index(value = ["email"], unique = true),
    ],
)
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nickname: String,
    val email: String,
    val passwordHash: String,
    val nativeLanguageCode: String,
    val avatarUri: String? = null,
)
