package com.darley.unifound.printer

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.darley.unifound.printer.APP.Companion.context

class APP : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        // 延迟初始化，声明一个不用赋初始的全局变量
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}

fun Context.isOnline(): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    var isWifiConn = false
    connMgr.allNetworks.forEach { network ->
        connMgr.getNetworkInfo(network)?.apply {
            if (type == ConnectivityManager.TYPE_WIFI) {
                isWifiConn = isWifiConn or isConnected
            }
        }
    }
    return networkInfo?.isConnected == true && isWifiConn
}