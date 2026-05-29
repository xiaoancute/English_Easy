package io.github.xiaoancute.englisheasy.data.llm

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * OpenAI 兼容的 Retrofit 接口。
 *
 * 端点是动态的：用户在设置里填什么 baseUrl，就拼到 chat/completions。
 * @Url 让我们能针对每次调用传完整 URL，避免 Retrofit 实例化时锁死 baseUrl。
 */
interface OpenAiCompatibleApi {
    @POST
    suspend fun chat(
        @Url fullUrl: String,
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest,
    ): ChatResponse
}
