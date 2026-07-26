package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.M3BlueContainer
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = M3PurplePrimary,
    changeText: String? = null,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isHighlighted) M3PurpleContainer else MaterialTheme.colorScheme.surface
    val cardBorder = if (isHighlighted) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.outline
    val textColor = if (isHighlighted) M3PurpleOnContainer else MaterialTheme.colorScheme.onSurface
    val labelColor = if (isHighlighted) M3PurpleOnContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        backgroundColor = cardBg,
        borderColor = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = labelColor
                )
                if (isHighlighted) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                if (changeText != null) {
                    Text(
                        text = changeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isHighlighted) M3PurpleOnContainer.copy(alpha = 0.7f) else EmeraldPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSwitcherHeader(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    gymName: String = "Metro Fitness Club",
    syncStatusInfo: com.example.data.model.SyncStatusInfo? = null,
    onSyncBadgeClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "WELCOME TO SAAS PORTAL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = M3PurplePrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = gymName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSettingsClick != null) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("top_bar_settings_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Box {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = M3PurpleContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, M3PurplePrimary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clickable { expanded = true }
                            .testTag("role_switcher_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Profile Avatar with Online Status Indicator
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Surface(
                                    shape = CircleShape,
                                    color = M3PurplePrimary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "User Profile",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                // Online Status Badge Dot
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = currentRole.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = M3PurpleOnContainer
                                )
                                Text(
                                    text = "Online",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Expand Roles",
                                tint = M3PurpleOnContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                text = "Switch Account Role",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = role.displayName,
                                            fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal,
                                            color = if (role == currentRole) M3PurplePrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    onRoleSelected(role)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomRevenueChart(
    dataPoints: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val maxVal = (dataPoints.maxOfOrNull { it.second } ?: 10000.0).coerceAtLeast(1000.0)

    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Revenue Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Weekly Trend",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val width = size.width
                val height = size.height
                val barWidth = width / (dataPoints.size * 2.2f)

                dataPoints.forEachIndexed { index, pair ->
                    val x = (index * 2.2f + 0.6f) * barWidth
                    val barHeight = (pair.second / maxVal * height * 0.85f).toFloat()
                    val y = height - barHeight

                    val isHighlightBar = index == 5 // Saturday peak bar highlighted
                    val barColor = if (isHighlightBar) M3PurplePrimary else M3BlueContainer

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dataPoints.forEachIndexed { index, pair ->
                    val isHighlight = index == 5
                    Text(
                        text = pair.first.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isHighlight) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun QrPassCard(
    memberName: String,
    membershipPlan: String,
    qrId: String,
    expiryDate: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = M3PurpleContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "PASS #$qrId",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = M3PurpleOnContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "EXP: $expiryDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(2.dp, M3PurplePrimary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "QR Pass",
                    tint = Color.Black,
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = memberName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = membershipPlan,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun GamificationCard(
    gamificationState: com.example.data.model.MemberGamificationState,
    onClaimQuest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = (gamificationState.currentXp.toFloat() / gamificationState.xpForNextLevel.toFloat()).coerceIn(0f, 1f)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Level Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = M3PurpleContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "LVL ${gamificationState.level}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = M3PurpleOnContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = gamificationState.levelTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${gamificationState.currentXp} / ${gamificationState.xpForNextLevel} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Streak Badge
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🔥", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${gamificationState.streakDays} Day Streak",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // XP Animated Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(M3PurplePrimary, Color(0xFF9C27B0))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                gamificationState.badges.forEach { badge ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f).padding(horizontal = 3.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                        ) {
                            Text(text = badge.iconEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = badge.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Daily Gym Quests ⚡", subtitle = "Complete daily tasks to gain XP & rank up")

            // Quests
            gamificationState.quests.forEach { quest ->
                QuestItemRow(
                    quest = quest,
                    onClaim = { onClaimQuest(quest.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun QuestItemRow(
    quest: com.example.data.model.DailyQuest,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (quest.isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (quest.isCompleted) EmeraldPrimary else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = quest.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = M3PurpleContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+${quest.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = M3PurpleOnContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = quest.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (quest.isCompleted) {
                Surface(
                    color = EmeraldPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "DONE ✓",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                androidx.compose.material3.Button(
                    onClick = onClaim,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("claim_quest_${quest.id}")
                ) {
                    Text("Claim", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LeaderboardWidget(
    leaderboard: List<com.example.data.model.LeaderboardUser>,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gym XP Leaderboard 🏆",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = M3PurpleContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "JULY LEAGUE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = M3PurpleOnContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            leaderboard.forEach { user ->
                val rankIcon = when (user.rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#${user.rank}"
                }
                val rowBg = if (user.isCurrentUser) M3PurpleContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = rowBg,
                    border = if (user.isCurrentUser) androidx.compose.foundation.BorderStroke(1.5.dp, M3PurplePrimary) else null,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rankIcon,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.width(32.dp)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (user.isCurrentUser) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (user.isCurrentUser) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(You)", style = MaterialTheme.typography.labelSmall, color = M3PurplePrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = user.levelName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${user.xp} XP",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = M3PurplePrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🔥${user.streakDays}d", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceAndSessionFrequencyChart(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Attendance Trends, 1: Peak Hourly Traffic
    var selectedIndex by remember { mutableStateOf(5) } // Default Saturday or 6PM

    val weeklyData = remember {
        listOf(
            Triple("Mon", 105, 42),
            Triple("Tue", 118, 55),
            Triple("Wed", 132, 60),
            Triple("Thu", 124, 48),
            Triple("Fri", 150, 72),
            Triple("Sat", 175, 88), // Peak Day
            Triple("Sun", 92, 35)
        )
    }

    val hourlyData = remember {
        listOf(
            Pair("6 AM", 38),
            Pair("8 AM", 82),
            Pair("10 AM", 55),
            Pair("12 PM", 64),
            Pair("2 PM", 45),
            Pair("4 PM", 70),
            Pair("6 PM", 110), // Peak Hour
            Pair("8 PM", 85),
            Pair("10 PM", 30)
        )
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Session & Attendance Trends",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time turnout & peak gym activity analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "LIVE TURNSTILE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = M3PurpleContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("TOTAL TURNOUT", style = MaterialTheme.typography.labelSmall, color = M3PurpleOnContainer.copy(alpha = 0.7f))
                        Text("1,048", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = M3PurpleOnContainer)
                        Text("▲ +18.4% vs last week", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = EmeraldPrimary)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("DAILY AVERAGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Text("149 / day", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        Text("Peak: Saturday (192)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart View Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTab == 0) M3PurplePrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedTab = 0
                            selectedIndex = 5 // Saturday
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Weekly Turnout",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTab == 1) M3PurplePrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedTab = 1
                            selectedIndex = 6 // 6 PM
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Peak Hourly Traffic",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Highlight Detail Banner
            val selectedDayName = if (selectedTab == 0) {
                val item = weeklyData.getOrNull(selectedIndex) ?: weeklyData[5]
                item.first
            } else {
                val item = hourlyData.getOrNull(selectedIndex) ?: hourlyData[6]
                item.first
            }

            val mainStatText = if (selectedTab == 0) {
                val item = weeklyData.getOrNull(selectedIndex) ?: weeklyData[5]
                "${item.second} Check-Ins • ${item.third} PT Sessions Logged"
            } else {
                val item = hourlyData.getOrNull(selectedIndex) ?: hourlyData[6]
                "${item.second} Active Members (Capacity 88%)"
            }

            Surface(
                color = M3PurpleContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = M3PurpleOnContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedDayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = M3PurpleOnContainer.copy(alpha = 0.8f),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Tap bar to inspect",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = M3PurpleOnContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = mainStatText,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = M3PurpleOnContainer,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Legend
            if (selectedTab == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF8B5CF6)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Check-ins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(EmeraldPrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PT Sessions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF6366F1)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Native Compose Canvas Chart
            if (selectedTab == 0) {
                val maxVal = 220f

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("weekly_attendance_chart")
                ) {
                    val width = size.width
                    val height = size.height
                    val itemCount = weeklyData.size
                    val spacing = width / itemCount

                    // Background Horizontal Gridlines (0, 50, 100, 150, 200)
                    listOf(0f, 50f, 100f, 150f, 200f).forEach { gridVal ->
                        val yGrid = height - (gridVal / maxVal) * height * 0.82f
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.35f),
                            start = Offset(0f, yGrid),
                            end = Offset(width, yGrid),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    // Smooth Trendline Path
                    val trendPath = Path()
                    weeklyData.forEachIndexed { index, data ->
                        val xCenter = spacing * index + spacing / 2f
                        val yVal = height - (data.second / maxVal) * height * 0.82f
                        if (index == 0) {
                            trendPath.moveTo(xCenter, yVal)
                        } else {
                            val prevX = spacing * (index - 1) + spacing / 2f
                            val prevY = height - (weeklyData[index - 1].second / maxVal) * height * 0.82f
                            trendPath.cubicTo(
                                prevX + spacing / 2f, prevY,
                                prevX + spacing / 2f, yVal,
                                xCenter, yVal
                            )
                        }
                    }

                    // Render Gradient Bars
                    weeklyData.forEachIndexed { index, data ->
                        val xCenter = spacing * index + spacing / 2f
                        val barWidth = spacing * 0.42f

                        val totalHeight = (data.second / maxVal) * height * 0.82f
                        val ptHeight = (data.third / maxVal) * height * 0.82f
                        val isSelected = index == selectedIndex

                        // Primary Check-In Gradient Bar
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = if (isSelected) listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                else listOf(Color(0xFFA78BFA), Color(0xFFC4B5FD))
                            ),
                            topLeft = Offset(xCenter - barWidth / 2f, height - totalHeight),
                            size = Size(barWidth, totalHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // PT Overlay Bar
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = if (isSelected) listOf(EmeraldPrimary, Color(0xFF059669))
                                else listOf(EmeraldPrimary.copy(alpha = 0.8f), EmeraldPrimary.copy(alpha = 0.5f))
                            ),
                            topLeft = Offset(xCenter - barWidth / 2f, height - ptHeight),
                            size = Size(barWidth, ptHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Highlight Border for Selected Bar
                        if (isSelected) {
                            drawRoundRect(
                                color = Color(0xFF7C3AED),
                                topLeft = Offset(xCenter - barWidth / 2f - 2.dp.toPx(), height - totalHeight - 2.dp.toPx()),
                                size = Size(barWidth + 4.dp.toPx(), totalHeight + 2.dp.toPx()),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    // Draw Trendline
                    drawPath(
                        path = trendPath,
                        color = Color(0xFF6366F1),
                        style = Stroke(width = 2.5f.dp.toPx())
                    )

                    // Draw Trend Dots
                    weeklyData.forEachIndexed { index, data ->
                        val xCenter = spacing * index + spacing / 2f
                        val yVal = height - (data.second / maxVal) * height * 0.82f
                        val isSelected = index == selectedIndex

                        drawCircle(
                            color = if (isSelected) EmeraldPrimary else Color(0xFF6366F1),
                            radius = if (isSelected) 6.dp.toPx() else 3.5f.dp.toPx(),
                            center = Offset(xCenter, yVal)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // X-Axis Labels & Tap Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weeklyData.forEachIndexed { index, triple ->
                        val isSelected = index == selectedIndex
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) M3PurplePrimary else Color.Transparent,
                            modifier = Modifier.clickable { selectedIndex = index }
                        ) {
                            Text(
                                text = triple.first,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                // Peak Hourly Traffic Area Smooth Curve Graph
                val maxVal = 130f

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("hourly_traffic_chart")
                ) {
                    val width = size.width
                    val height = size.height
                    val itemCount = hourlyData.size
                    val spacing = width / (itemCount - 1)

                    val path = Path()
                    val fillPath = Path()

                    hourlyData.forEachIndexed { index, pair ->
                        val x = index * spacing
                        val y = height - (pair.second / maxVal) * height * 0.82f

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * spacing
                            val prevY = height - (hourlyData[index - 1].second / maxVal) * height * 0.82f
                            val controlX1 = prevX + spacing / 2f
                            val controlY1 = prevY
                            val controlX2 = prevX + spacing / 2f
                            val controlY2 = y

                            path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        }

                        if (index == itemCount - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // Draw Gradient Area Under Curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(M3PurplePrimary.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )

                    // Draw Smooth Curve
                    drawPath(
                        path = path,
                        color = M3PurplePrimary,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw Points
                    hourlyData.forEachIndexed { index, pair ->
                        val x = index * spacing
                        val y = height - (pair.second / maxVal) * height * 0.82f
                        val isSelected = index == selectedIndex

                        drawCircle(
                            color = if (isSelected) EmeraldPrimary else M3PurplePrimary,
                            radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // X-Axis Labels & Tap Targets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    hourlyData.forEachIndexed { index, pair ->
                        val isSelected = index == selectedIndex
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) M3PurplePrimary else Color.Transparent,
                            modifier = Modifier.clickable { selectedIndex = index }
                        ) {
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(M3PurplePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Total Turnstile Check-Ins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PT Sessions Conducted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

