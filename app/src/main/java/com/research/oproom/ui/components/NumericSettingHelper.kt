package com.research.oproom.ui.components

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import java.text.DecimalFormat

/**
 * Quản lý cụm điều khiển tăng / giảm giá trị số (Nhiệt độ, Độ ẩm, Sấy, Làm lạnh).
 */
class NumericSettingHelper(
    initialValue: Float,
    private val minValue: Float,
    private val maxValue: Float,
    var step: Float,
    private var btnDown: ImageButton,
    private var txtValue: TextView,
    private var btnUp: ImageButton,
    private var txtLabel: TextView,
    private var imgIcon: ImageView
) {
    var value: Float = initialValue
    private val decimalFormat = DecimalFormat("0.0")

    /**
     * Cập nhật chuỗi số hiển thị trên TextView.
     */
    fun updateValue() {
        txtValue.text = decimalFormat.format(value)
    }

    /**
     * Tăng giá trị lên 1 bước (step).
     */
    fun up() {
        if (value < maxValue) {
            value += step
            updateValue()
        }
    }

    /**
     * Giảm giá trị đi 1 bước (step).
     */
    fun down() {
        if (value > (minValue + step)) {
            value -= step
            updateValue()
        }
    }

    /**
     * Cập nhật tham chiếu các View khi màn hình được tạo lại.
     */
    fun updateRef(
        btnDown: ImageButton,
        txtValue: TextView,
        btnUp: ImageButton,
        txtLabel: TextView,
        imgIcon: ImageView
    ) {
        this.btnDown = btnDown
        this.txtValue = txtValue
        this.btnUp = btnUp
        this.txtLabel = txtLabel
        this.imgIcon = imgIcon
    }

    /**
     * Ẩn toàn bộ cụm điều khiển (INVISIBLE).
     */
    fun submerge() {
        btnDown.visibility = View.INVISIBLE
        txtValue.visibility = View.INVISIBLE
        btnUp.visibility = View.INVISIBLE
        txtLabel.visibility = View.INVISIBLE
        imgIcon.visibility = View.INVISIBLE
    }

    /**
     * Hiện toàn bộ cụm điều khiển (VISIBLE).
     */
    fun emerge() {
        btnDown.visibility = View.VISIBLE
        txtValue.visibility = View.VISIBLE
        btnUp.visibility = View.VISIBLE
        txtLabel.visibility = View.VISIBLE
        imgIcon.visibility = View.VISIBLE
    }
}
