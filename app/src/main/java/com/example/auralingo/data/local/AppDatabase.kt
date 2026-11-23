package com.example.auralingo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Main Room database for AuraLingo.
 */
@Database(entities = [VocabularyWord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vocabularyDao(): VocabularyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from database version 1 to 2:
         * Adds 'language_code' and 'part_of_speech' columns to vocabulary_words.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN language_code TEXT NOT NULL DEFAULT 'en'")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN part_of_speech TEXT NOT NULL DEFAULT 'other'")
            }
        }

        /**
         * Returns the singleton instance of AppDatabase.
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auralingo.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }

        /**
         * Convenience alias for database instance retrieval.
         */
        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}
