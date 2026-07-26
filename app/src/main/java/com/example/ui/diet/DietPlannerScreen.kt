package com.example.ui.diet

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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.viewmodel.GymViewModel
import com.example.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietPlannerScreen(
    viewModel: GymViewModel,
    onPreviewPdfReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val gymName by viewModel.gymName.collectAsState()
    val currentDietPlan by viewModel.currentDietPlan.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var activeMember by remember { mutableStateOf(selectedMember ?: members.firstOrNull()) }
    var expandedMemberDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMember, members) {
        if (activeMember == null && members.isNotEmpty()) {
            activeMember = selectedMember ?: members.firstOrNull()
        }
    }

    // Quiz Step State (0..4)
    var currentQuizStep by remember { mutableStateOf(0) }
    val totalQuizSteps = 5

    // Quiz Answers State
    var ageText by remember { mutableStateOf("28") }
    var targetWeightText by remember { mutableStateOf((activeMember?.weightKg?.minus(4f) ?: 60f).toInt().toString()) }
    var goalChoice by remember { mutableStateOf("Fat Loss") }
    var foodTypeChoice by remember { mutableStateOf("High Protein Non-Veg") }
    var regionalCuisineChoice by remember { mutableStateOf("North Indian (Roti, Dal, Paneer/Chicken)") }
    var activityLevelChoice by remember { mutableStateOf("Moderate") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Gemini AI Nutrition & Diet Planner",
            subtitle = "Gamified Indian Sports Nutrition Engine (ICMR & NIN Standards)"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // GAMIFIED QUESTIONNAIRE WIZARD CARD
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header & Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUESTION ${currentQuizStep + 1} OF $totalQuizSteps",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${((currentQuizStep + 1) * 100) / totalQuizSteps}% COMPLETED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
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
                    color = EmeraldPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ANIMATED QUESTION CONTENT
                AnimatedContent(
                    targetState = currentQuizStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()) togetherWith (slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()) togetherWith (slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "diet_quiz_step_transition"
                ) { step ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when (step) {
                            // STEP 1: MEMBER & PHYSICAL METRICS
                            0 -> {
                                Text(
                                    text = "👤 Step 1: Member Selection & Metrics",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Select member & set baseline anthropometric targets",
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
                                        label = { Text("Gym Member Profile") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMemberDropdown) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("member_select_dropdown"),
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
                                        modifier = Modifier.weight(1f).testTag("age_field"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    OutlinedTextField(
                                        value = targetWeightText,
                                        onValueChange = { targetWeightText = it },
                                        label = { Text("Target Weight (kg)") },
                                        modifier = Modifier.weight(1f).testTag("target_weight_field"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { if (currentQuizStep < totalQuizSteps - 1) currentQuizStep++ },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Text("Continue to Meal Questions", color = Color.Black, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }

                            // STEP 2: PRIMARY GOAL
                            1 -> {
                                Text(
                                    text = "🎯 Step 2: Caloric Goal Target",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Select macro-nutrient strategy based on member goal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val goals = listOf(
                                    "Fat Loss" to "Caloric deficit of 400-500 kcal for active fat burning",
                                    "Muscle Gain" to "Caloric surplus of 300-400 kcal with 2.2g/kg protein",
                                    "Recomp" to "Isocaloric maintenance with high protein partition"
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
                                            containerColor = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isSelected) BorderStroke(2.dp, EmeraldPrimary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelect() },
                                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
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

                            // STEP 3: DIETARY CATEGORY (Veg / Eggetarian / Non-Veg / Jain)
                            2 -> {
                                Text(
                                    text = "🥗 Step 3: Indian Dietary Category",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Select food type & protein sources",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val foodTypes = listOf(
                                    "Pure Vegetarian (Strictly No Eggs)" to "Paneer, Soya Chunks, Tofu, Curd, Sprouts, Rajma, Chole & Dals",
                                    "Eggetarian" to "Eggs, Milk, Curd, Paneer, Whey, Sprouts & Dals (No Chicken/Fish)",
                                    "High Protein Non-Veg" to "Chicken, Fish, Eggs, Whey, Paneer & Dals",
                                    "Jain Vegetarian" to "Strict Pure Veg; No eggs, onion, garlic or root vegetables",
                                    "Vegan" to "Soya Chunks, Tofu, Sattu, Chana, Sprouts & Plant Protein"
                                )

                                foodTypes.forEach { (ftTitle, ftDesc) ->
                                    val isSelected = foodTypeChoice == ftTitle
                                    val onSelect = {
                                        foodTypeChoice = ftTitle
                                        if (currentQuizStep < totalQuizSteps - 1) currentQuizStep++
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { onSelect() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isSelected) BorderStroke(2.dp, EmeraldPrimary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelect() },
                                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(ftTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text(ftDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                }
                            }

                            // STEP 4: REGIONAL CUISINE & ACTIVITY
                            3 -> {
                                Text(
                                    text = "🌾 Step 4: Regional Cuisine & Activity Level",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tailor meal recipes to authentic regional Indian food preferences",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val regions = listOf(
                                    "North Indian (Roti, Dal, Paneer/Chicken)" to "Wheat Chapati, Dal Tadka, Paneer Bhurji, Chicken Tikka",
                                    "South Indian (Dosa, Idli, Sambar, Fish)" to "Steamed Idli, Ragi Mudde, Sambar, Fish Curry, Curd Rice",
                                    "West Indian (Poha, Thalipeeth, Moong Dal)" to "Poha with peanuts, Sprouts Bhel, Usal, Bhakri",
                                    "East Indian (Fish Curry, Rice, Chhena)" to "Steamed Rice, Fish Curry, Moong Dal, Chhena/Paneer"
                                )

                                regions.forEach { (regTitle, regDesc) ->
                                    val isSelected = regionalCuisineChoice == regTitle
                                    val onSelect = {
                                        regionalCuisineChoice = regTitle
                                        if (currentQuizStep < totalQuizSteps - 1) currentQuizStep++
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { onSelect() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isSelected) BorderStroke(2.dp, EmeraldPrimary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelect() },
                                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(regTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text(regDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                }
                            }

                            // STEP 5: REVIEW & GENERATE
                            4 -> {
                                Text(
                                    text = "⚡ Step 5: Review & Generate AI Diet Plan",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Review parameters before executing sports nutrition engine",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("👤 Member: ${activeMember?.name ?: "Selected Member"}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("🎯 Goal: $goalChoice", style = MaterialTheme.typography.bodySmall)
                                        Text("🥗 Diet Category: $foodTypeChoice", style = MaterialTheme.typography.bodySmall)
                                        Text("🌾 Regional Style: $regionalCuisineChoice", style = MaterialTheme.typography.bodySmall)
                                        Text("🇮🇳 Standard: ICMR & NIN Sports Nutrition Framework", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // STEP CONTROLS & GENERATE BUTTON
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
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Next", color = Color.Black, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val targetMember = activeMember ?: selectedMember ?: members.firstOrNull() ?: return@Button
                                viewModel.generateAiDiet(
                                    member = targetMember,
                                    age = ageText.toIntOrNull() ?: 28,
                                    targetWeightKg = targetWeightText.toFloatOrNull() ?: 60f,
                                    activityLevel = activityLevelChoice,
                                    goal = goalChoice,
                                    regionFoodPref = "$foodTypeChoice - $regionalCuisineChoice"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("generate_diet_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Calculating...", color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Generate", tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate AI Diet", color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DIET RESULT VIEW
        if (currentDietPlan != null) {
            val diet = currentDietPlan!!

            SectionHeader(title = "Generated Indian Diet & Macro Profile")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = diet.memberName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "BMI: ${diet.bmi}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Target Calories: ${diet.targetCalories} kcal / day",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Protein: ${diet.proteinGrams}g | Carbs: ${diet.carbsGrams}g | Fats: ${diet.fatGrams}g | Hydration: ${diet.waterLiters}L",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
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
                                ShareUtils.shareDietOnWhatsApp(context, diet, gymName)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f).testTag("whatsapp_share_diet_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp Share", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // PDF REPORT PREVIEW BUTTON
                        Button(
                            onClick = onPreviewPdfReport,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            modifier = Modifier.weight(1f).testTag("pdf_export_diet_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, contentDescription = "RAG", tint = IndigoSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = diet.explanationText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Structured Daily Indian Meals", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Spacer(modifier = Modifier.height(8.dp))

                    diet.meals.forEach { meal ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${meal.time} • ${meal.name}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${meal.calories} kcal",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = meal.foods,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "P: ${meal.protein}g | C: ${meal.carbs}g | F: ${meal.fat}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
