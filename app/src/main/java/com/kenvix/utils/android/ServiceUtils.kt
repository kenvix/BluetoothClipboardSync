@file:JvmName("ServiceUtils")
package com.kenvix.utils.android

import android.content.Context.BIND_AUTO_CREATE
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import com.kenvix.clipboardsync.ApplicationEnvironment
import java.lang.Exception

/**
 * 启动服务
 */
fun ContextWrapper.startService(serviceClass: Class<*>) {
    val intent = Intent(this, serviceClass)
    this.startService(intent)
}

/**
 * 在线程池启动服务
 */
@JvmOverloads
fun ContextWrapper.startServiceInThreadPool(serviceClass: Class<*>, onException: (exception: Exception) -> Unit = { throw it }) {
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
fun ContextWrapper.stopService(serviceClass: Class<*>): Boolean {
    val intent = Intent(this, serviceClass)
    return this.stopService(intent)
}

/**
 * 连接服务
 */
@JvmOverloads
fun ContextWrapper.bindService(serviceClass: Class<*>, connection: ServiceConnection, flags: Int = BIND_AUTO_CREATE): Boolean {
    val intent = Intent(this, serviceClass)
    return bindService(intent, connection, flags)
}