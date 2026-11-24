package com.example.auralingo.data.local

import android.content.Context

/**
 * Thread-safe manager handling per-user isolated Room database instances ("auralingo_vocab_<userId>.db").
 */
class UserDatabaseManager private constructor() {

    @Volatile
    private var activeDatabaseInstance: Pair<Long, AppDatabase>? = null

    /**
     * Retrieves or initializes the isolated AppDatabase instance for the specified userId.
     * Safely closes any existing database instance associated with a different user ID.
     */
    @Synchronized
    fun getDatabaseForUser(context: Context, userId: Long): AppDatabase {
        val currentPair = activeDatabaseInstance
        if (currentPair != null && currentPair.first == userId && currentPair.second.isOpen) {
            return currentPair.second
        }

        // Close existing database instance if user switched
        if (currentPair != null) {
            closeUserDatabase()
        }

        val newDatabase = AppDatabase.createDatabaseForUser(context, userId)
        activeDatabaseInstance = Pair(userId, newDatabase)
        return newDatabase
    }

    /**
     * Safely closes the currently active user database instance if open.
     */
    @Synchronized
    fun closeUserDatabase() {
        activeDatabaseInstance?.let { (_, db) ->
            if (db.isOpen) {
                db.close()
            }
        }
        activeDatabaseInstance = null
    }

    /**
     * Alias for closing all user databases (e.g. during logout).
     */
    fun closeAll() {
        closeUserDatabase()
    }

    companion object {
        @Volatile
        private var INSTANCE: UserDatabaseManager? = null

        /**
         * Returns singleton instance of UserDatabaseManager.
         */
        fun getInstance(): UserDatabaseManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserDatabaseManager().also { INSTANCE = it }
            }
    }
}
