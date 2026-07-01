package com.example.bulk.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.data.ExerciseItem

@Composable
fun HowToPanel(exercise: ExerciseItem?, visible: Boolean, onClose: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(360)) { it } + fadeIn(tween(280)),
        exit = slideOutVertically(tween(320)) { it } + fadeOut(tween(220))
    ) {
        Box(Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .systemBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 88.dp)
            ) {
                if (exercise == null) return@Column

                // Accent pill
                Box(
                    Modifier.size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp)).background(exercise.accent)
                )
                Spacer(Modifier.height(12.dp))

                Text(exercise.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))

                Spacer(Modifier.height(20.dp))

                // YouTube link
                val ctx = LocalContext.current
                val url = "https://www.youtube.com/results?search_query=" + Uri.encode(exercise.videoQuery)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(exercise.accent.copy(alpha = 0.07f))
                        .border(1.dp, exercise.accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                        .clickable { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(exercise.accent),
                        contentAlignment = Alignment.Center
                    ) { Text("▶", fontSize = 12.sp, color = Color.White) }
                    Column {
                        Text("Watch on YouTube", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        Text("See proper form", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(24.dp))
                SLabel("HOW TO DO IT")
                Spacer(Modifier.height(10.dp))
                exercise.steps.forEachIndexed { i, step ->
                    Row(Modifier.padding(bottom = 10.dp)) {
                        Text("${i + 1}.", fontSize = 14.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, modifier = Modifier.width(26.dp))
                        Text(step, fontSize = 14.sp, color = Color(0xFF475569), lineHeight = 21.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))
                SLabel("BAR PLACEMENT")
                Spacer(Modifier.height(8.dp))
                InfoBox(exercise.barPlacement)

                Spacer(Modifier.height(16.dp))
                SLabel("FORM CHECK")
                Spacer(Modifier.height(8.dp))
                InfoBox(exercise.formCheck)
            }

            // Close button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .systemBarsPadding()
                    .padding(20.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SLabel(text: String) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = Color(0xFF94A3B8))
}

@Composable
private fun InfoBox(text: String) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(text, fontSize = 14.sp, color = Color(0xFF475569), lineHeight = 21.sp)
    }
}
