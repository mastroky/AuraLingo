package com.example.auralingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.auralingo.ui.main.MainScreen
import com.example.auralingo.ui.theme.AuraLingoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Abilita il supporto edge-to-edge
        enableEdgeToEdge()
        setContent {
            AuraLingoTool()
        }
    }
}

@Composable
fun AuraLingoTool() {
    AuraLingoTheme {
        MainScreen()
    }
}
