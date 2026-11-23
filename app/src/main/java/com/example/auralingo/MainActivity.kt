package com.example.auralingo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import com.example.auralingo.data.local.AppDatabase
import com.example.auralingo.data.local.VocabularyWord
import com.example.auralingo.data.repository.VocabularyRepository
import com.example.auralingo.ui.main.MainScreen
import com.example.auralingo.ui.theme.AuraLingoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Verify Room database and repository integration
        verifyRoomDatabase()

        setContent {
            AuraLingoTool()
        }
    }

    /**
     * Test snippet to verify Room database and repository integration on startup.
     */
    private fun verifyRoomDatabase() {
        // 1. Initialize the database instance using AppDatabase.getDatabase(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)

        // 2. Create a VocabularyRepository passing the DAO
        val repository = VocabularyRepository(database.vocabularyDao())

        // 3. Use lifecycleScope.launch and Dispatchers.IO to insert a sample test word into the database
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val testWord = VocabularyWord(
                    originalWord = "Hola",
                    translatedWord = "Ciao",
                    contextSentence = "Testing room integration",
                    categoryTag = "Test • noun",
                    languageCode = "es",
                    partOfSpeech = "noun",
                )
                repository.insertWord(testWord)

                // 4. Add clear logging via Log.d to confirm successful insertion
                Log.d("AuraLingoDB", "Successfully inserted test word: ${testWord.originalWord} -> ${testWord.translatedWord}")
            } catch (e: Exception) {
                Log.e("AuraLingoDB", "Failed to insert test word", e)
            }
        }
    }
}

@Composable
fun AuraLingoTool() {
    AuraLingoTheme {
        MainScreen()
    }
}
