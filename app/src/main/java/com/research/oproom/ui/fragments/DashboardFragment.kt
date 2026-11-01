package com.research.oproom.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.research.oproom.MainActivity
import com.research.oproom.R
import com.research.oproom.databinding.ActivityMainBinding
import com.research.oproom.ui.main.MainViewModel
import kotlin.math.roundToInt

/**
 * Màn hình giám sát và điều khiển chính phòng mổ (Trang 0).
 * Tự động đồng bộ với MainViewModel, loại bỏ phụ thuộc findViewById từ Activity.
 */
class DashboardFragment : Fragment() {

    private var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigationAndClicks()
        observeViewModel()
    }

    private fun setupNavigationAndClicks() {
        val act = activity as? MainActivity

        // Chuyển trang
        binding.buttonSetting.setOnClickListener { act?.navigateToPage(1) }
        binding.buttonGo.setOnClickListener { act?.navigateToPage(2) }
        binding.imageButton.setOnClickListener { act?.hideSystemBars() }

        // Bật/tắt thiết bị
        binding.buttonLight1.setOnClickListener { viewModel.toggleLight1() }
        binding.buttonLight2.setOnClickListener { viewModel.toggleLight2() }
        binding.buttonSys.setOnClickListener { viewModel.toggleSys() }

        // Cài đặt giờ mổ & gây mê
        binding.textViewOpHourSet.setOnClickListener {
            if (viewModel.opCountUp.value != true) act?.showTimerDialog(isOpTimer = true)
        }
        binding.textViewOpMinSet.setOnClickListener {
            if (viewModel.opCountUp.value != true) act?.showTimerDialog(isOpTimer = true)
        }
        binding.textViewAnHourSet.setOnClickListener {
            if (viewModel.anCountUp.value != true) act?.showTimerDialog(isOpTimer = false)
        }
        binding.textViewAnMinSet.setOnClickListener {
            if (viewModel.anCountUp.value != true) act?.showTimerDialog(isOpTimer = false)
        }

        // Chọn đếm lên / đếm xuống
        binding.imageButtonOpUp.setOnClickListener { viewModel.setOpCountUp(true) }
        binding.imageButtonOpDown.setOnClickListener { viewModel.setOpCountUp(false) }
        binding.imageButtonAnUp.setOnClickListener { viewModel.setAnCountUp(true) }
        binding.imageButtonAnDown.setOnClickListener { viewModel.setAnCountUp(false) }

        // Lưu & nạp cấu hình
        binding.buttonSave.setOnClickListener { act?.showConfigDialog(save = true) }
        binding.buttonLoad.setOnClickListener { act?.showConfigDialog(save = false) }
    }

    private fun observeViewModel() {
        // Cảm biến
        viewModel.tempRaw.observe(viewLifecycleOwner) { raw ->
            val formatted = (raw.toFloat() / 10f).toString().padStart(4, '0')
            binding.textViewSensorTemp.text = formatted
        }

        viewModel.humRaw.observe(viewLifecycleOwner) { raw ->
            val formatted = (raw.toFloat() / 10f).toString().padStart(4, '0')
            binding.textViewSensorHum.text = formatted
        }

        viewModel.presRaw.observe(viewLifecycleOwner) { raw ->
            val formatted = (raw.toFloat() * 100 / 8192).roundToInt().toString().padStart(2, '0')
            binding.textViewSensorPres.text = formatted
        }

        // Báo động & Màng lọc
        viewModel.heaterAlarm.observe(viewLifecycleOwner) { hasError ->
            binding.alarmHeater.setImageResource(if (hasError) R.drawable.heater_error else R.drawable.heater_ok)
        }

        viewModel.ventAlarm.observe(viewLifecycleOwner) { hasError ->
            binding.alarmVent.setImageResource(if (hasError) R.drawable.vent_error else R.drawable.vent_ok)
        }

        viewModel.filterState.observe(viewLifecycleOwner) { isGood ->
            binding.textViewFilter.text = if (isGood) "Đạt" else "Kém"
        }

        // Đèn 1
        viewModel.light1State.observe(viewLifecycleOwner) { on ->
            binding.buttonLight1.setImageResource(if (on) R.drawable.light_on else R.drawable.light_off)
            try {
                binding.buttonLight1.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = if (on) 0.937f else 0.95f
                }
            } catch (_: Exception) {}
        }

        // Đèn 2
        viewModel.light2State.observe(viewLifecycleOwner) { on ->
            binding.buttonLight2.setImageResource(if (on) R.drawable.light_on else R.drawable.light_off)
            try {
                binding.buttonLight2.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = if (on) 0.937f else 0.95f
                }
            } catch (_: Exception) {}
        }

        // Hệ thống (Sys)
        viewModel.sysState.observe(viewLifecycleOwner) { on ->
            binding.buttonSys.setImageResource(if (on) R.drawable.sys_on else R.drawable.sys_off)
        }

        // Cài đặt giờ mổ
        viewModel.opSettingHr.observe(viewLifecycleOwner) { hr ->
            binding.textViewOpHourSet.text = hr.toString().padStart(2, '0')
        }
        viewModel.opSettingMin.observe(viewLifecycleOwner) { min ->
            binding.textViewOpMinSet.text = min.toString().padStart(2, '0')
        }

        // Cài đặt giờ gây mê
        viewModel.anSettingHr.observe(viewLifecycleOwner) { hr ->
            binding.textViewAnHourSet.text = hr.toString().padStart(2, '0')
        }
        viewModel.anSettingMin.observe(viewLifecycleOwner) { min ->
            binding.textViewAnMinSet.text = min.toString().padStart(2, '0')
        }

        // Nút đếm tiến / lùi Op
        viewModel.opCountUp.observe(viewLifecycleOwner) { up ->
            if (up) {
                binding.imageButtonOpUp.setImageResource(R.drawable.up_active)
                binding.imageButtonOpDown.setImageResource(R.drawable.down_inactive)
            } else {
                binding.imageButtonOpUp.setImageResource(R.drawable.up_inactive)
                binding.imageButtonOpDown.setImageResource(R.drawable.down_active)
            }
        }

        // Nút đếm tiến / lùi An
        viewModel.anCountUp.observe(viewLifecycleOwner) { up ->
            if (up) {
                binding.imageButtonAnUp.setImageResource(R.drawable.up_active)
                binding.imageButtonAnDown.setImageResource(R.drawable.down_inactive)
            } else {
                binding.imageButtonAnUp.setImageResource(R.drawable.up_inactive)
                binding.imageButtonAnDown.setImageResource(R.drawable.down_active)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
