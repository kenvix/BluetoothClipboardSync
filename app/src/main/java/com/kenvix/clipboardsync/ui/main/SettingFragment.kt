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


class SettingFragment internal constructor(private val activity: MainActivity): PreferenceFragmentCompat() {
    private lateinit var devicesListPreference: ListPreference
    private lateinit var bluetoothDevices: Array<BluetoothDevice>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager.sharedPreferencesName = MainPreferences.name
        preferenceManager.sharedPreferencesMode = MainPreferences.mode
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)

        devicesListPreference = findPreference("device_address")!!
        devicesListPreference.setOnPreferenceClickListener {
            bluetoothDevices = BluetoothUtils.bondedDevices.filter(::deviceFilter).toTypedArray()

            devicesListPreference.entries = bluetoothDevices.map<BluetoothDevice, CharSequence> {
                "${it.name} (${it.address})"
            }.toTypedArray()
            devicesListPreference.entryValues = bluetoothDevices.indices.map { it.toString() }.toTypedArray()

            true
        }
        devicesListPreference.setOnPreferenceChangeListener { _, newValue ->


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
    }

    private fun openURL(url: String): Boolean {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (ex: Exception) {
            Log.w("OpenURL", "Exception occurred: $ex")
        }

        return true
    }

    private fun deviceFilter(device: BluetoothDevice): Boolean
        = device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER
}