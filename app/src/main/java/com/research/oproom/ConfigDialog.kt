package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.research.oproom.databinding.DialogConfigBinding

class ConfigDialog(val save: Boolean = false) : DialogFragment() {

    interface OnInputListener {
        fun saveOrLoad(config: Int, save: Boolean)
    }

    var onInputListener: OnInputListener? = null
    private var _binding: DialogConfigBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonConfig1.setOnClickListener {
            onInputListener?.saveOrLoad(1, save)
            dismiss()
        }
        binding.buttonConfig2.setOnClickListener {
            onInputListener?.saveOrLoad(2, save)
            dismiss()
        }
        binding.buttonConfig3.setOnClickListener {
            onInputListener?.saveOrLoad(3, save)
            dismiss()
        }
        binding.buttonConfigCancel.setOnClickListener {
            dismiss()
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
