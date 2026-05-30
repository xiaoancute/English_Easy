package io.github.xiaoancute.englisheasy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.settings.ProviderConfig
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import io.github.xiaoancute.englisheasy.data.settings.ThemeConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    val config: StateFlow<ProviderConfig> = settings.configFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProviderConfig.DEFAULT,
        )

    val themeConfig: StateFlow<ThemeConfig> = settings.themeConfigFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeConfig.DEFAULT,
        )

    fun save(cfg: ProviderConfig) {
        viewModelScope.launch {
            settings.save(cfg)
        }
    }

    fun saveTheme(theme: ThemeConfig) {
        viewModelScope.launch {
            settings.saveTheme(theme)
        }
    }
}
