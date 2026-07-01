package com.example.bulk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class StepTrackerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != "android.intent.action.QUICKBOOT_POWERON") return
        val prefs = context.getSharedPreferences("step_session_state", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("isRunning", false)) return
        val svc = Intent(context, StepTrackerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(svc)
        else
            context.startService(svc)
    }
}
