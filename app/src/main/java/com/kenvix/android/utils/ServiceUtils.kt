@file:JvmName("ServiceUtils")
package com.kenvix.android.utils

import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import com.kenvix.android.ApplicationEnvironment


/**
 * 启动服务
 */
fun Context.startService(serviceClass: Class<*>) {
    val intent = Intent(this, serviceClass)
    this.startService(intent)
}

/**
 * 在线程池启动服务
 */
@JvmOverloads
fun Context.startServiceInThreadPool(serviceClass: Class<*>, onException: (exception: Exception) -> Unit = { throw it }) {
    ApplicationEnvironment.cachedThreadPool.execute {
        try {
            startService(serviceClass)
        } catch (e: Exception) {
            onException(e)
        }
    }
}

/**
 * 停止服务
 */
fun Context.stopService(serviceClass: Class<*>): Boolean {
    val intent = Intent(this, serviceClass)
    return this.stopService(intent)
}

/**
 * 连接服务
 */
@JvmOverloads
fun Context.bindService(serviceClass: Class<*>, connection: ServiceConnection, flags: Int = BIND_AUTO_CREATE): Boolean {
    val intent = Intent(this, serviceClass)
    return bindService(intent, connection, flags)
}