package com.example.bulk.ui

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.ui.theme.AppThemeState
import com.example.bulk.ui.theme.SectionState

@Composable
fun SettingsScreen(ctx: Context) {
    val cs   = MaterialTheme.colorScheme
    val dark = AppThemeState.isDarkMode

    Box(Modifier.fillMaxSize().background(cs.background).systemBarsPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text("⚙️  SETTINGS", fontSize = 13.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp, color = cs.onBackground)
            }

            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp)).background(cs.surface)) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dark Mode", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface)
                        Switch(
                            checked = dark,
                            onCheckedChange = { AppThemeState.isDarkMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = cs.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = cs.outlineVariant
                            )
                        )
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = cs.outlineVariant)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Play / Pause Controls", fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold, color = cs.onSurface)
                        Switch(
                            checked = SectionState.usePlayPauseButtons,
                            onCheckedChange = { SectionState.usePlayPauseButtons = it; SectionState.save(ctx) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = cs.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = cs.outlineVariant
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("SECTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp, color = cs.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp))

            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp)).background(cs.surface)) {
                Column {
                    SectionToggle("Steps",    SectionState.showSteps,
                        canToggle = SectionState.enabledCount > 1 || !SectionState.showSteps
                    ) { SectionState.showSteps = it; SectionState.save(ctx) }

                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = cs.outlineVariant)

                    SectionToggle("Train",    SectionState.showTrain,
                        canToggle = SectionState.enabledCount > 1 || !SectionState.showTrain
                    ) { SectionState.showTrain = it; SectionState.save(ctx) }

                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = cs.outlineVariant)

                    SectionToggle("Log",      SectionState.showWorkout,
                        canToggle = SectionState.enabledCount > 1 || !SectionState.showWorkout
                    ) { SectionState.showWorkout = it; SectionState.save(ctx) }

                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = cs.outlineVariant)

                    SectionToggle("Calendar", SectionState.showCalendar,
                        canToggle = SectionState.enabledCount > 1 || !SectionState.showCalendar
                    ) { SectionState.showCalendar = it; SectionState.save(ctx) }
                }
            }

            Text("At least one section must stay enabled.",
                fontSize = 11.sp, color = cs.outline,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp))

            Spacer(Modifier.height(48.dp))

            Column(Modifier.fillMaxWidth().padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("BULK  ·  v1.0", fontSize = 12.sp, letterSpacing = 3.sp,
                    color = cs.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Text("by Arjun", fontSize = 11.sp, color = cs.outline,
                    fontWeight = FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun SectionToggle(label: String, checked: Boolean, canToggle: Boolean, onToggle: (Boolean) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium,
            color = if (canToggle) cs.onSurface else cs.outline)
        Switch(
            checked = checked,
            onCheckedChange = if (canToggle) onToggle else null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = cs.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = cs.outlineVariant,
                disabledCheckedTrackColor = cs.primary.copy(0.4f),
                disabledCheckedThumbColor = Color.White.copy(0.7f)
            )
        )
    }
}
