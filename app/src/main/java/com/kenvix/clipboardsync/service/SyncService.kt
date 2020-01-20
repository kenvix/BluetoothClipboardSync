//--------------------------------------------------
// Class SyncService
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat.getSystemService
import com.kenvix.android.ApplicationProperties
import com.kenvix.clipboardsync.R
import com.kenvix.clipboardsync.broadcast.SendMessageBroadcast
import com.kenvix.clipboardsync.broadcast.SyncServiceStateBroadcast
import com.kenvix.clipboardsync.feature.bluetooth.RfcommCommunicator
import com.kenvix.clipboardsync.feature.bluetooth.RfcommFrame
import com.kenvix.clipboardsync.preferences.MainPreferences
import com.kenvix.clipboardsync.ui.main.MainActivity
import com.kenvix.android.utils.BaseService
import com.kenvix.android.utils.ServiceBinder
import com.kenvix.utils.log.finest
import com.kenvix.utils.log.severe
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class SyncService : BaseService() {
    private val binder = Binder()

    private lateinit var syncThreadExecutor: ExecutorService
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothServerSocket: BluetoothServerSocket

    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var dataOutputStream: DataOutputStream? = null
    private var dataInputStream: DataInputStream? = null
    private var communicator: RfcommCommunicator? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var serviceNotificationBuilder: NotificationCompat.Builder
    private lateinit var serviceInbox: NotificationCompat.InboxStyle
    private var emergencyNotificationCounter = 0
    private var continuousFailCounter = 0

    var onStatusChangedListener: ((status: ServiceStatus, bluetoothDevice: BluetoothDevice) -> Unit)? =
        null

    @Volatile
    private var keepConnection: Boolean = true
    @Volatile
    private var status: ServiceStatus = ServiceStatus.Stopped
        set(value) {
            if (bluetoothDevice != null)
                onStatusChangedListener?.invoke(value, bluetoothDevice!!)

            SyncServiceStateBroadcast.sendBroadcast(this, value)

            if (value != ServiceStatus.Stopped) {
                serviceNotificationBuilder.setContentTitle(this.getString(R.string.service_sync) + ": " + when (value) {
                    SyncService.ServiceStatus.Stopped -> getString(R.string.service_stopped)
                    SyncService.ServiceStatus.Starting -> getString(R.string.service_starting)
                    SyncService.ServiceStatus.StartedButNoDeviceConnected -> getString(R.string.service_no_device)
                    SyncService.ServiceStatus.DeviceConnected -> getString(R.string.service_connected)
                    SyncService.ServiceStatus.TemporaryError -> getString(R.string.service_temp_error)
                })
                notificationManager.notify(ServiceNotificationID, serviceNotificationBuilder.build())
            }

            field = value
        }

    private val uuid = UUID.fromString(ApplicationProperties.BluetoothSyncUUID)!!

    override fun onInitialize() {
        notificationManager = getSystemService(this, NotificationManager::class.java)!!
        Utils.instance = this

        val sendIntent = Intent(this, SendMessageBroadcast::class.java)
        sendIntent.putExtra("is_from_notification", true)
        val sendPendingIntent = PendingIntent.getBroadcast(
            this,
            0xA2,
            sendIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val stopServiceIntent = Intent(this, SyncService::class.java)
        stopServiceIntent.action = ActionStopService

        val actionSendMessage = NotificationCompat.Action.Builder(
            android.R.drawable.sym_def_app_icon,
            getString(R.string.send_message_to_pc),
            sendPendingIntent
        )
            .addRemoteInput(
                RemoteInput.Builder("message")
                    .setLabel(getString(R.string.send_message_to_pc)).build()
            )
            .build()

        val actionStartActivity = NotificationCompat.Action.Builder(
            android.R.drawable.sym_def_app_icon,
            getString(R.string.open_application),
            PendingIntent.getActivity(this, 0xA3, Intent(this, MainActivity::class.java), PendingIntent.FLAG_ONE_SHOT)
        )
            .build()

        val actionStop = NotificationCompat.Action.Builder(
            android.R.drawable.sym_def_app_icon,
            getString(R.string.stop),
            PendingIntent.getService(this, 0xA5, stopServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        )
            .build()

        serviceInbox = NotificationCompat.InboxStyle()
        val notificationBuilder = createServiceNotification()
            .setStyle(serviceInbox)
            .addAction(actionSendMessage)
            .addAction(actionStartActivity)
            .addAction(actionStop)

        serviceNotificationBuilder = notificationBuilder
    }

    enum class ServiceStatus { Stopped, Starting, StartedButNoDeviceConnected, DeviceConnected, TemporaryError }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null && intent.action == ActionStopService) {
            stop()
            stopForeground(true)
            return super.onStartCommand(intent, flags, startId)
        }

        if (status == ServiceStatus.Stopped) {
            startForeground(ServiceNotificationID, serviceNotificationBuilder.build())

            if (intent == null)
                throw IllegalArgumentException("Intent cannot be null")

            status = ServiceStatus.Starting
            syncThreadExecutor = Executors.newFixedThreadPool(2)

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            logger.finest("Starting, I'm ${bluetoothAdapter.name}")

            keepConnection = true

            syncThreadExecutor.submit {
                try {
                    bluetoothServerSocket =
                        bluetoothAdapter.listenUsingRfcommWithServiceRecord("clipboardSync", uuid)

                    while (keepConnection) {
                        try {
                            connectDevice()
                            val data = communicator?.readData()

                            if (data != null) {
                                when (data.type) {
                                    RfcommFrame.TypePing -> {
                                        if (data.length > 0)
                                            sendData(
                                                RfcommFrame.TypePong,
                                                0,
                                                data.data
                                            )
                                        else
                                            sendData(RfcommFrame.TypePong)
                                    }

                                    RfcommFrame.TypeUpdateClipboard -> {
                                        if (data.data != null)
                                            onReceiveClipboard(String(data.data))

                                        sendData(
                                            RfcommFrame.TypeUpdateClipboard,
                                            RfcommFrame.OptionMessageSuccess
                                        )
                                    }

                                    RfcommFrame.TypeEmergency -> {
                                        if (data.data != null) {
                                            emergencyNotificationCounter =
                                                (emergencyNotificationCounter + 1) % MainPreferences.maxEmergencyNotificationNum

                                            notificationManager.notify(
                                                EmergencyNotificationBaseID + emergencyNotificationCounter,
                                                createEmergencyNotification(String(data.data)).build()
                                            )
                                        }

                                        sendData(
                                            RfcommFrame.TypeEmergency,
                                            RfcommFrame.OptionMessageSuccess
                                        )
                                    }

                                    else -> logger.warning("Unknown bluetooth data type ${data.type}")
                                }
                            }
                        } catch (e: IOException) {
                            continuousFailCounter++

                            if (continuousFailCounter < 5) {
                                logger.warning("Detected IOException when receiving message continuous fail $continuousFailCounter times, will try again: ${e.message}")
                            } else {
                                status = ServiceStatus.TemporaryError
                                logger.warning("IOException too many times, force disconnect.")
                                continuousFailCounter = 0
                                disconnectDevice()
                            }
                        } catch (e: Exception) {
                            logger.severe(e, "Connect device failed")
                        }
                    }
                } catch (e: java.lang.Exception) {
                    logger.severe(e)
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        syncThreadExecutor.shutdown()
    }

    private fun stop() {
        keepConnection = false
        status = ServiceStatus.Stopped
        Utils.instance = null

        try {
            if (bluetoothSocket != null && bluetoothSocket?.isConnected == true)
                disconnectDevice()
        } catch (ignored: Exception) {
            logger.finest(ignored)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


    private fun connectDevice() {
        if (bluetoothSocket == null || bluetoothSocket?.isConnected == false) {
            logger.info("Server started and free. Accepting more devices.")
            status = ServiceStatus.StartedButNoDeviceConnected


            bluetoothSocket = bluetoothServerSocket.accept()
            bluetoothDevice = bluetoothSocket!!.remoteDevice
            dataInputStream = DataInputStream(bluetoothSocket!!.inputStream)
            dataOutputStream = DataOutputStream(bluetoothSocket!!.outputStream)
            communicator =
                RfcommCommunicator(
                    dataInputStream!!,
                    dataOutputStream!!
                )

            status = ServiceStatus.DeviceConnected
            logger.info("PC Client Device online: ${bluetoothDevice!!.name} (${bluetoothDevice!!.address})")

        }
    }

    private fun disconnectDevice() {
        status = ServiceStatus.StartedButNoDeviceConnected

        if (status != ServiceStatus.TemporaryError) {
            try {
                dataInputStream?.close()
                dataOutputStream?.close()
                bluetoothSocket?.close()
            } catch (ignored: Exception) { }
        }

        bluetoothSocket = null
        bluetoothDevice = null
        dataInputStream = null
        dataOutputStream = null
        communicator = null
    }

    private fun sendData(type: Byte, options: Byte = 0, data: ByteArray? = null) {
        syncThreadExecutor.submit {
            try {
                communicator!!.writeData(type, options, data)
            } catch (e: Throwable) {
                logger.severe(e)
            }
        }
    }

    private fun sendData(frame: RfcommFrame) {
        syncThreadExecutor.submit {
            try {
                communicator!!.writeData(frame)
            } catch (e: Throwable) {
                logger.severe(e)
            }
        }
    }

    private fun onReceiveClipboard(data: String) {
        serviceInbox.addLine(data)
        notificationManager.notify(ServiceNotificationID, serviceNotificationBuilder.build())
    }

    companion object Utils {
        const val ActionStopService = "StopService"

        const val ServiceNotificationID = 0xA1
        const val EmergencyNotificationBaseID = 0x30

        var instance: SyncService? = null
        val isRunning
            get() = instance != null && instance?.status != ServiceStatus.Stopped

        fun startService(context: ContextWrapper) {
            val intent = Intent(context, SyncService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }


    private fun createNotificationChannel() { // 在API>=26的时候创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //设定的通知渠道名称
            val channelName: String = this.getString(R.string.service_sync)
            val importance = NotificationManager.IMPORTANCE_MIN

            //构建通知渠道
            val channel = NotificationChannel(
                ApplicationProperties.BluetoothSyncChannelID,
                channelName,
                importance
            )
            channel.description = channelName
            channel.enableVibration(false)

            val channelE = NotificationChannel(
                ApplicationProperties.BluetoothSyncChannelIDEmergency,
                getString(R.string.service_sync_emergency), NotificationManager.IMPORTANCE_HIGH
            )
            channelE.description = getString(R.string.service_sync_emergency_desc)
            channelE.enableVibration(true)
            channelE.enableLights(true)

            //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(channelE)
        }
    }

    private fun createServiceNotification(): NotificationCompat.Builder {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(this, this.getString(R.string.service_sync))
        builder
            .setContentTitle(this.getString(R.string.service_sync)) //设置通知标题
            .setContentText(getString(R.string.service_notification_default)) //设置通知内容
            .setAutoCancel(true) //用户触摸时，自动关闭
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_server_3)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setChannelId(ApplicationProperties.BluetoothSyncChannelID)
            .setOngoing(true) //设置处于运行状态

        return builder
    }

    private fun updateServiceNotification() =
        notificationManager.notify(ServiceNotificationID, serviceNotificationBuilder.build())

    private fun createEmergencyNotification(text: String): NotificationCompat.Builder {
        val builder =
            NotificationCompat.Builder(this, this.getString(R.string.service_sync_emergency))

        builder
            .setContentTitle(this.getString(R.string.service_sync_emergency)) //设置通知标题
            .setContentText(text) //设置通知内容
            .setAutoCancel(true) //用户触摸时，自动关闭
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(android.R.drawable.stat_notify_voicemail)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setChannelId(ApplicationProperties.BluetoothSyncChannelIDEmergency)

        return builder
    }

    inner class Binder : ServiceBinder<SyncService>() {
        override val service: SyncService = this@SyncService
        val status: ServiceStatus
            get() = this@SyncService.status

        var onStatusChangedListener: ((status: ServiceStatus, bluetoothDevice: BluetoothDevice) -> Unit)?
            get() = this@SyncService.onStatusChangedListener
            set(value) {
                this@SyncService.onStatusChangedListener = value
            }

        fun sendDataAsync(type: Byte, options: Byte = 0, data: ByteArray? = null) =
            service.sendData(type, options, data)

        fun sendDataAsync(frame: RfcommFrame) = service.sendData(frame)
    }
}