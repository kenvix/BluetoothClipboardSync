//--------------------------------------------------
// Class BluetoothService
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kenvix.clipboardsync.ApplicationEnvironment
import com.kenvix.clipboardsync.exception.EnvironmentNotSatisfiedException

object BluetoothUtils {
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter() ?: throw EnvironmentNotSatisfiedException("No bluetooth device found")
    }

    val bondedDevices get() = bluetoothAdapter.bondedDevices

    fun tryEnableBluetoothDevice() {
        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
        }
    }
}