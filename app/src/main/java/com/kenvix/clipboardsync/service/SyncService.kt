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
import com.kenvix.clipboardsync.ApplicationEnvironment
import com.kenvix.clipboardsync.ApplicationProperties
import com.kenvix.utils.android.startService
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//                       Structure of sync frame
// -----------------------------------------------------------------
// | Type | Length |    (Optional) Data (GZIP Compressed)          |
// 0      1        5                                             5+Len
// -----------------------------------------------------------------

class SyncService : BaseService() {
    private lateinit var syncThreadExecutor: ExecutorService
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var dataOutputStream: DataOutputStream
    private lateinit var dataInputStream: DataInputStream

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
                sendData(ApplicationProperties.BluetoothSyncPing, byteArrayOf('f'.toByte(),'u'.toByte(),'c'.toByte(),'k'.toByte()))

                val type: Byte = dataInputStream.readByte()
                val len = dataInputStream.readInt()

                when (type) {
                    ApplicationProperties.BluetoothSyncPing -> {
                        sendData(ApplicationProperties.BluetoothSyncPong)
                    }
                    ApplicationProperties.BluetoothSyncUpdateClipboard -> {
                        val data = ByteArray(len)
                        dataInputStream.readFully(data)

                        sendData(ApplicationProperties.BluetoothSyncClipboardSuccess)
                    }
                    else -> logger.warning("Unknown bluetooth data type $type")
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
    }

    private fun sendData(type: Byte, data: ByteArray? = null) {
        syncThreadExecutor.submit {
            dataOutputStream.writeByte(type.toInt())

            if (data != null) {
                dataOutputStream.writeInt(data.size)
                dataOutputStream.write(data)
            } else {
                dataOutputStream.writeInt(0)
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