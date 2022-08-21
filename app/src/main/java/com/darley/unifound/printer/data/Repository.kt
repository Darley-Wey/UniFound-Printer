package com.darley.unifound.printer.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.darley.unifound.printer.APP.Companion.context
import com.darley.unifound.printer.data.dao.LoginInfoDao
import com.darley.unifound.printer.data.model.LoginInfo
import com.darley.unifound.printer.data.network.LoginData
import com.darley.unifound.printer.data.network.LoginResponse
import com.darley.unifound.printer.data.network.PrinterNetwork
import com.darley.unifound.printer.data.network.UploadResponse
import com.darley.unifound.printer.isOnline
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

object Repository {

    fun hasLoginInfo() = LoginInfoDao.hasLoginInfo()
    private fun saveLoginInfo(loginInfo: LoginInfo) = LoginInfoDao.saveLoginInfo(loginInfo)
    private fun getLoginInfo() = LoginInfoDao.getLoginInfo()


    private fun isOnline() = context.isOnline()


    fun login(username: String, password: String) = fire() {
        if (!isOnline()) {
            Result.success(LoginResponse(code = 1, message = "网络不可用，请连接WIFI", result = null))
        } else {
            // 调用挂起函数，当前协程会被阻塞，事件循环进入了被调用函数
            val authTokenResponse = PrinterNetwork.getAuthToken()
            val loginData = LoginData(
                username, password, authTokenResponse.szToken
            )
            val loginResponse = PrinterNetwork.login(loginData)
            if (loginResponse.code == 0) {
                // 成功登录，保存登录信息
                saveLoginInfo(
                    LoginInfo(username, password)
                )
            }
            Result.success(loginResponse)
        }
    }

    fun upload(
        file: MultipartBody.Part,
        dwPaperId: RequestBody,
        dwDuplex: RequestBody,
        dwColor: RequestBody,
        dwFrom: RequestBody,
        dwCopies: RequestBody,
        BackURL: RequestBody,
        dwTo: RequestBody,
    ) = fire() {
        if (!isOnline()) {
            Result.success(UploadResponse(code = 1, message = "网络不可用，请连接WIFI", result = null))
        } else if (!hasLoginInfo()) {
            Result.success(UploadResponse(code = 2, message = "没有登陆信息，请重新登陆", result = null))
        } else {
            val authTokenResponse = PrinterNetwork.getAuthToken()
            // 上传时先自动执行一遍登录刷新 cookies， 用户名和密码从存储的账号密码中获取
            val loginInfo = getLoginInfo()!!
            val loginData = LoginData(
                loginInfo.username,
                loginInfo.password,
                authTokenResponse.szToken
            )
            val loginResponse = PrinterNetwork.login(loginData)
            println("loginResponse: $loginResponse")
            val uploadResponse = PrinterNetwork.upload(
                file, dwPaperId, dwDuplex, dwColor, dwFrom, dwCopies, BackURL, dwTo
            )
            Log.d("retrofit", uploadResponse.toString())
            println("uploadResponse: $uploadResponse")
            Result.success(uploadResponse)
        }
    }

    // 定义一个统一的将网络请求响应封装为liveData并返回的方法，调用了挂起函数
    private fun <T> fire(block: suspend () -> Result<T>): LiveData<Result<T>> =
    // 网络请求不可发生在主线程，高IO操作切换到IO线程。
        // liveData方法可以指定在子线程中调用一个挂起函数，此处实现了使用非挂起函数调用挂起函数
        liveData(Dispatchers.IO) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }
}

/*
sealed class PlayState
object PlayLoading : PlayState()
data class PlaySuccess(val data: String) : PlayState()
data class PlayError(val error: Throwable) : PlayState()*/
