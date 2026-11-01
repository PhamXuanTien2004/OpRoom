package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.research.oproom.databinding.DialogPasswordBinding

class PasswordDialog : DialogFragment() {

    interface OnInputListener {
        fun sendPassword(password: String)
    }

    var onInputListener: OnInputListener? = null
    private var _binding: DialogPasswordBinding? = null
    private val binding get() = _binding!!
    private var password: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonCancelPassword.setOnClickListener {
            dismiss()
        }
        binding.buttonOKPassword.setOnClickListener {
            onInputListener?.sendPassword(password)
            dismiss()
        }
        binding.textViewPassword.doAfterTextChanged { editable ->
            password = editable?.toString() ?: ""
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
