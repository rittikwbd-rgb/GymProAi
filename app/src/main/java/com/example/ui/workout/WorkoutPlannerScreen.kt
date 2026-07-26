package com.example.ui.workout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary
import com.example.ui.viewmodel.GymViewModel
import com.example.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlannerScreen(
    viewModel: GymViewModel,
    onPreviewPdfReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val gymName by viewModel.gymName.collectAsState()
    val activeWorkoutPlan by viewModel.currentWorkoutPlan.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var activeMember by remember { mutableStateOf(selectedMember ?: members.firstOrNull()) }
    var expandedMemberDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMember, members) {
        if (activeMember == null && members.isNotEmpty()) {
            activeMember = selectedMember ?: members.firstOrNull()
        }
    }

    // Quiz Step State (0..5)
    var currentQuizStep by remember { mutableStateOf(0) }
    val totalQuizSteps = 6

    // Quiz Answers State
    var ageText by remember { mutableStateOf("28") }
    var targetWeightText by remember { mutableStateOf((activeMember?.weightKg?.minus(4f) ?: 60f).toInt().toString()) }
    var goalChoice by remember { mutableStateOf("Fat Loss & Shred") }
    var daysPerWeekChoice by remember { mutableStateOf(5) } // 3, 5, or 6 days
    var splitChoice by remember { mutableStateOf("Push Pull Legs (PPL)") } // Muscle split: Each muscle a day / 2 muscles a day / Push Pull Legs
    var monthlyRotationEnabled by remember { mutableStateOf(true) } // Update movements every month
    var workoutStyleChoice by remember { mutableStateOf("Full Commercial Gym") }

    // Day filter selection for generated plan
    var selectedDayFilter by remember { mutableStateOf("All") }

    // Main Workout Mode (0: PT Workout Logger, 1: AI Routine Generator)
    var mainWorkoutMode by remember { mutableStateOf(1) }

    // Tracking completed sets map
    val completedSetsMap = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP MODE SWITCHER SEGMENTED BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (mainWorkoutMode == 0) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { mainWorkoutMode = 0 }
                    .testTag("workout_mode_logger_tab")
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = if (mainWorkoutMode == 0) Color.Black else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PT Workout Logger",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (mainWorkoutMode == 0) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (mainWorkoutMode == 1) M3PurplePrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { mainWorkoutMode = 1 }
                    .testTag("workout_mode_ai_builder_tab")
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (mainWorkoutMode == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Routine Builder",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (mainWorkoutMode == 1) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (mainWorkoutMode == 0) {
            WorkoutLoggingScreen(
                viewModel = viewModel,
                preselectedMember = activeMember,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "Gemini AI Workout Routine Generator",
                    subtitle = "Gamified Indian Fitness Program Builder & Desi Conditioning Engine"
                )

                Spacer(modifier = Modifier.height(12.dp))

        // GAMIFIED QUESTIONNAIRE WIZARD CARD
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header & Animated Step Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUESTION ${currentQuizStep + 1} OF $totalQuizSteps",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = M3PurplePrimary
                    )
                    Surface(
                        color = M3PurpleContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${((currentQuizStep + 1) * 100) / totalQuizSteps}% COMPLETED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = M3PurpleOnContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (currentQuizStep + 1).toFloat() / totalQuizSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = M3PurplePrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ANIMATED QUESTION STEP CONTENT
                AnimatedContent(
                    targetState = currentQuizStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()) togetherWith (slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()) togetherWith (slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "workout_quiz_step_transition"
                ) { step ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when (step) {
                            // STEP 1: MEMBER SELECT & PHYSICAL METRICS
                            0 -> {
                                Text(
                                    text = "👤 Step 1: Select Gym Member & Metrics",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Choose the member to personalize the Indian training program",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                ExposedDropdownMenuBox(
                                    expanded = expandedMemberDropdown,
                                    onExpandedChange = { expandedMemberDropdown = !expandedMemberDropdown }
                                ) {
                                    OutlinedTextField(
                                        value = activeMember?.name ?: "Select Member",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Active Gym Member") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMemberDropdown) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("workout_member_select_dropdown"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedMemberDropdown,
                                        onDismissRequest = { expandedMemberDropdown = false }
                                    ) {
                                        members.forEach { m ->
                                            DropdownMenuItem(
                                                text = { Text("${m.name} (${m.membershipPlan})") },
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
                                        value = ageText,
                                        onValueChange = { ageText = it },
                                        label = { Text("Age (Years)") },
                                        modifier = Modifier.weight(1f).testTag("workout_age_field"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    OutlinedTextField(
                                        value = targetWeightText,
                                        onValueChange = { targetWeightText = it },
                                        label = { Text("Target Weight (kg)") },
                                        modifier = Modifier.weight(1f).testTag("workout_target_weight_field"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { if (currentQuizStep < totalQuizSteps - 1) currentQuizStep++ },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary)
                                ) {
                                    Text("Continue to Questions", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            // STEP 2: PRIMARY GOAL
                            1 -> {
                                Text(
                                    text = "🎯 Step 2: Primary Fitness Goal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "What is the primary objective for this training program?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val goals = listOf(
                                    "Fat Loss & Shred" to "Burn fat while preserving lean muscle mass",
                                    "Muscle Hypertrophy" to "Maximize muscle volume & strength gains",
                                    "Body Recomposition" to "Build muscle and drop body fat simultaneously",
                                    "Desi Power & Strength" to "Heavy compound lifts & traditional power",
                                    "Surya Flow & Core Mobility" to "Flexibility, core strength & endurance"
                                )

                                goals.forEach { (gTitle, gDesc) ->
                                    val isSelected = goalChoice == gTitle
                                    val onSelect = {
                                        goalChoice = gTitle
                                        if (currentQuizStep < totalQuizSteps - 1) currentQuizStep++
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { onSelect() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) M3PurpleContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isSelected) BorderStroke(2.dp, M3PurplePrimary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelect() },
                                                colors = RadioButtonDefaults.colors(selectedColor = M3PurplePrimary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(gTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text(gDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                }
                            }

                            // STEP 3: WORKOUT DAYS PER WEEK (3, 5, or 6 days)
                            2 -> {
                                Text(
                                    text = "🗓️ Step 3: Training Frequency",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "How many days per week do you want to workout?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val dayOptions = listOf(
                                    3 to "3 Days / Week (Full Body or Alternating Upper-Lower)",
                                    5 to "5 Days / Week (Optimal Muscle Split & Recovery)",
                                    6 to "6 Days / Week (Push-Pull-Legs 2x Pro Frequency)"
                                )

                                dayOptions.forEach { (days, desc) ->
                                    val isSelected = daysPerWeekChoice == days
                                    val onSelect = {
                                        daysPerWeekChoice = days
                                        if (currentQuizStep < totalQuizSteps - 1) currentQuizStep++
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable { onSelect() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) M3PurpleContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isSelected) BorderStroke(2.dp, M3PurplePrimary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) M3PurplePrimary else MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$days",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "$days Days per Week",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = M3PurplePrimary)
                                            }
                                        }
                                    }
                                }
                            }

                            // STEP 4: MUSCLE SPLIT PREFERENCE (Each muscle a day / 2 muscles a day / Push Pull Legs)
                            3 -> {
                                Text(
                                    text = "💪 Step 4: Muscle Split Preference",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Select how muscle groups should be targeted across workout days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val splits = listOf(
                                    "Push Pull Legs (PPL)" to "Push (Chest/Delts/Tri), Pull (Back/Bi), Legs (Quads/Hams/Calves)",
                                    "Each Muscle a Day (Bro Split)" to "Single muscle isolation per day (Chest Mon, Back Tue, Shoulder Wed...)",
                                    "2 Muscles a Day (Arnold/Upper-Lower)" to "Paired muscles per day (Chest & Triceps, Back & Biceps, Legs & Shoulders)",
                                    "Desi Akhada & Calisthenics Split" to "Full-body functional movement, Dands, Baithaks & Mudgar"
                                )

                                splits.forEach { (sTitle, sDesc) ->
                                    val isSelected = splitChoice == sTitle
                                    val onSelect = {
                                        splitChoice = sTitle
                                        if (currentQuizStep < totalQuizSteps - 1) currentQuizStep++
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                            .clickable { onSelect() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) M3PurpleContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isSelected) BorderStroke(2.dp, M3PurplePrimary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelect() },
                                                colors = RadioButtonDefaults.colors(selectedColor = M3PurplePrimary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(sTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text(sDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                }
                            }

                            // STEP 5: MONTHLY EXERCISE MOVEMENT ROTATION
                            4 -> {
                                Text(
                                    text = "🔄 Step 5: Monthly Movement Rotation",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Periodization & progressive variation settings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, tint = M3PurplePrimary)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "Rotate Exercises Every Month",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Text(
                                                        text = "Automatically cycle 20-30% of movement variations monthly to prevent adaptation plateaus",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }

                                            Switch(
                                                checked = monthlyRotationEnabled,
                                                onCheckedChange = { monthlyRotationEnabled = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = M3PurplePrimary)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (monthlyRotationEnabled) "✅ Monthly rotation active: Gemini AI will generate monthly exercise progression alternatives." else "📌 Fixed core program: Movements remain constant for strict strength tracking.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { if (currentQuizStep < totalQuizSteps - 1) currentQuizStep++ },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary)
                                ) {
                                    Text("Continue to Final Question", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            // STEP 6: TRAINING ENVIRONMENT & GENERATE BUTTON
                            5 -> {
                                Text(
                                    text = "🇮🇳 Step 6: Gym Environment & Generate",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Finalize Indian gym facilities & generate AI program",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val styles = listOf(
                                    "Full Commercial Gym" to "Barbells, cables, leg press & dumbbells",
                                    "Desi Akhada / Traditional" to "Dands, Baithaks, Mudgar swings & bodyweight",
                                    "Home Gym with Dumbbells" to "Adjustable dumbbells & resistance bands",
                                    "Surya Namaskar & Bodyweight" to "Calisthenics, Surya Namaskar & core"
                                )

                                styles.forEach { (stTitle, stDesc) ->
                                    val isSelected = workoutStyleChoice == stTitle
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { workoutStyleChoice = stTitle },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) M3PurpleContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isSelected) BorderStroke(2.dp, M3PurplePrimary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { workoutStyleChoice = stTitle },
                                                colors = RadioButtonDefaults.colors(selectedColor = M3PurplePrimary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(stTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text(stDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // STEP WIZARD NAVIGATION CONTROLS & GENERATE BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentQuizStep > 0) {
                        OutlinedButton(
                            onClick = { currentQuizStep-- },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous", maxLines = 1)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentQuizStep < totalQuizSteps - 1) {
                        Button(
                            onClick = { currentQuizStep++ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        // FINAL STEP: PROMINENT GENERATE BUTTON
                        Button(
                            onClick = {
                                val targetMember = activeMember ?: selectedMember ?: members.firstOrNull() ?: return@Button
                                viewModel.generateAiWorkout(
                                    member = targetMember,
                                    age = ageText.toIntOrNull() ?: 28,
                                    targetWeightKg = targetWeightText.toFloatOrNull() ?: 60f,
                                    goal = goalChoice,
                                    workoutStyle = workoutStyleChoice,
                                    daysPerWeek = daysPerWeekChoice,
                                    splitType = splitChoice,
                                    monthlyRotation = monthlyRotationEnabled
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("generate_workout_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Designing...", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Generate", tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Workout", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Quick-Load Sample Indian Routines Banner
        SectionHeader(
            title = "Preset Indian Gym Routines (1-Tap Quick Load)",
            subtitle = "Test sample workout routines tailored for Indian conditions"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val m = activeMember ?: members.firstOrNull()
                    if (m != null) {
                        viewModel.generateAiWorkout(m, 28, 62f, "Desi Power", "Desi Akhada / Traditional", 5, "Push Pull Legs (PPL)", true)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("5-Day Desi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Power Split", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            OutlinedButton(
                onClick = {
                    val m = activeMember ?: members.firstOrNull()
                    if (m != null) {
                        viewModel.generateAiWorkout(m, 26, 58f, "Fat Loss", "Surya Namaskar & Bodyweight", 6, "Each Muscle a Day (Bro Split)", true)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("6-Day Shred", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Bro Split", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            OutlinedButton(
                onClick = {
                    val m = activeMember ?: members.firstOrNull()
                    if (m != null) {
                        viewModel.generateAiWorkout(m, 30, 65f, "Muscle Gain", "Home Gym with Dumbbells", 3, "2 Muscles a Day (Upper-Lower)", false)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("3-Day Home", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Dumbbells", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ACTIVE GENERATED WORKOUT DISPLAY SECTION
        if (activeWorkoutPlan != null) {
            val workout = activeWorkoutPlan!!

            // Calculate total sets and completed sets
            var totalSetsCount = 0
            var completedSetsCount = 0
            workout.routines.forEach { r ->
                r.exercises.forEach { ex ->
                    val setsNum = ex.sets.toIntOrNull() ?: 3
                    totalSetsCount += setsNum
                    for (s in 1..setsNum) {
                        val key = "${r.dayName}_${ex.name}_Set_$s"
                        if (completedSetsMap[key] == true) {
                            completedSetsCount++
                        }
                    }
                }
            }

            val progressFraction = if (totalSetsCount > 0) completedSetsCount.toFloat() / totalSetsCount else 0f

            // Program Header & Export Actions (PDF + WhatsApp 1-Click)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Program: ${workout.memberName}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Goal: ${workout.goal} • ${workout.daysPerWeek} Days Routine Split",
                        style = MaterialTheme.typography.bodyMedium,
                        color = M3PurplePrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-CLICK EXPORT BUTTONS ROW: PDF + WHATSAPP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // WHATSAPP SHARE BUTTON (GREEN #25D366)
                        Button(
                            onClick = {
                                ShareUtils.shareWorkoutOnWhatsApp(context, workout, gymName)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f).testTag("whatsapp_share_workout_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp Share", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // PDF REPORT EXPORT BUTTON
                        Button(
                            onClick = onPreviewPdfReport,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.weight(1f).testTag("pdf_export_workout_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Completion Progress Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Weekly Sets Tracker", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Text(
                            text = "$completedSetsCount / $totalSetsCount Sets Logged (${(progressFraction * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indian Gym Conditions Hydration & Warmup Alert Card
            Surface(
                color = M3PurpleContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = M3PurpleOnContainer, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🇮🇳 Indian Gym & Tropical Climate Guidance",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = M3PurpleOnContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Begin each session with 5 rounds of Surya Namaskar. In warm weather, sip electrolyte water (Nimbu Pani/ORS with a pinch of salt) every 15 minutes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = M3PurpleOnContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day Selector Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedDayFilter == "All",
                    onClick = { selectedDayFilter = "All" },
                    label = { Text("All Days") }
                )
                workout.routines.forEach { routine ->
                    FilterChip(
                        selected = selectedDayFilter == routine.dayName,
                        onClick = { selectedDayFilter = routine.dayName },
                        label = { Text(routine.dayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Render Routines
            workout.routines.forEach { routine ->
                if (selectedDayFilter == "All" || selectedDayFilter == routine.dayName) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = M3PurplePrimary,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = routine.dayName.takeLast(1),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = routine.dayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = M3PurplePrimary
                                        )
                                        Text(
                                            text = routine.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${routine.exercises.size} Exercises",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            routine.exercises.forEach { ex ->
                                val setsNum = ex.sets.toIntOrNull() ?: 3

                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (ex.name.contains("Surya", ignoreCase = true) || ex.name.contains("Desi", ignoreCase = true)) {
                                                        Icons.Default.SelfImprovement
                                                    } else {
                                                        Icons.Default.FitnessCenter
                                                    },
                                                    contentDescription = null,
                                                    tint = M3PurplePrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = ex.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "${ex.sets} Sets x ${ex.reps} • ${ex.rest}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = IndigoSecondary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        if (ex.notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "💡 ${ex.notes}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Interactive Set Checkboxes Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Mark Sets:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )

                                            for (setIdx in 1..setsNum) {
                                                val setKey = "${routine.dayName}_${ex.name}_Set_$setIdx"
                                                val isChecked = completedSetsMap[setKey] == true

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isChecked) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier.clickable {
                                                        completedSetsMap[setKey] = !isChecked
                                                    }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Checkbox(
                                                            checked = isChecked,
                                                            onCheckedChange = { checked ->
                                                                completedSetsMap[setKey] = checked
                                                            },
                                                            colors = CheckboxDefaults.colors(
                                                                checkedColor = EmeraldPrimary,
                                                                uncheckedColor = MaterialTheme.colorScheme.secondary
                                                            ),
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                        Text(
                                                            text = "S$setIdx",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                                            ),
                                                            color = if (isChecked) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
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
            }
        }
    }
}
}
}
