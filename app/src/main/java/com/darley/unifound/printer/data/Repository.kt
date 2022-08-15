package com.darley.unifound.printer.data

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.liveData
import com.darley.unifound.printer.APP.Companion.context
import com.darley.unifound.printer.data.dao.LoginInfoDao
import com.darley.unifound.printer.data.model.LoginInfo
import com.darley.unifound.printer.data.network.LoginData
import com.darley.unifound.printer.data.network.LoginResponse
import com.darley.unifound.printer.data.network.PrinterNetwork
import com.darley.unifound.printer.data.network.UploadResponse
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import kotlin.coroutines.CoroutineContext

object Repository {

    fun hasLoginInfo() = LoginInfoDao.hasLoginInfo()
    private fun saveLoginInfo(loginInfo: LoginInfo) = LoginInfoDao.saveLoginInfo(loginInfo)
    private fun getLoginInfo() = LoginInfoDao.getLoginInfo()
    fun rmLoginInfo() = LoginInfoDao.rmLoginInfo()


    private fun isOnline(): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        var isWifiConn = false
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkInfo(network)?.apply {
                if (type == ConnectivityManager.TYPE_WIFI) {
                    isWifiConn = isWifiConn or isConnected
                }
            }
        }
        return networkInfo?.isConnected == true && isWifiConn
    }

    // 网络请求不可发生在主线程，所以需要切换到IO线程。liveData提供了一个简单的方法来让你在IO线程中调用一个异步操作。
    // 在这里，我们使用liveData来获取结果，并且在主线程中更新UI。
    fun login(username: String, password: String) = fire(Dispatchers.IO) {
        if (!isOnline()) {
            Result.success(LoginResponse(code = 1, message = "网络不可用，请连接WIFI", result = null))
        } else {
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
    ) = fire(Dispatchers.IO) {
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

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(context) {
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
