package com.example.delta_alarm.presentation

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.delta_alarm.R
import java.io.File
import java.io.FileOutputStream
import java.util.Timer
import java.util.TimerTask
import kotlin.math.pow

class AccelHandler(
    private val context: Context,
    private val sensorManager: SensorManager,
    private val mViewModel: MainViewModel,
    private val dir: File
) : SensorEventListener {
    private val samplingRateHz = 100
    private val samplingPeriodMicroseconds = (1e6 / samplingRateHz).toInt()
    private val motionDetectionThreshold = 100.0 // for now

    private val checkMotionTimeoutSecs = 60
    private var checkMotionTimer = Timer()
    private var checkingMotion = false
    private var sumX = 0.0
    private var sumY = 0.0
    private var sumZ = 0.0
    private var nSamples = 0

    private val loggingAccTimeoutSecs = 60 * 30 // 30 minutes
    private val loggingAccTimer = Timer()
    private var loggingAcc = false

    private var accFileStream = FileOutputStream(File(dir, "acc.csv"))

    fun init() {
        accFileStream.write("x,y,z".toByteArray())
    }

    private fun register() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            samplingPeriodMicroseconds
        )
    }

    private fun unregister() {
        sensorManager.unregisterListener(this)
    }

    fun handleOnDestroy() {
        unregister()
        accFileStream.close()
    }

    // Function to check if currently sensing any motion - asynchronous
    fun checkForMotion() {
        // Start timer - during timer, sum up squares of acc values and keep count
        // At end of timer, take mean of squares to detect motion
        Log.d("Deltaalarm", "AccelHandler - Checking for motion")

        // Start accelerometer
        register()

        // Set self into "checkingMotion" mode
        checkingMotion = true

        // Start Timer - when timer ends, results will be broadcast to MainActivity
        checkMotionTimer.schedule(CheckMotionTimerTask(), (checkMotionTimeoutSecs * 1e3).toLong())
    }

    private inner class CheckMotionTimerTask : TimerTask() {
        override fun run() {
            // Once set amount of time has passed, check if there was motion during this period

            Log.d("Deltaalarm", "AccelHandler - Check Motion Timer Task End")
            mViewModel.updateText("AccelHandler - Check Motion Timer Task End")

            // Check if Mean Square of sensors crosses threshold - TODO make this actually a thing
            var motionDetected = false
            if (
                sumX / nSamples > motionDetectionThreshold &&
                sumY / nSamples > motionDetectionThreshold &&
                sumZ / nSamples > motionDetectionThreshold
            ) {
                motionDetected = true
            }

            // Send broadcast to MainActivity with result and current logging state
            val i = Intent(context.getString(R.string.MOTION_DETECTION_BROADCAST))
            i.putExtra(context.getString(R.string.MotionDetected), motionDetected)
            i.putExtra(context.getString(R.string.IsLoggingAcc), loggingAcc)
            context.sendBroadcast(i)

            // Turn off checking-for-motion mode
            unregister()
            checkingMotion = false
        }
    }

    // Function to start writing acc values to file - asynchronous
    fun startLogging() {
        // Start timer and begin logging accelerometer values to file
        // At end of timer, send message to MainActivity to tell them time is up
        Log.d("Deltaalarm", "AccelHandler - Logging accelerometer values")

        // Register accelerometer listener
        register()

        // Put self into logging accelerometer to file mode
        loggingAcc = true

        // Start timer - when timer ends, MainActivity will be notified
        loggingAccTimer.schedule(StopLoggingAccTimerTask(), (loggingAccTimeoutSecs * 1e3).toLong())
    }
    fun stopLogging() {
        // Must manually stop logging once its started
        unregister()
        loggingAcc = false
    }

    private inner class StopLoggingAccTimerTask : TimerTask() {
        override fun run() {
            // Once set amount of time passes, send broadcast to MainActivity that time is up
            context.sendBroadcast(Intent(context.getString(R.string.STOP_LOGGING_BROADCAST)))
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        mViewModel.updateNSamples(mViewModel.nSamples + 1)
        val x = event.values[0].toDouble()
        val y = event.values[1].toDouble()
        val z = event.values[2].toDouble()

        // If checking-for-motion mode, save sum of vales and number of values for motion checking
        if (checkingMotion) {
            // update sums and counts
            Log.v("DeltaAcc", "Checking for Motion - x: ${event.values[0]}")
            sumX += x.pow(2)
            sumY += y.pow(2)
            sumZ += z.pow(2)
            nSamples += 1
        }

        // If logging mode, write values to file
        if (loggingAcc) {
            Log.v("DeltaAcc", "Recording Acc - x: ${event.values[0]}")
            accFileStream.write("$x,$y,$z".toByteArray())
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // do nothing
    }
}