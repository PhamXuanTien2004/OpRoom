package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class ConfigDialog(val save: Boolean): DialogFragment() {
    public interface OnInputListener{
        fun saveOrLoad(config: Int, save: Boolean)
    }
    public lateinit var onInputListener: OnInputListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        val view = inflater.inflate(R.layout.dialog_config, container, false)
        val buttonConfig1 = view.findViewById<Button>(R.id.buttonConfig1)
        val buttonConfig2 = view.findViewById<Button>(R.id.buttonConfig2)
        val buttonConfig3 = view.findViewById<Button>(R.id.buttonConfig3)
        val buttonCancel = view.findViewById<Button>(R.id.button_configCancel)
        buttonConfig1.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onInputListener.saveOrLoad(1, save)
                dialog?.dismiss()
            }
        })
        buttonConfig2.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onInputListener.saveOrLoad(2, save)
                dialog?.dismiss()
            }
        })
        buttonConfig3.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onInputListener.saveOrLoad(3, save)
                dialog?.dismiss()
            }
        })
        buttonCancel.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
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