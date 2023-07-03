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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private val barX = 0F
    private val barY = 0F
    private val barZ = 9.81F
    private val alpha = 0.1


    private lateinit var mViewModel: MainViewModel
    private var xSSE = 0F
    private var ySSE = 0F
    private var zSSE = 0F
    private var nSamples = 0



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
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        xSSE += (x - barX).pow(2)
        ySSE += (y - barY).pow(2)
        zSSE += (z - barZ).pow(2)
        nSamples++

        mViewModel.updateText("x: $x\ny: $y\nz: $z")

        if (nSamples == 3000) {
            i++

            val xRMSE = sqrt(xSSE / nSamples)
            val yRMSE = sqrt(ySSE / nSamples)
            val zRMSE = sqrt(zSSE / nSamples)

            if (xRMSE > alpha || yRMSE > alpha || zRMSE > alpha) {
                mViewModel.updateStatus("$i: Motion: $xRMSE, $yRMSE, $zRMSE")
            }
            else {
                mViewModel.updateStatus("$i: No Motion: $xRMSE, $yRMSE, $zRMSE")
            }

            nSamples = 0
            xSSE = 0F
            ySSE = 0F
            zSSE = 0F
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}
