/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.delta_alarm.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.delta_alarm.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


var mainActivityIsActive = false

class MainActivity : ComponentActivity() {
    private lateinit var mViewModel: MainViewModel

    private val CHECK_MOTION_INTERVAL_MINS: Float = 5F
    private lateinit var checkMotionAlarm: CheckMotionAlarmManager
    private var checkMotionReceiver: BroadcastReceiver = MainCheckMotionAlarmReceiver()
    private var motionDetectionReceiver: BroadcastReceiver = MotionDetectionReceiver()
    private var stopLoggingAccReceiver: BroadcastReceiver = StopLoggingAccReceiver()

    private lateinit var accelHandler: AccelHandler

    private var startTimeStr = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())
    private lateinit var dir: File
    private lateinit var logFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Deltaalarm", "Event: OnCreate")

        mViewModel = MainViewModel()

        dir = File(this.filesDir, startTimeStr)
        dir.mkdir()
        logFile = File(dir, "Log.txt")
        logFile.appendText("real_time_ms,event\n")

        // Set alarm
        checkMotionAlarm = CheckMotionAlarmManager(this)
        setAlarm(CHECK_MOTION_INTERVAL_MINS)

        // Start all broadcast receivers
        startListening()

        // Initialize SensorHandler
        accelHandler = AccelHandler(
            applicationContext,
            getSystemService(SENSOR_SERVICE) as SensorManager,
            mViewModel,
            dir
        )

        // To ensure screen turns on on alarm ring
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

        val filter2 = IntentFilter(getString(R.string.MOTION_DETECTION_BROADCAST))
        registerReceiver(motionDetectionReceiver, filter2)

        val filter3 = IntentFilter(getString(R.string.STOP_LOGGING_BROADCAST))
        registerReceiver(stopLoggingAccReceiver, filter3)
    }

    // TODO can make one broadcast receiver and distinguish message with intent extra

    inner class MainCheckMotionAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("Deltaalarm", "Main Alarm ring")
            logFile.appendText("${Calendar.getInstance().timeInMillis},CheckingForMotion\n")

            vibrate()

            // Bring MainActivity into view
            if (!mainActivityIsActive) {
                Log.d("Deltaalarm", "Bringing MainActivity to foreground")
                var i = Intent(context, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.action = Intent.ACTION_MAIN
                i.addCategory(Intent.CATEGORY_LAUNCHER)
                startActivity(i)
            }

            // Lock Screen on and check for motion using accelerometer
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // lock screen on
            accelHandler.checkForMotion()   // start check for motion process
        }

    }

    inner class MotionDetectionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val motionDetected = intent.getBooleanExtra(
                getString(R.string.MotionDetected),
                false)

            val accelIsLogging = intent.getBooleanExtra(
                getString(R.string.IsLoggingAcc),
                false)

            Log.d("Deltaalarm", "Main Get Motion Detection Results: $motionDetected")

            if (motionDetected) {
                logFile.appendText("${Calendar.getInstance().timeInMillis},MotionDetected\n")
                // Keep wake lock and, if not already logging acc,
                if (!accelIsLogging) {
                    // start logging accelerometer data
                    accelHandler.startLogging()
                }
            }
            else {
                logFile.appendText("${Calendar.getInstance().timeInMillis},NoMotionDetected\n")
                if (accelIsLogging) {
                    // if accel is still logging, stop it
                    accelHandler.stopLogging()
                }
                // let go of wake lock and reset alarm
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                setAlarm(CHECK_MOTION_INTERVAL_MINS)
                Log.d("Deltaalarm", "Reset Alarm - Next Alarm: ${checkMotionAlarm.test()}")
            }
        }
    }

    inner class StopLoggingAccReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // When logging timer goes off, check again if any motion
            logFile.appendText("${Calendar.getInstance().timeInMillis},CheckingForMotion\n")
            accelHandler.checkForMotion()
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
        accelHandler.handleOnDestroy()
        unregisterReceiver(checkMotionReceiver)
        unregisterReceiver(motionDetectionReceiver)
    }
}