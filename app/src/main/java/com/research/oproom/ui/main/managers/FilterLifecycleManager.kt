package com.research.oproom.ui.main.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.research.oproom.data.local.AppPreferences
import java.util.Timer
import java.util.TimerTask

/**
 * Quản lý vòng đời và cảnh báo màng lọc HEPA phòng mổ.
 * Đảm nhiệm:
 * - Đếm tích lũy thời gian hoạt động của màng lọc (mỗi phút).
 * - Cảnh báo màng lọc hết hạn khi vượt quá 256.200 phút.
 * - Hỗ trợ cưỡng bức làm hỏng (killFilter) và đặt lại màng lọc mới (resetFilter).
 */
class FilterLifecycleManager(private val prefs: AppPreferences) {

    private val _filterValue = MutableLiveData<UInt>(0u)
    val filterValue: LiveData<UInt> = _filterValue

    private val _filterState = MutableLiveData<Boolean>(true)
    val filterState: LiveData<Boolean> = _filterState

    private val _filterCount = MutableLiveData<Int>(0) 
    val filterCount: LiveData<Int> = _filterCount

    private val filterTimer = Timer()

    init {
        _filterCount.value = prefs.filterCount
        _filterState.value = prefs.filterState
        startFilterTimer()
    }

    private fun startFilterTimer() {
        filterTimer.schedule(object : TimerTask() {
            override fun run() {
                val current = _filterCount.value ?: 0
                if (current >= 256200) {
                    if (_filterState.value == true) killFilter()
                } else {
                    val updated = current + 1
                    _filterCount.postValue(updated)
                    prefs.filterCount = updated
                }
            }
        }, 0, 60000)
    }

    fun updateFilterValue(raw: UInt) {
        _filterValue.postValue(raw)
    }

    fun killFilter() {
        _filterCount.postValue(256200)
        _filterState.postValue(false)
        prefs.filterState = false
        prefs.filterCount = 256200
    }

    fun resetFilter() {
        _filterState.postValue(true)
        _filterCount.postValue(0)
        prefs.filterState = true
        prefs.filterCount = 0
    }

    fun clear() {
        filterTimer.cancel()
    }
}

