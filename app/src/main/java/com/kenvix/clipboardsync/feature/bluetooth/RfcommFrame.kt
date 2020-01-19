//--------------------------------------------------
// Class RfcommFrame
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.feature.bluetooth

//                       Structure of sync frame
// -----------------------------------------------------------------
// | Type | Options | Length | (Optional) Data (Optional GZIP Compressed) |
// 0      1         2        4                                           4+Len
// -----------------------------------------------------------------
//                          Options
// | Reserved | Test | Message-Error | Message-Success | Reserved | Reserved | Partial | Compressed |
// -----------------------------------------------------------------
data class RfcommFrame(
    val type: Byte,
    val option: Byte,
    val length: Short,
    val data: ByteArray? = null
) {
    companion object {
        const val OptionNone: Byte           = 0b0000000
        const val OptionCompressed: Byte     = 0b0000001
        const val OptionPartial: Byte        = 0b0000010
        const val OptionTest: Byte           = 0b1000000
        const val OptionMessageError: Byte   = 0b0100000
        const val OptionMessageSuccess: Byte = 0b0010000

        const val TypeUpdateClipboard: Byte  = 0x11
        const val TypeEmergency: Byte        = 0x18
        const val TypePing: Byte             = 0x01
        const val TypePong: Byte             = 0x02
        const val TypeHello: Byte            = 0x03
    }

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
        return 31 + type xor option.toInt() xor length.toInt() xor (data?.hashCode() ?: 0)
    }
}