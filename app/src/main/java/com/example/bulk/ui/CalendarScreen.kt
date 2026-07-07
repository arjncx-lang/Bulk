package com.example.bulk.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.data.WorkoutLog
import com.example.bulk.data.WorkoutRepository
import com.example.bulk.data.StepRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val ctx         = LocalContext.current
    val cs          = MaterialTheme.colorScheme
    val stepRepo    = remember { StepRepository(ctx) }
    val workoutRepo = remember { WorkoutRepository(ctx) }

    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayParts = today.split("-")
    var year  by remember { mutableIntStateOf(todayParts[0].toInt()) }
    var month by remember { mutableIntStateOf(todayParts[1].toInt() - 1) }

    var selectedDate by remember { mutableStateOf<String?>(null) }
    var reload       by remember { mutableIntStateOf(0) }

    val stepByDay    = remember(reload) { stepRepo.groupByDay().associateBy { it.dateKey } }
    val allLogs      = remember(reload) { workoutRepo.groupByDay() }
    val workoutByDay = remember(reload) { allLogs.associateBy { it.dateKey } }

    val fmt    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val MONTHS = arrayOf("January","February","March","April","May","June","July","August","September","October","November","December")
    val DAYS   = arrayOf("M","T","W","T","F","S","S")

    val cal = Calendar.getInstance().apply { set(year, month, 1) }
    val firstDow     = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    val daysInMonth  = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells   = firstDow + daysInMonth
    val rows         = (totalCells + 6) / 7

    // Logs to show: filtered by selectedDate if set, else all
    val logsToShow = remember(reload, selectedDate) {
        if (selectedDate != null) allLogs.filter { it.dateKey == selectedDate }
        else allLogs
    }

    Column(Modifier.fillMaxSize().background(cs.background).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text("CALENDAR", fontSize = 13.sp, fontWeight = FontWeight.Black,
                letterSpacing = 4.sp, color = cs.onBackground)
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // Month nav
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp)).background(cs.surface).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(cs.surfaceVariant)
                        .clickable { if (month == 0) { month = 11; year-- } else month-- },
                        Alignment.Center) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Prev",
                            tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    Text("${MONTHS[month]} $year", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    Box(Modifier.size(36.dp).clip(CircleShape).background(cs.surfaceVariant)
                        .clickable { if (month == 11) { month = 0; year++ } else month++ },
                        Alignment.Center) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Next",
                            tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Calendar grid
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp)).background(cs.surface).padding(12.dp)) {
                Column {
                    Row(Modifier.fillMaxWidth()) {
                        DAYS.forEach { d ->
                            Box(Modifier.weight(1f), Alignment.Center) {
                                Text(d, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    (0 until rows).forEach { row ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            (0..6).forEach { col ->
                                val day = row * 7 - firstDow + col + 1
                                if (day in 1..daysInMonth) {
                                    val cal2    = Calendar.getInstance().apply { set(year, month, day) }
                                    val dateKey = fmt.format(cal2.time)
                                    val hasSteps   = (stepByDay[dateKey]?.totalSteps ?: 0) > 0
                                    val hasWorkout = (workoutByDay[dateKey]?.logs?.size ?: 0) > 0
                                    val isToday    = dateKey == today
                                    val isSelected = dateKey == selectedDate
                                    Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                        .clip(CircleShape)
                                        .background(when {
                                            isSelected -> cs.primary
                                            isToday    -> cs.primary.copy(.15f)
                                            else       -> Color.Transparent
                                        })
                                        .clickable { selectedDate = if (selectedDate == dateKey) null else dateKey },
                                        Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$day", fontSize = 13.sp,
                                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = when { isSelected -> Color.White; isToday -> cs.primary; else -> cs.onSurface })
                                            if (hasSteps || hasWorkout) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    if (hasSteps)   Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White.copy(.8f) else cs.primary))
                                                    if (hasWorkout) Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White.copy(.8f) else cs.secondary))
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDotC(cs.primary, "Steps")
                        LegendDotC(cs.secondary, "Workout")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Log list header
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (selectedDate != null) dateLabelFull(selectedDate!!, today) else "All Workouts",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp, color = cs.onBackground)
                if (selectedDate != null) {
                    Text("Show all", fontSize = 12.sp, color = cs.primary,
                        modifier = Modifier.clickable { selectedDate = null })
                }
            }

            if (logsToShow.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                    Text("No workouts logged${if (selectedDate != null) " for this day" else " yet"}",
                        fontSize = 15.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            } else {
                logsToShow.forEach { day ->
                    CalendarDayLog(day.dateKey, day.logs, today, cs)
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalendarDayLog(dateKey: String, logs: List<WorkoutLog>, today: String, cs: ColorScheme) {
    val weekNum = weekOfYear(dateKey)
    val isToday  = dateKey == today
    val headerLabel = buildString {
        append(SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)!!))
        append(" (")
        if (isToday) append("Today · ")
        append("Week $weekNum)")
    }

    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        .clip(RoundedCornerShape(16.dp)).background(cs.surface).padding(14.dp)) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(headerLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                Text("${logs.size} ex", fontSize = 11.sp, color = cs.secondary, fontWeight = FontWeight.SemiBold)
            }
            if (logs.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                logs.forEach { log ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(cs.primary.copy(0.5f)))
                            Text(log.exercise, fontSize = 13.sp, color = cs.onSurface)
                        }
                        Text(buildString {
                            append(logTime(log.timestamp))
                            append("  ·  ")
                            append(log.setsLabel)
                        }, fontSize = 12.sp, color = cs.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDotC(color: Color, label: String) =
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

private fun weekOfYear(dateKey: String): Int = try {
    val cal = Calendar.getInstance()
    cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)!!
    cal.get(Calendar.WEEK_OF_YEAR)
} catch (_: Exception) { 0 }

private fun logTime(ts: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))

private fun dateLabelFull(dateKey: String, today: String): String {
    val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time)
    val weekNum = weekOfYear(dateKey)
    val base = when (dateKey) {
        today     -> "Today"
        yesterday -> "Yesterday"
        else      -> try {
            SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)!!)
        } catch (_: Exception) { dateKey }
    }
    return "$base (Week $weekNum)"
}
