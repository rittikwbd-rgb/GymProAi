package com.example.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.viewmodel.GymViewModel
import com.example.util.PdfUtils

@Composable
fun ReportPreviewScreen(
    viewModel: GymViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dietPlan by viewModel.currentDietPlan.collectAsState()
    val workoutPlan by viewModel.currentWorkoutPlan.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val gymName by viewModel.gymName.collectAsState()

    val memberName = dietPlan?.memberName ?: workoutPlan?.memberName ?: selectedMember?.name ?: "Sarah Jenkins"

    val exportAction = {
        PdfUtils.generateAndSharePdfReport(context, dietPlan, workoutPlan, gymName = gymName)
    }

    val whatsappShareAction = {
        com.example.util.ShareUtils.shareReportOnWhatsApp(context, dietPlan, workoutPlan, gymName)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Commercial PDF Report Preview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Client: $memberName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row {
                IconButton(onClick = exportAction) {
                    Icon(Icons.Default.Print, contentDescription = "Print", tint = EmeraldPrimary)
                }
                IconButton(onClick = exportAction) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = IndigoSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Document Paper Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "METRO FITNESS CLUB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Bandra West, Mumbai, Maharashtra • Ph: +91 98765 43210",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "OFFICIAL BODY RECOMPOSITION & NUTRITION REPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D5C3A)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Report QR",
                        tint = Color.Black,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                Spacer(modifier = Modifier.height(16.dp))

                // Member Details
                Text(
                    text = "MEMBER METRICS PROFILE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Name: $memberName", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text("Age: ${dietPlan?.age ?: 28} Yrs | Gender: ${dietPlan?.gender ?: "Female"}", fontSize = 12.sp, color = Color.Black)
                        Text("Goal: ${dietPlan?.goal ?: workoutPlan?.goal ?: "Fat Loss"}", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weight: ${dietPlan?.weightKg ?: selectedMember?.weightKg ?: 64f} kg | Height: ${dietPlan?.heightCm ?: selectedMember?.heightCm ?: 168f} cm", fontSize = 12.sp, color = Color.Black)
                        Text("BMI: ${dietPlan?.bmi ?: 22.7} | BMR: ${dietPlan?.bmr ?: 1420} kcal", fontSize = 12.sp, color = Color.Black)
                        Text("Target Calories: ${dietPlan?.targetCalories ?: 1850} kcal", fontSize = 12.sp, color = Color(0xFF0D5C3A), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                Spacer(modifier = Modifier.height(16.dp))

                // DIET SECTION
                if (dietPlan != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFF0D5C3A), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "1. DAILY NUTRITION & MACRO DISTRIBUTION",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Protein: ${dietPlan?.proteinGrams ?: 128}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Carbs: ${dietPlan?.carbsGrams ?: 180}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Fats: ${dietPlan?.fatGrams ?: 50}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Hydration: ${dietPlan?.waterLiters ?: 2.8}L", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D5C3A))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "STRUCTURED MEAL SCHEDULE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    dietPlan?.meals?.forEach { meal ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${meal.time} - ${meal.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("${meal.calories} kcal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D5C3A))
                            }
                            Text(meal.foods, fontSize = 10.sp, color = Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // WORKOUT SECTION
                if (workoutPlan != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFF0D5C3A), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "2. PERIODIZED WORKOUT PROGRAM (${workoutPlan?.daysPerWeek ?: 4} DAYS SPLIT)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    workoutPlan?.routines?.forEach { routine ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("${routine.dayName}: ${routine.title}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(2.dp))
                            routine.exercises.forEach { ex ->
                                Text("• ${ex.name} — ${ex.sets} sets x ${ex.reps} (${ex.rest} rest)", fontSize = 10.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Signatures
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Coach Marcus", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Certified Head PT", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Verified by GymAI Pro Engine", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D5C3A))
                        Text("Date: 2026-07-23", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = whatsappShareAction,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("whatsapp_share_pdf_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Share, contentDescription = "WhatsApp PDF", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp PDF", color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            Button(
                onClick = exportAction,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("export_pdf_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF", color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}
