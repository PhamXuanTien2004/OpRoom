package com.research.oproom.ui.components

import android.os.Handler
import android.os.Looper
import android.widget.TextView

/**
 * Quản lý hiển thị giá trị cảm biến kèm đơn vị (°C, %RH, Pa).
 */
open class SensorDisplayHelper(
    var value: UInt,
    var unit: String,
    var showUnit: Boolean,
    protected var textView: TextView
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Cập nhật văn bản hiển thị lên giao diện trên luồng UI chính.
     */
    open fun update() {
        mainHandler.post {
            val formatted = (value.toFloat() / 10f).toString().padStart(4, '0')
            textView.text = if (showUnit) "$formatted$unit" else formatted
        }
    }

    /**
     * Cập nhật tham chiếu TextView khi chuyển màn hình.
     */
    fun updateRef(newTextView: TextView) {
        this.textView = newTextView
    }
}
