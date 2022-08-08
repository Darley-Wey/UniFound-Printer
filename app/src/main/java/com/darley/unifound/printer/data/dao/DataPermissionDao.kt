package com.darley.unifound.printer.data.dao

import android.content.Context
import androidx.core.content.edit
import com.darley.unifound.printer.APP
import com.darley.unifound.printer.data.model.LoginInfo
import com.google.gson.Gson

object DataPermissionDao {
    private fun sharedPreferences() =
        APP.context.getSharedPreferences("data-permission", Context.MODE_PRIVATE)

    fun saveDataPermission() {
        sharedPreferences().edit() {
            putString("data-permission", "")
        }
    }

    fun hasDataPermission() = sharedPreferences().contains("data-permission")
}