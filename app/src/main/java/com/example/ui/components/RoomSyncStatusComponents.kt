package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.RoomSyncState
import com.example.data.model.SyncStatusInfo
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.viewmodel.GymViewModel

/**
 * Compact Sync Status Badge shown in headers or top bars.
 */
@Composable
fun RoomSyncStatusBadge(
    syncInfo: SyncStatusInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, labelText) = when (syncInfo.state) {
        RoomSyncState.SYNCED -> EmeraldPrimary to "Room Synced"
        RoomSyncState.SYNCING -> M3PurplePrimary to "Syncing..."
        RoomSyncState.PENDING_PUSH -> AmberTertiary to "${syncInfo.pendingMutationsCount} Queued"
        RoomSyncState.OFFLINE_CACHED -> AmberTertiary to "Offline Cache"
        RoomSyncState.SYNC_ERROR -> RoseDanger to "Sync Alert"
    }

    Surface(
        color = statusColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("room_sync_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (syncInfo.state == RoomSyncState.SYNCING) {
                val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing)
                    ),
                    label = "spin"
                )
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Syncing",
                    tint = statusColor,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(angle)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = statusColor
            )
        }
    }
}

/**
 * Modern, high-polish visual card for Local Room DB & Cloud Synchronization Status.
 */
