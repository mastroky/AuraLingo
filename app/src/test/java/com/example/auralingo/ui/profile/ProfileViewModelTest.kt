package com.example.auralingo.ui.profile

import android.app.Application
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.auralingo.data.auth.AuthDatabase
import com.example.auralingo.data.auth.AuthRepository
import com.example.auralingo.data.auth.SessionManager
import com.example.auralingo.data.local.UserDatabaseManager
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.data.repository.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Test di unità per ProfileViewModel che verificano il caricamento dati profilo, le statistiche vocabolario, la modifica e il logout.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ProfileViewModelTest {

    private lateinit var application: Application
    private lateinit var authDatabase: AuthDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var vocabularyRepository: VocabularyRepository
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        sessionManager = SessionManager.getInstance(application)

        authDatabase = Room.inMemoryDatabaseBuilder(application, AuthDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        authRepository = AuthRepository(
            userDao = authDatabase.userDao(),
            sessionManager = sessionManager,
            userDatabaseManager = UserDatabaseManager.getInstance(),
        )

        vocabularyRepository = VocabularyRepository(application, sessionManager)

        runBlocking {
            sessionManager.clearSession()
            UserDatabaseManager.getInstance().closeAll()
        }
    }

    @After
    fun tearDown() {
        authDatabase.close()
        Dispatchers.resetMain()
        runBlocking {
            sessionManager.clearSession()
            UserDatabaseManager.getInstance().closeAll()
        }
    }

    private suspend fun awaitProfileLoaded(viewModel: ProfileViewModel) {
        var retries = 0
        while (viewModel.uiState.value.email.isEmpty() && retries < 100) {
            delay(50)
            shadowOf(Looper.getMainLooper()).idle()
            retries++
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    private suspend fun awaitSavingFinished(viewModel: ProfileViewModel) {
        var retries = 0
        while (viewModel.uiState.value.isSaving && retries < 100) {
            delay(50)
            shadowOf(Looper.getMainLooper()).idle()
            retries++
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun testLoadingActiveUserProfileAndVocabularyStats() = runBlocking {
        val email = "profile_${System.currentTimeMillis()}@example.com"
        val registerResult = authRepository.registerUser(
            nickname = "OriginalNick",
            email = email,
            password = "password123",
            nativeLanguageCode = "en",
        )
        assertTrue(registerResult.isSuccess)

        // Insert vocabulary words for active user
        vocabularyRepository.insertWord(
            VocabularyWord(
                originalWord = "Hola",
                translatedWord = "Hello",
                contextSentence = "Hola amigo",
                categoryTag = "es • noun",
                languageCode = "es",
                partOfSpeech = "noun",
            ),
        )
        vocabularyRepository.insertWord(
            VocabularyWord(
                originalWord = "Ciao",
                translatedWord = "Hi",
                contextSentence = "Ciao mondo",
                categoryTag = "it • phrase",
                languageCode = "it",
                partOfSpeech = "phrase",
            ),
        )

        val viewModel = ProfileViewModel(application, authRepository, vocabularyRepository)
        awaitProfileLoaded(viewModel)

        val state = viewModel.uiState.value
        assertEquals("OriginalNick", state.nickname)
        assertEquals(email, state.email)
        assertEquals("en", state.nativeLanguageCode)
        assertEquals(2, state.totalSavedWords)
        assertEquals(2, state.distinctLanguagesCount)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun testEmptyNicknameValidationFailure() = runBlocking {
        val registerResult = authRepository.registerUser(
            nickname = "ValidNick",
            email = "user_${System.currentTimeMillis()}@example.com",
            password = "password123",
            nativeLanguageCode = "en",
        )
        assertTrue(registerResult.isSuccess)

        val viewModel = ProfileViewModel(application, authRepository, vocabularyRepository)
        awaitProfileLoaded(viewModel)

        viewModel.onNicknameChange("   ")
        viewModel.saveChanges()

        val state = viewModel.uiState.value
        assertEquals("Nickname cannot be empty.", state.errorMessage)
    }

    @Test
    fun testDuplicateNicknameUpdateFailure() = runBlocking {
        val email1 = "user1_${System.currentTimeMillis()}@example.com"
        val email2 = "user2_${System.currentTimeMillis()}@example.com"

        authRepository.registerUser("ExistingNick", email1, "password123", "en")
        val reg2 = authRepository.registerUser("UserTwo", email2, "password123", "es")
        assertTrue(reg2.isSuccess)

        val viewModel = ProfileViewModel(application, authRepository, vocabularyRepository)
        awaitProfileLoaded(viewModel)

        // Try updating UserTwo's nickname to "ExistingNick"
        viewModel.onNicknameChange("ExistingNick")
        viewModel.saveChanges()
        awaitSavingFinished(viewModel)

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("already taken"))
    }

    @Test
    fun testPersistingValidProfileChanges() = runBlocking {
        val email = "user_${System.currentTimeMillis()}@example.com"
        authRepository.registerUser("OldNick", email, "password123", "en")

        val viewModel = ProfileViewModel(application, authRepository, vocabularyRepository)
        awaitProfileLoaded(viewModel)

        viewModel.onNicknameChange("NewAwesomeNick")
        viewModel.onNativeLanguageSelected("it")
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)

        viewModel.saveChanges()
        awaitSavingFinished(viewModel)

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.successMessage)
        assertFalse(state.hasUnsavedChanges)
        assertEquals("NewAwesomeNick", state.nickname)
        assertEquals("it", state.nativeLanguageCode)

        // Verify changes persisted in AuthRepository
        val updatedUser = authRepository.getCurrentUserFlow().first()
        assertNotNull(updatedUser)
        assertEquals("NewAwesomeNick", updatedUser!!.nickname)
        assertEquals("it", updatedUser.nativeLanguageCode)
    }

    @Test
    fun testLogoutClearsSession() = runBlocking {
        authRepository.registerUser("UserLogout", "logout_${System.currentTimeMillis()}@example.com", "password123", "en")
        assertNotNull(sessionManager.currentUserId.first())

        val viewModel = ProfileViewModel(application, authRepository, vocabularyRepository)
        awaitProfileLoaded(viewModel)

        viewModel.logout()
        
        var retries = 0
        while (sessionManager.currentUserId.first() != null && retries < 100) {
            delay(50)
            shadowOf(Looper.getMainLooper()).idle()
            retries++
        }
        shadowOf(Looper.getMainLooper()).idle()

        assertNull(sessionManager.currentUserId.first())
    }
}
