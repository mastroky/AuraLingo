package com.example.auralingo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Main Room database for AuraLingo.
 * Version 3: Supports multi-language partitioning, part of speech categories, and database indices.
 */
@Database(entities = [VocabularyWord::class], version = 3, exportSchema = false)
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
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN language_code TEXT NOT NULL DEFAULT 'es'")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN part_of_speech TEXT NOT NULL DEFAULT 'other'")
            }
        }

        /**
         * Migration from database version 2 to 3:
         * Creates indices for 'language_code' and 'part_of_speech' columns.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_words_language_code` ON `vocabulary_words` (`language_code`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_words_part_of_speech` ON `vocabulary_words` (`part_of_speech`)")
            }
        }

        /**
         * Migration from database version 1 to 3 directly:
         * Adds columns and creates indices for 'language_code' and 'part_of_speech'.
         */
        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN language_code TEXT NOT NULL DEFAULT 'es'")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN part_of_speech TEXT NOT NULL DEFAULT 'other'")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_words_language_code` ON `vocabulary_words` (`language_code`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_words_part_of_speech` ON `vocabulary_words` (`part_of_speech`)")
            }
        }

        /**
         * Creates an isolated AppDatabase instance dedicated to a specific user ID ("auralingo_vocab_<userId>.db").
         */
        fun createDatabaseForUser(context: Context, userId: Long): AppDatabase {
            val dbName = "auralingo_vocab_$userId.db"
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                dbName,
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * Returns the default singleton instance of AppDatabase ("auralingo.db").
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auralingo.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
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
