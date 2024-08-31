package com.drtaa.core_data.repository

import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    suspend fun getAccessToken(): String
    suspend fun setAccessToken(accessToken: String)
    suspend fun clearToken()
}