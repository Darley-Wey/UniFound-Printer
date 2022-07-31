package com.darley.unifound.printer.data

import androidx.lifecycle.liveData
import com.darley.unifound.printer.data.dao.UserDao
import com.darley.unifound.printer.data.model.LoggedInUser
import com.darley.unifound.printer.data.network.LoginData
import com.darley.unifound.printer.data.network.PrinterNetwork
import kotlinx.coroutines.Dispatchers
import kotlin.Result

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

// Result type for the data source this could be network or cache
// This is the data source we will use to get the data
// We will use the network data source to get the data
// We will use the cache data source to get the data
// 类括号里面是类的构造方法参数，仅可用于构造方法和类属性，不可用于成员方法和属性
class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    fun isUserSaved() = UserDao.isUserSaved()
    fun saveUser(user: LoggedInUser) = UserDao.saveUser(user)
    fun getSavedUser() = UserDao.getSavedUser()
//    fun isLoggedIn() = LoginDao.isLoggedIn()

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun loggedIn(username: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(username, password)
        if (result.isSuccess) {
            setLoggedInUser(result.getOrNull()!!)
        }
        return result
    }

    // 网络请求不可发生在主线程，所以需要切换到IO线程。liveData提供了一个简单的方法来让你在IO线程中调用一个异步操作。
    // 在这里，我们使用liveData来获取结果，并且在主线程中更新UI。
    fun login(username: String, password: String) = liveData(Dispatchers.IO) {
        val result = try {
            val authTokenResponse = PrinterNetwork.getAuthToken()
            val loginData = LoginData(username, password, authTokenResponse.szToken)
            val loginResponse = PrinterNetwork.login(loginData)
            if (loginResponse.code == 0) {
                setLoggedInUser(
                    LoggedInUser(
                        loginResponse.result.szLogonName,
                        loginResponse.result.szTrueName
                    )
                )
                Result.success(loginResponse)
            } else {
                Result.failure(RuntimeException("response code is ${loginResponse.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
//        return@liveData Result.success(loginResponse)
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser

        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}