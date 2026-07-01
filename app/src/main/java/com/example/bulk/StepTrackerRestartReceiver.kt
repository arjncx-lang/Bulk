package com.example.bulk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class StepTrackerRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("step_session_state", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("isRunning", false)) return
        val svc = Intent(context, StepTrackerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(svc)
        else
            context.startService(svc)
    }
}
