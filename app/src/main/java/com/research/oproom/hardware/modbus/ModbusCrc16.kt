package com.research.oproom.hardware.modbus

/**
 * Thuật toán tính mã kiểm tra CRC16 cho giao thức Modbus RTU.
 * Đa thức chuẩn: 0xA001, Giá trị khởi tạo: 0xFFFF.
 * Byte thấp (Low byte) đi trước, byte cao (High byte) đi sau.
 * Sử dụng toán toán tử Int tiêu chuẩn, tương thích 100% với mọi phiên bản Android Studio/Kotlin.
 */
class ModbusCrc16 {
    var high: Byte = 0xFF.toByte()
        private set
    var low: Byte = 0xFF.toByte()
        private set
    var crc: Int = 0xFFFF
        private set

    /**
     * Cập nhật và tính toán mã CRC cho mảng byte dữ liệu đầu vào.
     */
    fun update(pack: ByteArray) {
        var crcVal = 0xFFFF
        for (b in pack) {
            crcVal = crcVal xor (b.toInt() and 0xFF)
            for (i in 0 until 8) {
                crcVal = if ((crcVal and 0x0001) != 0) {
                    (crcVal ushr 1) xor 0xA001
                } else {
                    crcVal ushr 1
                }
            }
        }
        this.crc = crcVal
        this.low = (crcVal and 0xFF).toByte()
        this.high = ((crcVal ushr 8) and 0xFF).toByte()
    }

    companion object {
        /**
         * Phương thức tiện ích tính nhanh CRC16: trả về Pair(LowByte, HighByte).
         */
        @JvmStatic
        fun calculate(pack: ByteArray): Pair<Byte, Byte> {
            val calculator = ModbusCrc16()
            calculator.update(pack)
            return Pair(calculator.low, calculator.high)
        }

        /**
         * Nối thêm 2 byte CRC vào cuối mảng dữ liệu Modbus.
         */
        @JvmStatic
        fun appendCrc(dataWithoutCrc: ByteArray): ByteArray {
            val (low, high) = calculate(dataWithoutCrc)
            return dataWithoutCrc + byteArrayOf(low, high)
        }
    }
}
