package com.research.oproom.ui.components

import android.os.Handler
import android.os.Looper
import android.widget.ImageView

/**
 * Quản lý trạng thái và hình ảnh cảnh báo của module (Sấy, Quạt thông gió).
 */
class ModuleAlarmHelper(
    var state: Boolean,
    private var imageView: ImageView,
    private val okDrawableRes: Int,
    private val errorDrawableRes: Int
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Cập nhật trạng thái lỗi (true = có lỗi/báo động, false = bình thường).
     */
    fun update(hasError: Boolean) {
        this.state = hasError
        mainHandler.post {
            imageView.setImageResource(if (hasError) errorDrawableRes else okDrawableRes)
        }
    }

    /**
     * Cập nhật tham chiếu ImageView khi chuyển màn hình.
     */
    fun updateRef(newImageView: ImageView) {
        this.imageView = newImageView
    }
}
