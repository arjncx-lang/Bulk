package com.example.bulk.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(onBack: () -> Unit) {
    val ctx  = LocalContext.current
    val cs   = MaterialTheme.colorScheme
    val repo = remember { WorkoutRepository(ctx) }

    var tab    by remember { mutableIntStateOf(0) }
    var reload by remember { mutableIntStateOf(0) }

    val todayLogs = remember(reload) { repo.logsForDate(repo.todayKey()) }
    val allDays   = remember(reload) { repo.groupByDay() }
    val exNames   = remember(reload) { repo.allExerciseNames() }

    var showLog  by remember { mutableStateOf(false) }
    var editLog  by remember { mutableStateOf<WorkoutLog?>(null) }
    var exFilter by remember { mutableStateOf<String?>(null) }

    if (showLog || editLog != null) {
        LogWorkoutSheet(
            initial   = editLog,
            existing  = exNames,
            onDismiss = { showLog = false; editLog = null },
            onSave    = { log ->
                if (editLog != null) repo.updateLog(log) else repo.addLog(log)
                showLog = false; editLog = null; reload++
            }
        )
    }
    if (exFilter != null) {
        ExerciseHistorySheet(name = exFilter!!, repo = repo, onDismiss = { exFilter = null })
    }

    Box(Modifier.fillMaxSize().background(cs.background).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("💪  LOG", fontSize = 13.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp, color = cs.onBackground)
                Box(Modifier.size(36.dp).clip(CircleShape).background(cs.secondary)
                    .clickable { showLog = true }, Alignment.Center) {
                    Text("+", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp)).background(cs.surface).padding(4.dp)) {
                listOf("Today", "History", "Exercises").forEachIndexed { i, label ->
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
                        if (todayLogs.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("No exercises logged today", fontSize = 16.sp, color = cs.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Tap + to log your first exercise", fontSize = 13.sp, color = cs.outline)
                                }
                            }
                        } else {
                            WorkoutSummaryCard(todayLogs, cs)
                            Spacer(Modifier.height(12.dp))
                            todayLogs.forEach { log ->
                                WorkoutLogCard(log, cs,
                                    onEdit   = { editLog = log },
                                    onDelete = { repo.deleteLog(log.id); reload++ })
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                    1 -> {
                        if (allDays.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
                                Text("No workout history yet", fontSize = 15.sp, color = cs.onSurfaceVariant,
                                    textAlign = TextAlign.Center)
                            }
                        } else {
                            allDays.forEach { day ->
                                DayWorkoutCard(day, cs,
                                    onEdit   = { editLog = it },
                                    onDelete = { repo.deleteLog(it.id); reload++ })
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                    else -> {
                        if (exNames.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
                                Text("Log a workout to see per-exercise history",
                                    fontSize = 14.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        } else {
                            Text("TAP AN EXERCISE TO SEE HISTORY", fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            exNames.forEach { name ->
                                val count = repo.exerciseHistory(name).size
                                Box(Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(14.dp)).background(cs.surface)
                                    .clickable { exFilter = name }.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface)
                                        Text("$count session${if (count != 1) "s" else ""}", fontSize = 12.sp, color = cs.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun WorkoutSummaryCard(logs: List<WorkoutLog>, cs: ColorScheme) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.secondary).padding(16.dp)) {
        Column {
            Text("TODAY", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, color = Color.White.copy(.7f))
            Spacer(Modifier.height(6.dp))
            Text("${logs.size} exercise${if (logs.size != 1) "s" else ""}",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            val totalReps = logs.sumOf { it.reps }
            val totalVol  = logs.sumOf { it.totalVolume.toDouble() }
            Text(buildString {
                append("$totalReps total reps")
                if (totalVol > 0) append("  ·  ${String.format("%.0f", totalVol)} kg vol")
            }, fontSize = 13.sp, color = Color.White.copy(.85f))
        }
    }
}

@Composable private fun WorkoutLogCard(log: WorkoutLog, cs: ColorScheme, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }
    if (showDelete) {
        AlertDialog(onDismissRequest = { showDelete = false },
            title = { Text("Delete entry?") },
            text  = { Text("Remove \"${log.exercise}\" from your log?") },
            confirmButton = { TextButton(onClick = { showDelete = false; onDelete() }) {
                Text("Delete", color = Color(0xFFEF4444)) } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } })
    }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surface).padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(log.exercise, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                Text(log.setsLabel, fontSize = 13.sp, color = cs.primary, fontWeight = FontWeight.Medium)
                val typeColor = if (log.type == "bodyweight") cs.secondary else cs.primary
                Spacer(Modifier.height(4.dp))
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(typeColor.copy(.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(log.type, fontSize = 10.sp, color = typeColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(cs.surfaceVariant)
                    .clickable { onEdit() }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("edit", fontSize = 11.sp, color = cs.onSurfaceVariant) }
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFF43F5E).copy(.12f))
                    .clickable { showDelete = true }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("del", fontSize = 11.sp, color = Color(0xFFF43F5E)) }
            }
        }
    }
}

@Composable private fun DayWorkoutCard(day: DayWorkoutSummary, cs: ColorScheme,
    onEdit: (WorkoutLog) -> Unit, onDelete: (WorkoutLog) -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surface).padding(16.dp)) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(day.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                Text("${day.exerciseCount} ex", fontSize = 12.sp, color = cs.secondary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            day.logs.forEach { log ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(log.exercise, fontSize = 13.sp, color = cs.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(log.setsLabel, fontSize = 12.sp, color = cs.primary)
                        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(cs.surfaceVariant)
                            .clickable { onEdit(log) }.padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("edit", fontSize = 10.sp, color = cs.onSurfaceVariant) }
                        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFF43F5E).copy(.1f))
                            .clickable { onDelete(log) }.padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("del", fontSize = 10.sp, color = Color(0xFFF43F5E)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ExerciseHistorySheet(name: String, repo: WorkoutRepository, onDismiss: () -> Unit) {
    val cs      = MaterialTheme.colorScheme
    val history = remember { repo.exerciseHistory(name) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
            Text("${history.size} session${if (history.size != 1) "s" else ""}",
                fontSize = 13.sp, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            if (history.isEmpty()) {
                Text("No history yet", fontSize = 14.sp, color = cs.onSurfaceVariant)
            } else {
                history.forEach { log ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(log.dateKey, fontSize = 13.sp, color = cs.onSurface)
                        Text(log.setsLabel, fontSize = 13.sp, color = cs.primary, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = cs.outlineVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun LogWorkoutSheet(
    initial: WorkoutLog?,
    existing: List<String>,
    onDismiss: () -> Unit,
    onSave: (WorkoutLog) -> Unit
) {
    val ctx      = LocalContext.current
    val cs       = MaterialTheme.colorScheme
    val repo     = WorkoutRepository(ctx)
    var exercise by remember { mutableStateOf(initial?.exercise ?: "") }
    var type     by remember { mutableStateOf(if (initial?.type == "bodyweight") "bodyweight" else "strength") }
    var reps     by remember { mutableStateOf(initial?.reps?.takeIf { it > 0 }?.toString() ?: "") }
    var weight   by remember { mutableStateOf(if ((initial?.weightKg ?: 0f) > 0f) String.format("%.1f", initial!!.weightKg) else "") }
    var showSug  by remember { mutableStateOf(false) }

    val suggestions = (COMMON_EXERCISES + existing).distinct()
        .filter { exercise.isNotBlank() && it.contains(exercise, ignoreCase = true) && it != exercise }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 40.dp)
            .verticalScroll(rememberScrollState())) {
            Text(if (initial != null) "Edit Exercise" else "Log Exercise",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = exercise,
                onValueChange = { exercise = it; showSug = it.isNotBlank() },
                label = { Text("Exercise name") }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth())

            if (showSug && suggestions.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(cs.surfaceVariant)) {
                    suggestions.take(4).forEach { s ->
                        Text(s, Modifier.fillMaxWidth().clickable { exercise = s; showSug = false }.padding(12.dp),
                            fontSize = 14.sp, color = cs.onSurface)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("strength", "bodyweight").forEach { t ->
                    Box(Modifier.clip(RoundedCornerShape(10.dp))
                        .background(if (type == t) cs.primary else cs.surfaceVariant)
                        .clickable { type = t }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(t, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (type == t) Color.White else cs.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = reps,
                    onValueChange = { reps = it.filter(Char::isDigit).take(4) },
                    label = { Text("Reps") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f))
                if (type == "strength") {
                    OutlinedTextField(value = weight,
                        onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' }.take(6) },
                        label = { Text("kg (optional)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(if (exercise.isNotBlank()) cs.primary else cs.outlineVariant)
                .clickable(enabled = exercise.isNotBlank()) {
                    onSave(WorkoutLog(
                        id          = initial?.id ?: System.currentTimeMillis(),
                        dateKey     = initial?.dateKey ?: repo.todayKey(),
                        timestamp   = initial?.timestamp ?: System.currentTimeMillis(),
                        exercise    = exercise.trim(),
                        type        = type,
                        sets        = 1,
                        reps        = reps.toIntOrNull() ?: 0,
                        weightKg    = weight.toFloatOrNull() ?: 0f
                    ))
                }.padding(vertical = 16.dp), Alignment.Center) {
                Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = if (exercise.isNotBlank()) Color.White else cs.onSurfaceVariant)
            }
        }
    }
}
