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

    // 泛型函数 第一个 <T> 声明一个泛型形参 T, 函数形参类型为 Class<T>, 返回值类型为 T
    // 调用泛型函数的方法为 函数名<泛型实参>(类型实参)，类型推导下可根据函数实参的类型推导泛型实参， <泛型实参> 可以省略
    // 如需指定泛型实参范围，泛型形参定义为 <T : 范围类型>，默认为 <T : Any?>
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    // inline 内联函数，内联函数的函数体会在编译期被嵌入到每一个被调用的地方，可以减少高阶函数产生的匿名类数以及函数执行的时间开销
    // 对泛型函数进行内联，泛型形参定义为 <reified T>, reified 原形 reify，意为使...具体化，
    // 此时可认为泛型形参 T 是已被实化的，可在函数体内使用 T::class.java 取得函数运行时的 T 的具体类型
    // 同名函数是函数重载，函数体内的 create(T::class.java) 会调用上一个泛型函数 create(serviceClass: Class<T>)
    // 调用时直接使用 ServiceCreator.create<ServiceClass>()，功能等价 ServiceCreator.create<ServiceClass>(ServiceClass::class.java)
    inline fun <reified T> create(): T = create(T::class.java)
}