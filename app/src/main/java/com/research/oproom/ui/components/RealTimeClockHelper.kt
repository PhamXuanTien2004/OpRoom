package com.research.oproom.ui.components

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.text.DateFormat
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

/**
 * Tiện ích cập nhật ngày giờ thời gian thực định kỳ lên màn hình.
 */
class RealTimeClockHelper(
    var date: String,
    var time: String,
    private var dateView: TextView,
    private var timeView: TextView
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private var isRunning = false

    /**
     * Cập nhật lại tham chiếu TextView.
     */
    fun updateRef(dateView: TextView, timeView: TextView) {
        this.dateView = dateView
        this.timeView = timeView
    }

    /**
     * Lấy ngày giờ hiện tại và cập nhật lên giao diện.
     */
    fun updateText() {
        val calendar = Calendar.getInstance()
        date = DateFormat.getDateInstance().format(calendar.time)
        time = DateFormat.getTimeInstance().format(calendar.time)
        mainHandler.post {
            timeView.text = time.padStart(8, '0')
            dateView.text = date
        }
    }

    /**
     * Khởi động bộ đếm cập nhật định kỳ mỗi 500ms.
     */
    fun start() {
        if (!isRunning) {
            isRunning = true
            timer = Timer().apply {
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        updateText()
                    }
                }, 0, 500)
            }
        }
    }

    /**
     * Dừng bộ đếm thời gian.
     */
    fun stop() {
        isRunning = false
        timer?.cancel()
        timer = null
    }
}
