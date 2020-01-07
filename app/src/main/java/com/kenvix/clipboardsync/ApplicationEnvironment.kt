package com.kenvix.clipboardsync

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.NonNull
import com.kenvix.utils.log.Logging
import com.kenvix.utils.android.AndroidLoggingHandler
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ApplicationEnvironment : Application(), Logging {
    override fun getLogTag(): String = "ApplicationEnvironment"

    companion object Utils {
        @SuppressLint("StaticFieldLeak")
        @NonNull
        @JvmStatic
        lateinit var appContext: Context
            private set

        @SuppressLint("StaticFieldLeak")
        @NonNull
        @JvmStatic
        lateinit var rootContext: Context
            private set

        @SuppressLint("StaticFieldLeak")
        @NonNull
        @JvmStatic
        lateinit var instance: ApplicationEnvironment
            private set

        val okHttpClient: OkHttpClient by lazy {
            val okHttpClientBuilder = OkHttpClient.Builder().
                    connectTimeout(ApplicationProperties.OkHttpClientTimeout, TimeUnit.SECONDS).
                    followRedirects(true)

            val cacheDir = appContext.cacheDir.resolve("okhttp")

            if (cacheDir.exists())
                cacheDir.mkdirs()

            okHttpClientBuilder.cache(Cache(cacheDir, ApplicationProperties.OkHttpClientCacheSize))
            okHttpClientBuilder.build()
        }

        @JvmStatic
        fun getViewString(id: Int, vararg formatArgs: Any): String {
            return appContext.getString(id, *formatArgs)
        }

        @JvmStatic
        fun getViewString(id: Int): String {
            return appContext.getString(id)
        }

        @JvmStatic
        fun getViewColor(id: Int): Int {
            return appContext.getColor(id)
        }

        @JvmStatic
        fun getViewDrawable(id: Int): Drawable? {
            return appContext.getDrawable(id)
        }

        @JvmStatic
        fun getRawResourceUri(id: Int) = "android.resource://${appContext.packageName}/$id"

        @JvmStatic
        val viewResources
            get() = appContext.resources

        @JvmStatic
        lateinit var cachedThreadPool: ThreadPoolExecutor
            private set

        @JvmStatic
        lateinit var timer: Timer
            private set

        @JvmStatic
        fun getPackageName(name: String) = BuildConfig.APPLICATION_ID + "." + name
    }

    private fun checkAndConfigureRuntime() {

    }


    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        rootContext = baseContext
        instance = this

        AndroidLoggingHandler.applyToKenvixLogger()

        logger.finer("Application Initialized")

        cachedThreadPool = ThreadPoolExecutor(1, 20,
                60L, TimeUnit.SECONDS, SynchronousQueue<Runnable>())
        timer = Timer()
    }
}