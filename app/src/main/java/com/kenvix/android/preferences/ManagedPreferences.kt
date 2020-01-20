//--------------------------------------------------
// Class AbstractPreferencesManager
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import com.kenvix.android.ApplicationEnvironment
import com.kenvix.clipboardsync.preferences.MainPreferences
import kotlin.reflect.KProperty


open class ManagedPreferences(val name: String, val mode: Int = Context.MODE_PRIVATE) {
    val preferences = ApplicationEnvironment.appContext.getSharedPreferences(name, mode)
    val editor = preferences.edit()

    fun commit() = editor.commit()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> preferenceOf(key: String, defValue: T): DelegatedPreference<T> {
        return object :
            DelegatedPreference<T> {
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return when (T::class) {
                    Float::class -> preferences.getFloat(key, defValue as Float) as T
                    Int::class -> preferences.getInt(key, defValue as Int) as T
                    Long::class -> preferences.getLong(key, defValue as Long) as T
                    Boolean::class -> preferences.getBoolean(key, defValue as Boolean) as T
                    String::class -> preferences.getString(key, defValue as String) as T
                    Set::class -> preferences.getStringSet(key, defValue as Set<String>) as T
                    else -> throw IllegalArgumentException("Type not supported: ${T::class.qualifiedName}")
                }
            }

            override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                when (T::class) {
                    Float::class -> editor.putFloat(key, value as Float)
                    Int::class -> editor.putInt(key, value as Int)
                    Long::class -> editor.putLong(key, value as Long)
                    Boolean::class -> editor.putBoolean(key, value as Boolean)
                    String::class -> editor.putString(key, value as String)
                    Set::class -> editor.putStringSet(key, value as Set<String>)
                    else -> throw IllegalArgumentException("Type not supported: ${T::class.qualifiedName}")
                }
            }
        }
    }

    fun applyToPreferenceManager(manager: PreferenceManager) {
        manager.sharedPreferencesName = MainPreferences.name
        manager.sharedPreferencesMode = MainPreferences.mode
    }

    interface DelegatedPreference<T> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }
}