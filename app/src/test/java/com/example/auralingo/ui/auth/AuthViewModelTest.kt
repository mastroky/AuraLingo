package com.example.auralingo.ui.auth

import android.app.Application
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.auralingo.data.auth.AuthDatabase
import com.example.auralingo.data.auth.AuthRepository
import com.example.auralingo.data.auth.SessionManager
import com.example.auralingo.data.local.UserDatabaseManager
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
 * Test di unità per AuthViewModel che verificano validazione, registrazione, login con email o nickname e cambi di stato.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthViewModelTest {

    private lateinit var application: Application
    private lateinit var authDatabase: AuthDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel
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

        viewModel = AuthViewModel(application, authRepository)

        runBlocking {
            sessionManager.clearSession()
        }
    }

    @After
    fun tearDown() {
        authDatabase.close()
        Dispatchers.resetMain()
        runBlocking {
            sessionManager.clearSession()
        }
    }

    private suspend fun awaitViewModelLoading() {
        var retries = 0
        while (viewModel.uiState.value.isLoading && retries < 100) {
            delay(20)
            shadowOf(Looper.getMainLooper()).idle()
            retries++
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun testRegistrationEmptyNicknameValidationError() {
        if (!viewModel.uiState.value.isRegisterMode) {
            viewModel.toggleAuthMode()
        }
        viewModel.onNicknameChange("   ")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.submitForm()

        val state = viewModel.uiState.value
        assertEquals("Nickname cannot be empty.", state.errorMessage)
    }

    @Test
    fun testRegistrationInvalidEmailValidationError() {
        if (!viewModel.uiState.value.isRegisterMode) {
            viewModel.toggleAuthMode()
        }
        viewModel.onNicknameChange("Alice")
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")

        viewModel.submitForm()

        val state = viewModel.uiState.value
        assertEquals("Please enter a valid email address.", state.errorMessage)
    }

    @Test
    fun testRegistrationShortPasswordValidationError() {
        if (!viewModel.uiState.value.isRegisterMode) {
            viewModel.toggleAuthMode()
        }
        viewModel.onNicknameChange("Alice")
        viewModel.onEmailChange("alice@example.com")
        viewModel.onPasswordChange("12345") // Meno di 6 caratteri

        viewModel.submitForm()

        val state = viewModel.uiState.value
        assertEquals("Password must be at least 6 characters long.", state.errorMessage)
    }

    @Test
    fun testDuplicateEmailRegistrationError() = runBlocking {
        val uniqueEmail = "user_${System.currentTimeMillis()}@example.com"

        authRepository.registerUser(
            nickname = "User1",
            email = uniqueEmail,
            password = "password123",
            nativeLanguageCode = "en",
        )

        if (!viewModel.uiState.value.isRegisterMode) {
            viewModel.toggleAuthMode()
        }
        viewModel.onNicknameChange("User2")
        viewModel.onEmailChange(uniqueEmail)
        viewModel.onPasswordChange("password123")

        viewModel.submitForm()
        awaitViewModelLoading()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("already exists"))
    }

    @Test
    fun testDuplicateNicknameRegistrationError() = runBlocking {
        val uniqueNickname = "UniqueNickname_${System.currentTimeMillis()}"

        // Registra il primo utente con il nickname univoco
        authRepository.registerUser(
            nickname = uniqueNickname,
            email = "email1_${System.currentTimeMillis()}@example.com",
            password = "password123",
            nativeLanguageCode = "en",
        )

        // Tenta di registrare un secondo utente con lo stesso nickname (case-insensitive)
        if (!viewModel.uiState.value.isRegisterMode) {
            viewModel.toggleAuthMode()
        }
        viewModel.onNicknameChange(uniqueNickname.lowercase())
        viewModel.onEmailChange("email2_${System.currentTimeMillis()}@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.submitForm()
        awaitViewModelLoading()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("already taken"))
    }

    @Test
    fun testLoginWithNicknameSuccess() = runBlocking {
        val uniqueNickname = "CoderBob_${System.currentTimeMillis()}"
        val userEmail = "coderbob_${System.currentTimeMillis()}@example.com"

        // Registra l'utente
        authRepository.registerUser(
            nickname = uniqueNickname,
            email = userEmail,
            password = "password123",
            nativeLanguageCode = "en",
        )

        // Resetta la sessione per disconnettere l'utente
        sessionManager.clearSession()
        assertNull(sessionManager.currentUserId.first())

        // Tenta il login usando il nickname invece dell'email
        if (viewModel.uiState.value.isRegisterMode) {
            viewModel.toggleAuthMode() // Passa alla modalità login
        }
        viewModel.onEmailChange(uniqueNickname.lowercase()) // Inserisce il nickname nel campo identifier
        viewModel.onPasswordChange("password123")

        var loggedIn = false
        viewModel.submitForm { loggedIn = true }
        awaitViewModelLoading()

        assertTrue(loggedIn)
        assertNull(viewModel.uiState.value.errorMessage)
        assertNotNull(sessionManager.currentUserId.first())
    }

    @Test
    fun testSuccessfulRegistrationAndLoginTransitions() = runBlocking {
        val email = "newuser_${System.currentTimeMillis()}@example.com"

        // Registrazione
        if (!viewModel.uiState.value.isRegisterMode) {
            viewModel.toggleAuthMode()
        }
        viewModel.onNicknameChange("NewUser")
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange("password123")
        viewModel.onNativeLanguageCodeChange("es")

        var registered = false
        viewModel.submitForm { registered = true }
        awaitViewModelLoading()

        assertTrue(registered)
        assertNull(viewModel.uiState.value.errorMessage)
        assertNotNull(sessionManager.currentUserId.first())

        // Disconnessione
        sessionManager.clearSession()
        assertNull(sessionManager.currentUserId.first())

        // Login tramite Email
        if (viewModel.uiState.value.isRegisterMode) {
            viewModel.toggleAuthMode()
        }
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange("password123")

        var loggedIn = false
        viewModel.submitForm { loggedIn = true }
        awaitViewModelLoading()

        assertTrue(loggedIn)
        assertNull(viewModel.uiState.value.errorMessage)
        assertNotNull(sessionManager.currentUserId.first())
    }
}
