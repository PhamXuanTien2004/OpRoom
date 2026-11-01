package com.research.oproom.ui.components

import android.os.Handler
import android.os.Looper
import android.widget.TextView

/**
 * Cấu hình hiển thị thời gian ca mổ / gây mê đã cài đặt trên trang chính.
 */
class TimerSettingHelper(
    var hr: Int,
    var min: Int,
    var hrText: TextView,
    var minText: TextView
) {
    fun updateRef(hrTextNew: TextView, minTextNew: TextView) {
        hrText = hrTextNew
        minText = minTextNew
    }

    fun updateData(hr: Int, min: Int) {
        this.hr = hr
        this.min = min
        updateData()
    }

    fun updateData() {
        hrText.text = hr.toString().padStart(2, '0')
        minText.text = min.toString().padStart(2, '0')
    }
}

/**
 * Quản lý đếm giờ tiến / lùi và hiển thị nhấp nháy dấu hai chấm (:).
 */
class TimerTextHelper(
    var hr: Int,
    var min: Int,
    var sec: Int,
    var pos: Int,
    var hrRef: TextView,
    var minRef: TextView,
    var secRef: TextView,
    var isRunning: Boolean,
    var countUp: Boolean,
    private val onTimeOut: (() -> Unit)? = null
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tick = 0

    fun tickUp() {
        if (tick >= 3) {
            tick = 0
            sec++
        } else {
            tick++
        }
        if (sec >= 60) {
            sec = 0
            min++
        }
        if (min >= 60) {
            min = 0
            hr++
        }
    }

    fun tickDown() {
        if ((sec + min + hr) == 0) {
            isRunning = false
            tick = 0
            onTimeOut?.invoke()
        } else {
            if (tick >= 3) {
                if (sec <= 0) {
                    if (min <= 0) {
                        hr--
                        min = 59
                    } else {
                        min--
                    }
                    sec = 59
                } else {
                    sec--
                }
                tick = 0
            } else {
                tick++
            }
        }
    }

    fun updateRef(hrRef: TextView, minRef: TextView, secRef: TextView) {
        this.hrRef = hrRef
        this.minRef = minRef
        this.secRef = secRef
    }

    fun updateText() {
        mainHandler.post {
            secRef.text = sec.toString().padStart(2, '0')
            minRef.text = min.toString().padStart(2, '0')
            val colon = if (tick % 4 < 2) ":" else " "
            hrRef.text = "${hr.toString().padStart(2, '0')}$colon"
        }
    }
}
