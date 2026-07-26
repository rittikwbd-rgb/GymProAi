package com.example.ui.crm

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.viewmodel.GymViewModel

@Composable
fun LeadCrmScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val leads by viewModel.leads.collectAsState()
    var showAddLeadDialog by remember { mutableStateOf(false) }

    var newLeadName by remember { mutableStateOf("") }
    var newLeadPhone by remember { mutableStateOf("") }
    var newLeadEmail by remember { mutableStateOf("") }
    var newLeadNotes by remember { mutableStateOf("Interested in 1-on-1 personal training.") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SectionHeader(
                title = "Lead & Sales Pipeline CRM",
                subtitle = "Inquiries, Trial Passes & Member Conversions"
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(leads) { lead ->
                    LeadCard(
                        lead = lead,
                        onUpdateStatus = { nextStatus ->
                            viewModel.updateLeadStatus(lead.id, nextStatus)
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddLeadDialog = true },
            containerColor = EmeraldPrimary,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_lead_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Lead")
        }
    }

    if (showAddLeadDialog) {
        AlertDialog(
            onDismissRequest = { showAddLeadDialog = false },
            title = { Text("Capture New Inquiry Lead", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newLeadName,
                        onValueChange = { newLeadName = it },
                        label = { Text("Lead Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("lead_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLeadPhone,
                        onValueChange = { newLeadPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().testTag("lead_phone_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLeadEmail,
                        onValueChange = { newLeadEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLeadNotes,
                        onValueChange = { newLeadNotes = it },
                        label = { Text("Notes / Inquiry Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addLead(newLeadName, newLeadPhone, newLeadEmail, newLeadNotes)
                        showAddLeadDialog = false
                    },
                    modifier = Modifier.testTag("save_lead_button")
                ) {
                    Text("Save Lead to CRM")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLeadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LeadCard(
    lead: Lead,
    onUpdateStatus: (LeadStatus) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lead.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = when (lead.status) {
                        LeadStatus.NEW -> EmeraldPrimary
                        LeadStatus.CONTACTED -> IndigoSecondary
                        LeadStatus.TRIAL_SCHEDULED -> AmberTertiary
                        LeadStatus.CONVERTED -> EmeraldPrimary
                        LeadStatus.LOST -> Color.Gray
                    }.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = lead.status.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${lead.phone} • Source: ${lead.source}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Notes: ${lead.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onUpdateStatus(LeadStatus.TRIAL_SCHEDULED) }) {
                    Text("Schedule Trial")
                }
                TextButton(onClick = { onUpdateStatus(LeadStatus.CONVERTED) }) {
                    Text("Convert to Member", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
