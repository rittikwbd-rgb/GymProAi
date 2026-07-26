package com.example.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.LoggedExercise
import com.example.data.model.LoggedSet
import com.example.data.model.LoggedWorkoutSession
import com.example.data.model.Member
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.RoseDanger
import com.example.ui.viewmodel.GymViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Mutable draft state representation for UI editing
private data class DraftExercise(
    var name: String,
    val sets: MutableList<DraftSet> = mutableStateListOf()
)

private data class DraftSet(
    var setNumber: Int,
    var repsText: String,
    var weightText: String,
    var isCompleted: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLoggingScreen(
    viewModel: GymViewModel,
    preselectedMember: Member? = null,
    modifier: Modifier = Modifier
) {
    val members by viewModel.members.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedMemberState by viewModel.selectedMember.collectAsState()

    var activeMember by remember {
        mutableStateOf(preselectedMember ?: selectedMemberState ?: members.firstOrNull())
    }

    LaunchedEffect(preselectedMember, selectedMemberState, members) {
        if (preselectedMember != null) {
            activeMember = preselectedMember
        } else if (activeMember == null && members.isNotEmpty()) {
            activeMember = selectedMemberState ?: members.firstOrNull()
        }
    }

    val loggedWorkoutsState = activeMember?.let { member ->
        viewModel.getLoggedWorkoutsForMember(member.id).collectAsState(initial = emptyList())
    } ?: viewModel.getAllLoggedWorkouts().collectAsState(initial = emptyList())

    val pastLogs = loggedWorkoutsState.value

    var expandedMemberDropdown by remember { mutableStateOf(false) }

    // Session Metadata Fields
    val todayDateStr = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    var sessionDate by remember { mutableStateOf(todayDateStr) }
    var sessionTitle by remember { mutableStateOf("Hypertrophy Chest & Triceps") }
    var trainerName by remember { mutableStateOf(currentUser.name.ifBlank { "Coach Alex Vance" }) }
    var sessionNotes by remember { mutableStateOf("") }

    // Preset exercises quick suggestions
    val commonExercises = remember {
        listOf(
            "Barbell Bench Press", "Incline DB Press", "Cable Flyes",
            "Barbell Back Squat", "Romanian Deadlift", "Leg Press",
            "Lat Pulldown", "Seated Cable Row", "Barbell Curl",
            "DB Shoulder Press", "Tricep Pushdown", "Plank"
        )
    }

    // Workout Exercises Draft State
    val draftExercises = remember {
        mutableStateListOf(
            DraftExercise(
                name = "Barbell Bench Press",
                sets = mutableStateListOf(
                    DraftSet(1, "12", "60.0"),
                    DraftSet(2, "10", "65.0"),
                    DraftSet(3, "8", "70.0")
                )
            ),
            DraftExercise(
                name = "Incline Dumbbell Press",
                sets = mutableStateListOf(
                    DraftSet(1, "12", "22.0"),
                    DraftSet(2, "10", "24.0"),
                    DraftSet(3, "10", "24.0")
                )
            )
        )
    }

    var customExerciseInput by remember { mutableStateOf("") }
    var showPastHistoryView by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Personal Trainer Workout Logger",
            subtitle = "Record sets, reps & weight loads for PT client sessions"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle Bar: New Log vs Past History
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (!showPastHistoryView) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { showPastHistoryView = false }
                    .testTag("tab_new_workout_log")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = if (!showPastHistoryView) Color.Black else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Log Workout Session",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!showPastHistoryView) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (showPastHistoryView) IndigoSecondary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { showPastHistoryView = true }
                    .testTag("tab_past_workout_logs")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = if (showPastHistoryView) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Past Logs (${pastLogs.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (showPastHistoryView) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showPastHistoryView) {
            // PAST LOGS HISTORY VIEW
            PastLogsHistorySection(
                pastLogs = pastLogs,
                onDeleteLog = { id -> viewModel.deleteLoggedWorkoutSession(id) }
            )
        } else {
            // NEW WORKOUT LOGGING FORM

            // Success feedback toast card
            saveSuccessMessage?.let { msg ->
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 1. Client & Trainer Details Glass Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Client & Trainer Session Info",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Member Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedMemberDropdown,
                        onExpandedChange = { expandedMemberDropdown = !expandedMemberDropdown }
                    ) {
                        OutlinedTextField(
                            value = activeMember?.let { "${it.name} (${it.membershipPlan})" } ?: "Select Member",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Client / Gym Member") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMemberDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("workout_logger_member_select"),
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
                                            Text("${m.membershipPlan} • ${m.weightKg}kg", style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        activeMember = m
                                        viewModel.selectMember(m)
                                        expandedMemberDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = sessionTitle,
                            onValueChange = { sessionTitle = it },
                            label = { Text("Workout Title / Focus") },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("workout_session_title_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = sessionDate,
                            onValueChange = { sessionDate = it },
                            label = { Text("Date") },
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("workout_session_date_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = trainerName,
                        onValueChange = { trainerName = it },
                        label = { Text("Personal Trainer / Coach Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_trainer_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Exercise & Sets Logger
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Exercise Sets & Weights Log",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${draftExercises.size} Exercises",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Exercise preset chips
                    Text(
                        text = "Quick Exercise Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        commonExercises.take(6).forEach { exPreset ->
                            FilterChip(
                                selected = draftExercises.any { it.name.equals(exPreset, ignoreCase = true) },
                                onClick = {
                                    if (!draftExercises.any { it.name.equals(exPreset, ignoreCase = true) }) {
                                        draftExercises.add(
                                            DraftExercise(
                                                name = exPreset,
                                                sets = mutableStateListOf(
                                                    DraftSet(1, "10", "40.0"),
                                                    DraftSet(2, "10", "40.0")
                                                )
                                            )
                                        )
                                    }
                                },
                                label = { Text(exPreset, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Render List of Draft Exercises
                    draftExercises.forEachIndexed { exIndex, draftEx ->
                        DraftExerciseCard(
                            exerciseIndex = exIndex + 1,
                            exercise = draftEx,
                            onRemoveExercise = { draftExercises.removeAt(exIndex) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Add Custom Exercise Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customExerciseInput,
                            onValueChange = { customExerciseInput = it },
                            placeholder = { Text("Enter exercise name (e.g., Cable Crossover)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_exercise_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customExerciseInput.isNotBlank()) {
                                    draftExercises.add(
                                        DraftExercise(
                                            name = customExerciseInput.trim(),
                                            sets = mutableStateListOf(
                                                DraftSet(1, "10", "20.0")
                                            )
                                        )
                                    )
                                    customExerciseInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_exercise_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Exercise")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Trainer Notes & Volume Summary
            val totalSetsCount = draftExercises.sumOf { it.sets.size }
            val totalVolumeLifted = draftExercises.sumOf { ex ->
                ex.sets.filter { it.isCompleted }.sumOf { s ->
                    val r = s.repsText.toIntOrNull() ?: 0
                    val w = s.weightText.toFloatOrNull() ?: 0f
                    (r * w).toDouble()
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Session Metrics & Trainer Notes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = IndigoSecondary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Completed Sets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("$totalSetsCount Sets", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = IndigoSecondary)
                            }
                        }

                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Volume Lifted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("${String.format("%,.1f", totalVolumeLifted)} kg", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = EmeraldPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("Trainer Feedback / Form Notes") },
                        placeholder = { Text("e.g., Client increased top set bench press load by 5kg. Great tempo and breathing.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("workout_session_notes_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Save Action
                    Button(
                        onClick = {
                            val member = activeMember
                            if (member != null && draftExercises.isNotEmpty()) {
                                val loggedExercises = draftExercises.map { draftEx ->
                                    LoggedExercise(
                                        exerciseName = draftEx.name,
                                        sets = draftEx.sets.map { draftSet ->
                                            LoggedSet(
                                                setNumber = draftSet.setNumber,
                                                reps = draftSet.repsText.toIntOrNull() ?: 10,
                                                weightKg = draftSet.weightText.toFloatOrNull() ?: 0f,
                                                isCompleted = draftSet.isCompleted
                                            )
                                        }
                                    )
                                }

                                val session = LoggedWorkoutSession(
                                    memberId = member.id,
                                    memberName = member.name,
                                    trainerName = trainerName.ifBlank { "Coach Alex Vance" },
                                    date = sessionDate.ifBlank { todayDateStr },
                                    workoutTitle = sessionTitle.ifBlank { "Workout Session" },
                                    exercises = loggedExercises,
                                    notes = sessionNotes
                                )

                                viewModel.saveLoggedWorkoutSession(session)
                                saveSuccessMessage = "Successfully logged workout for ${member.name} (${loggedExercises.size} exercises, $totalSetsCount sets, ${String.format("%.0f", totalVolumeLifted)}kg volume)!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_workout_session_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Completed Workout Session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftExerciseCard(
    exerciseIndex: Int,
    exercise: DraftExercise,
    onRemoveExercise: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("draft_exercise_card_$exerciseIndex")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = IndigoSecondary.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$exerciseIndex",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = IndigoSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = exercise.name,
                        onValueChange = { exercise.name = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onRemoveExercise, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Exercise",
                        tint = RoseDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table Header: Set # | Weight (kg) | Reps | Complete | Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SET", modifier = Modifier.width(44.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("WEIGHT (KG)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(8.dp))
                Text("REPS", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(8.dp))
                Text("DONE", modifier = Modifier.width(44.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(32.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sets rows
            exercise.sets.forEachIndexed { setIndex, draftSet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Set # Badge
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.width(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = "S${setIndex + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Weight Input with quick buttons
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val curr = draftSet.weightText.toFloatOrNull() ?: 0f
                                if (curr >= 2.5f) draftSet.weightText = String.format("%.1f", curr - 2.5f)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-2.5kg", modifier = Modifier.size(14.dp))
                        }

                        OutlinedTextField(
                            value = draftSet.weightText,
                            onValueChange = { draftSet.weightText = it },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_weight_input_${exerciseIndex}_$setIndex"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        IconButton(
                            onClick = {
                                val curr = draftSet.weightText.toFloatOrNull() ?: 0f
                                draftSet.weightText = String.format("%.1f", curr + 2.5f)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+2.5kg", modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Reps Input with quick buttons
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val curr = draftSet.repsText.toIntOrNull() ?: 0
                                if (curr > 1) draftSet.repsText = (curr - 1).toString()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-1 rep", modifier = Modifier.size(14.dp))
                        }

                        OutlinedTextField(
                            value = draftSet.repsText,
                            onValueChange = { draftSet.repsText = it },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_reps_input_${exerciseIndex}_$setIndex"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        IconButton(
                            onClick = {
                                val curr = draftSet.repsText.toIntOrNull() ?: 0
                                draftSet.repsText = (curr + 1).toString()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+1 rep", modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Completion Checkbox
                    Checkbox(
                        checked = draftSet.isCompleted,
                        onCheckedChange = { draftSet.isCompleted = it },
                        colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary, checkmarkColor = Color.Black),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete Set Button
                    IconButton(
                        onClick = {
                            if (exercise.sets.size > 1) {
                                exercise.sets.removeAt(setIndex)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Set", tint = RoseDanger.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add Set Button
            TextButton(
                onClick = {
                    val lastSet = exercise.sets.lastOrNull()
                    val newWeight = lastSet?.weightText ?: "40.0"
                    val newReps = lastSet?.repsText ?: "10"
                    exercise.sets.add(
                        DraftSet(
                            setNumber = exercise.sets.size + 1,
                            repsText = newReps,
                            weightText = newWeight,
                            isCompleted = true
                        )
                    )
                },
                modifier = Modifier.testTag("add_set_button_$exerciseIndex")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Set to ${exercise.name}", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PastLogsHistorySection(
    pastLogs: List<LoggedWorkoutSession>,
    onDeleteLog: (String) -> Unit
) {
    if (pastLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No workout logs recorded yet.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "Switch to 'Log Workout Session' tab to record client training data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            pastLogs.forEach { session ->
                var isExpanded by remember { mutableStateOf(false) }

                val sessionVolume = session.exercises.sumOf { ex ->
                    ex.sets.filter { it.isCompleted }.sumOf { (it.reps * it.weightKg).toDouble() }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.workoutTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Client: ${session.memberName} • Trainer: ${session.trainerName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }

                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = session.date,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "${session.exercises.size} Exercises",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = IndigoSecondary
                                )
                                Text(
                                    text = "Vol: ${String.format("%,.0f", sessionVolume)} kg",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { isExpanded = !isExpanded }) {
                                    Text(if (isExpanded) "Hide Sets" else "View Sets", fontSize = 12.sp)
                                }
                                IconButton(onClick = { onDeleteLog(session.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseDanger.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth()
                            ) {
                                session.exercises.forEach { ex ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = ex.exerciseName,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                ex.sets.forEach { set ->
                                                    Text(
                                                        text = "S${set.setNumber}: ${set.weightKg}kg × ${set.reps}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (session.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Trainer Notes: ${session.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
