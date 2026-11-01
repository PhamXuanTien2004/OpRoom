package com.research.oproom.hardware.modbus


/**
 * Danh mục mảng byte các bản tin lệnh Modbus RTU dùng trong hệ thống phòng mổ.
 * Bao gồm các lệnh đọc cảm biến, bật/tắt thiết bị và đồng bộ thông số cài đặt.
 */
@OptIn(ExperimentalUnsignedTypes::class)
object ModbusCommands {

    // === LỆNH ĐỌC CẢM BIẾN (FUNCTION 0x03) ===
    // Đọc cảm biến nhiệt độ & độ ẩm LEFOO (Slave 1)
    val HUM_TEMP_REQUEST = ubyteArrayOf(0x01u, 0x03u, 0x00u, 0x01u, 0x00u, 0x02u, 0x34u, 0x0bu).toByteArray()

    // Đọc cảm biến áp suất phòng mổ & màng lọc (Slave 2)
    val PRES_REQUEST = ubyteArrayOf(0x02u, 0x03u, 0x10u, 0x12u, 0x00u, 0x04u, 0x60u, 0xfdu).toByteArray()

    // Đọc trạng thái màng lọc (Slave 2)
    val FILTER_REQUEST = ubyteArrayOf(0x02u, 0x03u, 0x10u, 0x13u, 0x00u, 0x01u, 0x20u, 0xfcu).toByteArray()

