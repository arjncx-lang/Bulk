package com.example.bulk.data

import android.content.Context
import android.net.Uri
import com.example.bulk.ui.theme.SectionState
import org.json.JSONArray
import org.json.JSONObject

/**
 * Full-app backup as a single JSON document.
 * Import always MERGES: entries/logs are deduplicated by id, settings are overwritten.
 */
object BackupManager {

    fun export(ctx: Context, uri: Uri): Boolean = runCatching {
        val steps = JSONArray().apply {
            StepRepository(ctx).loadAll().forEach { e ->
                put(JSONObject().apply {
                    put("id", e.id); put("date", e.dateKey); put("steps", e.steps)
                    put("dist", e.distanceMeters.toDouble()); put("dur", e.durationMs); put("src", e.source)
                })
            }
        }
        val workouts = JSONArray().apply {
            WorkoutRepository(ctx).loadAll().forEach { l ->
                put(JSONObject().apply {
                    put("id", l.id); put("date", l.dateKey); put("ts", l.timestamp)
                    put("ex", l.exercise); put("type", l.type); put("sets", l.sets)
                    put("reps", l.reps); put("wkg", l.weightKg.toDouble())
                    put("dur", l.durationMin); put("notes", l.notes)
                })
            }
        }
        val root = JSONObject().apply {
            put("app", "bulk"); put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("steps", steps)
            put("workouts", workouts)
            put("settings", JSONObject().apply { put("dailyGoal", SectionState.dailyGoal) })
        }
        ctx.contentResolver.openOutputStream(uri, "wt")?.use {
            it.write(root.toString(2).toByteArray())
        } != null
    }.getOrDefault(false)

    /** Returns "steps added, workouts added" counts, or null if the file was invalid. */
    fun import(ctx: Context, uri: Uri): Pair<Int, Int>? = runCatching {
        val raw = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            ?: return null
        val root = JSONObject(raw)
        if (root.optString("app") != "bulk") return null

        val stepEntries = root.optJSONArray("steps")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                StepEntry(o.getLong("id"), o.getString("date"), o.getInt("steps"),
                    o.optDouble("dist", 0.0).toFloat(), o.optLong("dur", 0L), o.optString("src", "sensor"))
            }
        } ?: emptyList()

        val logs = root.optJSONArray("workouts")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                WorkoutLog(o.getLong("id"), o.getString("date"), o.getLong("ts"), o.getString("ex"),
                    o.optString("type", "strength"), o.optInt("sets", 0), o.optInt("reps", 0),
                    o.optDouble("wkg", 0.0).toFloat(), o.optInt("dur", 0), o.optString("notes", ""))
            }
        } ?: emptyList()

        root.optJSONObject("settings")?.optInt("dailyGoal", 0)?.takeIf { it >= 100 }?.let {
            SectionState.dailyGoal = it; SectionState.save(ctx)
        }

        Pair(StepRepository(ctx).merge(stepEntries), WorkoutRepository(ctx).merge(logs))
    }.getOrNull()
}
