package com.research.oproom.data.local

import android.content.Context
import android.content.SharedPreferences

// === CÁC KHÓA LƯU TRỮ VÀ TÊN FILE CẤU HÌNH ===
private const val PREF_NAME = "Config"
private const val KEY_TEMP_SETTING = "tempSetting"
private const val KEY_COOLER_SETTING = "coolerSetting"
private const val KEY_HEATER_SETTING = "heaterSetting"
private const val KEY_HUM_SETTING = "humSetting"
private const val KEY_HUM_TOL_SETTING = "humUnSetting"
private const val KEY_VALVE = "valve"
private const val KEY_OP_HR = "opHr"
private const val KEY_OP_MIN = "opMin"
private const val KEY_OP_UP = "opUp"
private const val KEY_AN_HR = "anHr"
private const val KEY_AN_MIN = "anMin"
private const val KEY_AN_UP = "anUp"
private const val KEY_FILTER_COUNT = "filterCount"
private const val KEY_FILTER_STATE = "filterState"
private const val KEY_TARGET_IP = "targetIp"

/**
 * Quản lý lưu trữ cấu hình cục bộ của ứng dụng phòng mổ qua SharedPreferences.
 * Tên file lưu trữ: "Config"
 */
class AppPreferences(context: Context) {

    private val sp: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // === CÀI ĐẶT THÔNG SỐ VẬN HÀNH PLC ===
    var tempSetting: Float
        get() = sp.getFloat(KEY_TEMP_SETTING, 23f)
        set(value) = sp.edit().putFloat(KEY_TEMP_SETTING, value).apply()

    var coolerSetting: Float
        get() = sp.getFloat(KEY_COOLER_SETTING, 0.5f)
        set(value) = sp.edit().putFloat(KEY_COOLER_SETTING, value).apply()

    var heaterSetting: Float
        get() = sp.getFloat(KEY_HEATER_SETTING, 1f)
        set(value) = sp.edit().putFloat(KEY_HEATER_SETTING, value).apply()

    var humSetting: Float
        get() = sp.getFloat(KEY_HUM_SETTING, 65f)
        set(value) = sp.edit().putFloat(KEY_HUM_SETTING, value).apply()

    var humUnSetting: Float
        get() = sp.getFloat(KEY_HUM_TOL_SETTING, 5f)
        set(value) = sp.edit().putFloat(KEY_HUM_TOL_SETTING, value).apply()

    var valveValue: Int
        get() = sp.getInt(KEY_VALVE, 0)
        set(value) = sp.edit().putInt(KEY_VALVE, value).apply()

    var targetIp: String
        get() = sp.getString(KEY_TARGET_IP, "") ?: ""
        set(value) = sp.edit().putString(KEY_TARGET_IP, value).apply()

    // === BỘ ĐẾM THỜI GIAN CA MỔ (OPERATION TIMER) ===
    var opHr: Int
        get() = sp.getInt(KEY_OP_HR, 0)
        set(value) = sp.edit().putInt(KEY_OP_HR, value).apply()

    var opMin: Int
        get() = sp.getInt(KEY_OP_MIN, 0)
        set(value) = sp.edit().putInt(KEY_OP_MIN, value).apply()

    var opUp: Boolean
        get() = sp.getBoolean(KEY_OP_UP, true)
        set(value) = sp.edit().putBoolean(KEY_OP_UP, value).apply()

    // === BỘ ĐẾM THỜI GIAN GÂY MÊ (ANESTHESIA TIMER) ===
    var anHr: Int
        get() = sp.getInt(KEY_AN_HR, 0)
        set(value) = sp.edit().putInt(KEY_AN_HR, value).apply()

    var anMin: Int
        get() = sp.getInt(KEY_AN_MIN, 0)
        set(value) = sp.edit().putInt(KEY_AN_MIN, value).apply()

    var anUp: Boolean
        get() = sp.getBoolean(KEY_AN_UP, true)
        set(value) = sp.edit().putBoolean(KEY_AN_UP, value).apply()

    // === TRẠNG THÁI MÀNG LỌC HEPA ===
    var filterCount: Int
        get() = sp.getInt(KEY_FILTER_COUNT, 259200)
        set(value) = sp.edit().putInt(KEY_FILTER_COUNT, value).apply()

    var filterState: Boolean
        get() = sp.getBoolean(KEY_FILTER_STATE, false)
        set(value) = sp.edit().putBoolean(KEY_FILTER_STATE, value).apply()

    // === LƯU VÀ TẢI PROFILE CẤU HÌNH (1, 2, 3) ===
    fun saveProfile(config: Int, opHr: Int, opMin: Int, opUp: Boolean, anHr: Int, anMin: Int, anUp: Boolean) {
        sp.edit()
            .putInt("opHr$config", opHr)
            .putInt("opMin$config", opMin)
            .putBoolean("opUp$config", opUp)
            .putInt("anHr$config", anHr)
            .putInt("anMin$config", anMin)
            .putBoolean("anUp$config", anUp)
            .apply()
    }

    fun loadProfile(config: Int): ProfileConfig {
        return ProfileConfig(
            opHr = sp.getInt("opHr$config", 0),
            opMin = sp.getInt("opMin$config", 0),
            opUp = sp.getBoolean("opUp$config", true),
            anHr = sp.getInt("anHr$config", 0),
            anMin = sp.getInt("anMin$config", 0),
            anUp = sp.getBoolean("anUp$config", true)
        )
    }

    data class ProfileConfig(
        val opHr: Int,
        val opMin: Int,
        val opUp: Boolean,
        val anHr: Int,
        val anMin: Int,
        val anUp: Boolean
    )

    companion object {
        const val PREF_NAME = "Config"

        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
