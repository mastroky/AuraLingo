package com.example.auralingo.data.auth

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Dedicated Room Database for authentication credentials and user profile management in AuraLingo.
 */
@Database(entities = [UserProfile::class], version = 1, exportSchema = false)
abstract class AuthDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AuthDatabase? = null

        /**
         * Returns the singleton instance of AuthDatabase backed by "auralingo_auth.db".
         */
        fun getInstance(context: Context): AuthDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AuthDatabase::class.java,
                    "auralingo_auth.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
