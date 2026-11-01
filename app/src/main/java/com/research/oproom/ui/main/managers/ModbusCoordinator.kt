@file:OptIn(ExperimentalUnsignedTypes::class)

package com.research.oproom.ui.main.managers

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.research.oproom.hardware.modbus.ModbusCommands
import com.research.oproom.hardware.modbus.ModbusCrc16
import com.research.oproom.hardware.modbus.ModbusParser
import com.research.oproom.hardware.serial.SerialPortManager
import java.util.Collections
import java.util.Timer
import java.util.TimerTask

/**
 * Điều phối toàn bộ hoạt động giao tiếp truyền thông Modbus RTU qua cổng USB Serial RS485.
 * Đảm nhiệm:
 * - Quản lý kết nối cổng nối tiếp SerialPortManager.
 * - Quản lý HandlerThread nền để truyền nhận dữ liệu bất đồng bộ.
 * - Phân tích, bóc tách và cập nhật thông số cảm biến (Nhiệt độ, Độ ẩm, Áp suất, Màng lọc, Báo động).
 * - Gửi lệnh điều khiển bật/tắt thiết bị và cập nhật gói tin cài đặt tự động xuống PLC.
 */
class ModbusCoordinator(
    context: Context,
    private val onFilterValueReceived: (UInt) -> Unit,
    private val onTempHumReceived: ((Float, Float) -> Unit)? = null
) {
    val serialManager = SerialPortManager(context)

    private val handlerThread = HandlerThread("ModbusBackgroundThread")
    private val threadHandler: Handler
    private val packageBuffer = Collections.synchronizedList(arrayListOf<Byte>())

    // --- Cảm biến & Cảnh báo ---
    private val _tempRaw = MutableLiveData<UInt>(0u)
    val tempRaw: LiveData<UInt> = _tempRaw

    private val _humRaw = MutableLiveData<UInt>(0u)
    val humRaw: LiveData<UInt> = _humRaw

    private val _presRaw = MutableLiveData<UInt>(0u)
    val presRaw: LiveData<UInt> = _presRaw

    private val _heaterAlarm = MutableLiveData<Boolean>(false)
    val heaterAlarm: LiveData<Boolean> = _heaterAlarm

    private val _ventAlarm = MutableLiveData<Boolean>(false)
    val ventAlarm: LiveData<Boolean> = _ventAlarm

    val isSerialConnected: Boolean get() = serialManager.isConnected

    private var pollTimer: Timer? = null
    private var transmitting = false

    private var tempForwardRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x02u, 0x00u, 0x00u, 0xbdu, 0x3au).toByteArray()
    private var humForwardRequest = ubyteArrayOf(0x02u, 0x06u, 0x10u, 0x06u, 0x00u, 0x00u, 0xbdu, 0x3au).toByteArray()
    private var autoSettingsRequest = ubyteArrayOf(
        0x02u, 0x10u, 0x10u, 0x02u, 0x00u, 0x08u, 0x10u, 0x01u, 0x0au, 0x00u, 0xe6u,
        0x00u, 0x05u, 0x00u, 0x14u, 0x02u, 0x5eu, 0x02u, 0x8au, 0x00u, 0x32u, 0x00u,
        0x00u, 0x05u, 0x18u
    ).toByteArray()
    private var dummyRequest = ubyteArrayOf(0x02u, 0x03u, 0x10u, 0x12u, 0x00u, 0x01u, 0x20u, 0xfcu).toByteArray()

    init {
        handlerThread.start()
        threadHandler = Handler(handlerThread.looper)
        setupSerialCommunication()
        startPolling()
    }

    private fun setupSerialCommunication() {
        serialManager.listener = object : SerialPortManager.SerialDataListener {
            override fun onNewData(data: ByteArray) {
                synchronized(packageBuffer) {
                    for (b in data) {
                        packageBuffer.add(b)
                    }
                    processModbusBuffer()
                }
            }

            override fun onRunError(e: Exception) {
                Log.e("SerialError", "Lỗi truyền nhận Modbus: ${e.message}")
            }

            override fun onConnectionStatusChanged(connected: Boolean, deviceName: String?) {
                Log.d("SerialConn", "Trạng thái USB: $connected, Thiết bị: $deviceName")
            }
        }
        serialManager.connect(9600)
    }

    private fun processModbusBuffer() {
        if (packageBuffer.size >= 5) {
            val tempHum = ModbusParser.parseTempHum(packageBuffer)
            if (tempHum != null) {
                _humRaw.postValue(tempHum.humidityRaw)
                _tempRaw.postValue(tempHum.temperatureRaw)

                tempForwardRequest[4] = tempHum.tempHighByte
                tempForwardRequest[5] = tempHum.tempLowByte
                humForwardRequest[4] = tempHum.humHighByte
                humForwardRequest[5] = tempHum.humLowByte

                autoSettingsRequest[ModbusCommands.IDX_TEMP_HI] = tempHum.tempHighByte
                autoSettingsRequest[ModbusCommands.IDX_TEMP_HI + 1] = tempHum.tempLowByte
                autoSettingsRequest[ModbusCommands.IDX_HUM_HI] = tempHum.humHighByte
                autoSettingsRequest[ModbusCommands.IDX_HUM_HI + 1] = tempHum.humLowByte

                onTempHumReceived?.invoke(tempHum.temperatureCelsius, tempHum.humidityPercent)

                transmitting = false
                return
            }

            val presAlarms = ModbusParser.parsePressureAndAlarms(packageBuffer)
            if (presAlarms != null) {
                _presRaw.postValue(presAlarms.pressureRaw)
                _heaterAlarm.postValue(presAlarms.heaterAlarm)
                _ventAlarm.postValue(presAlarms.ventAlarm)

                onFilterValueReceived(presAlarms.filterValue)

                transmitting = false
                return
            }

            if (packageBuffer[0] == 0x02.toByte() && packageBuffer.size == dummyRequest.size) {
                var match = true
                for (i in dummyRequest.indices) {
                    if (packageBuffer[i] != dummyRequest[i]) match = false
                }
                if (match) transmitting = false
            } else {
                errorCodeScan()
            }
        }
    }

    private fun errorCodeScan() {
        val (cLow, cHigh) = ModbusCrc16.calculate(packageBuffer.toByteArray().copyOfRange(0, 3))
        if (packageBuffer.size >= 5 && packageBuffer[3] == cLow && packageBuffer[4] == cHigh) {
            Log.d("TEC", packageBuffer.toByteArray().toUByteArray().contentToString())
        }
    }

    fun startPolling() {
        pollTimer?.cancel()
        pollTimer = Timer()
        pollTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                threadHandler.post(SendRequestRunnable(ModbusCommands.HUM_TEMP_REQUEST, true))
                threadHandler.post {
                    val (tLow, tHigh) = ModbusCrc16.calculate(tempForwardRequest.copyOfRange(0, 6))
                    tempForwardRequest[6] = tLow
                    tempForwardRequest[7] = tHigh

                    val (hLow, hHigh) = ModbusCrc16.calculate(humForwardRequest.copyOfRange(0, 6))
                    humForwardRequest[6] = hLow
                    humForwardRequest[7] = hHigh
                }
                threadHandler.post(SendRequestRunnable(ModbusCommands.PRES_REQUEST, true))
                threadHandler.post(object : SendRequestRunnable(tempForwardRequest, true) {
                    override fun run() {
                        dummyRequest = tempForwardRequest
                        super.run()
                    }
                })
                threadHandler.post(object : SendRequestRunnable(humForwardRequest, true) {
                    override fun run() {
                        dummyRequest = humForwardRequest
                        super.run()
                    }
                })
            }
        }, 1500, 1000)
    }

    fun sendState(state: Boolean, on: ByteArray, off: ByteArray) {
        val cmd = if (state) on else off
        threadHandler.post(SendRequestRunnable(cmd, false))
        try { Thread.sleep(30) } catch (_: Exception) {}
    }

    fun setAutoMode(auto: Boolean) {
        val cmd = if (auto) ModbusCommands.MODE_AUTO else ModbusCommands.MODE_MAN
        threadHandler.post(SendRequestRunnable(cmd, false))
    }

    fun updateAutoSettingsPacket(
        temp: Float,
        cooler: Float,
        heater: Float,
        hum: Float,
        humUn: Float,
        valve: Int
    ) {
        autoSettingsRequest = ModbusCommands.buildAutoSettingsRequest(
            temp,
            cooler,
            heater,
            hum,
            humUn,
            valve,
            autoSettingsRequest[ModbusCommands.IDX_TEMP_HI],
            autoSettingsRequest[ModbusCommands.IDX_TEMP_HI + 1],
            autoSettingsRequest[ModbusCommands.IDX_HUM_HI],
            autoSettingsRequest[ModbusCommands.IDX_HUM_HI + 1]
        )
    }

    fun applySettings(
        temp: Float,
        cooler: Float,
        heater: Float,
        hum: Float,
        humUn: Float,
        valve: Int
    ) {
        updateAutoSettingsPacket(temp, cooler, heater, hum, humUn, valve)
        threadHandler.post {
            serialManager.write(autoSettingsRequest, 800)
        }
    }

    fun postOnThread(task: () -> Unit) {
        threadHandler.post { task() }
    }

    fun stop() {
        pollTimer?.cancel()
        handlerThread.quit()
        serialManager.disconnect()
    }

    open inner class SendRequestRunnable(var request: ByteArray, var waitRespond: Boolean) : Runnable {
        private val timeOut = request.size * 5
        override fun run() {
            synchronized(packageBuffer) {
                packageBuffer.clear()
            }
            val (cLow, cHigh) = ModbusCrc16.calculate(request.copyOfRange(0, request.size - 2))
            request[request.size - 2] = cLow
            request[request.size - 1] = cHigh
            serialManager.write(request, timeOut)

            if (waitRespond) {
                transmitting = true
                val waitTime = if (request[1].compareTo(3) == 0 || request[1].compareTo(16) == 0) {
                    ((request[4].toInt() * 16 + request[5].toInt()) * 2 + 5) * 5
                } else {
                    (request[3] * 2 + 5) * 5
                }
                var wait = 0
                try {
                    Thread.sleep(waitTime.toLong())
                    while (transmitting) {
                        Thread.sleep(20)
                        wait++
                        if (wait > 6) transmitting = false
                    }
                } catch (_: Exception) {
                    transmitting = false
                }
            } else {
                try { Thread.sleep(100) } catch (_: Exception) {}
            }
        }
    }
}

