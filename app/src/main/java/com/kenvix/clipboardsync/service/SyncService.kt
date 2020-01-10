//--------------------------------------------------
// Class SyncService
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.service

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContextWrapper
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.kenvix.clipboardsync.ApplicationEnvironment
import com.kenvix.clipboardsync.ApplicationProperties
import com.kenvix.utils.android.GzipCompressUtils
import com.kenvix.utils.android.startService
import com.kenvix.utils.log.severe
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors




class SyncService : BaseService() {
    private lateinit var syncThreadExecutor: ExecutorService
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var dataOutputStream: DataOutputStream
    private lateinit var dataInputStream: DataInputStream
    private lateinit var communicator: RfcommCommunicator

    val uuid = UUID.fromString(ApplicationProperties.BluetoothSyncUUID)!!

    override fun onInitialize() {
        syncThreadExecutor = ApplicationEnvironment.cachedThreadPool
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            throw IllegalArgumentException("Intent cannot be null")

        bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>("device")
            ?: throw IllegalArgumentException("Device cannot be null")

        syncThreadExecutor.submit {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
            connectDevice()

            while (true) {
                sendData(ApplicationProperties.BluetoothSyncPing,0, "fuck".toByteArray())
                val data = communicator.readData()

                when (data.type) {
                    ApplicationProperties.BluetoothSyncPing -> {
                        sendData(ApplicationProperties.BluetoothSyncPong)
                    }
                    ApplicationProperties.BluetoothSyncUpdateClipboard -> {
                        Log.w("11", String(data.data!!))
                        sendData(ApplicationProperties.BluetoothSyncClipboardSuccess)
                    }
                    else -> logger.warning("Unknown bluetooth data type ${data.type}")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun connectDevice() {
        if (!bluetoothSocket.isConnected)
            bluetoothSocket.connect()

        dataInputStream = DataInputStream(bluetoothSocket.inputStream)
        dataOutputStream = DataOutputStream(bluetoothSocket.outputStream)
        communicator = RfcommCommunicator(dataInputStream, dataOutputStream)
    }

    private fun sendData(type: Byte, options: Byte = 0, data: ByteArray? = null) {
        syncThreadExecutor.submit {
            try {
                communicator.writeData(type, options, data)
            } catch (e: Throwable) {
                logger.severe(e)
            }
        }
    }

    companion object Utils {
        @JvmStatic
        fun startService(context: ContextWrapper, device: BluetoothDevice) {
            val intent = Intent(context, SyncService::class.java)
            intent.putExtra("device", device)
            context.startService(intent)
        }
    }
}