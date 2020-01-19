package com.kenvix.clipboardsync.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kenvix.clipboardsync.service.SyncService

abstract class SyncServiceStateBroadcast : BroadcastReceiver(), BroadcastReceiveEvent {
    final override fun onReceive(context: Context?, intent: Intent?) {
        onReceiveBroadcast(context!!, intent!!)
    }

    companion object Utils {
        const val KeyNewStatus = "new_status"
        @JvmStatic
        val BroadcastActionName: String = SyncServiceStateBroadcast::class.qualifiedName!!

        @JvmStatic
        fun sendBroadcast(context: Context, newStatus: SyncService.ServiceStatus) {
            val intent = Intent()
            intent.action = BroadcastActionName
            intent.putExtra(KeyNewStatus, newStatus)

            context.sendBroadcast(intent)
        }
    }
}