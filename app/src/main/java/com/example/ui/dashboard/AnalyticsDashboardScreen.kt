package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalyticsSummary
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.M3BlueContainer
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.viewmodel.GymViewModel
import kotlin.math.cos
import kotlin.math.sin

data class ChartDayData(
    val dayLabel: String,
    val newSignUps: Int,
    val activeCheckIns: Int,
    val ptSessions: Int,
    val revenue: Double,
    val dateStr: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    viewModel: GymViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.analytics.collectAsState()
    val members by viewModel.members.collectAsState()
    val ptSessions by viewModel.ptSessions.collectAsState()
    val aiInsight by viewModel.aiInsightResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    // Filters
    var selectedTimeframe by remember { mutableStateOf("30 Days") } // "7 Days", "30 Days", "90 Days", "1 Year"
    var selectedDomainFilter by remember { mutableStateOf("All") } // "All", "Sign-ups", "Sessions", "Revenue"
    var isCurrencyInUsd by remember { mutableStateOf(false) }

    // Selected Data Point for Interactive Inspector Card
    var selectedPointIndex by remember { mutableIntStateOf(4) } // Default Friday

    // Sample dataset based on timeframes
    val sampleData = remember(selectedTimeframe) {
        when (selectedTimeframe) {
            "7 Days" -> listOf(
                ChartDayData("Mon", 12, 110, 12, 3800.0, "Jul 14"),
                ChartDayData("Tue", 18, 125, 15, 4200.0, "Jul 15"),
                ChartDayData("Wed", 24, 142, 18, 5100.0, "Jul 16"),
                ChartDayData("Thu", 15, 118, 14, 4600.0, "Jul 17"),
                ChartDayData("Fri", 32, 165, 22, 6800.0, "Jul 18"),
                ChartDayData("Sat", 40, 180, 26, 8200.0, "Jul 19"),
                ChartDayData("Sun", 28, 135, 16, 5900.0, "Jul 20")
            )
            "30 Days" -> listOf(
                ChartDayData("Wk 1", 85, 620, 78, 21500.0, "Jul 1-7"),
                ChartDayData("Wk 2", 92, 690, 84, 24200.0, "Jul 8-14"),
                ChartDayData("Wk 3", 110, 780, 95, 28900.0, "Jul 15-21"),
                ChartDayData("Wk 4", 105, 740, 88, 26400.0, "Jul 22-28"),
                ChartDayData("Wk 5", 128, 850, 102, 31200.0, "Jul 29-31")
            )
            "90 Days" -> listOf(
                ChartDayData("May", 340, 2400, 310, 89000.0, "May 2026"),
                ChartDayData("Jun", 410, 2850, 365, 104000.0, "Jun 2026"),
                ChartDayData("Jul", 520, 3480, 440, 128500.0, "Jul 2026")
            )
            else -> listOf(
                ChartDayData("Q1", 950, 6800, 890, 245000.0, "Q1 2026"),
                ChartDayData("Q2", 1280, 8900, 1140, 312000.0, "Q2 2026"),
                ChartDayData("Q3", 1450, 10200, 1320, 368000.0, "Q3 2026"),
                ChartDayData("Q4 Proj", 1600, 11500, 1480, 410000.0, "Q4 2026")
            )
        }
    }

    val selectedData = sampleData.getOrElse(selectedPointIndex.coerceIn(0, sampleData.size - 1)) {
        sampleData.first()
    }

    val currencySymbol = if (isCurrencyInUsd) "$" else "₹"
    val currencyMultiplier = if (isCurrencyInUsd) 0.012 else 1.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("analytics_dashboard_screen")
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(M3PurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = M3PurpleOnContainer)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Analytics & Charts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Member Sign-ups • Session Activity • Revenue",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Currency Switcher
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .clickable { isCurrencyInUsd = !isCurrencyInUsd }
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCurrencyInUsd) "$ USD" else "₹ INR",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = M3PurplePrimary
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // FILTER CONTROL ROW (TIMEFRAME & DOMAIN)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = M3PurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Timeframe Range",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Sync", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EmeraldPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timeframe Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("7 Days", "30 Days", "90 Days", "1 Year").forEach { range ->
                            val isSelected = selectedTimeframe == range
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTimeframe = range
                                    selectedPointIndex = 0
                                },
                                label = { Text(range, fontSize = 12.sp) },
                                modifier = Modifier.testTag("timeframe_chip_${range.lowercase().replace(" ", "")}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = M3PurplePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Domain Filter Chips
                    Text(
                        text = "Metric Focus:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Sign-ups", "Sessions", "Revenue").forEach { domain ->
                            val isSelected = selectedDomainFilter == domain
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDomainFilter = domain },
                                label = { Text(domain, fontSize = 11.sp) },
                                modifier = Modifier.testTag("domain_filter_${domain.lowercase()}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TOP KPI STAT CARDS GRID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Sign-Ups",
                    value = "${sampleData.sumOf { it.newSignUps }}",
                    icon = Icons.Default.GroupAdd,
                    accentColor = M3PurplePrimary,
                    changeText = "+18.4% YoY",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Sessions",
                    value = "${sampleData.sumOf { it.ptSessions + it.activeCheckIns }}",
                    icon = Icons.Default.FitnessCenter,
                    accentColor = IndigoSecondary,
                    changeText = "+12.1%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val totalRev = sampleData.sumOf { it.revenue } * currencyMultiplier
                val formattedRev = if (totalRev >= 1000) "$currencySymbol${(totalRev / 1000).toInt()}k" else "$currencySymbol${totalRev.toInt()}"

                StatCard(
                    title = "Gross Revenue",
                    value = formattedRev,
                    icon = Icons.Default.AttachMoney,
                    accentColor = EmeraldPrimary,
                    changeText = "+24.5%",
                    isHighlighted = true,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Retention Rate",
                    value = "${analytics.retentionRatePercent}%",
                    icon = Icons.Default.TrendingUp,
                    accentColor = AmberTertiary,
                    changeText = "High Score",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DYNAMIC TAP INSPECTOR CARD (Shows data for selected point)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, M3PurplePrimary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("data_point_inspector_card")
            ) {
                Column {
                    // Top Gradient Header Strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(M3PurplePrimary, IndigoSecondary, EmeraldPrimary)
                                )
                            )
                    )

                    Column(modifier = Modifier.padding(18.dp)) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = M3PurplePrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Insights,
                                            contentDescription = null,
                                            tint = M3PurplePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Data Point Inspector",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = M3PurplePrimary
                                    )
                                    Text(
                                        text = "${selectedData.dateStr} • ${selectedData.dayLabel}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Tap guide pill badge
                            Surface(
                                color = M3PurpleContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Tap chart to inspect",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp
                                    ),
                                    color = M3PurpleOnContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val dayRev = (selectedData.revenue * currencyMultiplier).toInt()

                        // 2x2 Grid Layout for maximum clarity and zero text overlap
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Row 1: Sign-ups & Check-ins
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Metric 1: New Sign-ups
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = M3PurplePrimary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.GroupAdd,
                                                    contentDescription = null,
                                                    tint = M3PurplePrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "New Sign-ups",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${selectedData.newSignUps} Members",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                // Metric 2: Gym Check-ins
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = IndigoSecondary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = IndigoSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Gym Check-ins",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${selectedData.activeCheckIns} Visits",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Row 2: PT Sessions & Day Revenue
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Metric 3: PT Sessions
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = AmberTertiary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.FitnessCenter,
                                                    contentDescription = null,
                                                    tint = AmberTertiary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "PT Sessions",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${selectedData.ptSessions} Done",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                // Metric 4: Day Revenue
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldPrimary.copy(alpha = 0.18f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.AttachMoney,
                                                    contentDescription = null,
                                                    tint = EmeraldPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Day Revenue",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "$currencySymbol$dayRev",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = EmeraldPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: MEMBER SIGN-UPS & PLAN DISTRIBUTION CHARTS
            if (selectedDomainFilter == "All" || selectedDomainFilter == "Sign-ups") {
                SectionHeader(
                    title = "1. Member Sign-Ups & Plan Distribution",
                    subtitle = "New member acquisition trends vs plan type breakups"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // BAR CHART: Member Sign-Ups Over Time
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New Member Sign-Ups",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                color = M3PurpleContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Bar Visualizer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = M3PurpleOnContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Canvas Bar Chart
                        val maxSignUps = (sampleData.maxOfOrNull { it.newSignUps } ?: 100).coerceAtLeast(10)
                        val primaryColor = M3PurplePrimary
                        val accentHighlight = EmeraldPrimary

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("chart_member_signups_canvas")
                                .pointerInput(sampleData) {
                                    detectTapGestures { offset ->
                                        val barWidthStep = size.width / sampleData.size
                                        val clickedIndex = (offset.x / barWidthStep).toInt().coerceIn(0, sampleData.size - 1)
                                        selectedPointIndex = clickedIndex
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height
                            val barWidth = (w / sampleData.size) * 0.45f
                            val spacing = w / sampleData.size

                            // Gridlines
                            for (i in 0..4) {
                                val y = h * (i / 4f)
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                                )
                            }

                            // Bars
                            sampleData.forEachIndexed { idx, item ->
                                val barHeight = (item.newSignUps.toFloat() / maxSignUps.toFloat()) * (h - 30f)
                                val x = idx * spacing + (spacing - barWidth) / 2f
                                val y = h - 30f - barHeight
                                val isSelected = idx == selectedPointIndex

                                // Bar shadow/gradient
                                val gradient = Brush.verticalGradient(
                                    colors = if (isSelected) {
                                        listOf(EmeraldPrimary, EmeraldPrimary.copy(alpha = 0.6f))
                                    } else {
                                        listOf(primaryColor, primaryColor.copy(alpha = 0.5f))
                                    }
                                )

                                drawRoundRect(
                                    brush = gradient,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )

                                // Highlight ring if selected
                                if (isSelected) {
                                    drawCircle(
                                        color = EmeraldPrimary,
                                        radius = 6f,
                                        center = Offset(x + barWidth / 2f, y - 10f)
                                    )
                                }
                            }
                        }

                        // X-Axis Labels Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            sampleData.forEachIndexed { idx, item ->
                                val isSelected = idx == selectedPointIndex
                                Text(
                                    text = item.dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.clickable { selectedPointIndex = idx }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DONUT CHART: Membership Plan Breakdown
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Membership Tier Share",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = M3PurplePrimary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Canvas Donut Chart
                            val planSlices = listOf(
                                Triple("Annual VIP", 42f, M3PurplePrimary),
                                Triple("Gold 6-Month", 31f, IndigoSecondary),
                                Triple("Monthly Flex", 18f, EmeraldPrimary),
                                Triple("PT Starter", 9f, AmberTertiary)
                            )

                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .testTag("chart_plan_donut_canvas"),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    var startAngle = -90f
                                    val strokeWidth = 32f

                                    planSlices.forEach { (_, percentage, color) ->
                                        val sweepAngle = (percentage / 100f) * 360f
                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle - 3f, // 3deg gap between slices
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("100%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Plans", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Donut Legend
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                planSlices.forEach { (label, pct, col) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(col)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        }
                                        Text("${pct.toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = col)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // SECTION 2: SESSION ACTIVITY & PEAK HOURS CHARTS
            if (selectedDomainFilter == "All" || selectedDomainFilter == "Sessions") {
                SectionHeader(
                    title = "2. Gym Check-ins & PT Session Activity",
                    subtitle = "Attendance volume & peak gym hour utilization"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SPLINE LINE CHART: Sessions & Check-Ins over Time
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Session Attendance Curve",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(M3PurplePrimary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check-ins", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)

                                Spacer(modifier = Modifier.width(10.dp))

                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldPrimary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PT Sessions", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxCheckIns = (sampleData.maxOfOrNull { it.activeCheckIns } ?: 200).coerceAtLeast(20)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("chart_session_activity_canvas")
                                .pointerInput(sampleData) {
                                    detectTapGestures { offset ->
                                        val step = size.width / (sampleData.size - 1).coerceAtLeast(1)
                                        val clickedIndex = (offset.x / step + 0.5f).toInt().coerceIn(0, sampleData.size - 1)
                                        selectedPointIndex = clickedIndex
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height - 30f
                            val stepX = w / (sampleData.size - 1).coerceAtLeast(1)

                            // Background area path for Check-ins
                            val fillPath = Path()
                            fillPath.moveTo(0f, h)

                            val linePathCheckIns = Path()
                            val linePathPt = Path()

                            sampleData.forEachIndexed { idx, item ->
                                val x = idx * stepX
                                val yCheckIn = h - (item.activeCheckIns.toFloat() / maxCheckIns.toFloat()) * h
                                val yPt = h - ((item.ptSessions * 5).toFloat() / maxCheckIns.toFloat()) * h

                                if (idx == 0) {
                                    linePathCheckIns.moveTo(x, yCheckIn)
                                    fillPath.lineTo(x, yCheckIn)
                                    linePathPt.moveTo(x, yPt)
                                } else {
                                    val prevX = (idx - 1) * stepX
                                    val prevYCheckIn = h - (sampleData[idx - 1].activeCheckIns.toFloat() / maxCheckIns.toFloat()) * h
                                    val prevYPt = h - ((sampleData[idx - 1].ptSessions * 5).toFloat() / maxCheckIns.toFloat()) * h

                                    // Cubic Bezier curve control points
                                    val ctrl1X = prevX + stepX / 2f
                                    val ctrl1Y = prevYCheckIn
                                    val ctrl2X = prevX + stepX / 2f
                                    val ctrl2Y = yCheckIn

                                    linePathCheckIns.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x, yCheckIn)
                                    fillPath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x, yCheckIn)

                                    val ctrl1YPt = prevYPt
                                    val ctrl2YPt = yPt
                                    linePathPt.cubicTo(ctrl1X, ctrl1YPt, ctrl2X, ctrl2YPt, x, yPt)
                                }

                                // Draw node point
                                val isSelected = idx == selectedPointIndex
                                drawCircle(
                                    color = if (isSelected) EmeraldPrimary else M3PurplePrimary,
                                    radius = if (isSelected) 8f else 4f,
                                    center = Offset(x, yCheckIn)
                                )

                                drawCircle(
                                    color = EmeraldPrimary,
                                    radius = if (isSelected) 6f else 3f,
                                    center = Offset(x, yPt)
                                )
                            }

                            fillPath.lineTo(w, h)
                            fillPath.close()

                            // Draw gradient fill under CheckIns curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(M3PurplePrimary.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )

                            // Draw lines
                            drawPath(
                                path = linePathCheckIns,
                                color = M3PurplePrimary,
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )

                            drawPath(
                                path = linePathPt,
                                color = EmeraldPrimary,
                                style = Stroke(width = 3f, cap = StrokeCap.Round)
                            )
                        }

                        // X-Axis Labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            sampleData.forEachIndexed { idx, item ->
                                Text(
                                    text = item.dayLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (idx == selectedPointIndex) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontWeight = if (idx == selectedPointIndex) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // HOURLY PEAK ATTENDANCE HEATMAP / BARS
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hourly Peak Gym Rush Hours",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                color = RoseDanger.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Peak: 6-9 PM",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RoseDanger,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val peakData = listOf(
                            Pair("6-9 AM (Morning)", 82),
                            Pair("9-12 PM (Midday)", 45),
                            Pair("12-3 PM (Lunch)", 35),
                            Pair("3-6 PM (Afternoon)", 68),
                            Pair("6-9 PM (Evening Rush)", 98),
                            Pair("9-11 PM (Late)", 28)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            peakData.forEach { (timeSlot, capacityPct) ->
                                val barColor = when {
                                    capacityPct > 85 -> RoseDanger
                                    capacityPct > 60 -> AmberTertiary
                                    else -> EmeraldPrimary
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = timeSlot,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(130.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(12.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(fraction = capacityPct / 100f)
                                                .clip(CircleShape)
                                                .background(barColor)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = "$capacityPct%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = barColor,
                                        modifier = Modifier.width(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // SECTION 3: REVENUE & FINANCIAL METRICS CHARTS
            if (selectedDomainFilter == "All" || selectedDomainFilter == "Revenue") {
                SectionHeader(
                    title = "3. Revenue Metrics & Goal Progress",
                    subtitle = "Gross earnings, MRR tracking, and target attainment"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // REVENUE LINE CHART WITH AREA GRADIENT
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Revenue Trend Curve",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Monthly Recurring Revenue + PT Add-ons",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Text(
                                text = "$currencySymbol${(sampleData.sumOf { it.revenue } * currencyMultiplier).toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxRev = (sampleData.maxOfOrNull { it.revenue } ?: 10000.0).coerceAtLeast(1000.0)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .testTag("chart_revenue_canvas")
                                .pointerInput(sampleData) {
                                    detectTapGestures { offset ->
                                        val step = size.width / (sampleData.size - 1).coerceAtLeast(1)
                                        val clickedIndex = (offset.x / step + 0.5f).toInt().coerceIn(0, sampleData.size - 1)
                                        selectedPointIndex = clickedIndex
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height - 30f
                            val stepX = w / (sampleData.size - 1).coerceAtLeast(1)

                            val revPath = Path()
                            val fillPath = Path()

                            fillPath.moveTo(0f, h)

                            sampleData.forEachIndexed { idx, item ->
                                val x = idx * stepX
                                val y = h - (item.revenue / maxRev).toFloat() * h

                                if (idx == 0) {
                                    revPath.moveTo(x, y)
                                    fillPath.lineTo(x, y)
                                } else {
                                    val prevX = (idx - 1) * stepX
                                    val prevY = h - (sampleData[idx - 1].revenue / maxRev).toFloat() * h

                                    val ctrl1X = prevX + stepX / 2f
                                    val ctrl1Y = prevY
                                    val ctrl2X = prevX + stepX / 2f
                                    val ctrl2Y = y

                                    revPath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x, y)
                                    fillPath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x, y)
                                }

                                val isSelected = idx == selectedPointIndex
                                drawCircle(
                                    color = EmeraldPrimary,
                                    radius = if (isSelected) 8f else 4f,
                                    center = Offset(x, y)
                                )
                            }

                            fillPath.lineTo(w, h)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(EmeraldPrimary.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )

                            drawPath(
                                path = revPath,
                                color = EmeraldPrimary,
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )
                        }

                        // Labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            sampleData.forEachIndexed { idx, item ->
                                val isSelected = idx == selectedPointIndex
                                Text(
                                    text = item.dayLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // REVENUE GOAL PROGRESS ARC GAUGE
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Monthly Target Attainment Gauge",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Target: $currencySymbol${(50000.0 * currencyMultiplier).toInt()} / Month",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentRevSum = sampleData.sumOf { it.revenue }
                            val targetRev = 50000.0
                            val pctAchieved = ((currentRevSum / targetRev) * 100).coerceAtMost(100.0).toFloat()

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .testTag("chart_goal_gauge_canvas"),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 24f
                                    // Arc track
                                    drawArc(
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        startAngle = 135f,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )

                                    // Arc progress
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(M3PurplePrimary, EmeraldPrimary)
                                        ),
                                        startAngle = 135f,
                                        sweepAngle = (pctAchieved / 100f) * 270f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${pctAchieved.toInt()}%",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        text = "Goal",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FinancialItemRow(
                                    label = "Target Goal",
                                    value = "$currencySymbol${(50000 * currencyMultiplier).toInt()}",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                FinancialItemRow(
                                    label = "Achieved So Far",
                                    value = "$currencySymbol${(currentRevSum * currencyMultiplier).toInt()}",
                                    color = EmeraldPrimary
                                )
                                FinancialItemRow(
                                    label = "Projected EOM",
                                    value = "$currencySymbol${(analytics.projectedMonthlyRevenue * currencyMultiplier).toInt()}",
                                    color = M3PurplePrimary
                                )
                                FinancialItemRow(
                                    label = "Pending Invoices",
                                    value = "$currencySymbol${(analytics.pendingPaymentsAmount * currencyMultiplier).toInt()}",
                                    color = RoseDanger
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // SECTION 4: GEMINI AI BUSINESS ANALYTICS SYNTHESIZER
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = M3PurpleContainer.copy(alpha = 0.7f),
                borderColor = M3PurplePrimary.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(M3PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gemini AI Revenue Synthesis",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = M3PurpleOnContainer
                                )
                                Text(
                                    text = "Automated AI business recommendations",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = M3PurpleOnContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (aiInsight != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = aiInsight!!,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.queryAiBusinessInsight("Analyze member sign-up growth vs peak hour session capacity and provide 3 revenue enhancement strategies.")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("generate_ai_analytics_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Analyzing...", color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Run AI Report", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        }

                        val context = androidx.compose.ui.platform.LocalContext.current
                        Button(
                            onClick = {
                                com.example.util.PdfUtils.generateAndSharePdfReport(
                                    context = context,
                                    dietPlan = null,
                                    workoutPlan = null,
                                    analytics = analytics,
                                    aiInsight = aiInsight
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("export_analytics_pdf_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FinancialItemRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = color)
    }
}
