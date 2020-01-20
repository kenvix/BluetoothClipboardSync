package com.kenvix.android.utils


import android.view.View
import com.kenvix.utils.android.PreprocessorName
import com.kenvix.utils.log.Logging
import com.kenvix.android.ApplicationEnvironment
import com.kenvix.clipboardsync.R
import java.lang.reflect.InvocationTargetException

object Invoker : Logging {
    override fun getLogTag(): String = "Invoker"

    @JvmStatic
    private val classLoader by lazy {
        Invoker::class.java.classLoader ?: Thread.currentThread().contextClassLoader
    }

    @JvmStatic
    val formChecker: Class<*> by lazy { classLoader.loadClass(ApplicationEnvironment.getPackageName("generated.FormChecker")) }

    @JvmStatic
    val viewToolset: Class<*> by lazy { classLoader.loadClass(ApplicationEnvironment.getPackageName("generated.ViewToolset")) }

    @JvmStatic
    fun invokeViewAutoLoader(targetRaw: Any): Boolean {
        return try {
            viewToolset.getMethod(PreprocessorName.getViewAutoLoaderMethodName(targetRaw.javaClass.canonicalName), Any::class.java)
                    .invoke(null, targetRaw)
            true
        } catch (ex: NoSuchMethodException) {
            logger.severe("Invoker can't detect loader method, may cause NullPointerException: " + ex.message)
            false
        } catch (ex: InvocationTargetException) {
            logger.severe("Target Loader throws a unexpected exception: " + ex.message)
            ex.printStackTrace()
            false
        } catch (ex: Exception) {
            logger.severe("No such view auto loader generated: " + targetRaw.javaClass.canonicalName + " : " + ex.message)
            ex.printStackTrace()
            throw ex
        }

    }

    @JvmStatic
    fun invokeViewAutoLoader(targetRaw: Any, targetView: View): Boolean {
        return try {
            viewToolset.getMethod(PreprocessorName.getViewAutoLoaderMethodName(targetRaw.javaClass.canonicalName), Any::class.java, View::class.java)
                    .invoke(null, targetRaw, targetView)
            true
        } catch (ex: NoSuchMethodException) {
            logger.severe("Invoker can't detect loader method, may cause NullPointerException: " + ex.message)
            false
        } catch (ex: InvocationTargetException) {
            logger.severe("No such view auto loader generated: " + targetRaw.javaClass.canonicalName + " : " + ex.message)
            ex.printStackTrace()
            false
        } catch (ex: Exception) {
            logger.severe("Target Loader throws a unexpected exception: " + ex.message)
            ex.printStackTrace()
            false
        }

    }

    @JvmStatic
    fun invokeFormChecker(targetRaw: Any): Boolean {
        return try {
            formChecker.getMethod(PreprocessorName.getFormEmptyCheckerMethodName(targetRaw.javaClass.canonicalName), String::class.java, Any::class.java)
                    .invoke(null,
                        getString(R.string.error_field_required), targetRaw)
            true
        } catch (ex: Exception) {
            logger.severe("No such form checker generated: " + ex.message)
            ex.printStackTrace()
            false
        }

    }

    private fun getString(id: Int): String {
        return ApplicationEnvironment.getViewString(id)
    }
}
