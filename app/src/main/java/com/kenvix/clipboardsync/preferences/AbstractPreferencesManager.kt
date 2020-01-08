//--------------------------------------------------
// Class AbstractPreferencesManager
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.preferences

import android.content.Context
import com.kenvix.clipboardsync.ApplicationEnvironment
import kotlin.reflect.KProperty


abstract class AbstractPreferencesManager(val name: String, val mode: Int = Context.MODE_PRIVATE) {
    val preferences = ApplicationEnvironment.appContext.getSharedPreferences(name, mode)
    val editor = preferences.edit()

    fun commit() = editor.commit()

    @Suppress("UNCHECKED_CAST")
    protected inline fun <reified T> preferenceOf(key: String, defValue: T): DelegatedPreference<T> {
        return object : DelegatedPreference<T> {
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return when (T::class) {
                    Float::class -> preferences.getFloat(key, defValue as Float) as T
                    Int::class -> preferences.getInt(key, defValue as Int) as T
                    Long::class -> preferences.getLong(key, defValue as Long) as T
                    Boolean::class -> preferences.getBoolean(key, defValue as Boolean) as T
                    String::class -> preferences.getString(key, defValue as String) as T
                    Set::class -> preferences.getStringSet(key, defValue as Set<String>) as T
                    else -> throw IllegalArgumentException("Type not supported: ${T::class.java.name}")
                }
            }

            override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                when (T::class) {
                    Float::class -> editor.putFloat(key, value as Float) as T
                    Int::class -> editor.putInt(key, value as Int) as T
                    Long::class -> editor.putLong(key, value as Long) as T
                    Boolean::class -> editor.putBoolean(key, value as Boolean) as T
                    String::class -> editor.putString(key, value as String) as T
                    Set::class -> editor.putStringSet(key, value as Set<String>) as T
                    else -> throw IllegalArgumentException("Type not supported: ${T::class.java.name}")
                }
            }
        }
    }

    interface DelegatedPreference<T> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }
}