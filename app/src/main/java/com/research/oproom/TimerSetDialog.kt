package com.research.oproom


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class TimerSetDialog(var inputHr: Int, var inputMin: Int): DialogFragment() {
    private lateinit var buttonCancel: Button
    private lateinit var buttonOK: Button
    lateinit var hrPicker: NumberPicker
    lateinit var minPicker: NumberPicker

    public interface OnInputListener{
        fun sendInput(inputHr: Int, inputMin: Int)
    }
    public lateinit var onInputListener: OnInputListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_timer_settings, container, false)
        buttonCancel = view.findViewById(R.id.buttonCancel)
        buttonOK = view.findViewById(R.id.buttonOK)
        hrPicker = view.findViewById(R.id.hrPicker)
        minPicker = view.findViewById(R.id.minPicker)
        val colon = view.findViewById<TextView>(R.id.colon)
        hrPicker.textSize = colon.textSize
        hrPicker.maxValue = 12
        hrPicker.value = inputHr
        hrPicker.selectionDividerHeight = 0
        hrPicker.setOnValueChangedListener(object : NumberPicker.OnValueChangeListener{
            override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
                inputHr = newVal
            }
        })
        minPicker.textSize = colon.textSize
        minPicker.maxValue = 59
        minPicker.value = inputMin
        minPicker.selectionDividerHeight = 0
        minPicker.setOnValueChangedListener(object : NumberPicker.OnValueChangeListener{
            override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
                inputMin = newVal
            }
        })

        buttonCancel.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View){
                dialog?.dismiss()
            }
        })
        buttonOK.setOnClickListener(object  : View.OnClickListener{
            override fun onClick(v: View?) {
                onInputListener.sendInput(inputHr, inputMin)
                dialog?.dismiss()
            }
        })
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onInputListener = activity as OnInputListener
        }catch(e: ClassCastException){
        }
    }
}