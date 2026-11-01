package com.research.oproom.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.research.oproom.MainActivity
import com.research.oproom.R
import com.research.oproom.databinding.ActivitySettingsRealBinding
import com.research.oproom.ui.main.MainViewModel
import java.text.DecimalFormat
import kotlin.math.roundToInt

/**
 * Màn hình cài đặt thông số chi tiết hệ thống và PLC phòng mổ (Trang 1).
 * Đồng bộ hai chiều với MainViewModel.
 */
class SettingsFragment : Fragment() {

    private var _binding: ActivitySettingsRealBinding? = null
    val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private val decimalFormat = DecimalFormat("0.0")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySettingsRealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        observeViewModel()
    }

    private fun setupClicks() {
        val act = activity as? MainActivity

        binding.button2.setOnClickListener { act?.navigateToPage(0) }
        binding.imageButton.setOnClickListener { act?.hideSystemBars() }

        // Chế độ Auto / Manual
        binding.buttonAuto.setOnClickListener { viewModel.setAutoMode(true) }
        binding.buttonMan.setOnClickListener { viewModel.setAutoMode(false) }

        // Công tắc thiết bị
        binding.buttonNp.setOnClickListener { viewModel.toggleNp() }
        binding.imageButtonFan.setOnClickListener { viewModel.toggleFan() }
        binding.imageButtonCooler.setOnClickListener { viewModel.toggleCooler() }
        binding.imageButtonHeater.setOnClickListener { viewModel.toggleHeater() }
        binding.buttonApply.setOnClickListener { viewModel.applySettings() }

        // Tăng giảm thông số
        binding.imageButtonTempDown.setOnClickListener { viewModel.adjustTemp(false) }
        binding.imageButtonTempUp.setOnClickListener { viewModel.adjustTemp(true) }

        binding.imageButtonHumDown.setOnClickListener { viewModel.adjustHum(false) }
        binding.imageButtonHumUp.setOnClickListener { viewModel.adjustHum(true) }

        binding.imageButtonCoolerDown.setOnClickListener { viewModel.adjustCooler(false) }
        binding.imageButtonCoolerUp.setOnClickListener { viewModel.adjustCooler(true) }

        binding.imageButtonHumUnDown.setOnClickListener { viewModel.adjustHumUn(false) }
        binding.imageButtonHumUnUp.setOnClickListener { viewModel.adjustHumUn(true) }

        binding.imageButtonHeaterDown.setOnClickListener { viewModel.adjustHeater(false) }
        binding.imageButtonHeaterUp.setOnClickListener { viewModel.adjustHeater(true) }

        // Van gió & Mật khẩu nâng cao
        binding.textViewValveValue.setOnClickListener { act?.showValveDialog() }
        binding.header2.setOnClickListener { act?.showPasswordDialog() }
    }

    private fun observeViewModel() {
        // Giá trị số
        viewModel.tempSetting.observe(viewLifecycleOwner) { v ->
            binding.textViewTempValue.text = decimalFormat.format(v)
        }
        viewModel.humSetting.observe(viewLifecycleOwner) { v ->
            binding.textViewHumValue.text = decimalFormat.format(v)
        }
        viewModel.coolerSetting.observe(viewLifecycleOwner) { v ->
            binding.textViewCoolerValue.text = decimalFormat.format(v)
        }
        viewModel.humUnSetting.observe(viewLifecycleOwner) { v ->
            binding.textViewHumUnValue.text = decimalFormat.format(v)
        }
        viewModel.heaterSetting.observe(viewLifecycleOwner) { v ->
            binding.textViewHeaterValue.text = decimalFormat.format(v)
        }

        // Van gió & Áp suất màng lọc
        viewModel.valveValue.observe(viewLifecycleOwner) { valve ->
            binding.textViewValveValue.text = valve.toString()
        }
        viewModel.filterValue.observe(viewLifecycleOwner) { raw ->
            val formatted = (raw.toFloat() * 100 / 2048).roundToInt().toString().padStart(2, '0') + " Pa"
            binding.textViewFdpValue.text = formatted
        }

        // Trạng thái các nút
        viewModel.npState.observe(viewLifecycleOwner) { on ->
            binding.buttonNp.setImageResource(if (on) R.drawable.np_enable else R.drawable.np_disable)
        }
        viewModel.fanState.observe(viewLifecycleOwner) { on ->
            binding.imageButtonFan.setImageResource(if (on) R.drawable.fan_on else R.drawable.fan_off)
        }
        viewModel.coolerState.observe(viewLifecycleOwner) { on ->
            binding.imageButtonCooler.setImageResource(if (on) R.drawable.cooler_on else R.drawable.cooler_off)
        }
        viewModel.heaterState.observe(viewLifecycleOwner) { on ->
            binding.imageButtonHeater.setImageResource(if (on) R.drawable.heater_on else R.drawable.heater_off)
        }

        // Chế độ Auto / Manual và hiển thị cụm điều khiển
        viewModel.autoMode.observe(viewLifecycleOwner) { isAuto ->
            updateModeDisplay(isAuto, viewModel.advanceMode.value ?: false)
        }

        viewModel.advanceMode.observe(viewLifecycleOwner) { isAdvance ->
            updateModeDisplay(viewModel.autoMode.value ?: true, isAdvance)
        }
    }

    private fun updateModeDisplay(isAuto: Boolean, isAdvance: Boolean) {
        if (isAuto) {
            binding.buttonAuto.setImageResource(R.drawable.auto_enable)
            binding.buttonMan.setImageResource(R.drawable.man_disable)

            // Cụm Manual ẩn
            binding.imageButtonFan.visibility = View.INVISIBLE
            binding.imageButtonCooler.visibility = View.INVISIBLE
            binding.imageButtonHeater.visibility = View.INVISIBLE

            // Cụm Auto cơ bản
            setGroupVisibility(
                visible = true,
                binding.imageButtonTempDown, binding.textViewTempValue, binding.imageButtonTempUp,
                binding.textViewTemp, binding.imageViewTemp,
                binding.imageButtonHumDown, binding.textViewHumValue, binding.imageButtonHumUp,
                binding.textViewHum, binding.imageViewHum,
                binding.buttonApply
            )

            // Cụm nâng cao (cooler, humUn, heater, valve)
            val advVisibility = if (isAdvance) View.VISIBLE else View.GONE
            setGroupVisibility(
                isAdvance,
                binding.imageButtonCoolerDown, binding.textViewCoolerValue, binding.imageButtonCoolerUp,
                binding.textViewCooler, binding.imageViewCooler,
                binding.imageButtonHumUnDown, binding.textViewHumUnValue, binding.imageButtonHumUnUp,
                binding.textViewHumUn, binding.imageViewHumUn,
                binding.imageButtonHeaterDown, binding.textViewHeaterValue, binding.imageButtonHeaterUp,
                binding.textViewHeater, binding.imageViewHeater
            )
            binding.textViewValveText.visibility = advVisibility
            binding.textViewValveValue.visibility = advVisibility
            binding.imageViewValve.visibility = advVisibility
            binding.buttonMan.visibility = if (isAdvance) View.VISIBLE else View.GONE
        } else {
            binding.buttonAuto.setImageResource(R.drawable.auto_disable)
            binding.buttonMan.setImageResource(R.drawable.man_enable)

            // Ẩn tất cả cụm Auto
            setGroupVisibility(
                visible = false,
                binding.imageButtonTempDown, binding.textViewTempValue, binding.imageButtonTempUp,
                binding.textViewTemp, binding.imageViewTemp,
                binding.imageButtonHumDown, binding.textViewHumValue, binding.imageButtonHumUp,
                binding.textViewHum, binding.imageViewHum,
                binding.imageButtonCoolerDown, binding.textViewCoolerValue, binding.imageButtonCoolerUp,
                binding.textViewCooler, binding.imageViewCooler,
                binding.imageButtonHumUnDown, binding.textViewHumUnValue, binding.imageButtonHumUnUp,
                binding.textViewHumUn, binding.imageViewHumUn,
                binding.imageButtonHeaterDown, binding.textViewHeaterValue, binding.imageButtonHeaterUp,
                binding.textViewHeater, binding.imageViewHeater,
                binding.buttonApply
            )
            binding.textViewValveText.visibility = View.INVISIBLE
            binding.textViewValveValue.visibility = View.INVISIBLE
            binding.imageViewValve.visibility = View.INVISIBLE

            // Hiện các nút điều khiển tay (Manual)
            binding.imageButtonFan.visibility = View.VISIBLE
            binding.imageButtonCooler.visibility = View.VISIBLE
            binding.imageButtonHeater.visibility = View.VISIBLE
        }
    }

    private fun setGroupVisibility(visible: Boolean, vararg views: View) {
        val v = if (visible) View.VISIBLE else View.INVISIBLE
        for (view in views) {
            view.visibility = v
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
