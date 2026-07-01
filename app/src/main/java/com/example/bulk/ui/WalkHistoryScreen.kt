package com.example.bulk.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.example.bulk.data.DayGroup
import com.example.bulk.data.MonthGroup
import com.example.bulk.data.WalkRepository

@Composable
fun WalkHistoryScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val repo = remember { WalkRepository(ctx) }
    val dayGroups = remember { repo.groupByDay() }
    val monthGroups = remember { repo.groupByMonth() }

    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Days", "Weeks", "Months")

    val oneWeekAgo = remember { System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000 }
    val weekGroups = remember(dayGroups) { dayGroups.filter { it.dayStartMs >= oneWeekAgo } }

    Box(Modifier.fillMaxSize().background(Color(0xFFF2F4F8)).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            // Top bar
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape).clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) { Text("←", fontSize = 16.sp, color = Color(0xFF64748B)) }
                Text("HISTORY", fontSize = 13.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp, color = Color(0xFF1E293B))
                Spacer(Modifier.size(36.dp))
            }

            // Tab row
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                tabs.forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (tab == i) Color(0xFF1E293B) else Color.Transparent)
                            .clickable { tab = i }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (tab == i) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Content
            val isEmpty = when (tab) {
                0 -> dayGroups.isEmpty()
                1 -> weekGroups.isEmpty()
                else -> monthGroups.isEmpty()
            }

            if (isEmpty) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No walks yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8))
                        Spacer(Modifier.height(6.dp))
                        Text("Start a walk to see your history here",
                            fontSize = 14.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)
                    }
                }
            } else {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp)
                ) {
                    // Summary card
                    val allSessions = remember(dayGroups) { dayGroups.flatMap { it.sessions } }
                    HistorySummary(
                        totalSteps = allSessions.sumOf { it.steps },
                        totalDistKm = allSessions.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f,
                        sessionCount = allSessions.size
                    )
                    Spacer(Modifier.height(16.dp))

                    when (tab) {
                        0 -> dayGroups.forEach { group ->
                            DayCard(group)
                            Spacer(Modifier.height(10.dp))
                        }
                        1 -> if (weekGroups.isEmpty()) {
                            Text("No walks in the last 7 days", fontSize = 14.sp, color = Color(0xFF94A3B8))
                        } else weekGroups.forEach { group ->
                            DayCard(group)
                            Spacer(Modifier.height(10.dp))
                        }
                        else -> monthGroups.forEach { group ->
                            MonthCard(group)
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySummary(totalSteps: Int, totalDistKm: Float, sessionCount: Int) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF6366F1)).padding(20.dp)
    ) {
        Column {
            Text("TOTAL PROGRESS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, color = Color.White.copy(alpha = 0.65f))
            Spacer(Modifier.height(8.dp))
            Text("$totalSteps steps", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(String.format("%.1f km", totalDistKm), fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Text("·", fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
                Text("$sessionCount session${if (sessionCount != 1) "s" else ""}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun DayCard(group: DayGroup) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(group.dateLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text("${group.totalSteps} steps", fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, color = Color(0xFF6366F1))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${String.format("%.2f", group.totalDistKm)} km · ${group.sessions.size} session${if (group.sessions.size != 1) "s" else ""}",
                fontSize = 12.sp, color = Color(0xFF94A3B8)
            )
            if (group.sessions.size > 1) {
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))
                Spacer(Modifier.height(8.dp))
                group.sessions.forEachIndexed { idx, session ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Session ${idx + 1} · ${session.steps} steps",
                            fontSize = 13.sp, color = Color(0xFF475569))
                        Text(
                            "${String.format("%.2f", session.distanceMeters / 1000f)} km · ${fmtDur(session.durationMs)}",
                            fontSize = 12.sp, color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCard(group: MonthGroup) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(group.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text("${group.totalSteps} steps", fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, color = Color(0xFF6366F1))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${String.format("%.1f", group.totalDistKm)} km · ${group.sessionCount} session${if (group.sessionCount != 1) "s" else ""}",
                fontSize = 12.sp, color = Color(0xFF94A3B8)
            )
        }
    }
}

private fun fmtDur(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val m = totalSec / 60
    return if (m > 0) "${m}m ${totalSec % 60}s" else "${totalSec}s"
}
