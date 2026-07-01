package com.example.bulk

import android.app.*
import android.content.Intent
import android.hardware.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.bulk.data.StepEntry
import com.example.bulk.data.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepTrackerService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID   = "step_tracker_v2"
        const val NOTIF_ID     = 42
        const val ACTION_PAUSE = "com.example.bulk.PAUSE"
        const val ACTION_STOP  = "com.example.bulk.STOP"
        const val ACTION_ADD   = "com.example.bulk.ADD_STEPS"
        const val ACTION_RESET = "com.example.bulk.RESET"
        const val EXTRA_STEPS  = "extra_steps"

        private val _steps       = MutableStateFlow(0)
        val steps       = _steps.asStateFlow()

        private val _isRunning   = MutableStateFlow(false)
        val isRunning   = _isRunning.asStateFlow()

        private val _isPaused    = MutableStateFlow(false)
        val isPaused    = _isPaused.asStateFlow()

        private val _accActiveMs = MutableStateFlow(0L)
        val accActiveMs = _accActiveMs.asStateFlow()

        private val _segStartMs  = MutableStateFlow(0L)
        val segStartMs  = _segStartMs.asStateFlow()

        private val _hasSensor   = MutableStateFlow(true)
        val hasSensor   = _hasSensor.asStateFlow()
    }

    private val prefs get() = getSharedPreferences("step_session_state", MODE_PRIVATE)

    private var sensorManager: SensorManager? = null
    private var activeSensor: Sensor?  = null
    private var usingDetector          = false   // true when falling back to step_detector
    private var wakeLock: PowerManager.WakeLock? = null

    private var sessionBaseline    = -1L
    private var pauseOffset        = 0
    private var todayBase          = 0
    private var sessionSensorSteps = 0
    private var todayKey           = ""

    override fun onCreate() {
        super.onCreate()
        createChannel()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        activeSensor  = chooseSensor()
        _hasSensor.value = activeSensor != null
    }

    /**
     * Preference order (this device's step_count is non-wake-up so events stop with screen off):
     * 1. Wake-up step counter  – cumulative, survives screen-off
     * 2. Wake-up step detector – fires per-step, survives screen-off, we count manually
     * 3. Non-wake-up step counter – works only with screen on
     */
    private fun chooseSensor(): Sensor? {
        val sm = sensorManager ?: return null
        // Try wake-up step counter (API 21+)
        sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER, true)?.let {
            usingDetector = false; return it
        }
        // Try wake-up step detector
        sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR, true)?.let {
            usingDetector = true; return it
        }
        // Fall back to non-wake-up step counter
        sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let {
            usingDetector = false; return it
        }
        return null
    }

    private fun registerSensor() {
        val sensor = activeSensor ?: return
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent == null               -> restoreOrStart()   // system restarted after kill
            intent.action == ACTION_PAUSE -> togglePause()
            intent.action == ACTION_STOP  -> stopSession()
            intent.action == ACTION_ADD   -> manualAdjust(intent.getIntExtra(EXTRA_STEPS, 0))
            intent.action == ACTION_RESET -> resetToday()
            else                          -> startSession()    // user tapped Start (action is null)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Fallback: if OEM kills us anyway, schedule a restart in 1 second
        if (_isRunning.value) scheduleRestart()
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        releaseWakeLock()
        super.onDestroy()
    }

    private fun scheduleRestart() {
        val pi = PendingIntent.getBroadcast(
            this, 0,
            Intent(this, StepTrackerRestartReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1_000L, pi)
    }

    // ── Session lifecycle ────────────────────────────────────────────────────

    private fun startSession() {
        if (_isRunning.value) return
        // If there's a persisted session (service was killed mid-session), restore it
        if (prefs.getBoolean("isRunning", false)) {
            restoreSession(); return
        }
        val repo       = StepRepository(this)
        todayKey       = repo.todayKey()
        todayBase      = repo.todayTotal()
        sessionBaseline    = -1L
        pauseOffset        = 0
        sessionSensorSteps = 0
        _steps.value       = todayBase
        _isRunning.value   = true
        _isPaused.value    = false
        _accActiveMs.value = 0L
        _segStartMs.value  = SystemClock.elapsedRealtime()
        saveState()
        acquireWakeLock()
        registerSensor()
        StepTrackerWatchdog.schedule(this)
        val notif = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIF_ID, notif, 0x00000100)
        else startForeground(NOTIF_ID, notif)
    }

    /** Called by Android when it restarts the service via START_STICKY (intent is null). */
    private fun restoreOrStart() {
        if (prefs.getBoolean("isRunning", false)) restoreSession()
    }

    private fun restoreSession() {
        todayKey           = prefs.getString("todayKey", "") ?: ""
        todayBase          = prefs.getInt("todayBase", 0)
        sessionSensorSteps = prefs.getInt("sessionSensorSteps", 0)
        sessionBaseline    = -1L
        pauseOffset        = sessionSensorSteps
        _steps.value       = prefs.getInt("steps", 0)
        _isRunning.value   = true
        _isPaused.value    = prefs.getBoolean("isPaused", false)
        _accActiveMs.value = prefs.getLong("accActiveMs", 0L)
        _segStartMs.value  = if (!_isPaused.value) SystemClock.elapsedRealtime() else 0L
        if (!_isPaused.value) acquireWakeLock()
        registerSensor()
        StepTrackerWatchdog.schedule(this)
        val notif = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIF_ID, notif, 0x00000100)
        else startForeground(NOTIF_ID, notif)
    }

    private fun togglePause() {
        if (!_isRunning.value) return
        if (!_isPaused.value) {
            if (_segStartMs.value > 0L) {
                _accActiveMs.value += SystemClock.elapsedRealtime() - _segStartMs.value
                _segStartMs.value = 0L
            }
            _isPaused.value = true
            releaseWakeLock()
        } else {
            pauseOffset       = sessionSensorSteps
            sessionBaseline   = -1L
            _segStartMs.value = SystemClock.elapsedRealtime()
            _isPaused.value   = false
            acquireWakeLock()
        }
        saveState()
        updateNotification()
    }

    private fun stopSession() {
        val finalMs = if (_segStartMs.value > 0L)
            _accActiveMs.value + SystemClock.elapsedRealtime() - _segStartMs.value
        else _accActiveMs.value
        if (sessionSensorSteps > 0) {
            StepRepository(this).addEntry(StepEntry(
                id = System.currentTimeMillis(), dateKey = todayKey,
                steps = sessionSensorSteps, distanceMeters = sessionSensorSteps * 0.762f,
                durationMs = finalMs, source = "sensor"))
        }
        sensorManager?.unregisterListener(this)
        releaseWakeLock()
        _isRunning.value = false; _isPaused.value = false; _steps.value = 0
        _accActiveMs.value = 0L; _segStartMs.value = 0L
        sessionBaseline = -1L; pauseOffset = 0; sessionSensorSteps = 0; todayBase = 0
        clearState()
        StepTrackerWatchdog.cancel(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun manualAdjust(delta: Int) {
        if (delta == 0) return
        val repo = StepRepository(this)
        repo.addManual(todayKey, delta)
        todayBase = repo.todayTotal()
        _steps.value = (todayBase + sessionSensorSteps).coerceAtLeast(0)
        saveState()
        updateNotification()
    }

    private fun resetToday() {
        val repo = StepRepository(this)
        repo.deleteAllForDate(if (todayKey.isNotEmpty()) todayKey else repo.todayKey())
        todayBase = 0; sessionBaseline = -1L; pauseOffset = 0; sessionSensorSteps = 0
        _steps.value = 0
        saveState()
        if (_isRunning.value) updateNotification()
    }

    // ── Sensor ───────────────────────────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent) {
        if (!_isRunning.value || _isPaused.value) return
        if (usingDetector) {
            // step_detector fires event.values[0] = 1.0 for every step
            if (event.sensor.type != Sensor.TYPE_STEP_DETECTOR) return
            sessionSensorSteps++
            _steps.value = todayBase + sessionSensorSteps
            saveState()
            updateNotification()
        } else {
            if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
            val raw = event.values[0].toLong()
            if (sessionBaseline < 0L) { sessionBaseline = raw; return }
            val newSession = (pauseOffset + (raw - sessionBaseline).toInt()).coerceAtLeast(0)
            if (newSession != sessionSensorSteps) {
                sessionSensorSteps = newSession
                _steps.value = todayBase + sessionSensorSteps
                saveState()
                updateNotification()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── WakeLock ─────────────────────────────────────────────────────────────

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bulk:StepTracker")
            .also { it.acquire(4 * 60 * 60 * 1000L) } // max 4 hours
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) wakeLock?.release()
        wakeLock = null
    }

    // ── State persistence ─────────────────────────────────────────────────────

    private fun saveState() {
        prefs.edit()
            .putBoolean("isRunning", _isRunning.value)
            .putBoolean("isPaused", _isPaused.value)
            .putInt("steps", _steps.value)
            .putInt("todayBase", todayBase)
            .putInt("sessionSensorSteps", sessionSensorSteps)
            .putLong("accActiveMs", _accActiveMs.value)
            .putString("todayKey", todayKey)
            .apply()
    }

    private fun clearState() {
        prefs.edit().clear().apply()
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun buildNotification(): Notification {
        val n = _steps.value; val paused = _isPaused.value
        val distKm = sessionSensorSteps * 0.762f / 1000f
        val openPi = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pausePi = PendingIntent.getService(this, 1,
            Intent(this, StepTrackerService::class.java).apply { action = ACTION_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopPi = PendingIntent.getService(this, 2,
            Intent(this, StepTrackerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle(if (paused) "Walk Paused" else "Walking — $n steps today")
            .setContentText("Session: $sessionSensorSteps steps · ${String.format("%.2f", distKm)} km")
            .setContentIntent(openPi).setOngoing(true).setSilent(true)
            .addAction(0, if (paused) "Resume" else "Pause", pausePi)
            .addAction(0, "Stop & Save", stopPi).build()
    }

    private fun updateNotification() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIF_ID, buildNotification())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, "Step Tracker", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Tracks your walking session"; setShowBadge(false); setSound(null, null) }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(chan)
        }
    }
}
