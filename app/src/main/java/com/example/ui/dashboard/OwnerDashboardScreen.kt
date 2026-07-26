package com.example.ui.dashboard

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CustomRevenueChart
import com.example.ui.components.GlassCard
import com.example.ui.components.RoomSyncStatusDashboardCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary
import com.example.ui.viewmodel.GymViewModel

@Composable
fun OwnerDashboardScreen(
    viewModel: GymViewModel,
    onNavigateToMembers: () -> Unit,
    onNavigateToDietPlanner: () -> Unit,
    onNavigateToRenewals: () -> Unit,
    onOpenWhatsAppBot: () -> Unit,
    onNavigateToAnalytics: (() -> Unit)? = null,
    showRevenue: Boolean = true,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.analytics.collectAsState()
    val members by viewModel.members.collectAsState()
    val gymName by viewModel.gymName.collectAsState()
    val aiInsight by viewModel.aiInsightResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiChatMessages by viewModel.aiChatMessages.collectAsState()
    val gamificationState by viewModel.gamificationState.collectAsState()

    var aiQueryText by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero Banner
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_gym_1784799457927),
                        contentDescription = "Gym Hero Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = gymName.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Live SaaS Dashboard • ${members.size} Active Members",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD0BCFF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary KPI Grid (Matches Design HTML)
            Row(modifier = Modifier.fillMaxWidth()) {
                if (showRevenue) {
                    // Today's Rev Card (White M3 Card)
                    StatCard(
                        title = "Today's Rev",
                        value = "₹${analytics.todayRevenue.toInt()}",
                        icon = Icons.Default.AttachMoney,
                        accentColor = EmeraldPrimary,
                        changeText = "+12%",
                        isHighlighted = false,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    StatCard(
                        title = "Active Packages",
                        value = "${members.count { it.status == com.example.data.model.MembershipStatus.ACTIVE }}",
                        icon = Icons.Default.CheckCircle,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                // Active Members Card (Highlighted M3 Purple Container Card)
                StatCard(
                    title = "Active Members",
                    value = "${members.size}",
                    icon = Icons.Default.People,
                    accentColor = M3PurpleOnContainer,
                    changeText = "+12% MoM",
                    isHighlighted = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary KPI Row
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Today's Check-Ins",
                    value = "${analytics.todayCheckIns}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Upcoming Renewals",
                    value = "${analytics.upcomingRenewalsThisWeek}",
                    icon = Icons.Default.Refresh,
                    accentColor = AmberTertiary,
                    changeText = "${analytics.pendingPaymentsCount} pending",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showRevenue) {
                // Revenue Chart
                CustomRevenueChart(dataPoints = analytics.revenueTrend)

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Interactive Analytics & Charts Dashboard Callout
            if (showRevenue && onNavigateToAnalytics != null) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAnalytics() },
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = M3PurpleContainer,
                    borderColor = M3PurplePrimary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = M3PurplePrimary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Analytics",
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Full Analytics & Charts Screen",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = M3PurpleOnContainer
                                )
                                Text(
                                    text = "Visualize member sign-ups, peak hours & revenue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = M3PurpleOnContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToAnalytics,
                            modifier = Modifier.testTag("open_analytics_charts_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary)
                        ) {
                            Text("Open Charts", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gym Session Frequency & Member Attendance Trends Chart
            com.example.ui.components.AttendanceAndSessionFrequencyChart()

            Spacer(modifier = Modifier.height(16.dp))

            // Gemini FitOps AI Interactive Action Chatbot Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                backgroundColor = Color.Transparent,
                borderColor = Color(0xFFC4B5FD)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFF3E8FF), Color(0xFFE0E7FF))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(M3PurplePrimary, Color(0xFF8B5CF6))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Action Engine",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "FITOPS AI INTERACTIVE CHATBOT",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = M3PurpleOnContainer,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Prompt in-app action commands & queries",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "⚡ ACTION EXECUTOR LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Command Suggestion Chips
                        val quickCommands = listOf(
                            "🔍 How many sessions did Alex Vance attend from July 10 to July 25?",
                            "🥗 Assign Pure Veg fat loss diet to Sarah Jenkins",
                            "🏋️ Generate PPL workout plan for Marcus Brody",
                            "⚠️ List expiring members with churn risk"
                        )

                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickCommands.size) { idx ->
                                val cmd = quickCommands[idx]
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                                    modifier = Modifier.clickable {
                                        viewModel.executeAiCommand(cmd.substringAfter(" "))
                                    }
                                ) {
                                    Text(
                                        text = cmd,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = M3PurplePrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chat Messages Scroll Container
                        val scrollState = androidx.compose.foundation.rememberScrollState()
                        androidx.compose.runtime.LaunchedEffect(aiChatMessages.size) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                                .verticalScroll(scrollState)
                        ) {
                            aiChatMessages.forEach { msg ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp, topEnd = 16.dp,
                                            bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                            bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                        ),
                                        color = if (msg.isUser) M3PurplePrimary else Color.White,
                                        border = if (msg.isUser) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                                        modifier = Modifier.widthIn(max = 310.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            if (!msg.isUser && msg.actionExecutedText != null) {
                                                Surface(
                                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.padding(bottom = 6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = EmeraldPrimary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = msg.actionExecutedText,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = EmeraldPrimary
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                                color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Prompt Field
                        OutlinedTextField(
                            value = aiQueryText,
                            onValueChange = { aiQueryText = it },
                            placeholder = { Text("Ask or prompt command (e.g., date to date member A sessions)...", color = Color.Gray, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_query_input"),
                            trailingIcon = {
                                if (isAiLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = M3PurplePrimary)
                                } else {
                                    IconButton(
                                        onClick = {
                                            if (aiQueryText.isNotBlank()) {
                                                val textToSend = aiQueryText
                                                aiQueryText = ""
                                                viewModel.executeAiCommand(textToSend)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = M3PurplePrimary)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = M3PurplePrimary,
                                unfocusedBorderColor = Color(0xFFC4B5FD),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Member Gamification & Engagement Leaderboard
            com.example.ui.components.LeaderboardWidget(
                leaderboard = gamificationState.leaderboard
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Management Shortcuts Row
            SectionHeader(title = "Quick Management Operations")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onNavigateToMembers,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("members_crm_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary)
                ) {
                    Icon(Icons.Default.People, contentDescription = "Members")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Member CRM")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onNavigateToRenewals,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("renewals_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = M3PurpleContainer,
                        contentColor = M3PurpleOnContainer
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Renewals")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Renewals")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateToDietPlanner,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("diet_planner_shortcut"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = "AI Diet Planner", tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Gemini AI Diet & Workout Planner", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        FloatingActionButton(
            onClick = onOpenWhatsAppBot,
            containerColor = Color(0xFF25D366),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("whatsapp_bot_fab_button")
        ) {
            Icon(Icons.Default.HeadsetMic, contentDescription = "WhatsApp AI Assistant")
        }
    }
}
