/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.delta_alarm.presentation

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.delta_alarm.R


var mainActivityIsActive = false
class MainActivity : ComponentActivity() {
    private lateinit var mViewModel: MainViewModel
    private val CHECK_MOTION_INTERVAL_MINS: Float = 5F
    private lateinit var checkMotionAlarm: CheckMotionAlarmManager

    private var checkMotionReceiver: BroadcastReceiver = MainCheckMotionAlarmReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Deltaalarm", "Event: OnCreate")

        mViewModel = MainViewModel()
        checkMotionAlarm = CheckMotionAlarmManager(this)

        startListening()

        setAlarm(CHECK_MOTION_INTERVAL_MINS)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setContent {
            WearApp(mViewModel)
        }
    }
    private fun setAlarm(delayMins: Float) {
        checkMotionAlarm.setAlarm(delayMins, "")
    }
    private fun startListening() {
        val filter = IntentFilter(getString(R.string.CHECK_MOTION_BROADCAST))
        registerReceiver(checkMotionReceiver, filter)
    }

    inner class MainCheckMotionAlarmReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("Deltaalarm", "Main Alarm ring")
            vibrate()

            var km: KeyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this@MainActivity, null)

            // Get wake lock -> check for activtiy -> reset alarm or get wakelock for 30 mins
//            val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
//            val wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "deltaalarm:lock")
//            wl.acquire(10*60*1000L /*10 minutes*/)




            // Bring MainActivity into view
//            var i = Intent(context, MainActivity::class.java)
//            i.action = Intent.ACTION_MAIN
//            i.addCategory(Intent.CATEGORY_LAUNCHER)
//            startActivity(i)


            if (!mainActivityIsActive) {
//                var i = Intent(context, MainActivity::class.java)
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
//                startActivity(i)
                Log.d("Deltaalarm", "Bringing MainActivity to foreground")
                var i = Intent(context, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.action = Intent.ACTION_MAIN
                i.addCategory(Intent.CATEGORY_LAUNCHER)
                startActivity(i)
            }

            // reset alarm
            setAlarm(CHECK_MOTION_INTERVAL_MINS)
            checkMotionAlarm.test()
        }

    }

    fun vibrate() {
        // onTrigger, vibrate watch for 1s
        var vibrator: Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            var vibratorManager: VibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot((1 * 1e3).toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onResume() {
        super.onResume()
        mainActivityIsActive = true
    }
    override fun onPause() {
        super.onPause()
        mainActivityIsActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(checkMotionReceiver)
    }
}