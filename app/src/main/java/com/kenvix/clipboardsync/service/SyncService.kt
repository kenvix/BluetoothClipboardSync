//--------------------------------------------------
// Class SyncService
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.service

import android.content.Intent
import android.os.IBinder

class SyncService : BaseService() {
    override fun onInitialize() {

    }

    override fun onBind(intent: Intent?): IBinder? {
return null
    }
}