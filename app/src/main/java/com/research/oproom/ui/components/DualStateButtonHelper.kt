package com.research.oproom.ui.components

import android.view.View
import android.widget.ImageButton

/**
 * Quản lý nút bấm có 2 trạng thái Bật / Tắt (ví dụ: Nút Np, Quạt, Lạnh, Sưởi, Sys).
 */
class DualStateButtonHelper(
    var stateOn: Boolean,
    private var button: ImageButton,
    private val onDrawableRes: Int,
    private val offDrawableRes: Int
) {
    /**
     * Đảo trạng thái nút và cập nhật lại hình ảnh hiển thị.
     */
    fun switch(visible: Boolean = true) {
        stateOn = !stateOn
        if (visible) {
            update()
        }
    }

    /**
     * Cập nhật hình ảnh của nút theo trạng thái hiện tại (stateOn).
     */
    fun update() {
        if (stateOn) {
            button.setImageResource(onDrawableRes)
        } else {
            button.setImageResource(offDrawableRes)
        }
    }

    /**
     * Cập nhật tham chiếu View khi màn hình được tạo lại hoặc chuyển đổi.
     */
    fun updateRef(newButton: ImageButton) {
        this.button = newButton
    }

    /**
     * Ẩn nút đi (INVISIBLE).
     */
    fun submerge() {
        button.visibility = View.INVISIBLE
    }

    /**
     * Hiện nút lên (VISIBLE).
     */
    fun emerge() {
        button.visibility = View.VISIBLE
    }
}
