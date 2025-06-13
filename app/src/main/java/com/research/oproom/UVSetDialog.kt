package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class UVSetDialog(var uvTime: Int): DialogFragment() {
    private lateinit var buttonCancel: Button
    private lateinit var buttonOK: Button
    lateinit var uvPicker: NumberPicker

    public interface OnInputListener{
        fun sendUVTime(uvTime: Int)
    }
    public lateinit var onInputListener: OnInputListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_uv_setting, container, false)
        buttonCancel = view.findViewById(R.id.buttonCancel_UV)
        buttonOK = view.findViewById(R.id.buttonOK_UV)
        uvPicker = view.findViewById(R.id.uvPicker)
        val dummy = view.findViewById<TextView>(R.id.textViewDummy)
        uvPicker.textSize = dummy.textSize
        uvPicker.maxValue = 20
        uvPicker.minValue = 1
        uvPicker.value = uvTime
        uvPicker.selectionDividerHeight = 0
        uvPicker.setOnValueChangedListener(object : NumberPicker.OnValueChangeListener{
            override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
                uvTime = newVal
            }
        })
        buttonCancel.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View){
                dialog?.dismiss()
            }
        })
        buttonOK.setOnClickListener(object  : View.OnClickListener{
            override fun onClick(v: View?) {
                onInputListener.sendUVTime(uvTime)
                dialog?.dismiss()
            }
        })
        return view
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onInputListener = activity as UVSetDialog.OnInputListener
        }catch(e: ClassCastException){
        }
    }
}