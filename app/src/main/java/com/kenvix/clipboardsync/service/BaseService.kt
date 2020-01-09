//--------------------------------------------------
// Class BaseService
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.service

import android.app.Service
import com.kenvix.utils.log.Logging


abstract class BaseService : Service(), Logging {
    private lateinit var logTag: String

    var initException: Exception? = null

    override fun getLogTag(): String = logTag

    final override fun onCreate() {
        super.onCreate()

        try {
            logTag = this.javaClass.simpleName
            onInitialize()

            logger.finest("Created service")
        } catch (e: Exception) {
            initException = e
            stopSelf()
        }
    }

    protected abstract fun onInitialize()
}