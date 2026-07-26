package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExerciseLog
import com.example.data.model.Member
import com.example.data.model.PtSession
import com.example.data.model.PtSessionStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.viewmodel.GymViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerDashboardScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val members by viewModel.members.collectAsState()
    val ptSessions by viewModel.ptSessions.collectAsState()
    val ptAiFeedback by viewModel.ptAiFeedback.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    // Dashboard Sub-Tabs: 0 = Workout Session Logger, 1 = Client Roster, 2 = Session History
    var activeSubTab by remember { mutableStateOf(0) }

    // Quick FAB modal state
    var showQuickModal by remember { mutableStateOf(false) }

    // Active Member selected for session logging
    var selectedMemberForSession by remember {
        mutableStateOf<Member?>(members.firstOrNull())
    }
    var expandedMemberDropdown by remember { mutableStateOf(false) }

    // Session Meta Fields
    var startTimeText by remember { mutableStateOf("08:00 AM") }
    var endTimeText by remember { mutableStateOf("09:00 AM") }
    var sessionFocusCategory by remember { mutableStateOf("Push / Chest & Triceps") }

    // Multi-Exercise List State
    val exerciseList = remember {
        mutableStateListOf(
            ExerciseLog(name = "Flat Barbell Bench Press", sets = 4, reps = 10, weightKg = 75f, caloriesBurned = 90),
            ExerciseLog(name = "Incline Dumbbell Press", sets = 3, reps = 12, weightKg = 24f, caloriesBurned = 70)
        )
    }

    // Performance & Notes State
    var rpeRating by remember { mutableStateOf("8 (Hard Effort)") }
    var trainerNotesText by remember { mutableStateOf("Client executed excellent bench press form with steady bar control. Pushed for last 2 reps on incline press.") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var isPhotoAttached by remember { mutableStateOf(false) }

    // Status Banner feedback
    var lastSubmittedMessage by remember { mutableStateOf<String?>(null) }

    // Search query for history or roster
    var historySearchQuery by remember { mutableStateOf("") }
    var historyStatusFilter by remember { mutableStateOf("All") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Coach Marcus Header
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Coach Marcus",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Coach Marcus",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Senior PT Specialist • Strength & Conditioning",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Surface(
                            color = Color(0xFFFFB800).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("4.9 ⭐", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFFB800))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Trainer Metrics Summary Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val todayCount = ptSessions.size
                        val pendingCount = ptSessions.count { it.status == PtSessionStatus.PENDING_APPROVAL }
                        val approvedCount = ptSessions.count { it.status == PtSessionStatus.APPROVED }

                        MetricPill(
                            label = "Logged Today",
                            value = "$todayCount Sessions",
                            color = M3PurplePrimary
                        )
                        MetricPill(
                            label = "Pending Verification",
                            value = "$pendingCount Member",
                            color = IndigoSecondary
                        )
                        MetricPill(
                            label = "Approved Sessions",
                            value = "$approvedCount Complete",
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))



            // Sub-Navigation Segmented Tabs
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, "Log Session", Icons.Default.Add),
                        Triple(1, "Client Roster", Icons.Default.People),
                        Triple(2, "Session Logs", Icons.Default.History)
                    )

                    tabs.forEach { (tabIdx, label, icon) ->
                        val isSelected = activeSubTab == tabIdx
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) M3PurplePrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeSubTab = tabIdx }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tab Content
            when (activeSubTab) {
                0 -> {
                    // TAB 0: PT WORKOUT SESSION LOGGER FORM
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        if (lastSubmittedMessage != null) {
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = lastSubmittedMessage!!,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { lastSubmittedMessage = null }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Check, contentDescription = "Dismiss", tint = EmeraldPrimary)
                                    }
                                }
                            }
                        }

                        SectionHeader(
                            title = "Log Personal Training Workout Session",
                            subtitle = "Select client, record exercise sets/reps/weight, and submit for instant member verification"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. CLIENT SELECTION CARD
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Select Client / Member",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                ExposedDropdownMenuBox(
                                    expanded = expandedMemberDropdown,
                                    onExpandedChange = { expandedMemberDropdown = !expandedMemberDropdown }
                                ) {
                                    OutlinedTextField(
                                        value = selectedMemberForSession?.name ?: "Select Member",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMemberDropdown) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("pt_client_select_dropdown"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedMemberDropdown,
                                        onDismissRequest = { expandedMemberDropdown = false }
                                    ) {
                                        members.forEach { m ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(m.name, fontWeight = FontWeight.Bold)
                                                        Text("Plan: ${m.membershipPlan} • PT Package: ${m.ptPackageName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                                    }
                                                },
                                                onClick = {
                                                    selectedMemberForSession = m
                                                    expandedMemberDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                if (selectedMemberForSession != null) {
                                    val m = selectedMemberForSession!!
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Active Package: ${m.ptPackageName}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = M3PurplePrimary
                                                )
                                                Surface(
                                                    color = EmeraldPrimary.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "9/12 Sessions Left",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = EmeraldPrimary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Medical Notes: ${m.medicalConditions}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = "Goal: Hypertrophy & Fat Loss • Preferred Notes: ${m.notes}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. SESSION TIME & FOCUS CATEGORY
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Session Time Range", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = startTimeText,
                                        onValueChange = { startTimeText = it },
                                        label = { Text("Start Time") },
                                        modifier = Modifier.weight(1f).testTag("session_start_time")
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    OutlinedTextField(
                                        value = endTimeText,
                                        onValueChange = { endTimeText = it },
                                        label = { Text("End Time") },
                                        modifier = Modifier.weight(1f).testTag("session_end_time")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("08:00 AM - 09:00 AM", "11:00 AM - 12:00 PM", "05:00 PM - 06:00 PM").forEach { times ->
                                        val parts = times.split(" - ")
                                        FilterChip(
                                            selected = startTimeText == parts[0] && endTimeText == parts[1],
                                            onClick = {
                                                startTimeText = parts[0]
                                                endTimeText = parts[1]
                                            },
                                            label = { Text(times, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Workout Focus Category", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(6.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(
                                            selected = sessionFocusCategory == "Push / Chest & Triceps",
                                            onClick = { sessionFocusCategory = "Push / Chest & Triceps" },
                                            label = { Text("Push / Chest & Triceps", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = sessionFocusCategory == "Pull / Back & Biceps",
                                            onClick = { sessionFocusCategory = "Pull / Back & Biceps" },
                                            label = { Text("Pull / Back & Biceps", fontSize = 11.sp) }
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(
                                            selected = sessionFocusCategory == "Legs & Core",
                                            onClick = { sessionFocusCategory = "Legs & Core" },
                                            label = { Text("Legs & Core", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = sessionFocusCategory == "Desi Akhada Power",
                                            onClick = { sessionFocusCategory = "Desi Akhada Power" },
                                            label = { Text("Desi Akhada Power", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. EXERCISE LOG BUILDER (MULTI-EXERCISE LIST)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exercise Log Breakdown (${exerciseList.size} Exercises)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedButton(
                                onClick = {
                                    exerciseList.add(
                                        ExerciseLog(name = "New Exercise", sets = 3, reps = 10, weightKg = 20f)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Exercise", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Exercise Chips
                        Text("1-Tap Add Preset Exercise:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair("Bench Press", 75f),
                                Pair("Incline DB", 26f),
                                Pair("Lat Pulldown", 60f),
                                Pair("Squats", 80f),
                                Pair("Desi Dands", 0f),
                                Pair("Hindu Baithak", 0f)
                            ).forEach { (exName, exW) ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        exerciseList.add(ExerciseLog(name = exName, sets = 3, reps = 12, weightKg = exW, caloriesBurned = 75))
                                    }
                                ) {
                                    Text(
                                        text = "+ $exName",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = M3PurplePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Exercise List Items
                        exerciseList.forEachIndexed { index, exercise ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = M3PurplePrimary,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Exercise #${index + 1}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        }

                                        if (exerciseList.size > 1) {
                                            IconButton(
                                                onClick = { exerciseList.removeAt(index) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseDanger)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = exercise.name,
                                        onValueChange = { newName ->
                                            exerciseList[index] = exercise.copy(name = newName)
                                        },
                                        label = { Text("Exercise Name") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("exercise_name_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = exercise.sets.toString(),
                                            onValueChange = { sVal ->
                                                val sInt = sVal.toIntOrNull() ?: exercise.sets
                                                exerciseList[index] = exercise.copy(sets = sInt)
                                            },
                                            label = { Text("Sets") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("sets_input"),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedTextField(
                                            value = exercise.reps.toString(),
                                            onValueChange = { rVal ->
                                                val rInt = rVal.toIntOrNull() ?: exercise.reps
                                                exerciseList[index] = exercise.copy(reps = rInt)
                                            },
                                            label = { Text("Reps") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("reps_input"),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedTextField(
                                            value = exercise.weightKg.toInt().toString(),
                                            onValueChange = { wVal ->
                                                val wFloat = wVal.toFloatOrNull() ?: exercise.weightKg
                                                exerciseList[index] = exercise.copy(weightKg = wFloat)
                                            },
                                            label = { Text("Weight (kg)") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("weight_input"),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. TRAINER PERFORMANCE NOTES & RPE EFFORT
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Client Rate of Perceived Exertion (RPE Effort)", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("6 (Moderate)", "7 (Vigorous)", "8 (Hard Effort)", "9 (Near Max)").forEach { rpeOpt ->
                                        FilterChip(
                                            selected = rpeRating == rpeOpt,
                                            onClick = { rpeRating = rpeOpt },
                                            label = { Text(rpeOpt, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = trainerNotesText,
                                    onValueChange = { trainerNotesText = it },
                                    label = { Text("Trainer Performance & Form Notes") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("trainer_notes_input"),
                                    minLines = 3,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Gemini AI Coach Advice Button
                                OutlinedButton(
                                    onClick = {
                                        val exSummary = exerciseList.joinToString { "${it.name}: ${it.sets}x${it.reps} @ ${it.weightKg}kg" }
                                        viewModel.generatePtPerformanceAdvice(
                                            memberName = selectedMemberForSession?.name ?: "Client",
                                            exercisesStr = exSummary,
                                            trainerNotes = trainerNotesText
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_advice_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isAiLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gemini AI Analyzing Session...")
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = M3PurplePrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Get Gemini AI Progressive Overload Advice", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (ptAiFeedback != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = M3PurpleContainer,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Psychology, contentDescription = null, tint = M3PurpleOnContainer, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Gemini AI Coach Insights",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = M3PurpleOnContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = ptAiFeedback!!,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = M3PurpleOnContainer.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. ATTACHMENTS (Voice Note & Form Photo)
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isRecordingVoice = !isRecordingVoice }
                                ) {
                                    IconButton(
                                        onClick = { isRecordingVoice = !isRecordingVoice },
                                        modifier = Modifier.background(
                                            if (isRecordingVoice) RoseDanger else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Mic,
                                            contentDescription = "Voice Note",
                                            tint = if (isRecordingVoice) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Voice Memo", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = if (isRecordingVoice) "Recording voice note..." else "Tap mic to record audio note",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isRecordingVoice) RoseDanger else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Surface(
                                    color = if (isPhotoAttached) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.clickable { isPhotoAttached = !isPhotoAttached }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = if (isPhotoAttached) EmeraldPrimary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPhotoAttached) "Photo Attached ✓" else "+ Photo Proof",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isPhotoAttached) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 6. SUBMIT BUTTON
                        Button(
                            onClick = {
                                if (selectedMemberForSession != null && exerciseList.isNotEmpty()) {
                                    val member = selectedMemberForSession!!
                                    viewModel.logPtSession(
                                        member = member,
                                        startTime = startTimeText,
                                        endTime = endTimeText,
                                        exercises = exerciseList.toList(),
                                        trainerNotes = trainerNotesText,
                                        workoutNotes = "Focus: $sessionFocusCategory • RPE: $rpeRating"
                                    )
                                    lastSubmittedMessage = "PT Session logged for ${member.name}! Instant verification alert dispatched to member's app."
                                    activeSubTab = 2 // Navigate to Session Logs history tab
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_pt_session_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Submit", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit PT Session for Member Verification", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                1 -> {
                    // TAB 1: CLIENT ROSTER & PT PACKAGES
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SectionHeader(
                                title = "Assigned Personal Training Clients",
                                subtitle = "Manage active client packages, medical conditions, and session logs"
                            )
                        }

                        items(members) { m ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(m.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            Text("Plan: ${m.membershipPlan}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                        }

                                        Surface(
                                            color = EmeraldPrimary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = m.ptPackageName,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = EmeraldPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Medical Alerts: ${m.medicalConditions}", style = MaterialTheme.typography.bodySmall, color = RoseDanger)
                                        Text("Weight: ${m.weightKg.toInt()} kg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            selectedMemberForSession = m
                                            activeSubTab = 0
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Log Workout Session for ${m.name.split(" ").first()}")
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: LOGGED SESSIONS HISTORY
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        SectionHeader(
                            title = "PT Session Logs & Verification Status",
                            subtitle = "History of sessions submitted for client digital verification"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search & Filter
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = historySearchQuery,
                                onValueChange = { historySearchQuery = it },
                                placeholder = { Text("Search client name...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pt_history_search"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All", "Pending Approval", "Approved", "Rejected").forEach { filter ->
                                FilterChip(
                                    selected = historyStatusFilter == filter,
                                    onClick = { historyStatusFilter = filter },
                                    label = { Text(filter, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val filteredSessions = ptSessions.filter { session ->
                            val matchesSearch = session.memberName.contains(historySearchQuery, ignoreCase = true)
                            val matchesFilter = when (historyStatusFilter) {
                                "Approved" -> session.status == PtSessionStatus.APPROVED
                                "Pending Approval" -> session.status == PtSessionStatus.PENDING_APPROVAL
                                "Rejected" -> session.status == PtSessionStatus.REJECTED
                                else -> true
                            }
                            matchesSearch && matchesFilter
                        }

                        if (filteredSessions.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No PT sessions found matching criteria.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredSessions) { session ->
                                    PtSessionCardDetailed(
                                        session = session,
                                        onDuplicateTemplate = {
                                            selectedMemberForSession = members.find { it.id == session.memberId } ?: selectedMemberForSession
                                            exerciseList.clear()
                                            exerciseList.addAll(session.exercises)
                                            trainerNotesText = session.trainerNotes
                                            activeSubTab = 0
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                activeSubTab = 0
                if (selectedMemberForSession == null && members.isNotEmpty()) {
                    selectedMemberForSession = members.first()
                }
                showQuickModal = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("log_pt_session_fab"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Log PT Session", tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Log PT Session", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }

    // Quick Log Modal Dialog
    if (showQuickModal && selectedMemberForSession != null) {
        AlertDialog(
            onDismissRequest = { showQuickModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick PT Session Logger", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Client: ${selectedMemberForSession?.name}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = M3PurplePrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val firstEx = exerciseList.firstOrNull() ?: ExerciseLog("Bench Press", 4, 10, 75f)

                    OutlinedTextField(
                        value = firstEx.name,
                        onValueChange = { n ->
                            if (exerciseList.isNotEmpty()) {
                                exerciseList[0] = firstEx.copy(name = n)
                            } else {
                                exerciseList.add(ExerciseLog(n, 4, 10, 75f))
                            }
                        },
                        label = { Text("Exercise Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exercise_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = firstEx.sets.toString(),
                            onValueChange = { s -> exerciseList[0] = firstEx.copy(sets = s.toIntOrNull() ?: 4) },
                            label = { Text("Sets") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sets_input")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = firstEx.reps.toString(),
                            onValueChange = { r -> exerciseList[0] = firstEx.copy(reps = r.toIntOrNull() ?: 10) },
                            label = { Text("Reps") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reps_input")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = firstEx.weightKg.toInt().toString(),
                            onValueChange = { w -> exerciseList[0] = firstEx.copy(weightKg = w.toFloatOrNull() ?: 75f) },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("weight_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = trainerNotesText,
                        onValueChange = { trainerNotesText = it },
                        label = { Text("Trainer Performance Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trainer_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logPtSession(
                            member = selectedMemberForSession!!,
                            startTime = startTimeText,
                            endTime = endTimeText,
                            exercises = exerciseList.toList(),
                            trainerNotes = trainerNotesText,
                            workoutNotes = "Focus: $sessionFocusCategory"
                        )
                        showQuickModal = false
                        lastSubmittedMessage = "PT Session submitted for ${selectedMemberForSession?.name}!"
                    },
                    modifier = Modifier.testTag("save_pt_session_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Submit for Verification", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PtSessionCardDetailed(
    session: PtSession,
    onDuplicateTemplate: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.memberName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Logged by ${session.trainerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                val statusColor = when (session.status) {
                    PtSessionStatus.APPROVED -> EmeraldPrimary
                    PtSessionStatus.PENDING_APPROVAL -> IndigoSecondary
                    PtSessionStatus.REJECTED -> RoseDanger
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = session.status.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${session.startTime} - ${session.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "${session.exercises.size} Exercises Logged",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = M3PurplePrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exercise Pills
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                session.exercises.take(if (isExpanded) 10 else 2).forEach { ex ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${ex.name}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${ex.sets} sets x ${ex.reps} reps @ ${ex.weightKg.toInt()} kg",
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoSecondary
                            )
                        }
                    }
                }
            }

            if (session.exercises.size > 2) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isExpanded) "Collapse exercises ▲" else "View all ${session.exercises.size} exercises ▼",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = M3PurplePrimary,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }

            if (session.trainerNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trainer Notes: ${session.trainerNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDuplicateTemplate,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-use as Session Template", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = color, fontSize = 11.sp)
        }
    }
}
