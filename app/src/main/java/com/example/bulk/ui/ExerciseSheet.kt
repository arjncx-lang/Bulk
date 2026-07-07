package com.example.bulk.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bulk.data.ExerciseGroup
import com.example.bulk.data.ExerciseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSheet(
    groups: List<ExerciseGroup>,
    selected: ExerciseItem?,
    onSelect: (ExerciseItem) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.background,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp), Alignment.Center) {
                Box(Modifier.size(width = 36.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(cs.outlineVariant))
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 40.dp)) {
            Text(
                "Choose Exercise",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = cs.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
            )
            groups.forEach { group ->
                Text(
                    group.label,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
                )
                group.items.forEach { item ->
                    val isSel = item == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) item.accent.copy(alpha = 0.12f) else cs.surface)
                            .border(1.dp, if (isSel) item.accent.copy(.3f) else cs.outlineVariant, RoundedCornerShape(14.dp))
                            .clickable { onSelect(item) }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(item.accent.copy(.12f)),
                                Alignment.Center
                            ) { Text(item.name.take(1), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = item.accent) }
                            Column {
                                Text(
                                    item.name, fontSize = 15.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold,
                                    color = cs.onSurface
                                )
                                if (item.tip.isNotBlank()) {
                                    Text(item.tip, fontSize = 11.sp, color = cs.onSurfaceVariant)
                                }
                            }
                        }
                        Box(
                            Modifier.clip(RoundedCornerShape(50)).background(item.accent.copy(.15f)).padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text("${item.target} reps", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = item.accent)
                        }
                    }
                }
            }
        }
    }
}
