package com.drtaa.core_network.di

import com.drtaa.core_model.auth.TokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class AccessTokenInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val requestBuilder = originRequest.newBuilder()
        val accessToken: String? = runBlocking {
            var token: String? = ""
            val response = tokenProvider.getAccessToken()
            // 재발급 로직 필요하면 구조 바꿔야 됨
            token = response.ifBlank {
                null
            }

            token
        }
        Timber.tag("access_token").d(" $accessToken")
        val request = requestBuilder.addHeader("Authorization", "Bearer $accessToken").build()
        return chain.proceed(request)
    }
}