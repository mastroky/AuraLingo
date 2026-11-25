package com.example.auralingo.data.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) per gestire le operazioni di autenticazione e profilo in AuthDatabase.
 */
@Dao
interface UserDao {

    /**
     * Inserisce un nuovo profilo utente nel database di autenticazione, restituendo l'ID riga generato.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserProfile): Long

    /**
     * Ricerca un profilo utente in base all'indirizzo email (ricerca case-insensitive).
     */
    @Query("SELECT * FROM user_profiles WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfile?

    /**
     * Ricerca un profilo utente in base al nickname/username (ricerca case-insensitive).
     */
    @Query("SELECT * FROM user_profiles WHERE LOWER(nickname) = LOWER(:nickname) LIMIT 1")
    suspend fun getUserByNickname(nickname: String): UserProfile?

    /**
     * Ricerca un profilo utente in base all'ID univoco dell'utente.
     */
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserProfile?

    /**
     * Osserva un profilo utente in modo reattivo in base all'ID utente.
     */
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun observeUserById(id: Long): Flow<UserProfile?>

    /**
     * Aggiorna i campi del profilo per l'ID utente specificato.
     */
    @Query("UPDATE user_profiles SET nickname = :nickname, nativeLanguageCode = :nativeLanguageCode, avatarUri = :avatarUri WHERE id = :id")
    suspend fun updateProfile(id: Long, nickname: String, nativeLanguageCode: String, avatarUri: String?)
}
