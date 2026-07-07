package com.example.bulk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.ui.*
import com.example.bulk.ui.theme.AppThemeState
import com.example.bulk.ui.theme.BulkTheme
import com.example.bulk.ui.theme.SectionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SectionState.load(this)
        enableEdgeToEdge()
        setContent { BulkTheme { AppNavHost() } }
    }

}

private enum class Tab { STEPS, TRAIN, WORKOUT, CALENDAR, SETTINGS }

private data class NavItem(val tab: Tab, val icon: ImageVector, val label: String, val color: Color)

private val ALL_NAV_ITEMS = listOf(
    NavItem(Tab.STEPS,    Icons.AutoMirrored.Rounded.DirectionsWalk,     "Steps",    Color(0xFF6366F1)),
    NavItem(Tab.TRAIN,    Icons.Rounded.FitnessCenter,                   "Train",    Color(0xFFEC4899)),
    NavItem(Tab.WORKOUT,  Icons.AutoMirrored.Rounded.FormatListBulleted, "Log",      Color(0xFF22C55E)),
    NavItem(Tab.CALENDAR, Icons.Rounded.CalendarMonth,                   "Calendar", Color(0xFFF59E0B)),
    NavItem(Tab.SETTINGS, Icons.Rounded.Settings,                        "Settings", Color(0xFF94A3B8))
)

@Composable
private fun AppNavHost() {
    val ctx       = LocalContext.current
    val dark      = AppThemeState.isDarkMode
    val navBg     = if (dark) Color(0xFF1E293B) else Color.White
    val navBorder = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val visibleItems = remember(SectionState.showSteps, SectionState.showTrain,
        SectionState.showWorkout, SectionState.showCalendar) {
        ALL_NAV_ITEMS.filter { item ->
            when (item.tab) {
                Tab.STEPS    -> SectionState.showSteps
                Tab.TRAIN    -> SectionState.showTrain
                Tab.WORKOUT  -> SectionState.showWorkout
                Tab.CALENDAR -> SectionState.showCalendar
                Tab.SETTINGS -> true
            }
        }
    }

    var selectedTab     by remember { mutableStateOf(Tab.STEPS) }
    var showStepHistory by remember { mutableStateOf(false) }

    LaunchedEffect(SectionState.showSteps, SectionState.showTrain,
        SectionState.showWorkout, SectionState.showCalendar) {
        val visibleTabs = visibleItems.map { it.tab }
        if (selectedTab !in visibleTabs) {
            selectedTab = visibleTabs.firstOrNull { it != Tab.SETTINGS } ?: Tab.SETTINGS
        }
    }

    Box(Modifier.fillMaxSize().background(if (dark) Color(0xFF0F172A) else Color(0xFFF2F4F8))) {
        Box(Modifier.fillMaxSize().padding(bottom = 74.dp)) {
            when {
                showStepHistory             -> StepHistoryScreen(onBack = { showStepHistory = false })
                selectedTab == Tab.STEPS    -> StepTrackerScreen(onHistory = { showStepHistory = true })
                selectedTab == Tab.TRAIN    -> RepCounterScreen()
                selectedTab == Tab.WORKOUT  -> WorkoutScreen(onBack = { selectedTab = Tab.STEPS })
                selectedTab == Tab.CALENDAR -> CalendarScreen(onBack = { selectedTab = Tab.STEPS })
                selectedTab == Tab.SETTINGS -> SettingsScreen(ctx)
            }
        }

        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(navBg).navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(navBorder))
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                visibleItems.forEach { item ->
                    val isSel = !showStepHistory && selectedTab == item.tab
                    BottomNavItem(item, isSel, dark) {
                        showStepHistory = false; selectedTab = item.tab
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(item: NavItem, selected: Boolean, dark: Boolean, onClick: () -> Unit) {
    val bgColor   by animateColorAsState(if (selected) item.color.copy(.12f) else Color.Transparent, tween(220), label = "bg")
    val iconColor by animateColorAsState(
        if (selected) item.color else if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
        tween(220), label = "ic")

    Column(
        Modifier.clip(RoundedCornerShape(14.dp)).background(bgColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(item.icon, contentDescription = item.label, tint = iconColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(item.label, fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = iconColor)
        if (selected) {
            Spacer(Modifier.height(2.dp))
            Box(Modifier.size(4.dp).clip(CircleShape).background(item.color))
        }
    }
}
