/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.delta_alarm.presentation

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.delta_alarm.R
import com.example.delta_alarm.presentation.theme.DeltaalarmTheme

class MainActivity : ComponentActivity() {
    private lateinit var mViewModel: MainViewModel
    private lateinit var checkMotionJobScheduler: JobScheduler
    private val CHECK_MOTION_JOB_ID = 0
    private val CHECK_MOTION_INTERVAL_MINS = 15 // minimum is 15
        // workaround - dont use periodic - instead reschedule job each time its called

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = MainViewModel()
        scheduleCheckMotionJob()


        setContent {
            WearApp(mViewModel)
        }
    }
    private fun scheduleCheckMotionJob() {
        val checkMotionServiceName = ComponentName(applicationContext, CheckMotionJobService::class.java)
        val checkMotionJobBuilder = JobInfo.Builder(CHECK_MOTION_JOB_ID, checkMotionServiceName)
            .setPeriodic((CHECK_MOTION_INTERVAL_MINS * 60e3).toLong(), (0.1 * 60e3).toLong())
//            .setExtras(bundle)
        val checkMotionJobInfo = checkMotionJobBuilder.build()

        checkMotionJobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val status = checkMotionJobScheduler.schedule(checkMotionJobInfo)
        Log.d("DeltaAlarm", "Event: MainActivity.scheduleCheckMotionJob - status $status")
    }

    private fun cancelCheckMotionJob() {
        checkMotionJobScheduler.cancelAll()
    }
}