package com.research.oproom.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.research.oproom.Post
import com.research.oproom.data.local.AppPreferences
import com.research.oproom.data.remote.ApiClient
import com.research.oproom.hardware.modbus.ModbusCommands
import com.research.oproom.hardware.serial.SerialPortManager
import com.research.oproom.ui.main.managers.FilterLifecycleManager
import com.research.oproom.ui.main.managers.ModbusCoordinator
import com.research.oproom.ui.main.managers.SensorLogCoordinator
import com.research.oproom.ui.main.managers.SurgeryTimerManager
import com.research.oproom.utils.AppLogger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

/**
 * ViewModel trung tâm của ứng dụng phòng mổ.
 * Hoạt động theo mô hình Phân rã Trách nhiệm (Separation of Concerns / Façade Pattern):
 * - Đóng vai trò cầu nối mượt mà cho UI (Activity & Fragments).
 * - Ủy quyền các tác vụ chuyên biệt cho 4 Manager độc lập:
 *   1. [SurgeryTimerManager]: Quản lý đồng hồ ca mổ & gây mê.
 *   2. [FilterLifecycleManager]: Quản lý vòng đời màng lọc HEPA.
 *   3. [ModbusCoordinator]: Quản lý giao tiếp cổng nối tiếp RS485 và truyền nhận PLC.
 *   4. [SensorLogCoordinator]: Giám sát thông số cảm biến và chu kỳ ghi log sự cố.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    val prefs: AppPreferences = AppPreferences.getInstance(application)

    // --- Các Trình quản lý chuyên biệt (Specialized Managers) ---
    val timerManager = SurgeryTimerManager(prefs)
    val filterManager = FilterLifecycleManager(prefs)

    val modbusCoordinator = ModbusCoordinator(
        context = application,
        onFilterValueReceived = { filterManager.updateFilterValue(it) },
        onTempHumReceived = { _, _ -> updatePost() }
    )

    val serialManager: SerialPortManager get() = modbusCoordinator.serialManager

    private val sensorLogCoordinator = SensorLogCoordinator { buildCurrentSensorSnapshot() }

    // =========================================================================
    // 1. DỮ LIỆU CẢM BIẾN & CẢNH BÁO (Từ ModbusCoordinator & FilterLifecycleManager)
    // =========================================================================

    val tempRaw: LiveData<UInt> get() = modbusCoordinator.tempRaw
    val humRaw: LiveData<UInt> get() = modbusCoordinator.humRaw
    val presRaw: LiveData<UInt> get() = modbusCoordinator.presRaw
    val heaterAlarm: LiveData<Boolean> get() = modbusCoordinator.heaterAlarm
    val ventAlarm: LiveData<Boolean> get() = modbusCoordinator.ventAlarm

    val filterValue: LiveData<UInt> get() = filterManager.filterValue
    val filterState: LiveData<Boolean> get() = filterManager.filterState
    val filterCount: LiveData<Int> get() = filterManager.filterCount

    // =========================================================================
    // 2. DỮ LIỆU ĐỒNG HỒ CA MỔ & GÂY MÊ (Từ SurgeryTimerManager)
    // =========================================================================

    val opHr: LiveData<Int> get() = timerManager.opHr
    val opMin: LiveData<Int> get() = timerManager.opMin
    val opSec: LiveData<Int> get() = timerManager.opSec
    val opColon: LiveData<Boolean> get() = timerManager.opColon
    val opRunning: LiveData<Boolean> get() = timerManager.opRunning
    val opCountUp: LiveData<Boolean> get() = timerManager.opCountUp
    val opSettingHr: LiveData<Int> get() = timerManager.opSettingHr
    val opSettingMin: LiveData<Int> get() = timerManager.opSettingMin

    val anHr: LiveData<Int> get() = timerManager.anHr
    val anMin: LiveData<Int> get() = timerManager.anMin
    val anSec: LiveData<Int> get() = timerManager.anSec
    val anColon: LiveData<Boolean> get() = timerManager.anColon
    val anRunning: LiveData<Boolean> get() = timerManager.anRunning
    val anCountUp: LiveData<Boolean> get() = timerManager.anCountUp
    val anSettingHr: LiveData<Int> get() = timerManager.anSettingHr
    val anSettingMin: LiveData<Int> get() = timerManager.anSettingMin

    // =========================================================================
    // 3. TRẠNG THÁI THIẾT BỊ CHẤP HÀNH & CÀI ĐẶT PLC
    // =========================================================================

    private val _light1State = MutableLiveData<Boolean>(false)
    val light1State: LiveData<Boolean> = _light1State

    private val _light2State = MutableLiveData<Boolean>(false)
    val light2State: LiveData<Boolean> = _light2State

    private val _sysState = MutableLiveData<Boolean>(false)
    val sysState: LiveData<Boolean> = _sysState

    private val _npState = MutableLiveData<Boolean>(false)
    val npState: LiveData<Boolean> = _npState

    private val _fanState = MutableLiveData<Boolean>(false)
    val fanState: LiveData<Boolean> = _fanState

    private val _coolerState = MutableLiveData<Boolean>(false)
    val coolerState: LiveData<Boolean> = _coolerState

    private val _heaterState = MutableLiveData<Boolean>(false)
    val heaterState: LiveData<Boolean> = _heaterState

    private val _autoMode = MutableLiveData<Boolean>(true)
    val autoMode: LiveData<Boolean> = _autoMode

    private val _tempSetting = MutableLiveData<Float>(23f)
    val tempSetting: LiveData<Float> = _tempSetting

    private val _humSetting = MutableLiveData<Float>(65f)
    val humSetting: LiveData<Float> = _humSetting

    private val _coolerSetting = MutableLiveData<Float>(0.5f)
    val coolerSetting: LiveData<Float> = _coolerSetting

    private val _humUnSetting = MutableLiveData<Float>(5f)
    val humUnSetting: LiveData<Float> = _humUnSetting

    private val _heaterSetting = MutableLiveData<Float>(1f)
    val heaterSetting: LiveData<Float> = _heaterSetting

    private val _valveValue = MutableLiveData<Int>(0)
    val valveValue: LiveData<Int> = _valveValue

    private val _advanceMode = MutableLiveData<Boolean>(false)
    val advanceMode: LiveData<Boolean> = _advanceMode

    private val _uvTime = MutableLiveData<Int>(10)
    val uvTime: LiveData<Int> = _uvTime

    // =========================================================================
    // 4. SỰ KIỆN XUẤT LOG & BẢO MẬT
    // =========================================================================

    private val _usbExportResult = MutableLiveData<AppLogger.ExportResult>()
    val usbExportResult: LiveData<AppLogger.ExportResult> = _usbExportResult

    private val _openSendLogEvent = MutableLiveData<Boolean>(false)
    val openSendLogEvent: LiveData<Boolean> = _openSendLogEvent

    init {
        loadInitialPreferences()
        sensorLogCoordinator.start()
    }

    private fun loadInitialPreferences() {
        _tempSetting.value = prefs.tempSetting
        _coolerSetting.value = prefs.coolerSetting
        _heaterSetting.value = prefs.heaterSetting
        _humSetting.value = prefs.humSetting
        _humUnSetting.value = prefs.humUnSetting
        _valveValue.value = prefs.valveValue

        updateAutoSettingsPacket()
    }

    // =========================================================================
    // ĐIỀU KHIỂN CÔNG TẮC THIẾT BỊ
    // =========================================================================

    fun toggleLight1() {
        val next = !(_light1State.value ?: false)
        _light1State.value = next
        AppLogger.i("THIET_BI", if (next) "Đã BẬT Đèn 1 - Đèn 1 đang SÁNG" else "Đã TẮT Đèn 1 - Đèn 1 đang TẮT")
        modbusCoordinator.sendState(next, ModbusCommands.LIGHT_1_ON, ModbusCommands.LIGHT_1_OFF)
        updatePost()
    }

    fun toggleLight2() {
        val next = !(_light2State.value ?: false)
        _light2State.value = next
        AppLogger.i("THIET_BI", if (next) "Đã BẬT Đèn 2 - Đèn 2 đang SÁNG" else "Đã TẮT Đèn 2 - Đèn 2 đang TẮT")
        modbusCoordinator.sendState(next, ModbusCommands.LIGHT_2_ON, ModbusCommands.LIGHT_2_OFF)
    }

    fun toggleSys() {
        val next = !(_sysState.value ?: false)
        _sysState.value = next
        AppLogger.i("THIET_BI", if (next) "Đã BẬT Hệ thống điều khiển phòng mổ (SYS ON)" else "Đã TẮT Hệ thống điều khiển phòng mổ (SYS OFF)")
        modbusCoordinator.sendState(next, ModbusCommands.FAN_ON, ModbusCommands.FAN_OFF)
        _fanState.value = false
        modbusCoordinator.sendState(next, ModbusCommands.SYS_RUN, ModbusCommands.SYS_STOP)
    }

    fun toggleNp() {
        val next = !(_npState.value ?: false)
        _npState.value = next
        AppLogger.i("THIET_BI", if (next) "Đã BẬT Chế độ áp suất âm (NP ON)" else "Đã TẮT Chế độ áp suất âm (NP OFF)")
        modbusCoordinator.sendState(next, ModbusCommands.NP_ON, ModbusCommands.NP_OFF)
    }

    fun toggleFan() {
        val next = !(_fanState.value ?: false)
        _fanState.value = next
        AppLogger.i("THIET_BI", if (next) "Đã BẬT Quạt thông gió" else "Đã TẮT Quạt thông gió")
        modbusCoordinator.sendState(next, ModbusCommands.FAN_ON, ModbusCommands.FAN_OFF)
        _sysState.value = false
    }

    fun toggleCooler() {
        val next = !(_coolerState.value ?: false)
        _coolerState.value = next
        AppLogger.i("THIET_BI", if (next) "Đã BẬT Dàn lạnh (Cooler ON)" else "Đã TẮT Dàn lạnh (Cooler OFF)")
        modbusCoordinator.sendState(next, ModbusCommands.COOL_ON, ModbusCommands.COOL_OFF)
    }

    fun toggleHeater() {
        val next = !(_heaterState.value ?: false)
        _heaterState.value = next
        AppLogger.i("THIET_BI", if (next) "Đã BẬT Sưởi nhiệt (Heater ON)" else "Đã TẮT Sưởi nhiệt (Heater OFF)")
        modbusCoordinator.sendState(next, ModbusCommands.HEAT_ON, ModbusCommands.HEAT_OFF)
    }

    fun setAutoMode(auto: Boolean) {
        if (_autoMode.value != auto) {
            _autoMode.value = auto
            AppLogger.i("CHE_DO", if (auto) "Chuyển sang chế độ TỰ ĐỘNG (Auto Mode)" else "Chuyển sang chế độ THỦ CÔNG (Manual Mode)")
            modbusCoordinator.setAutoMode(auto)
        }
    }

    // =========================================================================
    // CÀI ĐẶT THÔNG SỐ VẬN HÀNH (NUMERIC SETTINGS)
    // =========================================================================

    fun adjustTemp(up: Boolean) {
        val cur = _tempSetting.value ?: 23f
        val next = if (up) (cur + 0.5f).coerceAtMost(30f) else (cur - 0.5f).coerceAtLeast(16f)
        _tempSetting.value = next
        prefs.tempSetting = next
        AppLogger.i("CAI_DAT", "Điều chỉnh nhiệt độ đặt: $next°C (từ $cur°C)")
    }

    fun adjustHum(up: Boolean) {
        val cur = _humSetting.value ?: 65f
        val next = if (up) (cur + 1f).coerceAtMost(80f) else (cur - 1f).coerceAtLeast(50f)
        _humSetting.value = next
        prefs.humSetting = next
        AppLogger.i("CAI_DAT", "Điều chỉnh độ ẩm đặt: $next% (từ $cur%)")
    }

    fun adjustCooler(up: Boolean) {
        val cur = _coolerSetting.value ?: 0.5f
        val next = if (up) (cur + 0.1f).coerceAtMost(1f) else (cur - 0.1f).coerceAtLeast(0f)
        _coolerSetting.value = next
        prefs.coolerSetting = next
        AppLogger.i("CAI_DAT", "Điều chỉnh ngưỡng lạnh: $next°C")
    }

    fun adjustHumUn(up: Boolean) {
        val cur = _humUnSetting.value ?: 5f
        val next = if (up) (cur + 1f).coerceAtMost(10f) else (cur - 1f).coerceAtLeast(0f)
        _humUnSetting.value = next
        prefs.humUnSetting = next
        AppLogger.i("CAI_DAT", "Điều chỉnh dung sai độ ẩm: $next%")
    }

    fun adjustHeater(up: Boolean) {
        val cur = _heaterSetting.value ?: 1f
        val next = if (up) (cur + 0.2f).coerceAtMost(3f) else (cur - 0.2f).coerceAtLeast(0f)
        _heaterSetting.value = next
        prefs.heaterSetting = next
        AppLogger.i("CAI_DAT", "Điều chỉnh ngưỡng sưởi: $next°C")
    }

    fun setValve(valve: Int) {
        _valveValue.value = valve
        prefs.valveValue = valve
        AppLogger.i("CAI_DAT", "Cài đặt góc mở van gió: $valve%")
    }

    fun updateAutoSettingsPacket() {
        modbusCoordinator.updateAutoSettingsPacket(
            temp = _tempSetting.value ?: 23f,
            cooler = _coolerSetting.value ?: 0.5f,
            heater = _heaterSetting.value ?: 1f,
            hum = _humSetting.value ?: 65f,
            humUn = _humUnSetting.value ?: 5f,
            valve = _valveValue.value ?: 0
        )
    }

    fun applySettings() {
        updateAutoSettingsPacket()
        AppLogger.i("CAI_DAT", "Đã gửi cấu hình cài đặt nhiệt độ ${_tempSetting.value}°C, độ ẩm ${_humSetting.value}%, van ${_valveValue.value}% xuống PLC")
        modbusCoordinator.applySettings(
            temp = _tempSetting.value ?: 23f,
            cooler = _coolerSetting.value ?: 0.5f,
            heater = _heaterSetting.value ?: 1f,
            hum = _humSetting.value ?: 65f,
            humUn = _humUnSetting.value ?: 5f,
            valve = _valveValue.value ?: 0
        )
    }

    fun setUVTime(time: Int) {
        AppLogger.i("CAI_DAT", "Cài đặt thời gian khử trùng UV: $time phút")
        _uvTime.value = time
    }

    // =========================================================================
    // ỦY QUYỀN ĐỒNG HỒ & MÀNG LỌC (DELEGATED ACTIONS)
    // =========================================================================

    fun toggleOpTimer() = timerManager.toggleOpTimer()
    fun startOpTimer() = timerManager.startOpTimer()
    fun pauseOpTimer() = timerManager.pauseOpTimer()
    fun setOpCountUp(countUp: Boolean) = timerManager.setOpCountUp(countUp)
    fun updateOpSetting(hr: Int, min: Int) = timerManager.updateOpSetting(hr, min)

    fun toggleAnTimer() = timerManager.toggleAnTimer()
    fun startAnTimer() = timerManager.startAnTimer()
    fun pauseAnTimer() = timerManager.pauseAnTimer()
    fun setAnCountUp(countUp: Boolean) = timerManager.setAnCountUp(countUp)
    fun updateAnSetting(hr: Int, min: Int) = timerManager.updateAnSetting(hr, min)

    fun resetTimers() = timerManager.resetTimers()
    fun saveProfile(config: Int) = timerManager.saveProfile(config)
    fun loadProfile(config: Int) = timerManager.loadProfile(config)

    fun killFilter() = filterManager.killFilter()

    // =========================================================================
    // BẢO MẬT & XUẤT NHẬT KÝ LOG
    // =========================================================================

    fun checkPassword(password: String) {
        AppLogger.i("BAO_MAT", "Người dùng nhập mật khẩu: '$password'")
        when (password) {
            "advst" -> _advanceMode.value = true
            "retlifteser" -> filterManager.resetFilter()
            "kft" -> filterManager.killFilter()
            "viewlog" -> exportLogsToUsb()
            "sendlog" -> openSendLogDialog()
            else -> _advanceMode.value = false
        }
    }

    fun openSendLogDialog() {
        _openSendLogEvent.value = true
    }

    fun resetOpenSendLogEvent() {
        _openSendLogEvent.value = false
    }

    fun exportLogsToUsb() {
        AppLogger.i("USB_EXPORT", "Bắt đầu quét ổ đĩa USB và xuất file log...")
        modbusCoordinator.postOnThread {
            val result = AppLogger.exportLogsToUsb(getApplication())
            _usbExportResult.postValue(result)
        }
    }

    fun sendLogsToIp(ip: String) {
        AppLogger.i("LOG_SENDER", "Người dùng yêu cầu gửi log tới máy tính IP: '$ip'")
        modbusCoordinator.postOnThread {
            val result = AppLogger.sendLogsToIp(getApplication(), ip)
            _usbExportResult.postValue(result)
        }
    }

    // =========================================================================
    // THEO DÕI CẢM BIẾN & API POST
    // =========================================================================

    private fun buildCurrentSensorSnapshot(): AppLogger.SensorSnapshot {
        val tempC = (tempRaw.value ?: 0u).toFloat() / 10f
        val humPercent = (humRaw.value ?: 0u).toFloat() / 10f
        val presPa = ((presRaw.value ?: 0u).toFloat() * 100 / 8192).roundToInt()
        val filterDpPa = ((filterValue.value ?: 0u).toFloat() * 100 / 2048).roundToInt()
        val filterGood = filterState.value ?: true
        val filterCnt = filterCount.value ?: 0
        val heaterAlm = heaterAlarm.value ?: false
        val ventAlm = ventAlarm.value ?: false
        val serialConnected = modbusCoordinator.isSerialConnected

        val reasons = mutableListOf<String>()
        if (heaterAlm) reasons.add("Lỗi bộ sấy (Heater Alarm)")
        if (ventAlm) reasons.add("Lỗi quạt thông gió (Vent Alarm)")
        if (!filterGood) reasons.add("Màng lọc kém/quá hạn")
        if (!serialConnected) reasons.add("Mất kết nối cổng Modbus")

        if (_sysState.value == true && presPa <= 0) {
            reasons.add("Mất áp suất phòng khi hệ thống đang chạy (${presPa} Pa)")
        }
        if (tempC > 45f || (tempC < 10f && tempC > 0f)) {
            reasons.add("Nhiệt độ phòng bất thường (${tempC}°C)")
        }
        if (humPercent > 95f || (humPercent < 15f && humPercent > 0f)) {
            reasons.add("Độ ẩm phòng bất thường (${humPercent}%)")
        }

        val hasIncident = reasons.isNotEmpty()
        val incidentReason = reasons.joinToString("; ")

        return AppLogger.SensorSnapshot(
            tempCelsius = tempC,
            humPercent = humPercent,
            pressurePa = presPa,
            filterDpPa = filterDpPa,
            filterGood = filterGood,
            filterCount = filterCnt,
            heaterAlarm = heaterAlm,
            ventAlarm = ventAlm,
            serialConnected = serialConnected,
            hasIncident = hasIncident,
            incidentDescription = incidentReason
        )
    }

    fun updatePost() {
        val t = (tempRaw.value ?: 0u).toFloat() / 10f
        val h = (humRaw.value ?: 0u).toFloat() / 10f
        val post = Post(1, t, h)
        ApiClient.api.patchPost(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {}
            override fun onFailure(call: Call<Post>, t: Throwable) {}
        })
    }

    override fun onCleared() {
        super.onCleared()
        sensorLogCoordinator.stop()
        timerManager.clear()
        filterManager.clear()
        modbusCoordinator.stop()
    }
}
