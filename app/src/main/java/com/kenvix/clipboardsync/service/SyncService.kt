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
import com.kenvix.utils.android.BaseService
import com.kenvix.utils.android.ServiceBinder
import com.kenvix.utils.log.severe
import com.kenvix.utils.log.warning
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class SyncService : BaseService() {
    private val binder = Binder()
    private var continuousFails = 0

    private lateinit var syncThreadExecutor: ExecutorService
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var dataOutputStream: DataOutputStream
    private lateinit var dataInputStream: DataInputStream
    private lateinit var communicator: RfcommCommunicator

    @Volatile
    public var keepConnection: Boolean = true

    val uuid = UUID.fromString(ApplicationProperties.BluetoothSyncUUID)!!

    override fun onInitialize() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            throw IllegalArgumentException("Intent cannot be null")

        syncThreadExecutor = Executors.newFixedThreadPool(2)

        bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>("device")
            ?: throw IllegalArgumentException("Device cannot be null")
        keepConnection = true

        syncThreadExecutor.submit {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)

            while (keepConnection) {
                try {
                    connectDevice()
                    val data = communicator.readData()

                    when (data.type) {
                        ApplicationProperties.BluetoothSyncPing -> {
                            sendData(ApplicationProperties.BluetoothSyncPong)
                        }

                        ApplicationProperties.BluetoothSyncUpdateClipboard -> {
                            sendData(ApplicationProperties.BluetoothSyncClipboardSuccess)
                        }

                        else -> logger.warning("Unknown bluetooth data type ${data.type}")
                    }

                    continuousFails = 0
                } catch (e: IOException) {
                    logger.warning(e)
                    continuousFails++

                    if (continuousFails > 2)
                        onMaxFailsReached()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onMaxFailsReached() {
        keepConnection = false
    }

    override fun onDestroy() {
        super.onDestroy()

        keepConnection = false

        try {
            if (bluetoothSocket.isConnected)
                bluetoothSocket.close()
        } catch (ignored: Exception) {}

        syncThreadExecutor.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private fun connectDevice() {
        if (!bluetoothSocket.isConnected) {
            bluetoothSocket.connect()

            dataInputStream = DataInputStream(bluetoothSocket.inputStream)
            dataOutputStream = DataOutputStream(bluetoothSocket.outputStream)
            communicator = RfcommCommunicator(dataInputStream, dataOutputStream)
        }
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

    private fun sendData(frame: RfcommFrame) {
        syncThreadExecutor.submit {
            try {
                communicator.writeData(frame)
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

    inner class Binder : ServiceBinder<SyncService>() {
        override val service: SyncService = this@SyncService

        fun sendDataAsync(type: Byte, options: Byte = 0, data: ByteArray? = null) = service.sendData(type, options, data)
        fun sendDataAsync(frame: RfcommFrame) = service.sendData(frame)
    }
}