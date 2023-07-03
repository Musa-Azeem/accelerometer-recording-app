/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.acc_listener.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private val bardvx = 0.0
    private val bardvy = 0.0
    private val bardvz = 0.0
    private val alpha = 0.1


    private lateinit var mViewModel: MainViewModel
    private var dvxSSE = 0.0
    private var dvySSE = 0.0
    private var dvzSSE = 0.0
    private var nSamples = 0
    private var lastTime = 0L

    private var i = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = MainViewModel()
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), (1e6 / 100).toInt())

        setContent {
            WearApp(mViewModel)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
//        if (lastTime == 0L) {
//            // First time in this timeframe - need initial time
//            lastTime = event.timestamp
//            return
//        }
        val ax = event.values[0].toDouble()
        val ay = event.values[1].toDouble()
        val az = event.values[2].toDouble()

        // High pass filter to exclude gravity


//        val dt = (event.timestamp - lastTime) / 1e9
//        lastTime = event.timestamp
//
//        // for x,y,z: SSE = SSE + (dv - bar_dv)^2, where dv = a * dt
//        dvxSSE += ((ax * dt) - bardvx).pow(2)
//        dvySSE += ((ay * dt) - bardvy).pow(2)
//        dvzSSE += ((az * dt) - bardvz).pow(2)
//        nSamples++

        mViewModel.updateText("ax: ${ax}\nay: $ay\naz: $az")

//        if (nSamples == 3000) {
//            i++
//
//            Log.d("Test", "$dvxSSE, $dvySSE, $dvzSSE, $nSamples")
//            val dvxRMSE = sqrt(dvxSSE / nSamples)
//            val dvyRMSE = sqrt(dvySSE / nSamples)
//            val dvzRMSE = sqrt(dvzSSE / nSamples)
//
//            if (dvxRMSE > alpha || dvyRMSE > alpha || dvzRMSE > alpha) {
//                mViewModel.updateStatus("$i: Motion: $dvxRMSE, $dvyRMSE, $dvzRMSE")
//            }
//            else {
//                mViewModel.updateStatus("$i: No Motion: $dvxRMSE, $dvyRMSE, $dvzRMSE")
//            }
//
//            nSamples = 0
//            dvxSSE = 0.0
//            dvySSE = 0.0
//            dvzSSE = 0.0
//        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}
