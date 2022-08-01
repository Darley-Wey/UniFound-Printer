package com.darley.unifound.printer.data.network

//import com.darley.unifound.printer.data.model.AuthTokenResponse
//import com.darley.unifound.printer.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginData(
    val szLogonName: String,
    val szPassword: String,
    val szToken: String
)

data class AuthTokenResponse(
    val szAuthType: String,
    val szInfo: Any,
    val szToken: String,
    val szStaSessionId: Any,
    val code: Int,
    val message: String,
    val result: Any
)

data class LoginResponse(
    val code: Int,
    val result: Result
) {
    data class Result(
        val szLogonName: String,
        val szTrueName: String
    )
}

interface LoginService {
    @GET("/api/client/Auth/GetAuthToken")
    fun getAuthToken(): retrofit2.Call<AuthTokenResponse>

    @POST("/api/client/Auth/Login")
    fun login(@Body loginData: LoginData): retrofit2.Call<LoginResponse>
}