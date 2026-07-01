package com.example.bulk

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import java.util.concurrent.TimeUnit

class StepTrackerWatchdog(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("step_session_state", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("isRunning", false)) return Result.success()
        if (StepTrackerService.isRunning.value) return Result.success()
        val svc = Intent(applicationContext, StepTrackerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            applicationContext.startForegroundService(svc)
        else
            applicationContext.startService(svc)
        return Result.success()
    }

    companion object {
        private const val TAG = "step_watchdog"

        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<StepTrackerWatchdog>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .addTag(TAG)
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                TAG, ExistingPeriodicWorkPolicy.UPDATE, req)
        }

        fun cancel(ctx: Context) {
            WorkManager.getInstance(ctx).cancelAllWorkByTag(TAG)
        }
    }
}
