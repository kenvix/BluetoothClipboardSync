//--------------------------------------------------
// Interface BroadcastEvent
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.broadcast

import android.content.Context
import android.content.Intent

@FunctionalInterface
interface BroadcastReceiveEvent {
    fun onReceiveBroadcast(context: Context, intent: Intent)
}