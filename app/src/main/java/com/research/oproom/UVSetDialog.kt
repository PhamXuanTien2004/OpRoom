package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.research.oproom.databinding.DialogUvSettingBinding

class UVSetDialog(var uvTime: Int = 10) : DialogFragment() {

    interface OnInputListener {
        fun sendUVTime(uvTime: Int)
    }

    var onInputListener: OnInputListener? = null
    private var _binding: DialogUvSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUvSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            uvPicker.textSize = textViewDummy.textSize
            uvPicker.minValue = 1
            uvPicker.maxValue = 20
            uvPicker.value = uvTime
            uvPicker.selectionDividerHeight = 0
            uvPicker.setOnValueChangedListener { _, _, newVal ->
                uvTime = newVal
            }

            buttonCancelUV.setOnClickListener {
                dismiss()
            }
            buttonOKUV.setOnClickListener {
                onInputListener?.sendUVTime(uvTime)
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
