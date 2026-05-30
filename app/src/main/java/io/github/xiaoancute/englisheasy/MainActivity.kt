package io.github.xiaoancute.englisheasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import io.github.xiaoancute.englisheasy.ui.AppRoot
import io.github.xiaoancute.englisheasy.ui.theme.EnglishEasyTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeConfig by settingsRepo.themeConfigFlow.collectAsState(
                initial = io.github.xiaoancute.englisheasy.data.settings.ThemeConfig.DEFAULT
            )
            EnglishEasyTheme(
                useDynamicColor = themeConfig.useDynamicColor,
                seedColor = Color(themeConfig.themeColor),
            ) {
                AppRoot()
            }
        }
    }
}
