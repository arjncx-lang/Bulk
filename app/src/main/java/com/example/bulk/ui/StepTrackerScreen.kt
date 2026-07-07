package com.example.bulk.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.StepTrackerService
import com.example.bulk.data.StepRepository
import com.example.bulk.ui.theme.SectionState
import kotlinx.coroutines.delay
import kotlin.math.min

private fun stepColor(steps: Int, goal: Int, primary: Color, secondary: Color, outline: Color): Color = when {
    steps >= (goal * 1.2).toInt() -> Color(0xFFF43F5E)
    steps >= goal                 -> primary
    steps >= (goal * 0.5).toInt() -> Color(0xFFF97316)
    steps >= (goal * 0.2).toInt() -> Color(0xFFF59E0B)
    steps >= (goal * 0.1).toInt() -> secondary
    steps >= (goal * 0.05).toInt()-> Color(0xFF14B8A6)
    else                          -> outline
}

@Composable
fun StepTrackerScreen(onHistory: () -> Unit) {
    val ctx       = LocalContext.current
    val cs        = MaterialTheme.colorScheme
    val svcSteps  by StepTrackerService.steps.collectAsState()
    val isRunning by StepTrackerService.isRunning.collectAsState()
    val accMs     by StepTrackerService.accActiveMs.collectAsState()
    val segStart  by StepTrackerService.segStartMs.collectAsState()
    val hasSensor by StepTrackerService.hasSensor.collectAsState()

    // Read persisted session state — used when the service was killed and flows reset to defaults
    val sessionPrefs = remember { ctx.getSharedPreferences("step_session_state", Context.MODE_PRIVATE) }
    val savedIsRunning = remember { sessionPrefs.getBoolean("isRunning", false) }
    val savedSteps     = remember { sessionPrefs.getInt("steps", 0) }

    var showBatteryBanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Restart service if it was killed mid-session
        if (savedIsRunning && !isRunning) launchSvc(ctx, null)

        // Check battery optimization — request if not yet exempted, show banner if denied
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(ctx.packageName)) {
                try {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${ctx.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (_: Exception) {
                    showBatteryBanner = true
                }
            }
        }
    }

    var storedToday by remember { mutableIntStateOf(StepRepository(ctx).todayTotal()) }
    LaunchedEffect(isRunning) { if (!isRunning) storedToday = StepRepository(ctx).todayTotal() }

    // Priority: live flow > saved session from prefs > DB total
    val displaySteps = when {
        isRunning        -> svcSteps
        savedIsRunning   -> if (svcSteps > 0) svcSteps else savedSteps
        else             -> storedToday
    }

    var nowMs by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(Unit) { while (true) { delay(1000L); nowMs = SystemClock.elapsedRealtime() } }
    val elapsedMs = if (segStart > 0L) accMs + (nowMs - segStart) else accMs

    val goal     = SectionState.dailyGoal
    val progress = (displaySteps.toFloat() / goal).coerceIn(0f, 1f)
    val kcal     = (displaySteps * 0.04f).toInt()

    val targetColor = stepColor(displaySteps, goal, cs.primary, cs.secondary, cs.outline)
    val ringColor   by animateColorAsState(targetColor, tween(500), label = "rc")
    val animProg    by animateFloatAsState(progress, tween(600, easing = FastOutSlowInEasing), label = "rp")

    var hasPermission by remember { mutableStateOf(checkActivityPermission(ctx)) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            r[Manifest.permission.ACTIVITY_RECOGNITION] == true else true
    }

    var showEdit     by remember { mutableStateOf(false) }
    var showGoalEdit by remember { mutableStateOf(false) }

    if (showEdit) {
        EditStepsDialog(current = displaySteps, onDismiss = { showEdit = false }) { newTotal ->
            showEdit = false
            val repo = StepRepository(ctx)
            repo.deleteAllForDate(repo.todayKey())
            if (newTotal > 0) repo.addManual(repo.todayKey(), newTotal)
            storedToday = repo.todayTotal()
        }
    }
    if (showGoalEdit) {
        EditGoalDialog(current = goal, onDismiss = { showGoalEdit = false }) { newGoal ->
            showGoalEdit = false
            SectionState.dailyGoal = newGoal
            SectionState.save(ctx)
        }
    }

    val surfaceColor = cs.surface
    val trackColor   = cs.outlineVariant
    val usePlayPause = SectionState.usePlayPauseButtons

    Box(Modifier.fillMaxSize().background(cs.background).systemBarsPadding()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("STEPS", fontSize = 13.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp, color = cs.onBackground)
                Box(Modifier.size(36.dp).clip(CircleShape).background(cs.surface)
                    .clickable { onHistory() }, Alignment.Center) {
                    Icon(Icons.Rounded.History, contentDescription = "History",
                        tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            if (showBatteryBanner) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3CD))
                        .clickable {
                            showBatteryBanner = false
                            ctx.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${ctx.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Allow background usage in Battery settings",
                        fontSize = 12.sp, color = Color(0xFF7A5800),
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("Fix", fontSize = 12.sp, color = Color(0xFF7A5800),
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }

            Column(Modifier.weight(1f).fillMaxWidth().padding(bottom = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {

                when {
                    !hasSensor     -> InfoCard("No step sensor on this device.", cs)
                    !hasPermission -> PermCard(cs) {
                        permLauncher.launch(buildList {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                add(Manifest.permission.ACTIVITY_RECOGNITION)
                            if (Build.VERSION.SDK_INT >= 33)
                                add(Manifest.permission.POST_NOTIFICATIONS)
                        }.toTypedArray())
                    }
                    else -> {
                        // Ring
                        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp).aspectRatio(1f), Alignment.Center) {
                            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                                val sz = min(size.width, size.height)
                                val sw = sz * 0.065f
                                val r  = (sz - sw) / 2f
                                val cx = size.width / 2f; val cy = size.height / 2f
                                val tl = Offset(cx - r, cy - r); val arc = Size(r * 2, r * 2)
                                drawCircle(surfaceColor, r - sw / 2f - 2f, Offset(cx, cy))
                                drawArc(trackColor, -90f, 360f, false, tl, arc, style = Stroke(sw, cap = StrokeCap.Round))
                                if (animProg > 0.004f)
                                    drawArc(ringColor, -90f, animProg * 360f, false, tl, arc, style = Stroke(sw, cap = StrokeCap.Round))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Rounded.DirectionsWalk,
                                    contentDescription = null, tint = ringColor,
                                    modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(4.dp))
                                Text("$displaySteps",
                                    fontSize = 58.sp, fontWeight = FontWeight.Bold, lineHeight = 58.sp,
                                    color = if (displaySteps == 0 && !isRunning) cs.outline else ringColor)
                                Text("steps today", fontSize = 12.sp, color = cs.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(String.format("%.2f km", displaySteps * 0.762f / 1000f),
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
                                    Box(Modifier.size(3.dp).clip(CircleShape).background(cs.outline))
                                    Text("$kcal kcal",
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
                                    if (isRunning) {
                                        Box(Modifier.size(3.dp).clip(CircleShape).background(cs.outline))
                                        Text(fmtElapsed(elapsedMs), fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    when {
                                        displaySteps >= (goal * 1.2).toInt() -> "${displaySteps - goal} beyond goal"
                                        displaySteps >= goal -> "Daily goal reached"
                                        displaySteps > 0    -> "${goal - displaySteps} steps to daily goal"
                                        else                -> "Goal: $goal steps"
                                    },
                                    fontSize = 12.sp,
                                    color = when {
                                        displaySteps >= goal -> ringColor
                                        displaySteps > 0    -> cs.onSurfaceVariant
                                        else                -> cs.onSurfaceVariant
                                    },
                                    fontWeight = if (displaySteps >= goal) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Action button — text or play/pause mode
                        if (usePlayPause) {
                            Box(
                                Modifier.size(76.dp).clip(CircleShape)
                                    .background(if (isRunning) Color(0xFFF43F5E) else cs.primary)
                                    .clickable {
                                        if (isRunning) launchSvc(ctx, StepTrackerService.ACTION_STOP)
                                        else launchSvc(ctx, null)
                                    },
                                Alignment.Center
                            ) {
                                Icon(
                                    if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isRunning) "Stop & Save" else "Start",
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        } else {
                            Box(
                                Modifier.fillMaxWidth().padding(horizontal = 28.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isRunning) Color(0xFFF43F5E) else cs.primary)
                                    .clickable {
                                        if (isRunning) launchSvc(ctx, StepTrackerService.ACTION_STOP)
                                        else launchSvc(ctx, null)
                                    }
                                    .padding(vertical = 18.dp),
                                Alignment.Center
                            ) {
                                Text(if (isRunning) "Stop & Save" else "Start Walking",
                                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        if (!isRunning) {
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("Edit steps", fontSize = 13.sp, color = cs.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { showEdit = true })
                                Text("·", fontSize = 13.sp, color = cs.outline)
                                Text("Edit · Goal: $goal", fontSize = 13.sp, color = cs.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { showGoalEdit = true })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun InfoCard(msg: String, cs: ColorScheme) =
    Box(Modifier.fillMaxWidth().padding(24.dp).clip(RoundedCornerShape(20.dp))
        .background(cs.surface).padding(24.dp)) {
        Text(msg, fontSize = 15.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center) }

@Composable private fun PermCard(cs: ColorScheme, onRequest: () -> Unit) =
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(20.dp))
        .background(cs.surface).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Permission needed", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
        Spacer(Modifier.height(6.dp))
        Text("Activity recognition required to count steps.",
            fontSize = 13.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.primary)
            .clickable { onRequest() }.padding(vertical = 14.dp), Alignment.Center) {
            Text("Grant Permission", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White) } }

@Composable private fun EditStepsDialog(current: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf(current.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit today's steps") },
        text = {
            OutlinedTextField(value = text,
                onValueChange = { text = it.filter(Char::isDigit).take(6) },
                label = { Text("Total steps") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        },
        confirmButton = {
            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = text.isNotBlank()) { onConfirm(text.toIntOrNull() ?: 0) }
                .padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable private fun EditGoalDialog(current: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf(current.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change daily goal") },
        text = {
            OutlinedTextField(value = text,
                onValueChange = { text = it.filter(Char::isDigit).take(6) },
                label = { Text("Steps (e.g. 8000)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        },
        confirmButton = {
            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = (text.toIntOrNull() ?: 0) >= 100) {
                    onConfirm(text.toIntOrNull() ?: current)
                }
                .padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun checkActivityPermission(ctx: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
    return ctx.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
}
private fun launchSvc(ctx: Context, action: String?) {
    val i = Intent(ctx, com.example.bulk.StepTrackerService::class.java)
        .also { if (action != null) it.action = action }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
    else ctx.startService(i)
}
private fun fmtElapsed(ms: Long): String {
    val s = (ms / 1000).toInt(); val m = s / 60; val h = m / 60
    return if (h > 0) "${h}h ${m % 60}m" else "${m}m ${s % 60}s"
}

