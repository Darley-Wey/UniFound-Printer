package com.darley.unifound.printer.data.dao

import android.content.Context
import androidx.core.content.edit
import com.darley.unifound.printer.APP

object CookiesDao {
    private fun sharedPreferences() =
        APP.context.getSharedPreferences("cookies", Context.MODE_PRIVATE)

    fun saveCookies(cookies: List<String>) {
        sharedPreferences().edit() {
            putStringSet("cookies", cookies.toSet())
        }
    }

    fun getCookies(): List<String> {
        val cookies = sharedPreferences().getStringSet("cookies", setOf())!!
        return cookies.toList()
    }

    fun hasCookies() = sharedPreferences().contains("cookies")
}