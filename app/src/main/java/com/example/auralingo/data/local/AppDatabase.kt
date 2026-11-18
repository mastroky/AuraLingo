package com.example.auralingo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database principale Room per AuraLingo.
 * Versione 2: aggiunge il supporto alla multilingua e alle categorie grammaticali.
 */
@Database(entities = [VocabularyWord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vocabularyDao(): VocabularyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migrazione del database dalla versione 1 alla versione 2.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN language_code TEXT NOT NULL DEFAULT 'es'")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN part_of_speech TEXT NOT NULL DEFAULT 'other'")
            }
        }

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

        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}
