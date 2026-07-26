package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ReceiptLong
import com.example.ui.invoices.InvoiceManagementScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.ui.auth.AuthScreen
import com.example.ui.components.RoleSwitcherHeader
import com.example.ui.components.RoomSyncInspectorDialog
import com.example.ui.crm.LeadCrmScreen
import com.example.ui.dashboard.AnalyticsDashboardScreen
import com.example.ui.dashboard.MemberDashboardScreen
import com.example.ui.dashboard.OwnerDashboardScreen
import com.example.ui.dashboard.TrainerDashboardScreen
import com.example.ui.diet.DietPlannerScreen
import com.example.ui.members.MemberManagementScreen
import com.example.ui.renewals.RenewalManagementScreen
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.DialogProperties
import com.example.ui.reports.ReportPreviewScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GymAiProTheme
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary
import com.example.ui.viewmodel.GymViewModel
import com.example.ui.workout.WorkoutLoggingScreen
import com.example.ui.workout.WorkoutPlannerScreen

class MainActivity : ComponentActivity() {

    private val viewModel: GymViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymAiProTheme {
                GymAiAppMainContent(viewModel = viewModel)
            }
        }
    }
}

enum class MainNavTab(val title: String) {
    DASHBOARD("Dashboard"),
    MEMBERS("Members CRM"),
    DIET_WORKOUT("AI Fitness"),
    WORKOUT_LOGGER("PT Logger"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings")
}

@Composable
fun GymAiAppMainContent(viewModel: GymViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val gymName by viewModel.gymName.collectAsState()
    val syncStatusInfo by viewModel.syncStatusInfo.collectAsState()
    var isAuthenticated by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(MainNavTab.DASHBOARD) }

    var showSyncInspectorDialog by remember { mutableStateOf(false) }
    var showWhatsAppBotDialog by remember { mutableStateOf(false) }

    var isViewingReportPreview by remember { mutableStateOf(false) }
    var fitnessSubTab by remember { mutableStateOf(0) } // 0: AI Diet, 1: AI Workout
    var memberSubTab by remember { mutableStateOf(0) } // 0: Directory, 1: Workout Logger, 2: Renewals, 3: Lead CRM

