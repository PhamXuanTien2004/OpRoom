package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.research.oproom.data.local.AppPreferences
import com.research.oproom.databinding.DialogSendLogBinding

/**
 * Hộp thoại cho phép người dùng nhập địa chỉ IP máy tính để gửi file log từ Tinker Board qua mạng.
 */
class SendLogDialog : DialogFragment() {

    interface OnSendLogListener {
        fun onSendLogToIp(ip: String)
    }

    private var listener: OnSendLogListener? = null
    private var _binding: DialogSendLogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSendLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = AppPreferences.getInstance(requireContext())
        val savedIp = prefs.targetIp
        if (savedIp.isNotEmpty()) {
            binding.editTextIpAddress.setText(savedIp)
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSend.setOnClickListener {
            val ip = binding.editTextIpAddress.text?.toString()?.trim() ?: ""
            if (ip.isNotEmpty()) {
                prefs.targetIp = ip
                listener?.onSendLogToIp(ip)
                dismiss()
            } else {
                binding.editTextIpAddress.error = "Vui lòng nhập địa chỉ IP"
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSendLogListener) {
            listener = context
        } else if (parentFragment is OnSendLogListener) {
            listener = parentFragment as OnSendLogListener
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

