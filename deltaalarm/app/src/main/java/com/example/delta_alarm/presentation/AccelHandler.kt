package com.example.delta_alarm.presentation

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.util.Log
import com.example.delta_alarm.R
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import kotlin.math.pow

class AccelHandler(
    private val context: Context,
    private val sensorManager: SensorManager,
    private val mViewModel: MainViewModel,
    private val dir: File,
    private val loggingAccTimeoutSecs: Int = 60 * 30, // 30 minutes
    private val checkMotionTimeoutSecs: Int = 60
) : SensorEventListener {

    // Accelerometer Parameters
    private val samplingRateHz = 100
    private val samplingPeriodMicroseconds = (1e6 / samplingRateHz).toInt()

    // Logging Accelerometer Mode
    private val loggingAccTimer = Timer()
    private var loggingAcc = false
    private var accFileStream = FileOutputStream(File(dir, "acc.csv"))

    // Checking for Motion Mode
    private var checkMotionTimer = Timer()
    private var checkingMotion = false
    private var motionDetectedByTrigger = false

    init {
        accFileStream.write("time_ns,x,y,z,realtime_ms\n".toByteArray())
    }

    private fun register() {
        /*
         * Function to register accelerometer and start receiving events
         * Uses samplingPeriodMicroseconds, defined in class constructor, as sampling period
         */
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            samplingPeriodMicroseconds
        )
    }

    private fun unregister() {
        /*
         * Function to unregister accelerometer and stop receiving events
         */
        sensorManager.unregisterListener(this)
    }

    fun handleOnDestroy() {
        /*
         * Public function to handle cleanup when MainActivity is destroyed
         * It unregisters the accelerometer in case it is running, and closes the acc file stream
         */
        unregister()
        accFileStream.close()
    }

    fun checkForMotion() {
        /*
         * "Asynchronous" function to put self into check-for-motion mode
         *  1) Set the trigger sensor to listen for significant motion and save results if it does
         *     detect any - this logic defined in the SignificantMotionListener inner class
         *  2) Wait 'checkMotionTimeoutSecs' seconds by starting a CheckMotionTimer
         *  3) Send broadcast to MainActivity indicating whether or not the trigger sensor detected
         *     significant motion - this logic is defined in the CheckMotionTimer TimerTask class
         */

        Log.d("Deltaalarm", "AccelHandler - Checking for motion")

        // Set self into "checkingMotion" mode
        checkingMotion = true

        // Set Trigger Sensor to detect significant motion - will save result if detected
        setTriggerSensor()

        // Start Timer - when timer ends, results will be broadcast to MainActivity
        checkMotionTimer.schedule(CheckMotionTimerTask(), (checkMotionTimeoutSecs * 1e3).toLong())
    }

    private inner class CheckMotionTimerTask : TimerTask() {
        /*
         * This inner class defines the task to run at the end of the check-for-motion mode
         * The run function will be called when set period ends
         */
        override fun run() {
            /*
             * Function to run at the end of the check-for-motion mode
             *  - Once set amount of time has passed, let MainActivity know if any significant motion
             *    was detected by the trigger sensor
             *  - Also tells MainActivity whether or not self is currently recording accelerometer data
             */

            Log.d("Deltaalarm", "AccelHandler - Check Motion Timer Task End")

            // Send broadcast to MainActivity with result and current logging state
            val i = Intent(context.getString(R.string.MOTION_DETECTION_BROADCAST))
            i.putExtra(context.getString(R.string.MotionDetected), motionDetectedByTrigger)
            i.putExtra(context.getString(R.string.IsLoggingAcc), loggingAcc)
            context.sendBroadcast(i)

            // Turn off checking-for-motion mode
            motionDetectedByTrigger = false
            checkingMotion = false
        }
    }

    fun startLogging() {
        /*
         * Function to start writing accelerometer values to a file
         *  1) Puts self into logging-acc mode and registers accelerometer, writing values to a file
         *  2) Waits 'loggingAccTimeoutSecs' seconds
         *  3) Tells MainActivity timer is complete - logic defined in LoggingAccTimer inner class
         *
         *  Note: This function does not stop logging accelerometer values, it just notifies MainActivity
         *        that it should stop
         */

        Log.d("Deltaalarm", "AccelHandler - Logging accelerometer values")

        // Register accelerometer listener
        register()

        // Put self in logging-acc mode
        loggingAcc = true

        // Start timer - when timer ends, MainActivity will be notified
        loggingAccTimer.schedule(StopLoggingAccTimerTask(), (loggingAccTimeoutSecs * 1e3).toLong())
    }
    fun continueLogging() {
        /*
         * Function to continue logging when motion is detected while already logging
         * It only starts the stop logging acc timer, since object is already in loggin state
         */

        loggingAccTimer.schedule(StopLoggingAccTimerTask(), (loggingAccTimeoutSecs * 1e3).toLong())
    }
    fun stopLogging() {
        /*
         * Function to stop logging accelerometer values to file
         * Unregisters accelerometer and turns off logging-acc mode
         */

        unregister()
        loggingAcc = false
    }

    private inner class StopLoggingAccTimerTask : TimerTask() {
        /*
         * This inner class defines the task to run at the end of logging-acc mode
         * The run function will be called when set period ends
         */
        override fun run() {
            /*
             * Function to run at end of set amount of time
             * Sends broadcast to MainActivity indicating that timer for acc-logging mode has completed
             */
            context.sendBroadcast(Intent(context.getString(R.string.STOP_LOGGING_BROADCAST)))
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        /*
         * Callback function for new accelerometer values
         *
         * logging-acc mode:
         *      When loggingAcc is true (ie., self is in logging-acc mode), this function writes
         *      all accelerometer values to a file
         */

        mViewModel.updateNSamples(mViewModel.nSamples + 1)
        val x = event.values[0].toDouble()
        val y = event.values[1].toDouble()
        val z = event.values[2].toDouble()

        // If logging mode, write values to file
        if (loggingAcc) {
            Log.v("DeltaAcc", "Recording Acc - x: ${event.values[0]}")
            accFileStream.write("${event.timestamp},$x,$y,$z,${Calendar.getInstance().timeInMillis}\n".toByteArray())
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // do nothing
    }

    private fun setTriggerSensor() {
        /*
         * Sets trigger sensor to listen for significant motion, with a callback function defined in
         * the SignificantMotionListener inner class
         */

        val triggerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        sensorManager.requestTriggerSensor(SignificantMotionListener(), triggerSensor)
    }

    inner class SignificantMotionListener : TriggerEventListener() {
        /*
         * This inner class defines the task to run if the trigger sensor detects significant motion
         * The function onTrigger is called if there is signicant motion detected
         */

        override fun onTrigger(triggerEvent: TriggerEvent) {
            /*
             * If the trigger sensor detects significant motion and self is in checking-for-motion
             *  mode, this function sets motionDetectedByTrigger to true
             * When CheckMotionTimerTask is called at the end of checking-for-motion mode,
             *  this result will be shared with MainActivity
             */

            Log.d("Deltaalarm", "AccelHandler - Significant Motion Detected")
            if (checkingMotion) {
                motionDetectedByTrigger = true
            }
        }

    }
}