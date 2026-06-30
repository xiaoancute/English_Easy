package io.github.xiaoancute.englisheasy.data.pronunciation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.content
import kotlinx.serialization.json.isString
import kotlinx.serialization.json.jsonArray
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.TimeUnit

object PronunciationAudio {
    private const val DictionaryUrl = "https://api.dictionaryapi.dev/api/v2/entries/en"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun findUrl(word: String): String? = withContext(Dispatchers.IO) {
        val query = word.trim().lowercase(Locale.US)
        if (query.isBlank()) return@withContext null

        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name()).replace("+", "%20")
        val request = Request.Builder()
            .url("$DictionaryUrl/$encoded")
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    firstDictionaryAudioUrl(response.body?.string().orEmpty())
                } else {
                    null
                }
            }
        }.getOrNull()
    }
}

internal fun firstDictionaryAudioUrl(json: String): String? {
    val entries = runCatching { Json.parseToJsonElement(json).jsonArray }.getOrNull() ?: return null
    return entries
        .asSequence()
        .filterIsInstance<JsonObject>()
        .flatMap { entry ->
            (entry["phonetics"] as? JsonArray)?.asSequence() ?: emptySequence()
        }
        .filterIsInstance<JsonObject>()
        .mapNotNull { phonetic ->
            (phonetic["audio"] as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.content
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }
        .map(::normalizeAudioUrl)
        .firstOrNull()
}

private fun normalizeAudioUrl(url: String): String = when {
    url.startsWith("//") -> "https:$url"
    else -> url
}