@Composable
fun RoomSyncStatusDashboardCard(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val syncInfo by viewModel.syncStatusInfo.collectAsState()
    var showInspectorDialog by remember { mutableStateOf(false) }

    val statusColor = when (syncInfo.state) {
        RoomSyncState.SYNCED -> EmeraldPrimary
        RoomSyncState.SYNCING -> M3PurplePrimary
        RoomSyncState.PENDING_PUSH, RoomSyncState.OFFLINE_CACHED -> AmberTertiary
        RoomSyncState.SYNC_ERROR -> RoseDanger
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("room_sync_status_card")
    ) {
        Column {
            // TOP GRADIENT STATUS ACCENT STRIP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                statusColor,
                                statusColor.copy(alpha = 0.4f),
                                M3PurplePrimary.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            Column(modifier = Modifier.padding(18.dp)) {
                // HEADER ROW WITH AVATAR & CONNECTION STATUS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Floating Status Avatar Container
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (syncInfo.state == RoomSyncState.SYNCING) {
                                val infiniteTransition = rememberInfiniteTransition(label = "rotation_card")
                                val angle by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(durationMillis = 1000, easing = LinearEasing)
                                    ),
                                    label = "spin_card"
                                )
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Syncing",
                                    tint = statusColor,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .rotate(angle)
                                )
                            } else {
                                Icon(
                                    imageVector = when (syncInfo.state) {
                                        RoomSyncState.SYNCED -> Icons.Default.CloudDone
                                        RoomSyncState.SYNCING -> Icons.Default.CloudSync
                                        RoomSyncState.PENDING_PUSH -> Icons.Default.CloudQueue
                                        RoomSyncState.OFFLINE_CACHED -> Icons.Default.CloudOff
                                        RoomSyncState.SYNC_ERROR -> Icons.Default.CloudOff
                                    },
                                    contentDescription = "Sync Status",
                                    tint = statusColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Room DB ↔ Cloud Sync",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                // Online / Offline Network Badge
                                Surface(
                                    color = if (syncInfo.isOnline) EmeraldPrimary.copy(alpha = 0.12f) else AmberTertiary.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (syncInfo.isOnline) EmeraldPrimary.copy(alpha = 0.3f) else AmberTertiary.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (syncInfo.isOnline) EmeraldPrimary else AmberTertiary)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (syncInfo.isOnline) "ONLINE" else "OFFLINE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                            color = if (syncInfo.isOnline) EmeraldPrimary else AmberTertiary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = when (syncInfo.state) {
                                    RoomSyncState.SYNCED -> "Offline-first SQLite active • ${syncInfo.localRecordCount} entities synchronized"
                                    RoomSyncState.SYNCING -> "Pushing local Room queue mutations to cloud API..."
                                    RoomSyncState.PENDING_PUSH -> "${syncInfo.pendingMutationsCount} local mutations queued for cloud replication"
                                    RoomSyncState.OFFLINE_CACHED -> "Offline Mode • Storing mutations in Room SQLite queue"
                                    RoomSyncState.SYNC_ERROR -> "Sync encounter • Tap button below to re-sync"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // INSPECT DATABASE BUTTON
                    Surface(
                        shape = CircleShape,
                        color = M3PurplePrimary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, M3PurplePrimary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { showInspectorDialog = true }
                            .testTag("open_sync_inspector_icon")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Inspect Database",
                                tint = M3PurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // ANIMATED PROGRESS BAR WHEN SYNCING
                AnimatedVisibility(visible = syncInfo.state == RoomSyncState.SYNCING) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = M3PurplePrimary,
                            trackColor = M3PurplePrimary.copy(alpha = 0.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STATS & STORAGE METRICS TILES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tile 1: SQLite Engine
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = M3PurplePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SQLite DB",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = syncInfo.dbFileName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "${syncInfo.localRecordCount} records",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Tile 2: Last Sync Timestamp
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = IndigoSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Last Synced",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = syncInfo.lastSyncedTime,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "Real-time Flow",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Tile 3: Pending Mutation Queue
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = if (syncInfo.pendingMutationsCount > 0) AmberTertiary else EmeraldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Queue Status",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (syncInfo.pendingMutationsCount > 0) "${syncInfo.pendingMutationsCount} Pending" else "0 Pending",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncInfo.pendingMutationsCount > 0) AmberTertiary else EmeraldPrimary
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = if (syncInfo.pendingMutationsCount > 0) "Awaiting push" else "Fully synced",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ACTION BUTTONS ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerOfflineSync() },
                        enabled = syncInfo.state != RoomSyncState.SYNCING,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                            .testTag("trigger_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (syncInfo.state == RoomSyncState.SYNCING) "Syncing..." else "Sync Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleOnlineMode() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("toggle_online_mode_button")
                    ) {
                        Icon(
                            imageVector = if (syncInfo.isOnline) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (syncInfo.isOnline) AmberTertiary else EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (syncInfo.isOnline) "Offline" else "Online",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.simulateOfflineMutation("Added Offline Member & PT Log")
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("simulate_offline_mutation_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+ Queue", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // INSPECTOR DIALOG
    if (showInspectorDialog) {
        RoomSyncInspectorDialog(
            syncInfo = syncInfo,
            viewModel = viewModel,
            onDismiss = { showInspectorDialog = false }
        )
    }
}

/**
 * Modern, clean Developer/Admin Dialog providing in-depth inspection of Room Database & Cloud Queue.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomSyncInspectorDialog(
    syncInfo: SyncStatusInfo,
    viewModel: GymViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(vertical = 16.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // TOP DIALOG HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(M3PurplePrimary.copy(alpha = 0.15f))
                                .border(1.dp, M3PurplePrimary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = M3PurplePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Room Database & Cloud Inspector",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Live SQLite Schema & Mutation Queue",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Inspector",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: ARCHITECTURE & ENGINE CARD
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = M3PurplePrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SQLite Database Engine",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = M3PurplePrimary
                                )
                            }

                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AES-256 Active", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = EmeraldPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Persistence Framework", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Android Room 2.6.1 + KSP", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Database File", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(syncInfo.dbFileName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Active Cache Count", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${syncInfo.localRecordCount} records", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sync Status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (syncInfo.isOnline) "Cloud Socket Active" else "Local SQLite Only",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (syncInfo.isOnline) EmeraldPrimary else AmberTertiary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Registered Room Entities & Daos:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("MemberDao", "PtSessionDao", "LeadDao", "DietPlanDao", "WorkoutPlanDao", "AuditLogDao").forEach { dao ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = M3PurplePrimary.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, M3PurplePrimary.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = dao,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = M3PurplePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: LIVE OFFLINE MUTATIONS QUEUE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = AmberTertiary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Offline Mutations Queue",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (syncInfo.pendingMutationsCount > 0) AmberTertiary.copy(alpha = 0.15f) else EmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${syncInfo.pendingMutationsCount} Items",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (syncInfo.pendingMutationsCount > 0) AmberTertiary else EmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (syncInfo.pendingDetails.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = EmeraldPrimary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Queue Empty & Synchronized", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldPrimary)
                                Text("All local Room transactions have been successfully written to the cloud database.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        syncInfo.pendingDetails.forEachIndexed { idx, detail ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AmberTertiary.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberTertiary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AmberTertiary,
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${idx + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.Black, fontSize = 10.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = detail,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "Awaiting cloud push sync",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: NETWORK CONTROL TOGGLE
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (syncInfo.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (syncInfo.isOnline) EmeraldPrimary else AmberTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Network Connection Simulation", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    if (syncInfo.isOnline) "Connected • Auto-replicating Room data" else "Simulating offline disconnect",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = syncInfo.isOnline,
                            onCheckedChange = { viewModel.toggleOnlineMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.triggerOfflineSync()
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                modifier = Modifier.testTag("force_full_sync_dialog_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Force Full Sync", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_sync_inspector_dialog_button")
            ) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
