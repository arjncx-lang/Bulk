package com.example.bulk

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

class StepTrackerRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("step_session_state", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("isRunning", false)) return
        if (!StepTrackerService.isRunning.value) {
            val svc = Intent(context, StepTrackerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(svc)
            else
                context.startService(svc)
        }
        // Keep the heartbeat alive as long as the session is meant to be running,
        // so a killed service is noticed within ~1 minute instead of the 15 min
        // WorkManager watchdog floor.
        scheduleHeartbeat(context)
    }

    companion object {
        private const val HEARTBEAT_REQUEST_CODE = 99
        private const val HEARTBEAT_INTERVAL_MS = 60_000L

        private fun pendingIntent(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
            ctx, HEARTBEAT_REQUEST_CODE,
            Intent(ctx, StepTrackerRestartReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun scheduleHeartbeat(ctx: Context) {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + HEARTBEAT_INTERVAL_MS,
                pendingIntent(ctx)
            )
        }

        fun cancelHeartbeat(ctx: Context) {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pendingIntent(ctx))
        }
    }
}
