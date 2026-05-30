package io.github.xiaoancute.englisheasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.InstallIn
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import io.github.xiaoancute.englisheasy.data.settings.ThemeConfig
import io.github.xiaoancute.englisheasy.ui.AppRoot
import io.github.xiaoancute.englisheasy.ui.theme.EnglishEasyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepo = EntryPointAccessors.fromApplication(
            applicationContext,
            MainActivityEntryPoint::class.java,
        ).settingsRepository()

        setContent {
            val themeConfig by settingsRepo.themeConfigFlow.collectAsState(
                initial = ThemeConfig.DEFAULT
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

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MainActivityEntryPoint {
    fun settingsRepository(): SettingsRepository
}
