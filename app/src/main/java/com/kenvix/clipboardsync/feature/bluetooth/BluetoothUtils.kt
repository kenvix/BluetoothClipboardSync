//--------------------------------------------------
// Class BluetoothService
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.feature.bluetooth

import android.bluetooth.BluetoothAdapter
import com.kenvix.android.exception.EnvironmentNotSatisfiedException

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