package com.darley.unifound.printer.data

import android.util.Log
import androidx.lifecycle.liveData
import com.darley.unifound.printer.data.model.LoggedInUser
import com.darley.unifound.printer.data.network.LoginData
import com.darley.unifound.printer.data.network.PrinterNetwork
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import java.io.File
import kotlin.Result

object Repository {
    fun getSzToken() = liveData(Dispatchers.IO) {
        val result = try {
            val authTokenResponse = PrinterNetwork.getAuthToken()
            Log.d("retrofit", authTokenResponse.toString())
            println("authTokenResponse: $authTokenResponse")
            if (authTokenResponse.code == 0) {
                val szToken = authTokenResponse.szToken
                Result.success(szToken)
            } else {
                Result.failure(RuntimeException("response code is ${authTokenResponse.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }

    fun login(username: String, password: String) = liveData(Dispatchers.IO) {
        val result = try {
            val authTokenResponse = PrinterNetwork.getAuthToken()
            val loginData = LoginData(
                username,
                password,
                authTokenResponse.szToken
            )
            val loginResponse = PrinterNetwork.login(loginData)

            Log.d("retrofit", loginResponse.toString())
            println("loginResponse: $loginResponse")
            if (loginResponse.code == 0) {
                Result.success(loginResponse)
            } else {
                Result.failure(RuntimeException("response code is ${loginResponse.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }

    fun upload(
        file: MultipartBody.Part,
//        map: Map<String, RequestBody>
//        /*file: File,
        dwPaperId: RequestBody,
        dwDuplex: RequestBody,
        dwColor: RequestBody,
        dwFrom: RequestBody,
        dwCopies: RequestBody,
        BackURL: RequestBody,
        dwTo: RequestBody
    ) = liveData(Dispatchers.IO) {
        val result = try {
            val authTokenResponse = PrinterNetwork.getAuthToken()
            val loginData = LoginData(
                "21218247",
                "130952",
                authTokenResponse.szToken
            )
            val loginResponse = PrinterNetwork.login(loginData)
            println("loginResponse: $loginResponse")
            val uploadResponse = PrinterNetwork.upload(
                file,
//                map
//                /*file,
                dwPaperId,
                dwDuplex,
                dwColor,
                dwFrom,
                dwCopies,
                BackURL,
                dwTo
            )
            Log.d("retrofit", uploadResponse.toString())

            println("uploadResponse: $uploadResponse")
            if (uploadResponse.code == 0) {
                Result.success(uploadResponse)
            } else {
                Result.failure(RuntimeException("response code is ${uploadResponse.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }
}