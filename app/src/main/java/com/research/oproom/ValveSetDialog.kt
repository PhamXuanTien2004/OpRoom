package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ValveSetDialog(var value: Int): DialogFragment() {
    private lateinit var buttonCancel: Button
    private lateinit var buttonOk: Button
    lateinit var valve: SeekBar
    lateinit var text: TextView
    interface OnInputListener{
        fun sendValve(valve: Int)
    }
    lateinit var onInputListener: OnInputListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_valve, container, false)
        buttonCancel = view.findViewById(R.id.buttonCancel_valve)
        buttonOk = view.findViewById(R.id.buttonOK_valve)
        valve = view.findViewById(R.id.seekBar)
        text = view.findViewById(R.id.textView_valve)
        valve.progress = value
        text.text = value.toString()
        buttonCancel.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                dialog?.dismiss()
            }
        })
        buttonOk.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onInputListener.sendValve(valve.progress)
                dialog?.dismiss()
            }
        })
        valve.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                text.text = progress.toString()
                value = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onInputListener = activity as OnInputListener
        }catch (e: ClassCastException){

        }
    }
}