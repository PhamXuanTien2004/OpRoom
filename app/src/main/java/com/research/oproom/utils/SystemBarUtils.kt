package com.research.oproom.utils

import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Tiện ích quản lý ẩn/hiện thanh điều hướng hệ thống (Status Bar, Navigation Bar)
 * để ứng dụng phòng mổ luôn chạy ở chế độ toàn màn hình (Kiosk/Fullscreen Mode).
 */
object SystemBarUtils {

    /**
     * Ẩn thanh trạng thái và thanh điều hướng hệ thống.
     * Vuốt từ cạnh màn hình chỉ hiển thị tạm thời rồi tự động ẩn đi.
     */
    @JvmStatic
    fun hideSystemBars(window: Window) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    /**
     * Hiện lại các thanh hệ thống khi cần thiết.
     */
    @JvmStatic
    fun showSystemBars(window: Window) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }
}
