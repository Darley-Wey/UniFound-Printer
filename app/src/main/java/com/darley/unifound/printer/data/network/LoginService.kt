package com.darley.unifound.printer.data.network

import com.darley.unifound.printer.data.network.model.AuthTokenRes
import com.darley.unifound.printer.data.network.model.CheckRes
import com.darley.unifound.printer.data.network.model.LoginData
import com.darley.unifound.printer.data.network.model.LoginRes
import com.darley.unifound.printer.data.network.model.PublicKeyRes
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface LoginService {
    @GET("/api/client/Auth/PublicKey")
    fun getPublicKey(): retrofit2.Call<PublicKeyRes>

    @GET("/api/client/Auth/GetAuthToken")
    fun getAuthToken(): retrofit2.Call<AuthTokenRes>

    @POST("/api/client/Auth/Login")
    fun login(@Body loginData: LoginData): retrofit2.Call<LoginRes>

    @POST("/api/client/Auth/Check")
    fun check(): retrofit2.Call<CheckRes>
}