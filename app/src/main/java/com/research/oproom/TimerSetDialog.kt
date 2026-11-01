package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.research.oproom.databinding.DialogTimerSettingsBinding

class TimerSetDialog(var inputHr: Int = 0, var inputMin: Int = 0) : DialogFragment() {

    interface OnInputListener {
        fun sendInput(inputHr: Int, inputMin: Int)
    }

    var onInputListener: OnInputListener? = null
    private var _binding: DialogTimerSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTimerSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            hrPicker.textSize = colon.textSize
            hrPicker.minValue = 0
            hrPicker.maxValue = 12
            hrPicker.value = inputHr
            hrPicker.selectionDividerHeight = 0
            hrPicker.setOnValueChangedListener { _, _, newVal ->
                inputHr = newVal
            }

            minPicker.textSize = colon.textSize
            minPicker.minValue = 0
            minPicker.maxValue = 59
            minPicker.value = inputMin
            minPicker.selectionDividerHeight = 0
            minPicker.setOnValueChangedListener { _, _, newVal ->
                inputMin = newVal
            }

            buttonCancel.setOnClickListener {
                dismiss()
            }
            buttonOK.setOnClickListener {
                onInputListener?.sendInput(inputHr, inputMin)
                dismiss()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnInputListener) {
            onInputListener = context
        } else if (parentFragment is OnInputListener) {
            onInputListener = parentFragment as OnInputListener
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
