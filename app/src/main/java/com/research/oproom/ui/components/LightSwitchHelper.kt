package com.research.oproom.ui.components

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.research.oproom.R

/**
 * Quản lý công tắc đèn (Đèn 1, Đèn 2) kèm hiệu ứng chuyển đổi trạng thái và vị trí.
 */
open class LightSwitchHelper(
    var button: ImageButton,
    var state: Boolean = false,
    var onDrawableRes: Int = R.drawable.light_on,
    var offDrawableRes: Int = R.drawable.light_off
) {
    companion object{
        private const val TAG = "Light Switch"
    }
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Bật / tắt đèn.
     */
    fun switch(currentPage: Int = 0) {
        state = !state
        if (state) setOn(currentPage) else setOff(currentPage)
    }

    /**
     * Cập nhật tham chiếu View khi chuyển đổi layout.
     */
    fun update(newButton: ImageButton, currentPage: Int = 0) {
        this.button = newButton
        if (state) setOn(currentPage) else setOff(currentPage)
    }

    fun setOn(currentPage: Int = 0) {
        mainHandler.post {
            setIcon()
            try {
                if (currentPage == 0) {
                    button.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.937f }
                } else {
                    button.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.32f }
                }
            } catch (e: Exception) {
                // Trong trường hợp View không thuộc ConstraintLayout
                Log.e(TAG, "Log error : ${e.message}")
            }
        }
    }

    fun setOff(currentPage: Int = 0) {
        mainHandler.post {
            setIcon()
            try {
                if (currentPage == 0) {
                    button.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.95f }
                } else {
                    button.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 0.33f }
                }
            } catch (e: Exception) {
                // Trong trường hợp View không thuộc ConstraintLayout
                Log.e(TAG, "Log error: ${e.message}")
            }
        }
    }

    protected open fun setIcon() {
        button.setImageResource(if (state) onDrawableRes else offDrawableRes)
    }
}
