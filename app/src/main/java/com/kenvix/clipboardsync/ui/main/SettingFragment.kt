//--------------------------------------------------
// Class SettingFragment
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.ui.main

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.gson.Gson
import com.kenvix.clipboardsync.R
import com.kenvix.clipboardsync.broadcast.SyncServiceStateBroadcast
import com.kenvix.clipboardsync.preferences.MainPreferences
import com.kenvix.clipboardsync.feature.bluetooth.BluetoothUtils
import com.kenvix.clipboardsync.service.SyncService
import com.kenvix.clipboardsync.ui.other.WebViewActivity
import com.kenvix.utils.android.exceptionIgnored
import com.kenvix.utils.log.Logging
import java.util.*


class SettingFragment internal constructor(private val activity: MainActivity): PreferenceFragmentCompat(), Logging {
    private lateinit var devicesListPreference: ListPreference
    private lateinit var bluetoothDevices: Array<BluetoothDevice>
    private var selectedDevice: BluetoothDevice? = null
    private lateinit var serviceStatusChangeReceiver: SyncServiceStateBroadcast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainPreferences.applyToPreferenceManager(preferenceManager)
    }

    private lateinit var deviceStatusPreference: Preference
    val statusUpdateHandler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            updateStatus(msg!!.obj as SyncService.ServiceStatus)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)

        deviceStatusPreference = findPreference<Preference>("device_status")!!
        devicesListPreference = findPreference("device_address")!!
        if (MainPreferences.deviceMacAddress.isNotBlank()) {
            try {
                selectedDevice = Gson().fromJson(MainPreferences.deviceData, BluetoothDevice::class.java)
                devicesListPreference.summary = "${MainPreferences.deviceName} (${MainPreferences.deviceMacAddress})"
            } catch (e: Exception) {
                resetDevicePreference()
            }
        }

        devicesListPreference.setOnPreferenceClickListener {
            bluetoothDevices = BluetoothUtils.bondedDevices.filter(::deviceFilter).toTypedArray()

            devicesListPreference.entries = Array<CharSequence?>(bluetoothDevices.size) { null }
            devicesListPreference.entryValues = Array<CharSequence?>(bluetoothDevices.size) { null }

            bluetoothDevices.forEachIndexed { index, data ->
                devicesListPreference.entries[index] = "${data.name} (${data.address})"
                devicesListPreference.entryValues[index] = index.toString()

                if (data.address == MainPreferences.deviceMacAddress)
                    setDeviceIndex(index, data.name, data.address)
            }

            true
        }
        devicesListPreference.setOnPreferenceChangeListener { _, newValue ->
            context?.exceptionIgnored {
                val index = Integer.parseInt(newValue as String)
                val selection = bluetoothDevices[index]

                MainPreferences.deviceMacAddress = selection.address
                MainPreferences.deviceName = selection.name
                MainPreferences.deviceUUID = selection.uuids[0].uuid.toString()
                MainPreferences.deviceData = Gson().toJson(selection)
                selectedDevice = selection

                MainPreferences.commit()
                setDeviceIndex(index, selection.name, selection.address)
                logger.fine("Using device: Address ${selection.address} | Name ${selection.name} | Type ${selection.type} | ${selection.bondState} | UUID ${MainPreferences.deviceUUID}")
            }

            false
        }

        findPreference<Preference>("system_bluetooth_settings")?.setOnPreferenceClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_BLUETOOTH_SETTINGS
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                startActivity(intent)
            } catch (e: Exception) {
                activity.ui.showAlertDialog("操作失败：$e")
            }

            true
        }

        findPreference<Preference>("app_name")?.setOnPreferenceClickListener {
            openURL(
                "market://details?id=" + Objects.requireNonNull(
                    context
                )?.packageName
            )
        }
        findPreference<Preference>("view_github")?.setOnPreferenceClickListener { openURL("https://github.com/kenvix/BluetoothClipboardSync") }
        findPreference<Preference>("author")?.setOnPreferenceClickListener { openURL("https://kenvix.com") }
        deviceStatusPreference.setOnPreferenceClickListener { pref ->
            if (!SyncService.isRunning) {
                SyncService.startService(activity)
            }

            false
        }

        serviceStatusChangeReceiver = object : SyncServiceStateBroadcast() {
            override fun onReceiveBroadcast(context: Context, intent: Intent) {
                updateStatus(intent.getSerializableExtra(KeyNewStatus) as SyncService.ServiceStatus)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= 29
            && activity.checkSelfPermission("android.permission.READ_LOGS") != PackageManager.PERMISSION_GRANTED) {
            val pref = findPreference<Preference>("enable_clipboard_sync")
            pref?.summary = getString(R.string.clipboard_sync_error)
            pref?.isPersistent = false
            pref?.setOnPreferenceClickListener {
                WebViewActivity.startActivity(activity, 0xA9, "file:///android_asset/grant_permission.html", "Grant Permission")
                false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(SyncServiceStateBroadcast.BroadcastActionName)
        activity.registerReceiver(serviceStatusChangeReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        activity.unregisterReceiver(serviceStatusChangeReceiver)
    }

    private fun updateStatus(serviceStatus: SyncService.ServiceStatus) {
        if (deviceStatusPreference != null) {
            deviceStatusPreference.summary =  when (serviceStatus) {
                SyncService.ServiceStatus.Stopped -> "服务已停止"
                SyncService.ServiceStatus.Starting -> "正在启动"
                SyncService.ServiceStatus.StartedButNoDeviceConnected -> "已启动 (无设备连接)"
                SyncService.ServiceStatus.DeviceConnected -> "已启动，设备已连接"
                SyncService.ServiceStatus.TemporaryError -> "暂时出错，请稍候"
            }
        }
    }

    private fun resetDevicePreference() {
        selectedDevice = null
        MainPreferences.deviceData = ""
        MainPreferences.deviceName = ""
        MainPreferences.deviceMacAddress = ""
        MainPreferences.deviceUUID = ""
        MainPreferences.commit()
    }

    private fun openURL(url: String): Boolean {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (ex: Exception) {
            Log.w("OpenURL", "Exception occurred: $ex")
        }

        return true
    }

    private fun setDeviceIndex(index: Int, name: String, address: String) {
        devicesListPreference.setValueIndex(index)
        devicesListPreference.summary = "$name ($address)"
    }

    private fun deviceFilter(device: BluetoothDevice): Boolean
        = device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER

    override fun getLogTag(): String = "SettingsActivity"
}