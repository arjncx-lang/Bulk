package com.example.bulk.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

@Composable
fun RepCounterScreen() {
    val ctx   = LocalContext.current
    val cs    = MaterialTheme.colorScheme
    val repo  = remember { WorkoutRepository(ctx) }

    var count        by remember { mutableIntStateOf(0) }
    var selected     by remember { mutableStateOf<ExerciseItem?>(null) }
    var showPicker   by remember { mutableStateOf(false) }
    var showHowTo    by remember { mutableStateOf(false) }
    var autoLogShown by remember { mutableStateOf(false) }
    var savedToast   by remember { mutableStateOf(false) }
    var bumpKey      by remember { mutableIntStateOf(0) }

    val haptic = LocalHapticFeedback.current
    val done   = selected != null && count >= selected!!.target
    val accent = selected?.accent ?: cs.outlineVariant

    val ringColor by animateColorAsState(if (done) cs.secondary else accent, tween(500), label = "rc")

    LaunchedEffect(done) {
        if (!done) { autoLogShown = false; return@LaunchedEffect }
        if (!autoLogShown) {
            autoLogShown = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repo.addLog(WorkoutLog(id = System.currentTimeMillis(), dateKey = today,
                timestamp = System.currentTimeMillis(), exercise = selected!!.name,
                type = "bodyweight", sets = 1, reps = count, weightKg = 0f))
            savedToast = true
        }
    }
    LaunchedEffect(savedToast) { if (savedToast) { delay(1800); savedToast = false } }

    fun hit(n: Int) { count += n; bumpKey++; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }

    val statusText = when {
        selected == null -> "choose an exercise below"
        count == 0       -> "tap the ring to start"
        !done            -> "${selected!!.target - count} more to reach goal"
        count == selected!!.target -> selected!!.achieveMsg
        else             -> "+${count - selected!!.target} beyond goal"
    }
    val statusColor = when {
        selected == null || count == 0            -> cs.outlineVariant
        done && count == selected!!.target        -> cs.secondary
        done                                      -> Color(0xFFF59E0B)
        else                                      -> cs.onSurfaceVariant
    }

    val surfaceColor     = cs.surface
    val trackColor       = cs.outlineVariant
    val surfaceVariant   = cs.surfaceVariant
    val onSurfaceVariant = cs.onSurfaceVariant

    Box(Modifier.fillMaxSize().background(cs.background).systemBarsPadding()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🏋️  TRAIN", fontSize = 13.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp, color = cs.onBackground)
                if (selected != null) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(cs.surface)
                        .clickable { showHowTo = true }, Alignment.Center) {
                        Text("ⓘ", fontSize = 15.sp, color = cs.onSurfaceVariant)
                    }
                }
            }

            Column(Modifier.fillMaxWidth().weight(1f).padding(bottom = 20.dp),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

                Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).aspectRatio(1f), Alignment.Center) {
                    RingZone(
                        progress    = selected?.let { (count.toFloat() / it.target).coerceIn(0f, 1f) } ?: 0f,
                        ringColor   = ringColor, count = count, bumpKey = bumpKey,
                        label       = selected?.name, statusText = statusText, statusColor = statusColor,
                        surfaceColor = surfaceColor, trackColor = trackColor,
                        onClick     = { hit(1) }
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(2, 5, 10).forEach { n ->
                        ChipBtn("+$n", Modifier.weight(1f),
                            bg = surfaceVariant, fg = onSurfaceVariant) { hit(n) }
                    }
                    ChipBtn("↺", Modifier.weight(1f),
                        bg = Color(0xFFF43F5E), fg = Color.White) {
                        count = 0; autoLogShown = false; bumpKey++
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cs.surface)
                        .clickable { showPicker = true }
                        .padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (selected != null) {
                            Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                                .background(selected!!.accent.copy(.12f)), Alignment.Center) {
                                Text(selected!!.emoji, fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(selected?.name ?: "Choose Exercise",
                                fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                                color = if (selected != null) cs.onSurface else cs.outlineVariant)
                            if (selected != null)
                                Text("Target · ${selected!!.target} reps",
                                    fontSize = 12.sp, color = cs.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Rounded.ExpandMore, contentDescription = null,
                        tint = cs.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
            }
        }

        // "Saved" toast
        if (savedToast) {
            Box(Modifier.fillMaxSize().padding(bottom = 120.dp), Alignment.BottomCenter) {
                Box(Modifier.clip(RoundedCornerShape(20.dp)).background(cs.secondary.copy(.9f))
                    .padding(horizontal = 22.dp, vertical = 11.dp)) {
                    Text("Saved to log ✓", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }

        if (showPicker) {
            ExerciseSheet(exerciseGroups, selected,
                onSelect = { selected = it; count = 0; autoLogShown = false; bumpKey++; showPicker = false },
                onDismiss = { showPicker = false })
        }
        HowToPanel(selected, showHowTo) { showHowTo = false }
    }
}

@Composable
private fun RingZone(
    progress: Float, ringColor: Color, count: Int, bumpKey: Int,
    label: String?, statusText: String, statusColor: Color,
    surfaceColor: Color, trackColor: Color,
    onClick: () -> Unit
) {
    val animProg  by animateFloatAsState(progress, tween(550, easing = FastOutSlowInEasing), label = "p")
    var scaleT    by remember { mutableFloatStateOf(1f) }
    val animScale by animateFloatAsState(scaleT, spring(0.28f, 520f), label = "s")
    LaunchedEffect(bumpKey) { if (bumpKey > 0) { scaleT = 1.13f; delay(75); scaleT = 1f } }

    val cs     = MaterialTheme.colorScheme
    val fa     = remember { Animatable(0f) }
    val fy     = remember { Animatable(0f) }
    val scope  = rememberCoroutineScope()
    LaunchedEffect(bumpKey) {
        if (bumpKey > 0) {
            fa.snapTo(1f); fy.snapTo(0f)
            scope.launch { fa.animateTo(0f, tween(700)) }
            fy.animateTo(-64f, tween(700, easing = FastOutSlowInEasing))
        }
    }

    Box(Modifier.fillMaxSize().clickable(
        interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(10.dp)) {
            val sz = min(size.width, size.height); val sw = sz * 0.072f; val r = (sz - sw) / 2f
            val cx = size.width / 2f; val cy = size.height / 2f
            val tl = Offset(cx - r, cy - r); val arc = Size(r * 2, r * 2)
            drawCircle(surfaceColor, r - sw / 2f - 2f, Offset(cx, cy))
            drawArc(trackColor, -90f, 360f, false, tl, arc, style = Stroke(sw, cap = StrokeCap.Round))
            if (animProg > 0.004f)
                drawArc(ringColor, -90f, animProg * 360f, false, tl, arc, style = Stroke(sw, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(animScale)) {
            Text(count.toString(), fontSize = 82.sp, fontWeight = FontWeight.Bold, lineHeight = 82.sp,
                color = if (label == null) cs.outline else ringColor)
            Text(label ?: "tap to count", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(statusText, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                color = statusColor, textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp))
        }
        Text("+1", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ringColor,
            modifier = Modifier.align(Alignment.Center).offset(y = fy.value.dp).alpha(fa.value))
    }
}

@Composable
private fun ChipBtn(text: String, modifier: Modifier, bg: Color, fg: Color, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (pressed) 0.91f else 1f, spring(stiffness = Spring.StiffnessHigh), label = "s")
    LaunchedEffect(pressed) { if (pressed) { delay(130); pressed = false } }
    Box(
        modifier.scale(s).clip(RoundedCornerShape(16.dp)).background(bg)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                pressed = true; onClick()
            }
            .padding(vertical = 15.dp),
        Alignment.Center
    ) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = fg) }
}
