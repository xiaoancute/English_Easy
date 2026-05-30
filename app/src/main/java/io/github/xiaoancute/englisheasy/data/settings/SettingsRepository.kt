package io.github.xiaoancute.englisheasy.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.xiaoancute.englisheasy.data.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "secrets")

private object Keys {
    val API_KEY_LEGACY = stringPreferencesKey("api_key")      // 旧明文，仅用于迁移读取
    val API_KEY_ENC = stringPreferencesKey("api_key_enc")     // 加密后的 key
    val BASE_URL = stringPreferencesKey("base_url")
    val MODEL = stringPreferencesKey("model")
    val THEME_COLOR = intPreferencesKey("theme_color")        // 主题色（ARGB int）
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")  // 是否跟随系统
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crypto: CryptoManager,
) {
    val configFlow: Flow<ProviderConfig> = context.dataStore.data.map { prefs ->
        val apiKey = prefs[Keys.API_KEY_ENC]?.takeIf { it.isNotEmpty() }
            ?.let { crypto.decrypt(it) }
            ?: prefs[Keys.API_KEY_LEGACY]               // 兼容老版本明文
            ?: ProviderConfig.DEFAULT.apiKey
        ProviderConfig(
            apiKey = apiKey,
            baseUrl = prefs[Keys.BASE_URL] ?: ProviderConfig.DEFAULT.baseUrl,
            model = prefs[Keys.MODEL] ?: ProviderConfig.DEFAULT.model,
        )
    }

    val themeConfigFlow: Flow<ThemeConfig> = context.dataStore.data.map { prefs ->
        ThemeConfig(
            themeColor = prefs[Keys.THEME_COLOR] ?: ThemeConfig.DEFAULT.themeColor,
            useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: ThemeConfig.DEFAULT.useDynamicColor,
        )
    }

    suspend fun load(): ProviderConfig = configFlow.first()

    suspend fun loadTheme(): ThemeConfig = themeConfigFlow.first()

    suspend fun save(cfg: ProviderConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_KEY_ENC] = crypto.encrypt(cfg.apiKey)
            prefs.remove(Keys.API_KEY_LEGACY)            // 清除可能残留的明文
            prefs[Keys.BASE_URL] = cfg.baseUrl
            prefs[Keys.MODEL] = cfg.model
        }
    }

    suspend fun saveTheme(theme: ThemeConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_COLOR] = theme.themeColor
            prefs[Keys.USE_DYNAMIC_COLOR] = theme.useDynamicColor
        }
    }
}
