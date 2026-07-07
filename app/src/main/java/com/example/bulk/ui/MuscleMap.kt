package com.example.bulk.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.data.MuscleKey

private enum class Region { HEAD, CHEST, ABS, SHOULDERS, UPPER_ARMS, FOREARMS, UPPER_BACK, LOWER_BACK, GLUTES, THIGHS, CALVES }

private fun MuscleKey.regions(): Pair<Set<Region>, Set<Region>> = when (this) {
    // Pair = (front figure highlights, back figure highlights)
    MuscleKey.CHEST         -> setOf(Region.CHEST) to emptySet()
    MuscleKey.ABS           -> setOf(Region.ABS) to emptySet()
    MuscleKey.SHOULDERS     -> setOf(Region.SHOULDERS) to setOf(Region.SHOULDERS)
    MuscleKey.BICEPS        -> setOf(Region.UPPER_ARMS) to emptySet()
    MuscleKey.TRICEPS       -> emptySet<Region>() to setOf(Region.UPPER_ARMS)
    MuscleKey.BACK          -> emptySet<Region>() to setOf(Region.UPPER_BACK, Region.LOWER_BACK)
    MuscleKey.GLUTES        -> emptySet<Region>() to setOf(Region.GLUTES)
    MuscleKey.LEGS          -> setOf(Region.THIGHS, Region.CALVES) to setOf(Region.THIGHS, Region.CALVES)
    MuscleKey.CHEST_TRICEPS -> setOf(Region.CHEST) to setOf(Region.UPPER_ARMS)
}

/** Front and back body silhouettes with the working muscles pulsing in the accent color. */
@Composable
fun MuscleMap(key: MuscleKey, accent: Color, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val base = cs.outlineVariant
    val pulse by rememberInfiniteTransition(label = "mm").animateFloat(
        0.55f, 1f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val (front, back) = key.regions()

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Figure(front, base, accent, pulse)
            Figure(back, base, accent, pulse)
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(66.dp)) {
            Text("FRONT", fontSize = 9.sp, letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold, color = cs.outline)
            Text("BACK", fontSize = 9.sp, letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold, color = cs.outline)
        }
    }
}

@Composable
private fun Figure(lit: Set<Region>, base: Color, accent: Color, pulse: Float) {
    Canvas(Modifier.size(width = 92.dp, height = 168.dp)) {
        fun c(vararg r: Region) = if (r.any { it in lit }) accent.copy(alpha = pulse) else base
        val u = size.width / 92f            // scale unit so shapes track canvas size
        fun rr(x: Float, y: Float, w: Float, h: Float, col: Color, rad: Float = 7f) =
            drawRoundRect(col, Offset(x * u, y * u), Size(w * u, h * u), CornerRadius(rad * u))

        drawCircle(c(Region.HEAD), 10f * u, Offset(46f * u, 12f * u))
        drawCircle(c(Region.SHOULDERS), 8f * u, Offset(24f * u, 34f * u))   // shoulders
        drawCircle(c(Region.SHOULDERS), 8f * u, Offset(68f * u, 34f * u))
        rr(30f, 27f, 32f, 26f, c(Region.CHEST, Region.UPPER_BACK))          // chest / upper back
        rr(32f, 55f, 28f, 24f, c(Region.ABS, Region.LOWER_BACK))            // abs / lower back
        rr(15f, 40f, 11f, 26f, c(Region.UPPER_ARMS))                        // upper arms
        rr(66f, 40f, 11f, 26f, c(Region.UPPER_ARMS))
        rr(15f, 68f, 10f, 24f, c(Region.FOREARMS))                          // forearms
        rr(67f, 68f, 10f, 24f, c(Region.FOREARMS))
        rr(31f, 81f, 30f, 14f, c(Region.GLUTES))                            // hips / glutes
        rr(32f, 97f, 13f, 32f, c(Region.THIGHS))                            // thighs
        rr(47f, 97f, 13f, 32f, c(Region.THIGHS))
        rr(33f, 131f, 11f, 30f, c(Region.CALVES))                           // calves
        rr(48f, 131f, 11f, 30f, c(Region.CALVES))
    }
}
