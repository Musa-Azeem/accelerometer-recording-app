package com.example.delta_alarm.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.delta_alarm.R

class CheckMotionAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Deltaalarm", "Event: Alarm ring")

        // Signal Main Activity
        context.sendBroadcast(Intent(context.getString(R.string.CHECK_MOTION_BROADCAST)))
    }
}