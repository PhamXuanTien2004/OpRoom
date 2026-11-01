package com.research.oproom.ui.main.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.research.oproom.data.local.AppPreferences
import com.research.oproom.utils.AppLogger
import java.util.Timer
import java.util.TimerTask

/**
 * Quản lý đồng hồ đếm thời gian ca phẫu thuật (OpTimer) và thời gian gây mê (AnTimer).
 * Đảm nhiệm:
 * - Đếm xuôi / Đếm ngược với chu kỳ 250ms (chớp nháy dấu hai chấm và đếm giây).
 * - Cài đặt thời gian đích, tạm dừng, tiếp tục, thiết lập lại (reset).
 * - Lưu và nạp các mẫu hồ sơ cấu hình (Profile 1, 2, 3).
 */
class SurgeryTimerManager(private val prefs: AppPreferences) {

    // --- Đồng hồ đếm giờ mổ (OpTimer) ---
    private val _opHr = MutableLiveData<Int>(0)
    val opHr: LiveData<Int> = _opHr

    private val _opMin = MutableLiveData<Int>(0)
    val opMin: LiveData<Int> = _opMin

    private val _opSec = MutableLiveData<Int>(0)
    val opSec: LiveData<Int> = _opSec

    private val _opColon = MutableLiveData<Boolean>(true)
    val opColon: LiveData<Boolean> = _opColon

    private val _opRunning = MutableLiveData<Boolean>(false)
    val opRunning: LiveData<Boolean> = _opRunning

    private val _opCountUp = MutableLiveData<Boolean>(true)
    val opCountUp: LiveData<Boolean> = _opCountUp

    private val _opSettingHr = MutableLiveData<Int>(0)
    val opSettingHr: LiveData<Int> = _opSettingHr

    private val _opSettingMin = MutableLiveData<Int>(0)
    val opSettingMin: LiveData<Int> = _opSettingMin

    // --- Đồng hồ đếm giờ gây mê (AnTimer) ---
    private val _anHr = MutableLiveData<Int>(0)
    val anHr: LiveData<Int> = _anHr

    private val _anMin = MutableLiveData<Int>(0)
    val anMin: LiveData<Int> = _anMin

    private val _anSec = MutableLiveData<Int>(0)
    val anSec: LiveData<Int> = _anSec

    private val _anColon = MutableLiveData<Boolean>(true)
    val anColon: LiveData<Boolean> = _anColon

    private val _anRunning = MutableLiveData<Boolean>(false)
    val anRunning: LiveData<Boolean> = _anRunning

    private val _anCountUp = MutableLiveData<Boolean>(true)
    val anCountUp: LiveData<Boolean> = _anCountUp

    private val _anSettingHr = MutableLiveData<Int>(0)
    val anSettingHr: LiveData<Int> = _anSettingHr

    private val _anSettingMin = MutableLiveData<Int>(0)
    val anSettingMin: LiveData<Int> = _anSettingMin

    private var opTimerTask: Timer? = null
    private var anTimerTask: Timer? = null
    private var opTick = 0
    private var anTick = 0

    init {
        loadInitialPreferences()
    }

    private fun loadInitialPreferences() {
        _opSettingHr.value = prefs.opHr
        _opSettingMin.value = prefs.opMin
        _opCountUp.value = prefs.opUp
        _opHr.value = prefs.opHr
        _opMin.value = prefs.opMin

        _anSettingHr.value = prefs.anHr
        _anSettingMin.value = prefs.anMin
        _anCountUp.value = prefs.anUp
        _anHr.value = prefs.anHr
        _anMin.value = prefs.anMin
    }

    // =========================================================================
    // QUẢN LÝ ĐỒNG HỒ CA MỔ (OP TIMER)
    // =========================================================================

    fun toggleOpTimer() {
        if (_opRunning.value == true) {
            pauseOpTimer()
        } else {
            startOpTimer()
        }
    }

