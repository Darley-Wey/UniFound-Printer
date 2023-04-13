package com.darley.unifound.printer.data.network

import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object PrinterNetwork {
    private val loginService = ServiceCreator.create<LoginService>()

    // private val loginService = ServiceCreator.create<LoginService>(LoginService::class.java)
    private val uploadService = ServiceCreator.create<UploadService>()
    suspend fun getPublicKey() = loginService.getPublicKey().await()

    // service 的方法返回 Call<Response> 对象，然后调用扩展的 await 方法
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
        dwTo: RequestBody,
    ) = uploadService.upload(
        file, dwPaperId, dwDuplex, dwColor, dwFrom, dwCopies, BackURL, dwTo
    ).await()

    suspend fun check() = loginService.check().await()

    //    suspend 挂起函数，实现协程，使之可以调用其他挂起函数。类.函数名()，对类定义一个新的扩展函数
    private suspend fun <T> Call<T>.await(): T {
//        suspendCoroutine，会使得当前协程立即挂起，然后执行内部匿名函数，直至其内的 continuation 返回数据给协程
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
