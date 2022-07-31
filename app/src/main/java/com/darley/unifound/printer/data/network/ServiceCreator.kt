package com.darley.unifound.printer.data.network

import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UnifoundCookie : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host()]
        println("reqCookies: $cookies")
        return cookies ?: ArrayList()
//        if (cookies != null) {
//            return cookies.toList()
//        }
//        return emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.size == 2) {
            cookieStore[url.host()] = cookies
        }
//        cookieStore[url.host()].add(Set(cookies))
        println("resCookies: $cookies")
    }
}

//object 单例类
object ServiceCreator {
    private val client = OkHttpClient.Builder().cookieJar(UnifoundCookie()).build()

    private const val BASE_URL = "http://10.135.0.139:9130/"
//    private val request = Request.Builder().url(BASE_URL + "/api/client/Auth/GetAuthToken").build()
//    val response = client.newCall(request).execute()
//    val token = response.body()?.string() ?: ""


    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 泛型函数
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    // inline 内联函数，同名函数是函数重载
    inline fun <reified T> create(): T = create(T::class.java)
}