//--------------------------------------------------
// Class RfcommFrame
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.service

//                       Structure of sync frame
// -----------------------------------------------------------------
// | Type | Options | Length | (Optional) Data (Optional GZIP Compressed) |
// 0      1         2        6                                           6+Len
// -----------------------------------------------------------------
data class RfcommFrame(
    val type: Byte,
    val option: Byte,
    val length: Int,
    val data: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RfcommFrame

        if (type != other.type) return false
        if (option != other.option) return false
        if (length != other.length) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 + type xor option.toInt() xor length xor (data?.hashCode() ?: 0)
    }
}