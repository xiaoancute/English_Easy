package io.github.xiaoancute.englisheasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.xiaoancute.englisheasy.ui.HomeScreen
import io.github.xiaoancute.englisheasy.ui.theme.EnglishEasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnglishEasyTheme {
                HomeScreen()
            }
        }
    }
}
