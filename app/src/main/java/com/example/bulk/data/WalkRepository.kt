package com.example.bulk.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class WalkSession(
    val id: Long,
    val startTime: Long,
    val steps: Int,
    val distanceMeters: Float,
    val durationMs: Long
)

data class DayGroup(
    val dateLabel: String,
    val dayStartMs: Long,
    val totalSteps: Int,
    val totalDistKm: Float,
    val sessions: List<WalkSession>
)

data class MonthGroup(
    val label: String,
    val totalSteps: Int,
    val totalDistKm: Float,
    val sessionCount: Int
)

class WalkRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("walk_history", Context.MODE_PRIVATE)

    fun saveSession(session: WalkSession) {
        val list = loadAll().toMutableList()
        list.add(0, session)
        val arr = JSONArray()
        list.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("startTime", s.startTime)
                put("steps", s.steps)
                put("dist", s.distanceMeters.toDouble())
                put("dur", s.durationMs)
            })
        }
        prefs.edit().putString("sessions", arr.toString()).apply()
    }

    fun loadAll(): List<WalkSession> {
        val raw = prefs.getString("sessions", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                WalkSession(
                    id = o.getLong("id"),
                    startTime = o.getLong("startTime"),
                    steps = o.getInt("steps"),
                    distanceMeters = o.getDouble("dist").toFloat(),
                    durationMs = o.getLong("dur")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    fun groupByDay(): List<DayGroup> {
        return loadAll()
            .groupBy { s ->
                val c = Calendar.getInstance().apply { timeInMillis = s.startTime }
                Triple(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            }
            .entries
            .sortedByDescending { (k, _) -> k.first * 10000 + k.second * 100 + k.third }
            .map { (key, sessions) ->
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, key.first)
                    set(Calendar.MONTH, key.second)
                    set(Calendar.DAY_OF_MONTH, key.third)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                DayGroup(
                    dateLabel = formatDate(c),
                    dayStartMs = c.timeInMillis,
                    totalSteps = sessions.sumOf { it.steps },
                    totalDistKm = sessions.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f,
                    sessions = sessions
                )
            }
    }

    fun groupByMonth(): List<MonthGroup> {
        return loadAll()
            .groupBy { s ->
                val c = Calendar.getInstance().apply { timeInMillis = s.startTime }
                Pair(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
            }
            .entries
            .sortedByDescending { (k, _) -> k.first * 100 + k.second }
            .map { (key, sessions) ->
                val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                MonthGroup(
                    label = "${months[key.second]} ${key.first}",
                    totalSteps = sessions.sumOf { it.steps },
                    totalDistKm = sessions.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f,
                    sessionCount = sessions.size
                )
            }
    }

    private fun formatDate(cal: Calendar): String {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return when {
            sameDay(cal, today) -> "Today"
            sameDay(cal, yesterday) -> "Yesterday"
            else -> {
                val m = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                "${m[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}"
            }
        }
    }

    private fun sameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
