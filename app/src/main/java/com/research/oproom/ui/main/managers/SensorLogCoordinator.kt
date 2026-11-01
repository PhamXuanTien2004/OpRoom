package com.research.oproom.ui.main.managers

import android.util.Log
import com.research.oproom.utils.AppLogger
import java.util.Timer
import java.util.TimerTask

/**
 * Điều phối chu kỳ ghi nhật ký thông số cảm biến và cảnh báo sự cố ra file riêng.
 * Đảm nhiệm:
 * - Chu kỳ bình thường: ghi 5 giây / 1 lần.
 * - Chu kỳ khi có sự cố: lưu liên tục (1 giây / 1 lần).
 * - Phát hiện và phản ứng ngay lập tức khi trạng thái sự cố thay đổi.
 */
class SensorLogCoordinator(
    private val snapshotProvider: () -> AppLogger.SensorSnapshot
) {
    private var sensorLogTimer: Timer? = null
    private var lastSensorLogTime: Long = 0L
    private var wasSensorIncident: Boolean = false

    fun start() {
        sensorLogTimer?.cancel()
        sensorLogTimer = Timer("SensorLoggingTimer", true)
        sensorLogTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    recordSensorDataCycle()
                } catch (e: Exception) {
                    Log.e("SensorLog", "Lỗi chu kỳ ghi log cảm biến: ${e.message}")
                }
            }
        }, 2000L, 1000L) // Kiểm tra mỗi 1 giây để đáp ứng lưu liên tục khi có sự cố
    }

    private fun recordSensorDataCycle() {
        val now = System.currentTimeMillis()
        val snapshot = snapshotProvider()
        val stateChanged = (snapshot.hasIncident != wasSensorIncident)

        val shouldLog = snapshot.hasIncident || stateChanged || (now - lastSensorLogTime >= 5000L)

        if (shouldLog) {
            AppLogger.logSensorReading(snapshot)
            lastSensorLogTime = now
            wasSensorIncident = snapshot.hasIncident
        }
    }

    fun stop() {
        sensorLogTimer?.cancel()
    }
}

