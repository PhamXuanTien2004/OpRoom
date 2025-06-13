package com.research.oproom

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import org.w3c.dom.Text

class PasswordDialog: DialogFragment() {
    private lateinit var buttonCancel: Button
    private lateinit var buttonOk: Button
    lateinit var passwordText: TextView
    private var password: String = ""
    interface OnInputListener{
        fun sendPassword(password: String)
    }
    lateinit var onInputListener: OnInputListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_password, container, false)
        buttonCancel = view.findViewById(R.id.buttonCancel_password)
        buttonOk = view.findViewById(R.id.buttonOK_password)
        passwordText = view.findViewById(R.id.textView_password)

        buttonCancel.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?){
                dialog?.dismiss()
            }
        })
        buttonOk.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onInputListener.sendPassword(password)
                dialog?.dismiss()
            }
        })
        passwordText.addTextChangedListener(object : TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                password = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            onInputListener = activity as OnInputListener
        }catch (e: ClassCastException){

        }
    }
}