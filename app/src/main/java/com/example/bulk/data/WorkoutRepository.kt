package com.example.bulk.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class WorkoutLog(
    val id: Long,
    val dateKey: String,    // "2026-06-29"
    val timestamp: Long,
    val exercise: String,
    val type: String = "strength",   // "strength" | "cardio" | "bodyweight"
    val sets: Int = 0,
    val reps: Int = 0,
    val weightKg: Float = 0f,
    val durationMin: Int = 0,
    val notes: String = ""
) {
    val totalVolume get() = sets * reps * weightKg
    val setsLabel get() = when {
        weightKg > 0f -> "$reps reps · ${if (weightKg == weightKg.toInt().toFloat()) weightKg.toInt().toString() else String.format("%.1f", weightKg)}kg"
        reps > 0      -> "$reps reps"
        durationMin > 0 -> "${durationMin} min"
        else          -> "logged"
    }
}

data class DayWorkoutSummary(
    val dateKey: String,
    val logs: List<WorkoutLog>
) {
    val label get() = friendlyDate(dateKey)
    val exerciseCount get() = logs.size
    val totalSets get() = logs.sumOf { it.sets }
    val totalVolume get() = logs.sumOf { it.totalVolume.toDouble() }.toFloat()
}

class WorkoutRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("workouts_v1", Context.MODE_PRIVATE)
    private val fmt   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun todayKey(): String = fmt.format(Date())

    fun addLog(log: WorkoutLog) {
        val list = loadAll().toMutableList()
        list.add(0, log)
        persist(list)
    }

    fun updateLog(updated: WorkoutLog) {
        persist(loadAll().map { if (it.id == updated.id) updated else it })
    }

    fun deleteLog(id: Long) {
        persist(loadAll().filter { it.id != id })
    }

    fun logsForDate(dateKey: String): List<WorkoutLog> =
        loadAll().filter { it.dateKey == dateKey }.sortedByDescending { it.timestamp }

    fun groupByDay(): List<DayWorkoutSummary> =
        loadAll()
            .groupBy { it.dateKey }
            .entries.sortedByDescending { it.key }
            .map { (key, logs) -> DayWorkoutSummary(key, logs) }

    fun exerciseHistory(name: String): List<WorkoutLog> =
        loadAll().filter { it.exercise.equals(name, ignoreCase = true) }
            .sortedByDescending { it.timestamp }

    fun allExerciseNames(): List<String> =
        loadAll().map { it.exercise }.distinct().sorted()

    fun loadAll(): List<WorkoutLog> {
        val raw = prefs.getString("logs", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                WorkoutLog(
                    id          = o.getLong("id"),
                    dateKey     = o.getString("date"),
                    timestamp   = o.getLong("ts"),
                    exercise    = o.getString("ex"),
                    type        = o.optString("type", "strength"),
                    sets        = o.optInt("sets", 0),
                    reps        = o.optInt("reps", 0),
                    weightKg    = o.optDouble("wkg", 0.0).toFloat(),
                    durationMin = o.optInt("dur", 0),
                    notes       = o.optString("notes", "")
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun persist(logs: List<WorkoutLog>) {
        val arr = JSONArray()
        logs.forEach { l ->
            arr.put(JSONObject().apply {
                put("id", l.id); put("date", l.dateKey); put("ts", l.timestamp)
                put("ex", l.exercise); put("type", l.type)
                put("sets", l.sets); put("reps", l.reps); put("wkg", l.weightKg.toDouble())
                put("dur", l.durationMin); put("notes", l.notes)
            })
        }
        prefs.edit().putString("logs", arr.toString()).apply()
    }
}

private fun friendlyDate(key: String): String {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d = fmt.parse(key) ?: return key
        val today = fmt.format(Date())
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = fmt.format(cal.time)
        when (key) {
            today     -> "Today"
            yesterday -> "Yesterday"
            else      -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(d)
        }
    } catch (_: Exception) { key }
}

val COMMON_EXERCISES = listOf(
    "Bench Press", "Squat", "Deadlift", "Overhead Press", "Barbell Row",
    "Pull-Up", "Push-Up", "Dip", "Lunge", "Plank",
    "Bicep Curl", "Tricep Extension", "Lateral Raise", "Leg Press",
    "Running", "Cycling", "Jump Rope"
)
