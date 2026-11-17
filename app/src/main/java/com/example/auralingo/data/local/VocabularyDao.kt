package com.example.auralingo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Insert
    suspend fun insert(word: VocabularyWord)

    @Delete
    suspend fun delete(word: VocabularyWord)

    @Query("SELECT * FROM vocabulary_words ORDER BY timestamp DESC")
    fun getAll(): Flow<List<VocabularyWord>>
}