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
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    fun tryEnableBluetoothDevice() {
        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
        }
    }

    fun registerIntentFilter() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND) //发现设备
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //扫描完毕
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //扫描结束
        ApplicationEnvironment.appContext.registerReceiver(receiver, intentFilter)
    }
}