    if (!isAuthenticated) {
        AuthScreen(
            onLoginSuccess = { role, name, email, gymName ->
                viewModel.setUserSession(name, email, role, gymName)
                isAuthenticated = true
            }
        )
    } else if (isViewingReportPreview) {
        ReportPreviewScreen(
            viewModel = viewModel,
            onBack = { isViewingReportPreview = false }
        )
    } else {
        Scaffold(
            topBar = {
                RoleSwitcherHeader(
                    currentRole = currentUser.role,
                    onRoleSelected = { newRole ->
                        viewModel.switchRole(newRole)
                    },
                    gymName = gymName,
                    syncStatusInfo = syncStatusInfo,
                    onSyncBadgeClick = { showSyncInspectorDialog = true },
                    onSettingsClick = { currentTab = MainNavTab.SETTINGS }
                )
            },
            bottomBar = {
                val visibleTabs = when (currentUser.role) {
                    UserRole.RECEPTIONIST -> listOf(MainNavTab.DASHBOARD, MainNavTab.MEMBERS, MainNavTab.WORKOUT_LOGGER)
                    UserRole.TRAINER -> listOf(MainNavTab.DASHBOARD, MainNavTab.MEMBERS, MainNavTab.DIET_WORKOUT, MainNavTab.WORKOUT_LOGGER)
                    else -> listOf(MainNavTab.DASHBOARD, MainNavTab.MEMBERS, MainNavTab.DIET_WORKOUT, MainNavTab.WORKOUT_LOGGER, MainNavTab.ANALYTICS)
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    if (visibleTabs.contains(MainNavTab.DASHBOARD)) {
                        NavigationBarItem(
                            selected = currentTab == MainNavTab.DASHBOARD,
                            onClick = { currentTab = MainNavTab.DASHBOARD },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontWeight = if (currentTab == MainNavTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("tab_dashboard"),
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = M3PurpleContainer,
                                selectedIconColor = M3PurpleOnContainer,
                                selectedTextColor = M3PurpleOnContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                    if (visibleTabs.contains(MainNavTab.MEMBERS)) {
                        NavigationBarItem(
                            selected = currentTab == MainNavTab.MEMBERS,
                            onClick = { currentTab = MainNavTab.MEMBERS },
                            icon = { Icon(Icons.Default.People, contentDescription = "Members") },
                            label = { Text("Members", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontWeight = if (currentTab == MainNavTab.MEMBERS) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("tab_members"),
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = M3PurpleContainer,
                                selectedIconColor = M3PurpleOnContainer,
                                selectedTextColor = M3PurpleOnContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                    if (visibleTabs.contains(MainNavTab.DIET_WORKOUT)) {
                        NavigationBarItem(
                            selected = currentTab == MainNavTab.DIET_WORKOUT,
                            onClick = { currentTab = MainNavTab.DIET_WORKOUT },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Fitness") },
                            label = { Text("AI Fitness", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontWeight = if (currentTab == MainNavTab.DIET_WORKOUT) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("tab_ai_fitness"),
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = M3PurpleContainer,
                                selectedIconColor = M3PurpleOnContainer,
                                selectedTextColor = M3PurpleOnContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                    if (visibleTabs.contains(MainNavTab.WORKOUT_LOGGER)) {
                        NavigationBarItem(
                            selected = currentTab == MainNavTab.WORKOUT_LOGGER,
                            onClick = { currentTab = MainNavTab.WORKOUT_LOGGER },
                            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "PT Logger") },
                            label = { Text("PT Logger", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontWeight = if (currentTab == MainNavTab.WORKOUT_LOGGER) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("tab_workout_logger"),
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = M3PurpleContainer,
                                selectedIconColor = M3PurpleOnContainer,
                                selectedTextColor = M3PurpleOnContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                    if (visibleTabs.contains(MainNavTab.ANALYTICS)) {
                        NavigationBarItem(
                            selected = currentTab == MainNavTab.ANALYTICS,
                            onClick = { currentTab = MainNavTab.ANALYTICS },
                            icon = { Icon(Icons.Default.Insights, contentDescription = "Analytics") },
                            label = { Text("Analytics", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontWeight = if (currentTab == MainNavTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("tab_analytics"),
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = M3PurpleContainer,
                                selectedIconColor = M3PurpleOnContainer,
                                selectedTextColor = M3PurpleOnContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab, label = "TabSwitch") { tab ->
                    when (tab) {
                        MainNavTab.DASHBOARD -> {
                            when (currentUser.role) {
                                UserRole.RECEPTIONIST -> {
                                    OwnerDashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToMembers = { currentTab = MainNavTab.MEMBERS; memberSubTab = 0 },
                                        onNavigateToDietPlanner = { currentTab = MainNavTab.DIET_WORKOUT },
                                        onNavigateToRenewals = { currentTab = MainNavTab.MEMBERS; memberSubTab = 1 },
                                        onOpenWhatsAppBot = { showWhatsAppBotDialog = true },
                                        onNavigateToAnalytics = null,
                                        showRevenue = false
                                    )
                                }
                                UserRole.TRAINER -> {
                                    OwnerDashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToMembers = { currentTab = MainNavTab.MEMBERS; memberSubTab = 0 },
                                        onNavigateToDietPlanner = { currentTab = MainNavTab.DIET_WORKOUT },
                                        onNavigateToRenewals = { currentTab = MainNavTab.MEMBERS; memberSubTab = 1 },
                                        onOpenWhatsAppBot = { showWhatsAppBotDialog = true },
                                        onNavigateToAnalytics = null,
                                        showRevenue = false
                                    )
                                }
                                else -> {
                                    OwnerDashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToMembers = { currentTab = MainNavTab.MEMBERS; memberSubTab = 0 },
                                        onNavigateToDietPlanner = { currentTab = MainNavTab.DIET_WORKOUT },
                                        onNavigateToRenewals = { currentTab = MainNavTab.MEMBERS; memberSubTab = 1 },
                                        onOpenWhatsAppBot = { showWhatsAppBotDialog = true },
                                        onNavigateToAnalytics = { currentTab = MainNavTab.ANALYTICS },
                                        showRevenue = true
                                    )
                                }
                            }
                        }
                        MainNavTab.ANALYTICS -> {
                            AnalyticsDashboardScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentTab = MainNavTab.DASHBOARD }
                            )
                        }
                        MainNavTab.MEMBERS -> {
                            Column(modifier = Modifier.fillMaxSize()) {
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
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                            )
                                            .padding(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (memberSubTab == 0) M3PurplePrimary else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { memberSubTab = 0 }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.People,
                                                        contentDescription = null,
                                                        tint = if (memberSubTab == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Directory",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (memberSubTab == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (memberSubTab == 1) M3PurplePrimary else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { memberSubTab = 1 }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = null,
                                                        tint = if (memberSubTab == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Renewals",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (memberSubTab == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (memberSubTab == 2) M3PurplePrimary else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { memberSubTab = 2 }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.ReceiptLong,
                                                        contentDescription = null,
                                                        tint = if (memberSubTab == 2) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Leads",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (memberSubTab == 2) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (memberSubTab == 3) M3PurplePrimary else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { memberSubTab = 3 }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.ReceiptLong,
                                                        contentDescription = null,
                                                        tint = if (memberSubTab == 3) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Invoices",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (memberSubTab == 3) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                when (memberSubTab) {
                                    0 -> MemberManagementScreen(
                                        viewModel = viewModel,
                                        onSelectMemberForDiet = { selectedMem ->
                                            viewModel.selectMember(selectedMem)
                                            currentTab = MainNavTab.DIET_WORKOUT
                                        }
                                    )
                                    1 -> RenewalManagementScreen(viewModel = viewModel)
                                    2 -> LeadCrmScreen(viewModel = viewModel)
                                    3 -> InvoiceManagementScreen(viewModel = viewModel)
                                }
                            }
                        }
                        MainNavTab.WORKOUT_LOGGER -> {
                            WorkoutLoggingScreen(viewModel = viewModel)
                        }
                        MainNavTab.DIET_WORKOUT -> {
                            Column(modifier = Modifier.fillMaxSize()) {
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
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                            )
                                            .padding(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (fitnessSubTab == 0) M3PurplePrimary else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { fitnessSubTab = 0 }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = if (fitnessSubTab == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "AI Diet Planner",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (fitnessSubTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (fitnessSubTab == 1) M3PurplePrimary else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { fitnessSubTab = 1 }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.FitnessCenter,
                                                        contentDescription = null,
                                                        tint = if (fitnessSubTab == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "AI Workout Routine",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (fitnessSubTab == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                when (fitnessSubTab) {
                                    0 -> DietPlannerScreen(
                                        viewModel = viewModel,
                                        onPreviewPdfReport = { isViewingReportPreview = true }
                                    )
                                    else -> WorkoutPlannerScreen(
                                        viewModel = viewModel,
                                        onPreviewPdfReport = { isViewingReportPreview = true }
                                    )
                                }
                            }
                        }
                        MainNavTab.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onLogout = { isAuthenticated = false }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSyncInspectorDialog) {
        RoomSyncInspectorDialog(
            syncInfo = syncStatusInfo,
            viewModel = viewModel,
            onDismiss = { showSyncInspectorDialog = false }
        )
    }

    if (showWhatsAppBotDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppBotDialog = false },
            title = null,
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(560.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    com.example.ui.support.WhatsAppSupportScreen(
                        viewModel = viewModel
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showWhatsAppBotDialog = false },
                    modifier = Modifier.testTag("close_whatsapp_dialog_button")
                ) {
                    Text("Close AI Assistant", fontWeight = FontWeight.Bold)
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        )
    }
}
