package com.darley.unifound.printer.data

import android.util.Log
import androidx.lifecycle.liveData
import com.darley.unifound.printer.data.dao.LoginInfoDao
import com.darley.unifound.printer.data.dao.LoginInfoDao.rmLoginInfo
import com.darley.unifound.printer.data.dao.UserDao
import com.darley.unifound.printer.data.model.LoggedInUser
import com.darley.unifound.printer.data.model.LoginInfo
import com.darley.unifound.printer.data.network.LoginData
import com.darley.unifound.printer.data.network.PrinterNetwork
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import kotlin.Result
import kotlin.coroutines.CoroutineContext

object Repository {
    fun isUserSaved() = UserDao.isUserSaved()
    fun saveUser(user: LoggedInUser) = UserDao.saveUser(user)
    fun getSavedUser() = UserDao.getSavedUser()
    fun hasLoginInfo() = LoginInfoDao.hasLoginInfo()
    fun saveLoginInfo(loginInfo: LoginInfo) = LoginInfoDao.saveLoginInfo(loginInfo)
    fun getLoginInfo() = LoginInfoDao.getLoginInfo()

    fun logout() {
        LoginInfoDao.rmLoginInfo()
        UserDao.rmSavedUser()
    }

    // 网络请求不可发生在主线程，所以需要切换到IO线程。liveData提供了一个简单的方法来让你在IO线程中调用一个异步操作。
    // 在这里，我们使用liveData来获取结果，并且在主线程中更新UI。
    fun login(username: String, password: String) = fire(Dispatchers.IO) {
        val authTokenResponse = PrinterNetwork.getAuthToken()
        val loginData = LoginData(
            username, password, authTokenResponse.szToken
        )
        val loginResponse = PrinterNetwork.login(loginData)
        if (loginResponse.code == 0) {
            saveUser(
                LoggedInUser(
                    loginResponse.result.szLogonName,
                    loginResponse.result.szTrueName
                )
            )
            saveLoginInfo(
                LoginInfo(username, password)
            )
            Result.success(loginResponse)
        } else {
            Result.failure(RuntimeException("response code is ${loginResponse.code}"))
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
        dwTo: RequestBody
    ) = fire(Dispatchers.IO) {
        val authTokenResponse = PrinterNetwork.getAuthToken()
        val loginInfo = getLoginInfo()!!
        val loginData = LoginData(
//                "21218247",
//                "130952",
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
        if (uploadResponse.code == 0) {
            Result.success(uploadResponse)
        } else {
            Result.failure(RuntimeException("response code is ${uploadResponse.code}"))
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