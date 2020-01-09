//--------------------------------------------------
// Class SettingFragment
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.ui.main

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kenvix.clipboardsync.R
import java.util.*
import android.content.ActivityNotFoundException
import android.provider.Settings
import androidx.preference.ListPreference
import com.kenvix.clipboardsync.ApplicationEnvironment
import com.kenvix.clipboardsync.preferences.MainPreferences
import com.kenvix.clipboardsync.service.BluetoothUtils
import com.kenvix.utils.android.exceptionIgnored
import com.kenvix.utils.android.printDebug
import com.kenvix.utils.log.Logging
import java.lang.IllegalArgumentException


class SettingFragment internal constructor(private val activity: MainActivity): PreferenceFragmentCompat(), Logging {
    private lateinit var devicesListPreference: ListPreference
    private lateinit var bluetoothDevices: Array<BluetoothDevice>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainPreferences.applyToPreferenceManager(preferenceManager)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)

        devicesListPreference = findPreference("device_address")!!
        if (MainPreferences.deviceMacAddress.isNotBlank()) {
            devicesListPreference.summary = "${MainPreferences.deviceName} (${MainPreferences.deviceMacAddress})"
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
        findPreference<Preference>("device_status")?.setOnPreferenceClickListener {
            false
        }
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