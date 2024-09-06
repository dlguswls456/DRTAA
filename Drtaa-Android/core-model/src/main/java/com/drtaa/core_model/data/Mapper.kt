package com.drtaa.core_model.data

import com.drtaa.core_model.network.RequestSocialLogin
import com.drtaa.core_model.network.ResponseLogin

fun ResponseLogin.toTokens(): Tokens {
    return Tokens(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken
    )
}

fun SocialUser.toRequestLogin(): RequestSocialLogin {
    return RequestSocialLogin(
        userLogin = this.userLogin,
        userProviderId = this.id,
        userNickname = this.nickname
    )
}