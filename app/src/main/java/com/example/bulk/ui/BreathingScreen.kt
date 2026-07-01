package com.example.bulk.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.ui.theme.AppThemeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BreathPattern(
    val name: String,
    val desc: String,
    val inhale: Int,
    val holdIn: Int,
    val exhale: Int,
    val holdOut: Int
)

val BREATH_PATTERNS = listOf(
    BreathPattern("Box 4-4-4-4",    "Calm & focused",  4, 4, 4, 4),
    BreathPattern("4-7-8 Relax",    "Deep relaxation", 4, 7, 8, 0),
    BreathPattern("3-0-6 Calm",     "Gentle calming",  3, 0, 6, 0),
    BreathPattern("Energize 6-2-2", "Morning boost",   6, 2, 2, 0)
)

private enum class BPhase { IDLE, INHALE, HOLD_IN, EXHALE, HOLD_OUT }

@Composable
fun BreathingScreen(onSettings: () -> Unit) {
    val dark = AppThemeState.isDarkMode

    val bgBrush = if (dark)
        Brush.verticalGradient(listOf(Color(0xFF081828), Color(0xFF061812)))
    else
        Brush.verticalGradient(listOf(Color(0xFFD8EEFB), Color(0xFFCCF5E2)))

    var selected  by remember { mutableStateOf(BREATH_PATTERNS[0]) }
    var isRunning by remember { mutableStateOf(false) }
    var phase     by remember { mutableStateOf(BPhase.IDLE) }
    var countdown by remember { mutableIntStateOf(0) }
    var rounds    by remember { mutableIntStateOf(0) }

    val circleScale = remember { Animatable(0.48f) }
    val scope       = rememberCoroutineScope()

    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) { while (true) { delay(30_000L); now = Date() } }

    val phaseColor by animateColorAsState(
        when (phase) {
            BPhase.INHALE   -> if (dark) Color(0xFF38BDF8) else Color(0xFF0EA5E9)
            BPhase.HOLD_IN  -> if (dark) Color(0xFF5EEAD4) else Color(0xFF14B8A6)
            BPhase.EXHALE   -> if (dark) Color(0xFF34D399) else Color(0xFF10B981)
            BPhase.HOLD_OUT -> if (dark) Color(0xFF86EFAC) else Color(0xFF22C55E)
            BPhase.IDLE     -> if (dark) Color(0xFF38BDF8) else Color(0xFF0EA5E9)
        }, tween(600), label = "pc"
    )

    LaunchedEffect(isRunning, selected) {
        if (!isRunning) {
            phase = BPhase.IDLE
            circleScale.animateTo(0.48f, tween(900, easing = FastOutSlowInEasing))
            return@LaunchedEffect
        }
        while (isRunning) {
            phase = BPhase.INHALE
            launch { circleScale.animateTo(1f, tween(selected.inhale * 1000, easing = LinearEasing)) }
            for (i in selected.inhale downTo 1) { countdown = i; delay(1000L) }

            if (selected.holdIn > 0) {
                phase = BPhase.HOLD_IN
                for (i in selected.holdIn downTo 1) { countdown = i; delay(1000L) }
            }

            phase = BPhase.EXHALE
            launch { circleScale.animateTo(0.48f, tween(selected.exhale * 1000, easing = LinearEasing)) }
            for (i in selected.exhale downTo 1) { countdown = i; delay(1000L) }

            if (selected.holdOut > 0) {
                phase = BPhase.HOLD_OUT
                for (i in selected.holdOut downTo 1) { countdown = i; delay(1000L) }
            }
            rounds++
        }
    }

    Box(Modifier.fillMaxSize().background(bgBrush).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {

            // Header — date/time + settings icon
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
                        fontSize = 30.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color.White else Color(0xFF0369A1)
                    )
                    Text(
                        SimpleDateFormat("EEEE, d MMM", Locale.getDefault()).format(now),
                        fontSize = 13.sp,
                        color = if (dark) Color.White.copy(.5f) else Color(0xFF0369A1).copy(.65f)
                    )
                }
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .background(if (dark) Color.White.copy(.08f) else Color(0xFF0EA5E9).copy(.1f))
                        .clickable { onSettings() },
                    Alignment.Center
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings",
                        tint = if (dark) Color.White.copy(.6f) else Color(0xFF0369A1),
                        modifier = Modifier.size(18.dp))
                }
            }

            // Rounds badge (visible when session is active)
            if (isRunning || rounds > 0) {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(phaseColor.copy(.12f)).padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text("$rounds completed", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = phaseColor)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Pattern selector (hidden while running)
            if (!isRunning) {
                Spacer(Modifier.height(4.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BREATH_PATTERNS) { pat ->
                        val sel = pat == selected
                        Box(
                            Modifier.clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (sel) (if (dark) Color(0xFF0EA5E9).copy(.22f) else Color(0xFF0EA5E9).copy(.12f))
                                    else (if (dark) Color.White.copy(.05f) else Color.White.copy(.6f))
                                )
                                .border(1.dp,
                                    if (sel) (if (dark) Color(0xFF38BDF8).copy(.5f) else Color(0xFF0EA5E9).copy(.35f))
                                    else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { selected = pat; rounds = 0 }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(pat.name, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    color = if (dark) Color.White else Color(0xFF0369A1))
                                Text(pat.desc, fontSize = 10.sp,
                                    color = if (dark) Color.White.copy(.45f) else Color(0xFF0369A1).copy(.55f))
                                Spacer(Modifier.height(5.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BTag("in ${pat.inhale}s",   if (dark) Color(0xFF38BDF8) else Color(0xFF0EA5E9))
                                    if (pat.holdIn > 0) BTag("hold ${pat.holdIn}s", if (dark) Color(0xFF5EEAD4) else Color(0xFF14B8A6))
                                    BTag("out ${pat.exhale}s", if (dark) Color(0xFF34D399) else Color(0xFF10B981))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            } else {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    Text(selected.name, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = if (dark) Color.White.copy(.45f) else Color(0xFF0369A1).copy(.5f))
                }
                Spacer(Modifier.height(8.dp))
            }

            // Circle — positioned in lower portion via weight
            Box(Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp), Alignment.BottomCenter) {
                // Outer glow layers
                Box(Modifier.size(320.dp).scale(circleScale.value * 1.22f).clip(CircleShape).background(phaseColor.copy(.05f)).align(Alignment.Center))
                Box(Modifier.size(320.dp).scale(circleScale.value * 1.10f).clip(CircleShape).background(phaseColor.copy(.09f)).align(Alignment.Center))
                // Main circle
                Box(
                    Modifier.size(320.dp).scale(circleScale.value).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(phaseColor.copy(.55f), phaseColor.copy(.2f))))
                        .border(2.dp, phaseColor.copy(.45f), CircleShape)
                        .align(Alignment.Center),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isRunning && phase != BPhase.IDLE) {
                            Text("$countdown", fontSize = 70.sp, fontWeight = FontWeight.Bold, lineHeight = 70.sp,
                                color = if (dark) Color.White else Color(0xFF0C4A6E))
                            Text(when (phase) {
                                BPhase.INHALE   -> "INHALE"
                                BPhase.HOLD_IN  -> "HOLD"
                                BPhase.EXHALE   -> "EXHALE"
                                BPhase.HOLD_OUT -> "HOLD"
                                BPhase.IDLE     -> ""
                            }, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp,
                                color = if (dark) Color.White.copy(.75f) else Color(0xFF0C4A6E).copy(.8f),
                                textAlign = TextAlign.Center)
                        } else {
                            Text(if (rounds > 0) "$rounds done" else "ready",
                                fontSize = 20.sp, fontWeight = FontWeight.Light,
                                color = if (dark) Color.White.copy(.4f) else Color(0xFF0C4A6E).copy(.4f))
                        }
                    }
                }
            }

            // Start / Stop button — always visible at fixed bottom position
            Box(
                Modifier.fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp, top = 16.dp, bottom = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isRunning)
                            if (dark) Color(0xFFEF4444).copy(.15f) else Color(0xFFFEE2E2)
                        else
                            if (dark) Color(0xFF0EA5E9).copy(.22f) else Color(0xFF0EA5E9)
                    )
                    .border(1.dp,
                        if (isRunning) Color(0xFFEF4444).copy(.35f) else Color(0xFF0EA5E9).copy(if (dark) .4f else .0f),
                        RoundedCornerShape(20.dp))
                    .clickable { if (isRunning) { isRunning = false } else { rounds = 0; isRunning = true } }
                    .padding(vertical = 18.dp),
                Alignment.Center
            ) {
                Text(
                    if (isRunning) "Stop Session" else "Begin Breathing",
                    fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = if (isRunning) Color(0xFFEF4444) else Color.White
                )
            }
        }
    }
}

@Composable private fun BTag(label: String, color: Color) =
    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(.12f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(label, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
    }
