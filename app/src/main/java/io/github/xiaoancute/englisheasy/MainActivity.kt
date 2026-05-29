package io.github.xiaoancute.englisheasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.xiaoancute.englisheasy.ui.AppRoot
import io.github.xiaoancute.englisheasy.ui.theme.EnglishEasyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnglishEasyTheme {
                AppRoot()
            }
        }
    }
}
