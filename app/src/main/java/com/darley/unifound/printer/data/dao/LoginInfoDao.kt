package com.darley.unifound.printer.data.dao

import android.content.Context
import androidx.core.content.edit
import com.darley.unifound.printer.APP
import com.darley.unifound.printer.data.model.LoginInfo
import com.google.gson.Gson

object LoginInfoDao {
    private fun sharedPreferences() =
        APP.context.getSharedPreferences("login-info", Context.MODE_PRIVATE)

    fun saveLoginInfo(user: LoginInfo) {
        sharedPreferences().edit() {
            putString("login-info", Gson().toJson(user))
        }
    }

    fun getLoginInfo(): LoginInfo? {
        val user = sharedPreferences().getString("login-info", null)
        return Gson().fromJson(user, LoginInfo::class.java)
    }

    fun rmLoginInfo() {
        sharedPreferences().edit() {
            remove("login-info")
        }
    }

    fun hasLoginInfo() = sharedPreferences().contains("login-info")
}
