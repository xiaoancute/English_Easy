package io.github.xiaoancute.englisheasy.data.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ProviderConnectionResult {
    val message: String

    data class Success(override val message: String) : ProviderConnectionResult
    data class Failure(override val message: String) : ProviderConnectionResult
}

@Singleton
class ProviderConnectionTester @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) {
    suspend fun test(config: ProviderConfig): ProviderConnectionResult = withContext(Dispatchers.IO) {
        val baseUrl = config.baseUrl.trim()
        val model = config.model.trim()
        val apiKey = config.apiKey.trim()

        when {
            baseUrl.isBlank() -> return@withContext ProviderConnectionResult.Failure("请先填写 Base URL")
            model.isBlank() -> return@withContext ProviderConnectionResult.Failure("请先填写模型名")
            apiKey.isBlank() -> return@withContext ProviderConnectionResult.Failure("请先填写 API Key")
        }

        runCatching {
            val request = Request.Builder()
                .url(buildModelsUrl(baseUrl))
                .header("Authorization", "Bearer $apiKey")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@use classifyProviderConnectionHttpError(response.code)
                }

                val models = parseModelIds(response.body?.string().orEmpty())
                    ?: return@use ProviderConnectionResult.Failure("接口返回不是 OpenAI 兼容模型列表")

                if (model in models) {
                    ProviderConnectionResult.Success("连接成功，模型可用：$model")
                } else {
                    ProviderConnectionResult.Failure("连接成功，但模型列表里没有：$model")
                }
            }
        }.getOrElse(::classifyProviderConnectionThrowable)
    }

    private fun parseModelIds(rawJson: String): Set<String>? {
        return try {
            json.decodeFromString<ModelsResponse>(rawJson)
                .data
                .map { it.id.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

internal fun buildModelsUrl(baseUrl: String): String {
    return "${baseUrl.trim().trimEnd('/')}/models"
}

internal fun classifyProviderConnectionHttpError(code: Int): ProviderConnectionResult.Failure {
    val message = when (code) {
        401 -> "API Key 无效或缺失（HTTP 401）"
        403 -> "账号无权限，或模型服务拒绝访问（HTTP 403）"
        404 -> "Base URL 不对，或该服务不支持 /models（HTTP 404）"
        429 -> "请求过于频繁或额度不足（HTTP 429）"
        in 500..599 -> "服务商暂时异常（HTTP $code）"
        else -> "连接失败（HTTP $code）"
    }
    return ProviderConnectionResult.Failure(message)
}

internal fun classifyProviderConnectionThrowable(error: Throwable): ProviderConnectionResult.Failure {
    val message = when (error) {
        is IllegalArgumentException -> "Base URL 格式不正确，请填写完整的 http:// 或 https:// 地址"
        is IOException -> "网络连接失败，请检查 Base URL 或网络环境"
        else -> error.message ?: "测试连接失败"
    }
    return ProviderConnectionResult.Failure(message)
}

@Serializable
private data class ModelsResponse(
    val data: List<ModelItem> = emptyList(),
)

@Serializable
private data class ModelItem(
    val id: String = "",
)
