package com.example.delta_alarm.presentation

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import android.widget.Toast

class CheckMotionJobService : JobService() {
    override fun onStartJob(jobParams: JobParameters): Boolean {
        Log.d("DeltaAlarm", "Event: CheckMotionJobService.onStartJob")
        Toast.makeText(this, "ALarm", Toast.LENGTH_SHORT).show()
        return false    // stopping job is handled within this function so return false
    }

    override fun onStopJob(jobParams: JobParameters): Boolean {
        // return true so that job is rescheduled if it fails
        return true
    }

}