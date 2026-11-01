package com.research.oproom

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.research.oproom.ui.fragments.DashboardFragment
import com.research.oproom.ui.fragments.PresentationFragment
import com.research.oproom.ui.fragments.SettingsFragment
import com.research.oproom.ui.main.MainViewModel
import com.research.oproom.utils.AppLogger
import com.research.oproom.utils.SystemBarUtils

/**
 * Activity chính của ứng dụng bảng điều khiển phòng mổ Sìn Hồ - Lai Châu.
 * Hoạt động theo kiến trúc Single Activity hiện đại: đóng vai trò khung chứa (Host)
 * và điều hướng giữa 3 Fragment (Dashboard, Settings, Presentation).
 */
class MainActivity : AppCompatActivity(),
    TimerSetDialog.OnInputListener,
    UVSetDialog.OnInputListener,
    ConfigDialog.OnInputListener,
    ValveSetDialog.OnInputListener,
    PasswordDialog.OnInputListener,
    SendLogDialog.OnSendLogListener {

    val viewModel: MainViewModel by viewModels()

    private val dashboardFragment = DashboardFragment()
    private val settingsFragment = SettingsFragment()
    private val presentationFragment = PresentationFragment()

    var currentPage = 0
        private set

    private var currentTimerTargetIsOp = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.init(this) // Tự động kích hoạt bộ ghi log và bắt toàn bộ Terminal Logcat ra file
        setContentView(R.layout.activity_main_container)

        supportActionBar?.hide()
        SystemBarUtils.hideSystemBars(window)

        if (savedInstanceState == null) {
            setupFragments()
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.usbExportResult.observe(this) { result ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(if (result.success) "Xuất Log Thành Công" else "Thông Báo")
                .setMessage(result.message)
                .setPositiveButton("Đóng") { dialog, _ ->
                    dialog.dismiss()
                    SystemBarUtils.hideSystemBars(window)
                }
                .setOnDismissListener {
                    SystemBarUtils.hideSystemBars(window)
                }
                .show()
        }

        viewModel.openSendLogEvent.observe(this) { open ->
            if (open) {
                SendLogDialog().show(supportFragmentManager, "SendLogDialog")
                viewModel.resetOpenSendLogEvent()
            }
        }
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, dashboardFragment, TAG_DASHBOARD)
            .add(R.id.fragment_container, settingsFragment, TAG_SETTINGS).hide(settingsFragment)
            .add(R.id.fragment_container, presentationFragment, TAG_PRESENTATION).hide(presentationFragment)
            .commit()
    }

    /**
     * Điều hướng hiển thị tức thì giữa 3 màn hình, giữ nguyên trạng thái trong bộ nhớ.
     */
    fun navigateToPage(targetPage: Int) {
        currentPage = targetPage
        val pageName = when (targetPage) {
            0 -> "Trang 0 (Giám sát chính - Dashboard)"
            1 -> "Trang 1 (Cài đặt thông số - Settings)"
            2 -> "Trang 2 (Trình chiếu tài liệu - Presentation)"
            else -> "Trang $targetPage"
        }
        AppLogger.i("DIEU_HUONG", "Người dùng chuyển sang màn hình: $pageName")
        val transaction = supportFragmentManager.beginTransaction()
        when (targetPage) {
            0 -> transaction.show(dashboardFragment).hide(settingsFragment).hide(presentationFragment)
            1 -> transaction.show(settingsFragment).hide(dashboardFragment).hide(presentationFragment)
            2 -> transaction.show(presentationFragment).hide(dashboardFragment).hide(settingsFragment)
        }
        transaction.commit()
        SystemBarUtils.hideSystemBars(window)
    }

    fun hideSystemBars() {
        SystemBarUtils.hideSystemBars(window)
    }

    @Suppress("UNUSED_PARAMETER")
    fun hideThatShit(v: View) {
        hideSystemBars()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClick(v: View?) {
        // Tương thích ngược với các thuộc tính android:onClick trong XML
    }

    // --- Mở các Dialog tương tác ---
    fun showTimerDialog(isOpTimer: Boolean) {
        currentTimerTargetIsOp = isOpTimer
        val hr = if (isOpTimer) viewModel.opSettingHr.value ?: 0 else viewModel.anSettingHr.value ?: 0
        val min = if (isOpTimer) viewModel.opSettingMin.value ?: 0 else viewModel.anSettingMin.value ?: 0
        TimerSetDialog(hr, min).show(supportFragmentManager, "TimerSetDialog")
    }

    fun showConfigDialog(save: Boolean) {
        ConfigDialog(save).show(supportFragmentManager, "ConfigDialog")
    }

    fun showValveDialog() {
        val valve = viewModel.valveValue.value ?: 0
        ValveSetDialog(valve).show(supportFragmentManager, "ValveSetDialog")
    }

    fun showPasswordDialog() {
        PasswordDialog().show(supportFragmentManager, "PasswordDialog")
    }

    fun showUVDialog() {
        val time = viewModel.uvTime.value ?: 10
        UVSetDialog(time).show(supportFragmentManager, "UVSetDialog")
    }

    // --- Dialog Callbacks chuyển tiếp vào ViewModel ---
    override fun sendInput(inputHr: Int, inputMin: Int) {
        if (currentTimerTargetIsOp) {
            viewModel.updateOpSetting(inputHr, inputMin)
        } else {
            viewModel.updateAnSetting(inputHr, inputMin)
        }
    }

    override fun saveOrLoad(config: Int, save: Boolean) {
        if (save) {
            viewModel.saveProfile(config)
        } else {
            viewModel.loadProfile(config)
        }
    }

    override fun sendValve(valve: Int) {
        viewModel.setValve(valve)
    }

    override fun sendPassword(password: String) {
        viewModel.checkPassword(password)
    }

    override fun sendUVTime(uvTime: Int) {
        viewModel.setUVTime(uvTime)
    }

    override fun onSendLogToIp(ip: String) {
        viewModel.sendLogsToIp(ip)
    }

    companion object {
        private const val TAG_DASHBOARD = "dashboard_fragment"
        private const val TAG_SETTINGS = "settings_fragment"
        private const val TAG_PRESENTATION = "presentation_fragment"
    }
}