package com.research.oproom
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if(Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            Log.d("Boot up", "Ahhh, you're finally awake")
            val i = Intent(context, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}