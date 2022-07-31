package com.darley.unifound.printer.data.dao

import android.content.Context
import androidx.core.content.edit
import com.darley.unifound.printer.APP
import com.darley.unifound.printer.data.model.LoggedInUser
import com.google.gson.Gson

object UserDao {
    private fun sharedPreferences() =
        APP.context.getSharedPreferences("user", Context.MODE_PRIVATE)

    fun saveUser(user: LoggedInUser) {
        sharedPreferences().edit() {
            putString("user", Gson().toJson(user))
        }
    }

    fun getSavedUser(): LoggedInUser? {
        val user = sharedPreferences().getString("user", null)
        return Gson().fromJson(user, LoggedInUser::class.java)
    }

    fun isUserSaved() = sharedPreferences().contains("user")
}