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
import com.research.oproom.databinding.ActivitySettingBinding
import com.research.oproom.ui.components.RealTimeClockHelper
import com.research.oproom.ui.components.SlideShowHelper
import com.research.oproom.ui.main.MainViewModel
import kotlin.math.roundToInt

/**
 * Màn hình trình chiếu slide ảnh ca mổ và đồng hồ đếm phụ (Trang 2).
 * Đồng bộ hai chiều với MainViewModel.
 */
class PresentationFragment : Fragment() {

    private var _binding: ActivitySettingBinding? = null
    val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private var clockHelper: RealTimeClockHelper? = null
    private var slideShowHelper: SlideShowHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClockAndSlideShow()
        setupClicks()
        observeViewModel()
    }

    private fun setupClockAndSlideShow() {
        clockHelper = RealTimeClockHelper(
            "2021", "00:00",
            binding.textViewDate,
            binding.textViewTime
        ).apply {
            updateText()
            start()
        }

        slideShowHelper = SlideShowHelper(
            binding.buttonNextP,
            binding.buttonBackP,
            binding.imageViewInfo,
            binding.buttonSlideShow
        )
    }

    private fun setupClicks() {
        val act = activity as? MainActivity

        binding.button.setOnClickListener { act?.navigateToPage(0) }
        binding.imageButton2.setOnClickListener { act?.hideSystemBars() }

        // Bắt đầu / Tạm dừng / Reset bộ đếm giờ
        binding.imageButtonStartOp.setOnClickListener { viewModel.toggleOpTimer() }
        binding.imageButtonStartAn.setOnClickListener { viewModel.toggleAnTimer() }
        binding.buttonReset.setOnClickListener { viewModel.resetTimers() }

        // Mini lights
        binding.miniLight1.setOnClickListener { viewModel.toggleLight1() }
        binding.miniLight2.setOnClickListener { viewModel.toggleLight2() }

        // Slide show
        binding.buttonInfo.setOnClickListener { slideShowHelper?.emerge() }
        binding.imageViewInfo.setOnClickListener { slideShowHelper?.submerge() }
    }

    private fun observeViewModel() {
        // Cảm biến mini
        viewModel.tempRaw.observe(viewLifecycleOwner) { raw ->
            val formatted = (raw.toFloat() / 10f).toString().padStart(4, '0')
            binding.textViewSensorTempMini.text = formatted
        }

        viewModel.humRaw.observe(viewLifecycleOwner) { raw ->
            val formatted = (raw.toFloat() / 10f).toString().padStart(4, '0')
            binding.textViewSensorHumMini.text = formatted
        }

        viewModel.presRaw.observe(viewLifecycleOwner) { raw ->
            val formatted = (raw.toFloat() * 100 / 8192).roundToInt().toString().padStart(2, '0')
            binding.textViewSensorPresMini.text = formatted
        }

        // Mini Light 1
        viewModel.light1State.observe(viewLifecycleOwner) { on ->
            binding.miniLight1.setImageResource(if (on) R.drawable.light_on else R.drawable.light_off)
            try {
                binding.miniLight1.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = if (on) 0.32f else 0.33f
                }
            } catch (_: Exception) {}
        }

        // Mini Light 2
        viewModel.light2State.observe(viewLifecycleOwner) { on ->
            binding.miniLight2.setImageResource(if (on) R.drawable.light_on else R.drawable.light_off)
            try {
                binding.miniLight2.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = if (on) 0.32f else 0.33f
                }
            } catch (_: Exception) {}
        }

        // Đồng hồ đếm Op
        viewModel.opRunning.observe(viewLifecycleOwner) { running ->
            binding.imageButtonStartOp.setImageResource(if (running) R.drawable.pause_count else R.drawable.start_count)
        }

        viewModel.opSec.observe(viewLifecycleOwner) { s ->
            binding.textViewOpSec.text = s.toString().padStart(2, '0')
        }
        viewModel.opMin.observe(viewLifecycleOwner) { m ->
            binding.textViewOpMin.text = m.toString().padStart(2, '0')
        }
        viewModel.opColon.observe(viewLifecycleOwner) { showColon ->
            val h = viewModel.opHr.value ?: 0
            val colon = if (showColon) ":" else " "
            binding.textViewOpHour.text = "${h.toString().padStart(2, '0')}$colon"
        }

        // Đồng hồ đếm An
        viewModel.anRunning.observe(viewLifecycleOwner) { running ->
            binding.imageButtonStartAn.setImageResource(if (running) R.drawable.pause_count else R.drawable.start_count)
        }

        viewModel.anSec.observe(viewLifecycleOwner) { s ->
            binding.textViewAnSec.text = s.toString().padStart(2, '0')
        }
        viewModel.anMin.observe(viewLifecycleOwner) { m ->
            binding.textViewAnMin.text = m.toString().padStart(2, '0')
        }
        viewModel.anColon.observe(viewLifecycleOwner) { showColon ->
            val h = viewModel.anHr.value ?: 0
            val colon = if (showColon) ":" else " "
            binding.textViewAnHour.text = "${h.toString().padStart(2, '0')}$colon"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clockHelper?.stop()
        slideShowHelper?.stopSlideShow()
        clockHelper = null
        slideShowHelper = null
        _binding = null
    }
}
