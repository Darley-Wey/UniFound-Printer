package com.darley.unifound.printer.data.network

import com.darley.unifound.printer.data.network.model.CheckRes
import com.darley.unifound.printer.data.network.model.PublicKeyRes
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class AuthTokenResponse(
    val code: Int,
    val szToken: String,
)

data class LoginData(
    val szLogonName: String,
    val szPassword: String,
    val szToken: String,
)

data class LoginResponse(
    val code: Int,
    val message: String,
    val result: Result?,
) {
    data class Result(
        val szLogonName: String,
        val szTrueName: String,
    )
}


interface LoginService {
    @GET("/api/client/Auth/PublicKey")
    fun getPublicKey(): retrofit2.Call<PublicKeyRes>

    @GET("/api/client/Auth/GetAuthToken")
    fun getAuthToken(): retrofit2.Call<AuthTokenResponse>

    @POST("/api/client/Auth/Login")
    fun login(@Body loginData: LoginData): retrofit2.Call<LoginResponse>

    @POST("/api/client/Auth/Check")
    fun check(): retrofit2.Call<CheckRes>
}