package com.darley.unifound.printer.data.network

import android.util.Log
import com.darley.unifound.printer.data.dao.CookiesDao
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class UnifoundCookie : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host()]
//        println("reqCookies: $cookies")
        return cookies ?: ArrayList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // 返回超过一个cookies时，刷新cookie
        if (cookies.size > 1) {
            cookieStore[url.host()] = cookies
            val cookiesStr: MutableList<String> = ArrayList()
            for (cookie in cookies) {
                cookiesStr.add(cookie.toString())
            }
            CookiesDao.saveCookies(cookiesStr)
            Log.d("ServiceCreator", "cookies[0]: ${cookies[0]}")
            Log.d("ServiceCreator", "cookies[1]: ${cookies[1]}")
            Log.d("ServiceCreator", "cookiesStr: $cookiesStr")
        }
//        println("resCookies: $cookies")
    }
}

//object 单例类
object ServiceCreator {
    // 创建一个okHttpClient对象，用于管理cookie和timeout
    private val client =
        OkHttpClient.Builder().cookieJar(UnifoundCookie())
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS).build()

    private const val BASE_URL = "http://10.135.0.139:9130/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 泛型函数
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    // inline 内联函数，同名函数是函数重载
    inline fun <reified T> create(): T = create(T::class.java)
}