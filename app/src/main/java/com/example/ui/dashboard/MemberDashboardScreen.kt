package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.PtSession
import com.example.data.model.PtSessionStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.QrPassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.RoseDanger
import com.example.ui.viewmodel.GymViewModel

@Composable
fun MemberDashboardScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val members by viewModel.members.collectAsState()
    val currentMember = members.firstOrNull { it.id == "mem_1" } ?: members.firstOrNull()

    val ptSessions by viewModel.ptSessions.collectAsState()
    val pendingSessions = ptSessions.filter { it.status == PtSessionStatus.PENDING_APPROVAL }

    val currentDiet by viewModel.currentDietPlan.collectAsState()
    val currentWorkout by viewModel.currentWorkoutPlan.collectAsState()
    val waterGlasses by viewModel.waterGlassesDrunk.collectAsState()
    val gamificationState by viewModel.gamificationState.collectAsState()

    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedSessionToReject by remember { mutableStateOf<PtSession?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (currentMember != null) {
            // Digital Pass Card
            QrPassCard(
                memberName = currentMember.name,
                membershipPlan = currentMember.membershipPlan,
                qrId = currentMember.qrId,
                expiryDate = currentMember.expiryDate
            )
        }

        Spacer(modifier = Modifier.height(16.dp))



        // Gamification & Level Quests Card
        com.example.ui.components.GamificationCard(
            gamificationState = gamificationState,
            onClaimQuest = { questId ->
                viewModel.completeQuest(questId)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pending PT Verification Alert Banner
        if (pendingSessions.isNotEmpty()) {
            SectionHeader(title = "Action Required: PT Session Verification")

            pendingSessions.forEach { session ->
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Trainer ${session.trainerName} logged a PT Session:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${session.startTime} - ${session.endTime} (${session.exercises.size} exercises)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.verifyPtSession(session.id, approve = true) },
                                modifier = Modifier.weight(1f).testTag("approve_pt_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    selectedSessionToReject = session
                                    showRejectDialog = true
                                },
                                modifier = Modifier.weight(1f).testTag("reject_pt_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Reject", tint = RoseDanger)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dispute", color = RoseDanger)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Water Hydration Tracker
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(IndigoSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Water Hydration",
                            tint = IndigoSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Daily Hydration Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$waterGlasses / 10 Glasses (2.5L Goal)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.incrementWater() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                        .testTag("add_water_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Water", tint = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gym Leaderboard Widget
        com.example.ui.components.LeaderboardWidget(
            leaderboard = gamificationState.leaderboard
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Active Diet Plan Summary
        SectionHeader(title = "Active AI Nutrition Plan")

        if (currentDiet != null) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Target: ${currentDiet?.targetCalories} kcal / day",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Protein: ${currentDiet?.proteinGrams}g | Carbs: ${currentDiet?.carbsGrams}g | Fats: ${currentDiet?.fatGrams}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    currentDiet?.meals?.forEach { meal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${meal.time} - ${meal.name}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${meal.calories} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = IndigoSecondary
                            )
                        }
                    }
                }
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active diet plan assigned yet. Ask Trainer to generate via Gemini AI.")
                }
            }
        }
    }

    if (showRejectDialog && selectedSessionToReject != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Dispute PT Session Log", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please describe why you are disputing this PT session:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("e.g. Session was only 30 mins instead of 60 mins") },
                        modifier = Modifier.fillMaxWidth().testTag("dispute_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyPtSession(selectedSessionToReject!!.id, approve = false, reason = rejectReason)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("Submit Dispute")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
