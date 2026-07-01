package com.example.bulk.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.data.DayStepSummary
import com.example.bulk.data.StepRepository
import com.example.bulk.ui.theme.SectionState

@Composable
fun StepHistoryScreen(onBack: () -> Unit) {
    val ctx  = LocalContext.current
    val cs   = MaterialTheme.colorScheme
    val repo = remember { StepRepository(ctx) }
    val goal = SectionState.dailyGoal

    var tab  by remember { mutableIntStateOf(0) }
    val tabs = listOf("Days", "Weeks", "Months")

    var reload     by remember { mutableIntStateOf(0) }
    val dayGroups  = remember(reload) { repo.groupByDay() }          // newest first
    val last4Weeks = remember(reload) { repo.last4Weeks() }

    val totalSteps = remember(dayGroups) { dayGroups.sumOf { it.totalSteps } }
    val bestDay    = remember(dayGroups) { dayGroups.maxOfOrNull { it.totalSteps } ?: 0 }
    val avgDay     = remember(dayGroups) {
        if (dayGroups.isEmpty()) 0 else totalSteps / dayGroups.size
    }

    var editing by remember { mutableStateOf<DayStepSummary?>(null) }
    editing?.let { day ->
        EditDayStepsDialog(day, repo, onDismiss = { editing = null; reload++ })
    }

    Box(Modifier.fillMaxSize().background(cs.background).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {

            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(cs.surface)
                    .clickable { onBack() }, Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back",
                        tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                Text("STEP HISTORY", fontSize = 13.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp, color = cs.onBackground)
                Spacer(Modifier.size(36.dp))
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp)).background(cs.surface).padding(4.dp)) {
                tabs.forEachIndexed { i, label ->
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (tab == i) cs.primary else Color.Transparent)
                        .clickable { tab = i }.padding(vertical = 10.dp), Alignment.Center) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (tab == i) Color.White else cs.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp)) {

                when (tab) {
                    0 -> {
                        if (dayGroups.isEmpty()) {
                            EmptyMsg("No walks recorded yet")
                        } else {
                            // ── Summary banner ──
                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp)).background(cs.primary)
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatBlock("Total", fmtK(totalSteps))
                                Box(Modifier.width(1.dp).height(36.dp)
                                    .background(Color.White.copy(0.25f)))
                                StatBlock("Best Day", fmtK(bestDay))
                                Box(Modifier.width(1.dp).height(36.dp)
                                    .background(Color.White.copy(0.25f)))
                                StatBlock("Daily Avg", fmtK(avgDay))
                            }

                            Spacer(Modifier.height(14.dp))

                            // ── All-days scrollable chart ──
                            AllDaysChart(
                                days  = dayGroups.reversed(), // oldest → newest (left → right)
                                goal  = goal,
                                cs    = cs
                            )

                            Spacer(Modifier.height(16.dp))

                            // ── Day cards ──
                            dayGroups.forEach { day ->
                                DayStepCard(day, cs, goal, onEdit = { editing = day })
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }

                    1 -> {
                        if (last4Weeks.all { it.second == 0 }) {
                            EmptyMsg("No walk data yet")
                        } else {
                            FixedBarChart("Last 4 Weeks", last4Weeks, cs)
                            Spacer(Modifier.height(16.dp))
                            last4Weeks.reversed().forEach { (label, total) ->
                                if (total > 0) {
                                    SummaryCard(label, total, "${String.format("%.1f", total * 0.762f / 1000f)} km", cs)
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    else -> {
                        val byMonth = dayGroups
                            .groupBy { it.dateKey.substring(0, 7) }
                            .entries.sortedByDescending { it.key }

                        if (byMonth.isEmpty()) {
                            EmptyMsg("No history yet")
                        } else {
                            val MOS = arrayOf("Jan","Feb","Mar","Apr","May","Jun",
                                              "Jul","Aug","Sep","Oct","Nov","Dec")
                            val monthData = byMonth.map { (key, days) ->
                                val label = try { "${MOS[key.split("-")[1].toInt() - 1]} ${key.split("-")[0]}" }
                                            catch (_: Exception) { key }
                                Pair(label, days.sumOf { it.totalSteps })
                            }.reversed()

                            FixedBarChart("All Months", monthData, cs)
                            Spacer(Modifier.height(16.dp))

                            byMonth.forEach { (monthKey, days) ->
                                val parts = monthKey.split("-")
                                val label = try { "${MOS[parts[1].toInt() - 1]} ${parts[0]}" }
                                            catch (_: Exception) { monthKey }
                                val total = days.sumOf { it.totalSteps }
                                SummaryCard(label, total, "${days.size} active day${if (days.size != 1) "s" else ""}", cs)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Charts ────────────────────────────────────────────────────────────────────

@Composable
private fun AllDaysChart(days: List<DayStepSummary>, goal: Int, cs: ColorScheme) {
    val maxSteps = days.maxOfOrNull { it.totalSteps }?.takeIf { it > 0 } ?: 1
    val maxBarH  = 100 // dp
    val colW     = 36.dp
    val barW     = 22.dp

    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surface)
        .padding(16.dp)) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("ALL DAYS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = cs.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendDot(cs.secondary, "Goal")
                    LegendDot(cs.primary,   "Under")
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { idx, day ->
                    val frac   = day.totalSteps.toFloat() / maxSteps
                    val barH   = (maxBarH * frac).coerceAtLeast(if (day.totalSteps > 0) 4f else 0f)
                    val hitGoal = day.totalSteps >= goal

                    val prevDay = if (idx > 0) days[idx - 1] else null
                    val newMonth = prevDay == null ||
                        day.dateKey.substring(5, 7) != prevDay.dateKey.substring(5, 7)

                    Column(
                        Modifier.width(colW),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Value above bar
                        Text(
                            if (day.totalSteps > 0) fmtK(day.totalSteps) else "",
                            fontSize = 7.sp, color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center, lineHeight = 8.sp,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        // Bar
                        Box(
                            Modifier.width(barW).height(barH.dp)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(
                                    when {
                                        day.totalSteps == 0 -> Color.Transparent
                                        hitGoal             -> cs.secondary
                                        else                -> cs.primary
                                    }
                                )
                        )
                        Spacer(Modifier.height(5.dp))
                        // Day number
                        Text(
                            day.dateKey.takeLast(2),
                            fontSize = 9.sp, color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center, fontWeight = FontWeight.Medium
                        )
                        // Month label — only when month changes
                        Text(
                            if (newMonth) monthAbbr(day.dateKey.substring(5, 7)) else "",
                            fontSize = 7.sp, color = cs.primary,
                            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedBarChart(title: String, data: List<Pair<String, Int>>, cs: ColorScheme) {
    if (data.isEmpty()) return
    val max     = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1
    val maxBarH = 90

    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surface)
        .padding(16.dp)) {
        Column {
            Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth().height((maxBarH + 44).dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                data.forEach { (label, value) ->
                    val frac = value.toFloat() / max
                    val barH = (maxBarH * frac).coerceAtLeast(if (value > 0) 4f else 0f)
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Value above bar
                        Text(
                            if (value > 0) fmtK(value) else "",
                            fontSize = 8.sp, color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center, maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        // Bar — fillMaxWidth(0.6f) gives breathing room without squish
                        Box(
                            Modifier.fillMaxWidth(0.6f).height(barH.dp)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(if (value > 0) cs.primary else cs.outlineVariant)
                        )
                        Spacer(Modifier.height(5.dp))
                        // Label — maxLines = 1 ensures it never wraps into neighbour column
                        Text(
                            label, fontSize = 9.sp, color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center, maxLines = 1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ── Cards ─────────────────────────────────────────────────────────────────────

@Composable
private fun DayStepCard(day: DayStepSummary, cs: ColorScheme, goal: Int, onEdit: () -> Unit) {
    val hitGoal = day.totalSteps >= goal
    val accentColor = if (hitGoal) cs.secondary else cs.primary

    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surface)
        .padding(16.dp)) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(day.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    Text("${String.format("%.2f", day.distKm)} km · ${day.entries.size} session${if (day.entries.size != 1) "s" else ""}",
                        fontSize = 12.sp, color = cs.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(fmtK(day.totalSteps), fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = accentColor)
                        Text("steps", fontSize = 10.sp, color = cs.onSurfaceVariant)
                    }
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(cs.surfaceVariant)
                        .clickable { onEdit() }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("edit", fontSize = 11.sp, color = cs.onSurfaceVariant,
                            fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (hitGoal) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.clip(RoundedCornerShape(6.dp))
                    .background(cs.secondary.copy(0.1f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("Goal reached ⭐", fontSize = 10.sp, color = cs.secondary,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            if (day.entries.size > 1) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = cs.outlineVariant)
                Spacer(Modifier.height(8.dp))
                day.entries.forEachIndexed { idx, e ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${e.source.replaceFirstChar { it.uppercase() }} ${idx + 1}",
                            fontSize = 12.sp, color = cs.onSurface)
                        Text("${e.steps} steps${if (e.durationMs > 0) " · ${fmtDur(e.durationMs)}" else ""}",
                            fontSize = 12.sp, color = cs.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, steps: Int, subtitle: String, cs: ColorScheme) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surface)
        .padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                Text(subtitle, fontSize = 12.sp, color = cs.onSurfaceVariant)
            }
            Text(fmtK(steps), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = cs.primary)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.White.copy(0.75f), letterSpacing = 0.5.sp)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) =
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

@Composable
private fun EmptyMsg(msg: String) =
    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
        Text(msg, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }

@Composable
private fun EditDayStepsDialog(day: DayStepSummary, repo: StepRepository, onDismiss: () -> Unit) {
    val cs      = MaterialTheme.colorScheme
    var addText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${day.label}") },
        text  = { Column {
            Text("Current: ${day.totalSteps} steps", fontSize = 14.sp, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = addText,
                onValueChange = { addText = it.filter(Char::isDigit).take(6) },
                placeholder = { Text("Steps to add") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF43F5E))
                .clickable { repo.deleteAllForDate(day.dateKey); onDismiss() }
                .padding(12.dp), Alignment.Center) {
                Text("Delete all entries for this day", fontSize = 13.sp,
                    color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }},
        confirmButton = {
            TextButton(
                onClick  = { val n = addText.toIntOrNull() ?: 0; if (n > 0) repo.addManual(day.dateKey, n); onDismiss() },
                enabled  = addText.isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun fmtK(n: Int): String = when {
    n >= 10_000 -> "${n / 1000}k"
    n >= 1_000  -> "${n / 1000}.${(n % 1000) / 100}k"
    else        -> "$n"
}

private fun monthAbbr(mm: String): String =
    arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        .getOrNull((mm.toIntOrNull() ?: 0) - 1) ?: ""

private fun fmtDur(ms: Long): String {
    val s = (ms / 1000).toInt(); val m = s / 60
    return if (m > 0) "${m}m ${s % 60}s" else "${s}s"
}