    fun startOpTimer() {
        AppLogger.i("DONG_HO", "Đồng hồ mổ: BẮT ĐẦU đếm (${if (_opCountUp.value == true) "Đếm xuôi" else "Đếm ngược"})")
        _opRunning.value = true
        opTimerTask?.cancel()
        opTimerTask = Timer()
        opTimerTask?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val isUp = _opCountUp.value ?: true
                if (isUp) tickUpOp() else tickDownOp()
            }
        }, 0, 250)
    }

    fun pauseOpTimer() {
        AppLogger.i("DONG_HO", "Đồng hồ mổ: TẠM DỪNG đếm")
        opTimerTask?.cancel()
        opTimerTask = null
        _opRunning.postValue(false)
    }

    private fun tickUpOp() {
        if (opTick >= 3) {
            opTick = 0
            var s = (_opSec.value ?: 0) + 1
            var m = _opMin.value ?: 0
            var h = _opHr.value ?: 0
            if (s >= 60) {
                s = 0
                m++
            }
            if (m >= 60) {
                m = 0
                h++
            }
            _opSec.postValue(s)
            _opMin.postValue(m)
            _opHr.postValue(h)
        } else {
            opTick++
        }
        _opColon.postValue(opTick % 4 < 2)
    }

    private fun tickDownOp() {
        var s = _opSec.value ?: 0
        var m = _opMin.value ?: 0
        var h = _opHr.value ?: 0
        if (s + m + h == 0) {
            pauseOpTimer()
            opTick = 0
        } else {
            if (opTick >= 3) {
                if (s <= 0) {
                    if (m <= 0) {
                        h--
                        m = 59
                    } else {
                        m--
                    }
                    s = 59
                } else {
                    s--
                }
                opTick = 0
                _opSec.postValue(s)
                _opMin.postValue(m)
                _opHr.postValue(h)
            } else {
                opTick++
            }
            _opColon.postValue(opTick % 4 < 2)
        }
    }

    fun setOpCountUp(countUp: Boolean) {
        AppLogger.i("DONG_HO", "Đồng hồ mổ: Chọn ${if (countUp) "Đếm xuôi (Count Up)" else "Đếm ngược (Count Down)"}")
        _opCountUp.value = countUp
        prefs.opUp = countUp
        if (countUp) {
            _opSettingHr.value = 0
            _opSettingMin.value = 0
            _opHr.value = 0
            _opMin.value = 0
            _opSec.value = 0
            prefs.opHr = 0
            prefs.opMin = 0
        }
    }

    fun updateOpSetting(hr: Int, min: Int) {
        AppLogger.i("DONG_HO", "Cài đặt thời gian mổ: ${hr}h ${min}p")
        _opSettingHr.value = hr
        _opSettingMin.value = min
        prefs.opHr = hr
        prefs.opMin = min
        if (_opRunning.value != true) {
            _opHr.value = hr
            _opMin.value = min
            _opSec.value = 0
        }
    }

    // =========================================================================
    // QUẢN LÝ ĐỒNG HỒ GÂY MÊ (AN TIMER)
    // =========================================================================

    fun toggleAnTimer() {
        if (_anRunning.value == true) {
            pauseAnTimer()
        } else {
            startAnTimer()
        }
    }

    fun startAnTimer() {
        AppLogger.i("DONG_HO", "Đồng hồ gây mê: BẮT ĐẦU đếm (${if (_anCountUp.value == true) "Đếm xuôi" else "Đếm ngược"})")
        _anRunning.value = true
        anTimerTask?.cancel()
        anTimerTask = Timer()
        anTimerTask?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val isUp = _anCountUp.value ?: true
                if (isUp) tickUpAn() else tickDownAn()
            }
        }, 0, 250)
    }

    fun pauseAnTimer() {
        AppLogger.i("DONG_HO", "Đồng hồ gây mê: TẠM DỪNG đếm")
        anTimerTask?.cancel()
        anTimerTask = null
        _anRunning.postValue(false)
    }

    private fun tickUpAn() {
        if (anTick >= 3) {
            anTick = 0
            var s = (_anSec.value ?: 0) + 1
            var m = _anMin.value ?: 0
            var h = _anHr.value ?: 0
            if (s >= 60) {
                s = 0
                m++
            }
            if (m >= 60) {
                m = 0
                h++
            }
            _anSec.postValue(s)
            _anMin.postValue(m)
            _anHr.postValue(h)
        } else {
            anTick++
        }
        _anColon.postValue(anTick % 4 < 2)
    }

    private fun tickDownAn() {
        var s = _anSec.value ?: 0
        var m = _anMin.value ?: 0
        var h = _anHr.value ?: 0
        if (s + m + h == 0) {
            pauseAnTimer()
            opTick = 0
        } else {
            if (anTick >= 3) {
                if (s <= 0) {
                    if (m <= 0) {
                        h--
                        m = 59
                    } else {
                        m--
                    }
                    s = 59
                } else {
                    s--
                }
                anTick = 0
                _anSec.postValue(s)
                _anMin.postValue(m)
                _anHr.postValue(h)
            } else {
                anTick++
            }
            _anColon.postValue(anTick % 4 < 2)
        }
    }

    fun setAnCountUp(countUp: Boolean) {
        AppLogger.i("DONG_HO", "Đồng hồ gây mê: Chọn ${if (countUp) "Đếm xuôi (Count Up)" else "Đếm ngược (Count Down)"}")
        _anCountUp.value = countUp
        prefs.anUp = countUp
        if (countUp) {
            _anSettingHr.value = 0
            _anSettingMin.value = 0
            _anHr.value = 0
            _anMin.value = 0
            _anSec.value = 0
            prefs.anHr = 0
            prefs.anMin = 0
        }
    }

    fun updateAnSetting(hr: Int, min: Int) {
        AppLogger.i("DONG_HO", "Cài đặt thời gian gây mê: ${hr}h ${min}p")
        _anSettingHr.value = hr
        _anSettingMin.value = min
        prefs.anHr = hr
        prefs.anMin = min
        if (_anRunning.value != true) {
            _anHr.value = hr
            _anMin.value = min
            _anSec.value = 0
        }
    }

    // =========================================================================
    // CÁC THAO TÁC CHUNG (RESET & PROFILE)
    // =========================================================================

    fun resetTimers() {
        AppLogger.i("DONG_HO", "Đã ĐẶT LẠI (Reset) toàn bộ đồng hồ ca mổ & gây mê")
        pauseOpTimer()
        pauseAnTimer()
        _opHr.value = _opSettingHr.value ?: 0
        _opMin.value = _opSettingMin.value ?: 0
        _opSec.value = 0

        _anHr.value = _anSettingHr.value ?: 0
        _anMin.value = _anSettingMin.value ?: 0
        _anSec.value = 0
    }

    fun saveProfile(config: Int) {
        AppLogger.i("PROFILE", "Đã LƯU hồ sơ cấu hình số $config")
        prefs.saveProfile(
            config,
            _opSettingHr.value ?: 0,
            _opSettingMin.value ?: 0,
            _opCountUp.value ?: true,
            _anSettingHr.value ?: 0,
            _anSettingMin.value ?: 0,
            _anCountUp.value ?: true
        )
    }

    fun loadProfile(config: Int) {
        AppLogger.i("PROFILE", "Đã NẠP hồ sơ cấu hình số $config")
        val profile = prefs.loadProfile(config)
        _opSettingHr.value = profile.opHr
        _opSettingMin.value = profile.opMin
        _opCountUp.value = profile.opUp
        _anSettingHr.value = profile.anHr
        _anSettingMin.value = profile.anMin
        _anCountUp.value = profile.anUp

        resetTimers()
    }

    fun clear() {
        opTimerTask?.cancel()
        anTimerTask?.cancel()
    }
}

