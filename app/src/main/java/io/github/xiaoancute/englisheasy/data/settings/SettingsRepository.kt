package io.github.xiaoancute.englisheasy.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "secrets")

private object Keys {
    val API_KEY = stringPreferencesKey("api_key")
    val BASE_URL = stringPreferencesKey("base_url")
    val MODEL = stringPreferencesKey("model")
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val configFlow: Flow<ProviderConfig> = context.dataStore.data.map { prefs ->
        ProviderConfig(
            apiKey = prefs[Keys.API_KEY] ?: ProviderConfig.DEFAULT.apiKey,
            baseUrl = prefs[Keys.BASE_URL] ?: ProviderConfig.DEFAULT.baseUrl,
            model = prefs[Keys.MODEL] ?: ProviderConfig.DEFAULT.model,
        )
    }

    suspend fun load(): ProviderConfig = configFlow.first()

    suspend fun save(cfg: ProviderConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_KEY] = cfg.apiKey
            prefs[Keys.BASE_URL] = cfg.baseUrl
            prefs[Keys.MODEL] = cfg.model
        }
    }
}
