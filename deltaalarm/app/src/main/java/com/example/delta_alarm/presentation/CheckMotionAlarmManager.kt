package com.example.delta_alarm.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class CheckMotionAlarmManager(context: Context){
    private val context: Context = context
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setAlarm(delayMins: Float, dir: String) {
        var intent = Intent(context, CheckMotionAlarmReceiver::class.java)
        intent.putExtra("dir", dir)
        var pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // This alarm should wake device up from sleep, but can only be set to go off once every 15 minutes
        // while device is in doze mode
        // alarmManager.setExactAndAllowWhileIdle()

        // This alarm should go off when in doze mode
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                System.currentTimeMillis() + (delayMins * 60e3).toLong(),
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            ),
            pi
        )
    }

    fun cancelAlarm() {

    }

    fun test() : Double {
        return if (alarmManager.nextAlarmClock != null) {
            (alarmManager.nextAlarmClock.triggerTime - System.currentTimeMillis())*1e-3
        } else {
            0.0
        }
    }
}