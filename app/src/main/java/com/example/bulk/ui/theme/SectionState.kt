package com.example.bulk.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SectionState {
    var showSteps           by mutableStateOf(true)
    var showTrain           by mutableStateOf(true)
    var showWorkout         by mutableStateOf(true)
    var showCalendar        by mutableStateOf(true)
    var usePlayPauseButtons by mutableStateOf(false)
    var dailyGoal           by mutableIntStateOf(10_000)

    val enabledCount get() = listOf(showSteps, showTrain, showWorkout, showCalendar).count { it }

    fun load(ctx: Context) {
        val p = ctx.getSharedPreferences("bulk_prefs", Context.MODE_PRIVATE)
        showSteps           = p.getBoolean("show_steps",          true)
        showTrain           = p.getBoolean("show_train",          true)
        showWorkout         = p.getBoolean("show_workout",        true)
        showCalendar        = p.getBoolean("show_calendar",       true)
        usePlayPauseButtons = p.getBoolean("use_play_pause",      false)
        dailyGoal           = p.getInt("daily_goal",              10_000)
        AppThemeState.isDarkMode = p.getBoolean("dark_mode",      false)
    }

    fun save(ctx: Context) {
        ctx.getSharedPreferences("bulk_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("show_steps",     showSteps)
            .putBoolean("show_train",     showTrain)
            .putBoolean("show_workout",   showWorkout)
            .putBoolean("show_calendar",  showCalendar)
            .putBoolean("use_play_pause", usePlayPauseButtons)
            .putInt("daily_goal",         dailyGoal)
            .putBoolean("dark_mode",      AppThemeState.isDarkMode)
            .apply()
    }
}
