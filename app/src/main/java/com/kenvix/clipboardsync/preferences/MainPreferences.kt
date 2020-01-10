package com.kenvix.clipboardsync.preferences

object MainPreferences : ManagedPreferences("main") {
    var enableSync: Boolean by preferenceOf("enable_sync", true)
    var enableClipboardSync: Boolean by preferenceOf("enable_clipboard_sync", false)
    var enableClipboardNotify: Boolean by preferenceOf("enable_clipboard_notify", false)

    var deviceData: String by preferenceOf("device_data", "")
    var deviceUUID: String by preferenceOf("device_uuid", "")
    var deviceName: String by preferenceOf("device_name", "")
    var deviceMacAddress: String by preferenceOf("device_mac_address", "")
}