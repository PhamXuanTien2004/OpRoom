package com.research.oproom.ui.components

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.research.oproom.R
import java.util.Timer
import java.util.TimerTask

/**
 * Quản lý tính năng trình chiếu slide ảnh tài liệu / ca mổ trong phòng mổ.
 */
class SlideShowHelper(
    private var btnNext: Button,
    private var btnBack: Button,
    private var imgView: ImageView,
    private var btnRun: Button
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    var currentPicIndex = 0
    private val picList = arrayListOf(R.drawable.pic0)
    private var slideTimer: Timer? = null

    init {
        setupListeners()
    }

    private fun setupListeners() {
        btnNext.setOnClickListener { nextPic() }
        btnBack.setOnClickListener { prevPic() }
        btnRun.setOnClickListener { startSlideShow(30000L) }
    }

    /**
     * Hiển thị toàn bộ cụm điều khiển slide.
     */
    fun emerge() {
        btnNext.visibility = View.VISIBLE
        btnBack.visibility = View.VISIBLE
        btnRun.visibility = View.VISIBLE
        imgView.visibility = View.VISIBLE
        nextPic()
    }

    /**
     * Ẩn toàn bộ cụm điều khiển slide.
     */
    fun submerge() {
        stopSlideShow()
        btnNext.visibility = View.GONE
        btnBack.visibility = View.GONE
        btnRun.visibility = View.GONE
        imgView.visibility = View.GONE
    }

    /**
     * Chuyển sang ảnh tiếp theo.
     */
    fun nextPic() {
        if (picList.isEmpty()) return
        currentPicIndex = (currentPicIndex + 1) % picList.size
        mainHandler.post {
            imgView.setImageResource(picList[currentPicIndex])
        }
    }

    /**
     * Quay lại ảnh trước.
     */
    fun prevPic() {
        if (picList.isEmpty()) return
        currentPicIndex = if (currentPicIndex == 0) picList.size - 1 else currentPicIndex - 1
        mainHandler.post {
            imgView.setImageResource(picList[currentPicIndex])
        }
    }

    /**
     * Bắt đầu tự động chuyển slide theo chu kỳ.
     */
    fun startSlideShow(periodMs: Long = 30000L) {
        stopSlideShow()
        slideTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    nextPic()
                }
            }, 1000, periodMs)
        }
    }

    /**
     * Dừng trình chiếu tự động.
     */
    fun stopSlideShow() {
        slideTimer?.cancel()
        slideTimer = null
    }

    /**
     * Cập nhật tham chiếu các View khi chuyển đổi layout.
     */
    fun updateRef(btnNext: Button, btnBack: Button, imgView: ImageView, btnRun: Button) {
        this.btnNext = btnNext
        this.btnBack = btnBack
        this.imgView = imgView
        this.btnRun = btnRun
        setupListeners()
    }
}
