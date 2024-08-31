package com.drtaa.core_data.datasource

interface TokenDataSource {
    suspend fun clearToken()
    suspend fun getAccessToken(): String
    suspend fun setAccessToken(accessToken: String)
}