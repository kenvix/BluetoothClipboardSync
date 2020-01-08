package com.kenvix.clipboardsync.ui.main

import android.os.Bundle
import android.widget.LinearLayout
import com.kenvix.clipboardsync.R
import com.kenvix.clipboardsync.exception.EnvironmentNotSatisfiedException
import com.kenvix.clipboardsync.service.BluetoothUtils
import com.kenvix.clipboardsync.ui.base.BaseActivity
import com.kenvix.utils.android.annotation.ViewAutoLoad

class MainActivity : BaseActivity() {
    @ViewAutoLoad lateinit var mainContainer: LinearLayout
    private lateinit var settingFragment: SettingFragment

    override fun onInitialize(savedInstanceState: Bundle?) {
        try {
            settingFragment = SettingFragment()

            BluetoothUtils.tryEnableBluetoothDevice()
            BluetoothUtils.registerIntentFilter()
            val devies = BluetoothUtils.bondedDevices
            devies.forEach {
                logger.fine("Address ${it.address} | Name ${it.name} | Type ${it.type} | ${it.bondState} | UUID ${it.uuids}")
            }

            setForegroundFragment(baseContainer, settingFragment)
        } catch (e: EnvironmentNotSatisfiedException) {
            ui.showAlertDialog(getString(R.string.no_bluetooth_device)) {
                finish()
            }
        }
    }

    override fun getBaseLayout(): Int = R.layout.activity_main
    override fun getBaseContainer(): Int = R.id.main_container
}
