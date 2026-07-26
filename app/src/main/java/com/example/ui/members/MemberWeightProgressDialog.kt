package com.example.ui.members

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Member
import com.example.data.model.WeightLog
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.RoseDanger
import com.example.ui.viewmodel.GymViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemberWeightProgressDialog(
    member: Member,
    viewModel: GymViewModel,
    onDismiss: () -> Unit
) {
    val weightLogsState = viewModel.getWeightLogsForMember(member.id).collectAsState(initial = emptyList())
    val weightLogs = weightLogsState.value

    val todayDateStr = remember { SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date()) }
    var inputWeight by remember { mutableStateOf(member.weightKg.toString()) }
    var inputDate by remember { mutableStateOf(todayDateStr) }
    var inputNote by remember { mutableStateOf("") }
    var selectedLogIndex by remember { mutableIntStateOf(-1) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("weight_progress_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorWeight,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${member.name}'s Weight Progress",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "CRM Member Weight Tracking & Trend Graph",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stat Cards Row
                val startingWeight = weightLogs.firstOrNull()?.weightKg ?: member.weightKg
                val currentWeight = weightLogs.lastOrNull()?.weightKg ?: member.weightKg
                val totalChange = currentWeight - startingWeight

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(
                        title = "Starting",
                        value = "${startingWeight} kg",
                        color = IndigoSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        title = "Current",
                        value = "${currentWeight} kg",
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        title = "Total Delta",
                        value = "${if (totalChange > 0) "+" else ""}${String.format("%.1f", totalChange)} kg",
                        color = if (totalChange <= 0) EmeraldPrimary else RoseDanger,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Weight Progress Line Graph
                Text(
                    text = "Weight Trend Graph",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (weightLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "No weight logs recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "Log the first entry below to render the trend line.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    WeightLineChart(
                        logs = weightLogs,
                        selectedIndex = selectedLogIndex,
                        onSelectIndex = { selectedLogIndex = it }
                    )
                }

                // Selected point callout
                if (selectedLogIndex in weightLogs.indices) {
                    val selLog = weightLogs[selectedLogIndex]
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = IndigoSecondary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Log: ${selLog.date} • ${selLog.weightKg} kg",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = IndigoSecondary
                                )
                                if (selLog.note.isNotBlank()) {
                                    Text(
                                        text = "Note: ${selLog.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            TextButton(onClick = { selectedLogIndex = -1 }) {
                                Text("Clear Selection", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Log New Weight Form
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Log New Weight Reading",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = inputWeight,
                                onValueChange = { inputWeight = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("log_weight_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = inputDate,
                                onValueChange = { inputDate = it },
                                label = { Text("Date") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("log_date_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputNote,
                            onValueChange = { inputNote = it },
                            label = { Text("Note (e.g., Morning empty stomach, post-workout)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("log_note_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val w = inputWeight.toFloatOrNull()
                                if (w != null && w > 0f) {
                                    viewModel.addWeightLog(
                                        memberId = member.id,
                                        weightKg = w,
                                        date = inputDate.ifBlank { todayDateStr },
                                        note = inputNote
                                    )
                                    inputNote = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_weight_log_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Weight Entry", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // History Log Table
                Text(
                    text = "Historical Logs (${weightLogs.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val reversedLogs = remember(weightLogs) { weightLogs.reversed() }
                val cardBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                val chipBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                val primaryColor = MaterialTheme.colorScheme.primary

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    reversedLogs.forEachIndexed { index, log ->
                        val prevLog = reversedLogs.getOrNull(index + 1)
                        val diff = if (prevLog != null) log.weightKg - prevLog.weightKg else 0f

                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = chipBgColor,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = log.date,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = primaryColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${log.weightKg} kg",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (log.note.isNotBlank()) {
                                            Text(
                                                text = log.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (prevLog != null) {
                                        val diffColor = if (diff <= 0f) EmeraldPrimary else RoseDanger
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (diff <= 0f) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = diffColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "${if (diff > 0) "+" else ""}${String.format("%.1f", diff)} kg",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = diffColor
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteWeightLog(log.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete log",
                                            tint = RoseDanger.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
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

@Composable
fun StatBadge(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
fun WeightLineChart(
    logs: List<WeightLog>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val minWeight = (logs.minOfOrNull { it.weightKg } ?: 50f) - 2f
    val maxWeight = (logs.maxOfOrNull { it.weightKg } ?: 100f) + 2f
    val range = if (maxWeight - minWeight < 1f) 5f else maxWeight - minWeight

    val emeraldColor = EmeraldPrimary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable {
                    // Tap point index calculation
                }
        ) {
            val width = size.width
            val height = size.height - 30f // Space for bottom labels

            if (logs.size == 1) {
                val point = Offset(width / 2f, height / 2f)
                drawCircle(color = emeraldColor, radius = 8.dp.toPx(), center = point)
                return@Canvas
            }

            // Draw horizontal grid lines (3 steps)
            val steps = 3
            for (i in 0..steps) {
                val y = height - (i * (height / steps))
                val weightVal = minWeight + (i * (range / steps))

                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "${weightVal.toInt()} kg",
                    8f,
                    y - 6f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                    }
                )
            }

            // Calculate point offsets
            val points = logs.indices.map { index ->
                val x = (index.toFloat() / (logs.size - 1)) * width
                val normalizedY = (logs[index].weightKg - minWeight) / range
                val y = height - (normalizedY * height)
                Offset(x, y)
            }

            // Fill area path
            val fillPath = Path().apply {
                moveTo(points.first().x, height)
                lineTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    val controlX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                    val controlY1 = pPrev.y
                    val controlX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                    val controlY2 = pCurr.y
                    cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                }
                lineTo(points.last().x, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        emeraldColor.copy(alpha = 0.35f),
                        emeraldColor.copy(alpha = 0.02f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Line path
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    val controlX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                    val controlY1 = pPrev.y
                    val controlX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                    val controlY2 = pCurr.y
                    cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                }
            }

            drawPath(
                path = linePath,
                color = emeraldColor,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Point nodes and date text labels
            points.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex

                drawCircle(
                    color = if (isSelected) Color.White else emeraldColor,
                    radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = if (isSelected) emeraldColor else Color.Black,
                    radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
                    center = point
                )

                // Date label below
                drawContext.canvas.nativeCanvas.drawText(
                    logs[index].date,
                    point.x - 20f,
                    size.height - 4f,
                    android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.LTGRAY
                        textSize = 24f
                        isFakeBoldText = isSelected
                    }
                )

                // Weight value above
                drawContext.canvas.nativeCanvas.drawText(
                    "${logs[index].weightKg}",
                    point.x - 22f,
                    point.y - 12f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 26f
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}
