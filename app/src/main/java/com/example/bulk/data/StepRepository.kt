package com.example.bulk.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class StepEntry(
    val id: Long,
    val dateKey: String,        // "2026-06-29"
    val steps: Int,
    val distanceMeters: Float = 0f,
    val durationMs: Long = 0L,
    val source: String = "sensor"   // "sensor" | "manual"
)

data class DayStepSummary(
    val dateKey: String,
    val totalSteps: Int,
    val entries: List<StepEntry>
) {
    val distKm get() = entries.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f
    val label get() = friendlyDate(dateKey)
}

class StepRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("steps_v2", Context.MODE_PRIVATE)
    private val fmt   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun todayKey(): String = fmt.format(Date())

    fun todayTotal(): Int = entriesFor(todayKey()).sumOf { it.steps }

    fun entriesFor(dateKey: String): List<StepEntry> = loadAll().filter { it.dateKey == dateKey }

    fun addEntry(entry: StepEntry) {
        val list = loadAll().toMutableList()
        list.add(0, entry)
        persist(list)
    }

    fun addManual(dateKey: String, steps: Int) {
        addEntry(StepEntry(id = System.currentTimeMillis(), dateKey = dateKey,
            steps = steps, source = "manual"))
    }

    fun updateEntry(updated: StepEntry) {
        persist(loadAll().map { if (it.id == updated.id) updated else it })
    }

    fun deleteEntry(id: Long) {
        persist(loadAll().filter { it.id != id })
    }

    fun deleteAllForDate(dateKey: String) {
        persist(loadAll().filter { it.dateKey != dateKey })
    }

    /** Merge imported entries: existing ids win, new ids are added. Returns count added. */
    fun merge(imported: List<StepEntry>): Int {
        val existing = loadAll()
        val ids = existing.map { it.id }.toHashSet()
        val fresh = imported.filter { it.id !in ids }
        if (fresh.isNotEmpty()) persist((existing + fresh).sortedByDescending { it.id })
        return fresh.size
    }

    // ── Aggregations ─────────────────────────────────────────────────────────

    fun groupByDay(): List<DayStepSummary> =
        loadAll()
            .groupBy { it.dateKey }
            .entries.sortedByDescending { it.key }
            .map { (key, entries) -> DayStepSummary(key, entries.sumOf { it.steps }, entries) }

    /** Last 7 calendar days oldest-first, zero-filled for missing days. */
    fun last7Days(): List<DayStepSummary> {
        val all = groupByDay().associateBy { it.dateKey }
        val cal = Calendar.getInstance()
        return (6 downTo 0).reversed().map { offset ->
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
            val key = fmt.format(c.time)
            all[key] ?: DayStepSummary(key, 0, emptyList())
        }
    }

    /** Last 4 complete weeks (Sun-Sat), oldest-first. */
    fun last4Weeks(): List<Pair<String, Int>> {
        val byDay = groupByDay().associateBy { it.dateKey }
        return (3 downTo 0).reversed().map { wAgo ->
            val label = if (wAgo == 0) "This Wk" else "–${wAgo}W"
            val total = (0..6).sumOf { d ->
                val c = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -wAgo); add(Calendar.DAY_OF_WEEK, -d) }
                byDay[fmt.format(c.time)]?.totalSteps ?: 0
            }
            Pair(label, total)
        }
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    fun loadAll(): List<StepEntry> {
        migrateV1IfNeeded()
        val raw = prefs.getString("entries", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                StepEntry(
                    id             = o.getLong("id"),
                    dateKey        = o.getString("date"),
                    steps          = o.getInt("steps"),
                    distanceMeters = o.optDouble("dist", 0.0).toFloat(),
                    durationMs     = o.optLong("dur", 0L),
                    source         = o.optString("src", "sensor")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun persist(entries: List<StepEntry>) {
        val arr = JSONArray()
        entries.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id); put("date", e.dateKey); put("steps", e.steps)
                put("dist", e.distanceMeters.toDouble()); put("dur", e.durationMs)
                put("src", e.source)
            })
        }
        prefs.edit().putString("entries", arr.toString()).apply()
    }

    // Migrate old WalkRepository data (walk_history / sessions) into this schema
    private fun migrateV1IfNeeded() {
        if (prefs.getBoolean("migrated_v1", false)) return
        prefs.edit().putBoolean("migrated_v1", true).apply()
        try {
            val old = context.getSharedPreferences("walk_history", Context.MODE_PRIVATE)
            val raw = old.getString("sessions", null) ?: return
            val arr = JSONArray(raw)
            val entries = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val date = fmt.format(Date(o.getLong("startTime")))
                StepEntry(
                    id             = o.getLong("id"),
                    dateKey        = date,
                    steps          = o.getInt("steps"),
                    distanceMeters = o.getDouble("dist").toFloat(),
                    durationMs     = o.getLong("dur"),
                    source         = "sensor"
                )
            }
            if (entries.isNotEmpty()) persist(entries)
        } catch (_: Exception) { }
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
