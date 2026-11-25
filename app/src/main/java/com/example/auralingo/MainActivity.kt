package com.example.auralingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.auralingo.data.auth.SessionManager
import com.example.auralingo.ui.auth.AuthScreen
import com.example.auralingo.ui.main.MainScreen
import com.example.auralingo.ui.theme.AuraLingoTheme
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.ObsidianDark

private sealed interface RootAuthState {
    object InitialLoading : RootAuthState
    data class Authenticated(val userId: Long) : RootAuthState
    object Unauthenticated : RootAuthState
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            AuraLingoTool()
        }
    }
}

@Composable
fun AuraLingoTool() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }

    val rootAuthState by produceState<RootAuthState>(initialValue = RootAuthState.InitialLoading, sessionManager) {
        sessionManager.currentUserId.collect { userId ->
            value = if (userId != null) {
                RootAuthState.Authenticated(userId)
            } else {
                RootAuthState.Unauthenticated
            }
        }
    }

    AuraLingoTheme {
        when (rootAuthState) {
            is RootAuthState.InitialLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianDark),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = NeonViolet)
                }
            }
            is RootAuthState.Unauthenticated -> {
                AuthScreen()
            }
            is RootAuthState.Authenticated -> {
                MainScreen()
            }
        }
    }
}
