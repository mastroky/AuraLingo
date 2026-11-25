package com.example.auralingo.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.auralingo.data.auth.AuthRepository
import com.example.auralingo.data.auth.SessionManager
import com.example.auralingo.data.local.UserDatabaseManager
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test verifying per-user database isolation and session persistence in AuraLingo.
 */
@RunWith(AndroidJUnit4::class)
class AuthDatabaseIsolationTest {

    private lateinit var context: Context
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var vocabularyRepository: VocabularyRepository
    private lateinit var userDatabaseManager: UserDatabaseManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sessionManager = SessionManager.getInstance(context)
        userDatabaseManager = UserDatabaseManager.getInstance()
        authRepository = AuthRepository.getInstance(context)
        vocabularyRepository = VocabularyRepository(context, sessionManager, userDatabaseManager)

        runBlocking {
            sessionManager.clearSession()
            userDatabaseManager.closeAll()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            sessionManager.clearSession()
            userDatabaseManager.closeAll()
        }
    }

    @Test
    fun testPerUserDatabaseIsolation() = runBlocking {
        // 1. Register User Profile A
        val resultA = authRepository.registerUser(
            nickname = "Alice",
            email = "alice_${System.currentTimeMillis()}@example.com",
            password = "password123",
            nativeLanguageCode = "en",
        )
        assertTrue("Profile A registration should succeed", resultA.isSuccess)
        val profileA = resultA.getOrThrow()
        val userIdA = profileA.id

        // Verify active session set to User A
        assertEquals(userIdA, sessionManager.currentUserId.first())

        // 2. Save a vocabulary word under Profile A
        val wordA = VocabularyWord(
            originalWord = "Bonjour",
            translatedWord = "Hello",
            contextSentence = "Bonjour tout le monde",
            categoryTag = "fr • noun",
            languageCode = "fr",
            partOfSpeech = "noun",
        )
        vocabularyRepository.insertWord(wordA)

        // Verify word exists in Profile A's database
        val wordsA = vocabularyRepository.allWords.first()
        assertEquals(1, wordsA.size)
        assertEquals("Bonjour", wordsA[0].originalWord)

        // 3. Register and switch to User Profile B
        val resultB = authRepository.registerUser(
            nickname = "Bob",
            email = "bob_${System.currentTimeMillis()}@example.com",
            password = "password456",
            nativeLanguageCode = "es",
        )
        assertTrue("Profile B registration should succeed", resultB.isSuccess)
        val profileB = resultB.getOrThrow()
        val userIdB = profileB.id

        // Verify active session switched to User B
        assertEquals(userIdB, sessionManager.currentUserId.first())

        // 4. Assert that Profile B's vocabulary database is completely empty/isolated
        val wordsB = vocabularyRepository.allWords.first()
        assertTrue("Profile B vocabulary should be completely empty and isolated from Profile A", wordsB.isEmpty())

        // 5. Verify physical database files exist on disk for both users ("auralingo_vocab_<userId>.db")
        val dbFileA = context.getDatabasePath("auralingo_vocab_$userIdA.db")
        val dbFileB = context.getDatabasePath("auralingo_vocab_$userIdB.db")
        assertTrue("Database file for User A should exist on disk", dbFileA.exists())
        assertTrue("Database file for User B should exist on disk", dbFileB.exists())

        // 6. Switch back to Profile A and verify Profile A's vocabulary is intact
        val loginResult = authRepository.loginUser(profileA.email, "password123")
        assertTrue("Profile A login should succeed", loginResult.isSuccess)
        assertEquals(userIdA, sessionManager.currentUserId.first())
        val wordsARetrieved = vocabularyRepository.allWords.first()
        assertEquals(1, wordsARetrieved.size)
        assertEquals("Bonjour", wordsARetrieved[0].originalWord)
    }
}
