package io.github.xiaoancute.englisheasy.data.settings

import java.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProviderConnectionTesterTest {

    @Test
    fun buildsModelsUrlFromBaseUrl() {
        assertEquals(
            "https://api.openai.com/v1/models",
            buildModelsUrl("https://api.openai.com/v1/"),
        )
        assertEquals(
            "https://api.deepseek.com/models",
            buildModelsUrl("https://api.deepseek.com"),
        )
    }

    @Test
    fun classifiesAuthFailure() {
        val result = classifyProviderConnectionHttpError(401)

        assertTrue(result is ProviderConnectionResult.Failure)
        assertEquals("API Key 无效或缺失（HTTP 401）", result.message)
    }

    @Test
    fun classifiesMissingEndpointOrModel() {
        val result = classifyProviderConnectionHttpError(404)

        assertTrue(result is ProviderConnectionResult.Failure)
        assertEquals("Base URL 不对，或该服务不支持 /models（HTTP 404）", result.message)
    }

    @Test
    fun classifiesNetworkFailure() {
        val result = classifyProviderConnectionThrowable(IOException("boom"))

        assertTrue(result is ProviderConnectionResult.Failure)
        assertEquals("网络连接失败，请检查 Base URL 或网络环境", result.message)
    }

    @Test
    fun returnsReadableFailureForMalformedBaseUrl() = runTest {
        val tester = ProviderConnectionTester(
            client = OkHttpClient(),
            json = Json { ignoreUnknownKeys = true },
        )

        val result = tester.test(
            ProviderConfig(
                apiKey = "sk-test",
                baseUrl = "api.openai.com/v1",
                model = "gpt-5.4-mini",
            ),
        )

        assertTrue(result is ProviderConnectionResult.Failure)
        assertEquals("Base URL 格式不正确，请填写完整的 http:// 或 https:// 地址", result.message)
    }
}
