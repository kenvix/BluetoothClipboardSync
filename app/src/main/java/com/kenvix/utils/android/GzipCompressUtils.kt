package com.kenvix.utils.android

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object GzipCompressUtils {
    fun compress(bytes: ByteArray): ByteArray {
        val os = ByteArrayOutputStream(bytes.size)
        val gos = GZIPOutputStream(os)
        gos.write(bytes)
        gos.close()

        val compressed: ByteArray = os.toByteArray()
        os.close()

        return compressed
    }

    fun compress(string: String): ByteArray = compress(string.toByteArray())

    fun decompress(compressed: ByteArray, bufferSize: Int = 128): ByteArray {
        ByteArrayInputStream(compressed).use { input ->
            GZIPInputStream(input, bufferSize).use { gis ->
                ByteArrayOutputStream(bufferSize).use { result ->
                    val data = ByteArray(bufferSize)
                    var bytesRead: Int

                    while (gis.read(data).also { bytesRead = it } != -1) {
                        result.write(data, 0, bytesRead)
                    }

                    return result.toByteArray()
                }
            }
        }
    }

    fun decompressToString(compressed: ByteArray, bufferSize: Int = 128): String {
        val `is` = ByteArrayInputStream(compressed)
        val gis = GZIPInputStream(`is`, bufferSize)
        val string = StringBuilder()
        val data = ByteArray(bufferSize)
        var bytesRead: Int

        while (gis.read(data).also { bytesRead = it } != -1) {
            string.append(String(data, 0, bytesRead))
        }

        gis.close()
        `is`.close()

        return string.toString()
    }
}