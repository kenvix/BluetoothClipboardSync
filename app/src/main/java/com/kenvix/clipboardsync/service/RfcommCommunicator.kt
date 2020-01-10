package com.kenvix.clipboardsync.service

import com.kenvix.clipboardsync.ApplicationProperties
import com.kenvix.utils.android.GzipCompressUtils
import com.kenvix.utils.log.Logging
import com.kenvix.utils.log.severe
import java.io.DataInputStream
import java.io.DataOutputStream

class RfcommCommunicator(private val dataInputStream: DataInputStream, private val dataOutputStream: DataOutputStream) {
    fun writeData(type: Byte, options: Byte = 0, data: ByteArray? = null) {
        dataOutputStream.write(byteArrayOf(type, options))

        if (data != null) {
            dataOutputStream.writeInt(data.size)

            if (data.size > ApplicationProperties.MinGzipCompressSize)
                dataOutputStream.write(GzipCompressUtils.compress(data))
            else
                dataOutputStream.write(data)
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
        val length: Int = dataInputStream.readInt()
        var data: ByteArray? = null

        if (length > 0) {
            data = ByteArray(length)
            dataInputStream.readFully(data)

            if (length > ApplicationProperties.MinGzipCompressSize)
                data = GzipCompressUtils.decompress(data, length)
        }

        return RfcommFrame(
            type = type,
            option = option,
            length = length,
            data = data
        )
    }
}