    // === ĐIỀU KHIỂN ĐÈN PHÒNG MỔ (SLAVE 2, FUNCTION 0x06) ===
    val LIGHT_1_ON  = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x11u, 0x00u, 0x01u, 0x9cu, 0xfbu).toByteArray()
    val LIGHT_1_OFF = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x11u, 0x00u, 0x00u, 0x5du, 0x3bu).toByteArray()

    val LIGHT_2_ON  = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0au, 0x00u, 0x01u, 0x6cu, 0xfbu).toByteArray()
    val LIGHT_2_OFF = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0au, 0x00u, 0x00u, 0xadu, 0x3bu).toByteArray()

    // === ĐÈN KHỬ KHUẨN UV ===
    val UV_ON  = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0bu, 0x00u, 0x01u, 0x3du, 0x3bu).toByteArray()
    val UV_OFF = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0bu, 0x00u, 0x00u, 0xfcu, 0xfbu).toByteArray()

    // === CHẾ ĐỘ HOẠT ĐỘNG (AUTO / MANUAL) ===
    val MODE_AUTO = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x00u, 0x00u, 0x01u, 0x4cu, 0xf9u).toByteArray()
    val MODE_MAN  = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x00u, 0x00u, 0x00u, 0x8du, 0x39u).toByteArray()

    // === BẬT / TẮT HỆ THỐNG PHÒNG MỔ (RUN / STOP) ===
    val SYS_RUN  = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x01u, 0x00u, 0x01u, 0x1du, 0x39u).toByteArray()
    val SYS_STOP = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x01u, 0x00u, 0x00u, 0xdcu, 0xf9u).toByteArray()

    // === ÁP SUẤT ÂM (NEGATIVE PRESSURE) ===
    val NP_ON  = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0du, 0x00u, 0x01u, 0xddu, 0x3au).toByteArray()
    val NP_OFF = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0du, 0x00u, 0x00u, 0x1cu, 0xfau).toByteArray()

    // === QUẠT THÔNG GIÓ (SLAVE 3) ===
    val FAN_ON  = ubyteArrayOf(0x03u, 0x06u, 0x20u, 0x00u, 0x00u, 0x01u, 0x42u, 0x28u).toByteArray()
    val FAN_OFF = ubyteArrayOf(0x03u, 0x06u, 0x20u, 0x00u, 0x00u, 0x05u, 0x43u, 0xebu).toByteArray()

    // === LÀM LẠNH (COOLER) ===
    val COOL_ON  = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0eu, 0x00u, 0x01u, 0x2du, 0x3au).toByteArray()
    val COOL_OFF = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0eu, 0x00u, 0x00u, 0xecu, 0xfau).toByteArray()

    // === SƯỞI / SẤY (HEATER) ===
    val HEAT_ON  = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0fu, 0x00u, 0x01u, 0x7cu, 0xfau).toByteArray()
    val HEAT_OFF = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x0fu, 0x00u, 0x00u, 0xbdu, 0x3au).toByteArray()

    // === VỊ TRÍ OFFSET TRONG BẢN TIN AUTO SETTINGS (FUNCTION 0x10) ===
    const val IDX_TEMP_HI = 7
    const val IDX_TEMP_SET_HI = 9
    const val IDX_TEMP_TOL_HI = 11
    const val IDX_HEAT_TOL_HI = 13
    const val IDX_HUM_HI = 15
    const val IDX_HUM_SET_HI = 17
    const val IDX_HUM_TOL_HI = 19
    const val IDX_VALVE_HI = 21

    /**
     * Tạo bản tin chuyển tiếp nhiệt độ (Temp Forward) có tự động tính CRC16.
     */
    fun createTempForwardRequest(highByte: Byte, lowByte: Byte): ByteArray {
        val base = byteArrayOf(0x02, 0x06, 0x10, 0x02, highByte, lowByte)
        val (crcLow, crcHigh) = ModbusCrc16.calculate(base)
        return base + byteArrayOf(crcLow, crcHigh)
    }

    /**
     * Tạo bản tin chuyển tiếp độ ẩm (Hum Forward) có tự động tính CRC16.
     */
    fun createHumForwardRequest(highByte: Byte, lowByte: Byte): ByteArray {
        val base = byteArrayOf(0x02, 0x06, 0x10, 0x06, highByte, lowByte)
        val (crcLow, crcHigh) = ModbusCrc16.calculate(base)
        return base + byteArrayOf(crcLow, crcHigh)
    }

    /**
     * Xây dựng bản tin cấu hình tự động (Auto Settings Request - Function 0x10) gửi xuống PLC.
     */
    fun buildAutoSettingsRequest(
        tempSettingVal: Float,
        coolerSettingVal: Float,
        heaterSettingVal: Float,
        humSettingVal: Float,
        humTolSettingVal: Float,
        valveValue: Int,
        curTempHigh: Byte = 0x01,
        curTempLow: Byte = 0x0a,
        curHumHigh: Byte = 0x02,
        curHumLow: Byte = 0x5e
    ): ByteArray {
        val tempSet = (tempSettingVal * 10).toInt()
        val tempTol = (coolerSettingVal * 10).toInt()
        val heatTol = (heaterSettingVal * 10).toInt()
        val humSet = (humSettingVal * 10).toInt()
        val humTol = (humTolSettingVal * 10).toInt()

        val buffer = ubyteArrayOf(
            0x02u, 0x10u, 0x10u, 0x02u, 0x00u, 0x08u, 0x10u,
            0x00u, 0x00u, // 7-8: Temp hiện tại
            0x00u, 0x00u, // 9-10: Temp cài đặt
            0x00u, 0x00u, // 11-12: Temp dung sai (lạnh)
            0x00u, 0x00u, // 13-14: Nhiệt dung sai (sấy)
            0x00u, 0x00u, // 15-16: Hum hiện tại
            0x00u, 0x00u, // 17-18: Hum cài đặt
            0x00u, 0x00u, // 19-20: Hum dung sai
            0x00u, 0x00u, // 21-22: Góc mở van gió
            0x00u, 0x00u  // 23-24: 2 byte CRC16
        ).toByteArray()

        buffer[IDX_TEMP_HI] = curTempHigh
        buffer[IDX_TEMP_HI + 1] = curTempLow
        buffer[IDX_TEMP_SET_HI] = tempSet.and(0xff00).rotateRight(8).toByte()
        buffer[IDX_TEMP_SET_HI + 1] = tempSet.and(0xff).toByte()
        buffer[IDX_TEMP_TOL_HI] = tempTol.and(0xff00).rotateRight(8).toByte()
        buffer[IDX_TEMP_TOL_HI + 1] = tempTol.and(0xff).toByte()
        buffer[IDX_HEAT_TOL_HI] = heatTol.and(0xff00).rotateRight(8).toByte()
        buffer[IDX_HEAT_TOL_HI + 1] = heatTol.and(0xff).toByte()
        buffer[IDX_HUM_HI] = curHumHigh
        buffer[IDX_HUM_HI + 1] = curHumLow
        buffer[IDX_HUM_SET_HI] = humSet.and(0xff00).rotateRight(8).toByte()
        buffer[IDX_HUM_SET_HI + 1] = humSet.and(0xff).toByte()
        buffer[IDX_HUM_TOL_HI] = humTol.and(0xff00).rotateRight(8).toByte()
        buffer[IDX_HUM_TOL_HI + 1] = humTol.and(0xff).toByte()
        buffer[IDX_VALVE_HI] = valveValue.and(0xff00).rotateRight(8).toByte()
        buffer[IDX_VALVE_HI + 1] = valveValue.and(0xff).toByte()

        val (crcLow, crcHigh) = ModbusCrc16.calculate(buffer.copyOfRange(0, 23))
        buffer[buffer.size - 2] = crcLow
        buffer[buffer.size - 1] = crcHigh
        return buffer
    }
}
