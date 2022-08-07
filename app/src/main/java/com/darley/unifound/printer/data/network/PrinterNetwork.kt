package com.darley.unifound.printer.data.network

import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object PrinterNetwork {
    //    private val authTokenService = ServiceCreator.create(AuthTokenService::class.java)
    private val loginService = ServiceCreator.create<LoginService>()
    private val uploadService = ServiceCreator.create<UploadService>()

    suspend fun getAuthToken() = loginService.getAuthToken().await()
    suspend fun login(data: LoginData) = loginService.login(data).await()
    suspend fun upload(
        file: MultipartBody.Part,
        dwPaperId: RequestBody,
        dwDuplex: RequestBody,
        dwColor: RequestBody,
        dwFrom: RequestBody,
        dwCopies: RequestBody,
        BackURL: RequestBody,
        dwTo: RequestBody
    ) = uploadService.upload(
        file, dwPaperId, dwDuplex, dwColor, dwFrom, dwCopies, BackURL, dwTo
    ).await()


    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : retrofit2.Callback<T> {
                override fun onResponse(call: Call<T>, response: retrofit2.Response<T>) {
                    val body = response.body()
//                    val code = response.code()
//                    TODO if code == 413,文件过大
                    println("onResponse: body = $body")
                    Log.d("PrinterNetwork", "onResponse: body = $body")
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        IllegalArgumentException("response body is null")
                    )
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    Log.d("PrinterNetwork", "onFailure: ${t.message}")
                    println("onFailure: ${t.message}")
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}
