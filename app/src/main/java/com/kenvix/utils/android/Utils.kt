package com.kenvix.utils.android

import android.content.Context
import android.util.Log
import com.kenvix.clipboardsync.R
import com.kenvix.clipboardsync.ui.base.BaseActivityUI

fun Context.exceptionIgnored(execute: () -> Unit) {
    try {
        execute()
    } catch (e: Exception) {
        Log.e("IgnoredException", e.toString())
        BaseActivityUI.getAlertBuilder(this, e.toString(), getString(R.string.error_operation_failed), null).show()
    }
}

fun printDebug(str: Any?) {
    Log.d("DebugPrint", str?.toString())
}