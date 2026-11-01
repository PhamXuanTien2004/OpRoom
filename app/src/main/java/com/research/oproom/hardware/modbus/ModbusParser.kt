@file:OptIn(ExperimentalUnsignedTypes::class)

package com.research.oproom.hardware.modbus

/**
 * Kết quả phân tích gói tin cảm biến nhiệt độ & độ ẩm LEFOO (Slave 1).
 */
data class TempHumData(
    val temperatureRaw: UInt,
    val humidityRaw: UInt,
    val tempHighByte: Byte,
    val tempLowByte: Byte,
    val humHighByte: Byte,
    val humLowByte: Byte
) {
    val temperatureCelsius: Float get() = temperatureRaw.toFloat() / 10f
    val humidityPercent: Float get() = humidityRaw.toFloat() / 10f
}

/**
 * Kết quả phân tích gói tin cảm biến áp suất phòng mổ & cảnh báo (Slave 2).
 */
data class PressureFilterData(
    val pressureRaw: UInt,
    val filterValue: UInt,
    val heaterAlarm: Boolean,
    val ventAlarm: Boolean
) {
    // Công thức tính áp suất Pa theo bo mạch thực tế (giá trị / 8192 * 100)
    val pressurePa: Int get() = (pressureRaw.toFloat() * 100 / 8192).toInt()
}

/**
 * Lớp chịu trách nhiệm giải mã các frame phản hồi Modbus RTU từ cổng nối tiếp.
 */
object ModbusParser {

    /**
     * Giải mã gói phản hồi nhiệt độ & độ ẩm từ cảm biến LEFOO (Slave 1, Function 0x03).
     * Cấu trúc: [0x01][0x03][ByteCount][TempHi][TempLo][HumHi][HumLo][CrcLo][CrcHi]
     */
    fun parseTempHum(buffer: List<Byte>): TempHumData? {
        if (buffer.size < 5) return null
        if (buffer[0] != 0x01.toByte() || buffer[1] != 0x03.toByte()) return null

        val dataByteCount = buffer[2].toInt()
        val expectedSize = dataByteCount + 5
        if (buffer.size < expectedSize) return null

        // Cảm biến LEFOO: byte 3-4 là Temp, byte 5-6 là Hum
        val tempRaw = buffer[3].toUByte().toUInt() * 256u + buffer[4].toUByte().toUInt()
        val humRaw = buffer[5].toUByte().toUInt() * 256u + buffer[6].toUByte().toUInt()

        return TempHumData(
            temperatureRaw = tempRaw,
            humidityRaw = humRaw,
            tempHighByte = buffer[3],
            tempLowByte = buffer[4],
            humHighByte = buffer[5],
            humLowByte = buffer[6]
        )
    }

    /**
     * Giải mã gói phản hồi áp suất, màng lọc và trạng thái cảnh báo (Slave 2, Function 0x03).
     * Cấu trúc: [0x02][0x03][ByteCount][PresHi][PresLo][FilterHi][FilterLo]...[HeaterAlarm][...][VentAlarm]...
     */
    fun parsePressureAndAlarms(buffer: List<Byte>): PressureFilterData? {
        if (buffer.size < 5) return null
        if (buffer[0] != 0x02.toByte() || buffer[1] != 0x03.toByte()) return null

        val dataByteCount = buffer[2].toInt()
        val expectedSize = dataByteCount + 5
        if (buffer.size < expectedSize) return null

        val presRaw = buffer[3].toUByte().toUInt() * 256u + buffer[4].toUByte().toUInt()
        val filterVal = buffer[5].toUByte().toUInt() * 256u + buffer[6].toUByte().toUInt()

        val heaterAlarm = if (buffer.size > 8) buffer[8] == 0x01.toByte() else false
        val ventAlarm = if (buffer.size > 10) buffer[10] == 0x01.toByte() else false

        return PressureFilterData(
            pressureRaw = presRaw,
            filterValue = filterVal,
            heaterAlarm = heaterAlarm,
            ventAlarm = ventAlarm
        )
    }

    /**
     * Kiểm tra tính hợp lệ của mã kiểm tra CRC16 trong gói tin nhận về.
     */
    fun verifyCrc(buffer: ByteArray): Boolean {
        if (buffer.size < 4) return false
        val dataLength = buffer.size - 2
        val (calcLow, calcHigh) = ModbusCrc16.calculate(buffer.copyOfRange(0, dataLength))
        return buffer[dataLength] == calcLow && buffer[dataLength + 1] == calcHigh
    }
}
