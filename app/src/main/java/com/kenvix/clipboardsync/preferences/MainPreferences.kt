package com.kenvix.clipboardsync.preferences

import com.kenvix.android.preferences.ManagedPreferences

object MainPreferences : ManagedPreferences("main") {
    var enableSync: Boolean by preferenceOf("enable_sync", true)
    var enableClipboardSync: Boolean by preferenceOf("enable_clipboard_sync", false)
    var enableClipboardNotify: Boolean by preferenceOf("enable_clipboard_notify", false)

    var deviceData: String by preferenceOf("device_data", "")
    var deviceUUID: String by preferenceOf("device_uuid", "")
    var deviceName: String by preferenceOf("device_name", "")
    var deviceMacAddress: String by preferenceOf("device_mac_address", "")

    var maxEmergencyNotificationNum: Int by preferenceOf("maxEmergencyNotificationNum", 20)
    var minGzipCompressSize: Int by preferenceOf("MinGzipCompressSize", 256)
}