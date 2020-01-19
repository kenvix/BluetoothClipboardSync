//--------------------------------------------------
// Class MessageIntentService
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.service

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kenvix.utils.log.Logging

class SendMessageBroadcastReceiver : BroadcastReceiver(), Logging {
    override fun onReceive(context: Context, intent: Intent) {

        val data = if (intent.getBooleanExtra("is_from_notification", false)) {
            val inputBundle = RemoteInput.getResultsFromIntent(intent)
            inputBundle.getString("message")
        } else {
            intent.getStringExtra("message")
        }

        if (data == null) {
            logger.severe("Invoked but no notification_send_message given")
            return
        }

        logger.finest("Invoked with message: $data")
    }

    override fun getLogTag(): String = "SendMessageBroadcastReceiver"
}