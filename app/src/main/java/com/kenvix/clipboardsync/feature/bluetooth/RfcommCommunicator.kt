package com.kenvix.clipboardsync.feature.bluetooth

import com.kenvix.clipboardsync.preferences.MainPreferences
import com.kenvix.android.utils.GzipCompressUtils
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.experimental.and
import kotlin.experimental.or

class RfcommCommunicator(private val dataInputStream: DataInputStream, private val dataOutputStream: DataOutputStream) {
    fun writeData(type: Byte, options: Byte = 0, data: ByteArray? = null) {
        dataOutputStream.write(byteArrayOf(type, options))

        if (data != null) {
            dataOutputStream.writeInt(data.size)
            var option = options

            //Compress determine
            val outputData = if (data.size > MainPreferences.minGzipCompressSize) {
                    option = option or RfcommFrame.OptionCompressed
                    GzipCompressUtils.compress(data)
                } else {
                    data
                }

            dataOutputStream.writeByte(option as Int)
            dataOutputStream.write(outputData)
        } else {
            dataOutputStream.writeInt(0)
        }
    }

    fun writeData(frame: RfcommFrame) {
        writeData(frame.type, frame.option, frame.data)
    }

    fun readData(): RfcommFrame {
        val type: Byte = dataInputStream.readByte()
        val option: Byte = dataInputStream.readByte()
        val length: Short = dataInputStream.readShort()
        var data: ByteArray? = null

        if (length > 0) {
            data = ByteArray(length.toInt())
            dataInputStream.readFully(data)

            if (option and RfcommFrame.OptionCompressed > 0)
                data = GzipCompressUtils.decompress(data, length.toInt())
        }

        return RfcommFrame(
            type = type,
            option = option,
            length = length,
            data = data
        )
    }
}