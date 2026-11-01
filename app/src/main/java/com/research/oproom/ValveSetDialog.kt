package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.research.oproom.databinding.DialogValveBinding

class ValveSetDialog(var value: Int = 0) : DialogFragment() {

    interface OnInputListener {
        fun sendValve(valve: Int)
    }

    var onInputListener: OnInputListener? = null
    private var _binding: DialogValveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogValveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            seekBar.progress = value
            textViewValve.text = value.toString()
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    textViewValve.text = progress.toString()
                    value = progress
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}

                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

            buttonCancelValve.setOnClickListener {
                dismiss()
            }
            buttonOKValve.setOnClickListener {
                onInputListener?.sendValve(seekBar.progress)
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
