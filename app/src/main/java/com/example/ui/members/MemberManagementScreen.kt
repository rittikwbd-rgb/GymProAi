package com.example.ui.members

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Member
import com.example.util.ShareUtils
import java.io.File
import java.io.FileOutputStream
import com.example.data.model.MembershipStatus
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.RoseDanger
import com.example.ui.viewmodel.GymViewModel

@Composable
fun MemberManagementScreen(
    viewModel: GymViewModel,
    onSelectMemberForDiet: (Member) -> Unit,
    modifier: Modifier = Modifier
) {
    val members by viewModel.members.collectAsState()
    val membershipPackages by viewModel.membershipPackages.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf<MembershipStatus?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<Member?>(null) }
    var weightProgressMember by remember { mutableStateOf<Member?>(null) }

    val filteredMembers = members.filter { m ->
        val matchesQuery = m.name.contains(searchQuery, ignoreCase = true) ||
                m.phone.contains(searchQuery) ||
                m.membershipPlan.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedFilterStatus == null || m.status == selectedFilterStatus
        matchesQuery && matchesStatus
    }

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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search member name, phone, plan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("member_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilterStatus == null,
                    onClick = { selectedFilterStatus = null },
                    label = { Text("All (${members.size})") }
                )
                FilterChip(
                    selected = selectedFilterStatus == MembershipStatus.ACTIVE,
                    onClick = { selectedFilterStatus = MembershipStatus.ACTIVE },
                    label = { Text("Active") }
                )
                FilterChip(
                    selected = selectedFilterStatus == MembershipStatus.EXPIRING_SOON,
                    onClick = { selectedFilterStatus = MembershipStatus.EXPIRING_SOON },
                    label = { Text("Expiring Soon") }
                )
                FilterChip(
                    selected = selectedFilterStatus == MembershipStatus.EXPIRED,
                    onClick = { selectedFilterStatus = MembershipStatus.EXPIRED },
                    label = { Text("Expired") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMembers) { member ->
                    MemberListItemCard(
                        member = member,
                        onEdit = {
                            editingMember = member
                            showAddDialog = true
                        },
                        onDelete = {
                            viewModel.deleteMember(member.id)
                        },
                        onGenerateDiet = {
                            onSelectMemberForDiet(member)
                        },
                        onViewWeightProgress = {
                            weightProgressMember = member
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingMember = null
                showAddDialog = true
            },
            containerColor = EmeraldPrimary,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_member_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Member")
        }
    }

    if (showAddDialog) {
        AddEditMemberDialog(
            existingMember = editingMember,
            packages = membershipPackages,
            onDismiss = { showAddDialog = false },
            onSave = { memberToSave ->
                viewModel.addOrUpdateMember(memberToSave)
                showAddDialog = false
            }
        )
    }

    weightProgressMember?.let { targetMember ->
        MemberWeightProgressDialog(
            member = targetMember,
            viewModel = viewModel,
            onDismiss = { weightProgressMember = null }
        )
    }
}

@Composable
fun MemberListItemCard(
    member: Member,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onGenerateDiet: () -> Unit,
    onViewWeightProgress: () -> Unit
) {
    val context = LocalContext.current

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (member.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = member.photoUrl,
                            contentDescription = member.name,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, IndigoSecondary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(IndigoSecondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = member.name,
                                tint = IndigoSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = member.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                val statusColor = when (member.status) {
                    MembershipStatus.ACTIVE -> EmeraldPrimary
                    MembershipStatus.EXPIRING_SOON -> AmberTertiary
                    MembershipStatus.EXPIRED, MembershipStatus.CANCELLED -> RoseDanger
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = member.status.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plan: ${member.membershipPlan} • Weight: ${member.weightKg}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "Exp: ${member.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onViewWeightProgress,
                        modifier = Modifier.testTag("weight_progress_button_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Weight Log", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }

                    // 1-CLICK WHATSAPP INVOICE BUTTON
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF25D366).copy(alpha = 0.15f),
                        modifier = Modifier
                            .clickable { ShareUtils.shareInvoiceOnWhatsApp(context, member) }
                            .testTag("whatsapp_invoice_button_${member.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Send WhatsApp Invoice",
                                tint = Color(0xFF1E7E34),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Invoice",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E7E34)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onGenerateDiet) {
                        Text("AI Diet", color = IndigoSecondary, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseDanger)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditMemberDialog(
    existingMember: Member?,
    packages: List<com.example.data.model.GymMembershipPackage> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Member) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingMember?.name ?: "") }
    var phone by remember { mutableStateOf(existingMember?.phone ?: "") }
    var email by remember { mutableStateOf(existingMember?.email ?: "") }
    var heightText by remember { mutableStateOf(existingMember?.heightCm?.toInt()?.toString() ?: "175") }
    var weightText by remember { mutableStateOf(existingMember?.weightKg?.toInt()?.toString() ?: "72") }
    var planName by remember { mutableStateOf(existingMember?.membershipPlan ?: (packages.firstOrNull()?.packageName ?: "1 Month Monthly Basic")) }
    var medicalConditions by remember { mutableStateOf(existingMember?.medicalConditions ?: "None") }
    var capturedPhotoUrl by remember { mutableStateOf(existingMember?.photoUrl ?: "") }
    var showPackageDropdown by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "member_pic_${System.currentTimeMillis()}.jpg")
                val out = FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                capturedPhotoUrl = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            capturedPhotoUrl = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingMember == null) "Add New Member" else "Edit Member Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // PHOTO CAPTURE & CAMERA PICKER SECTION
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, IndigoSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (capturedPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = capturedPhotoUrl,
                                contentDescription = "Member Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Member Photo",
                                tint = IndigoSecondary,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp).testTag("take_photo_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Take Photo", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp).testTag("select_gallery_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("member_name_field")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth().testTag("member_phone_field")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // PACKAGE PICKLIST DROPDOWN
                Text(
                    text = "Select Membership Package (Configured in Settings)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = planName,
                        onValueChange = { planName = it },
                        label = { Text("Selected Package / Plan") },
                        trailingIcon = {
                            androidx.compose.material3.IconButton(onClick = { showPackageDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Package Picklist")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("member_package_select"),
                        readOnly = false
                    )

                    androidx.compose.material3.DropdownMenu(
                        expanded = showPackageDropdown,
                        onDismissRequest = { showPackageDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        packages.forEach { pkg ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = pkg.packageName,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${pkg.durationMonths} Month(s) • ₹${pkg.price.toInt()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = com.example.ui.theme.EmeraldPrimary
                                        )
                                    }
                                },
                                onClick = {
                                    planName = "${pkg.packageName} (₹${pkg.price.toInt()})"
                                    showPackageDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = medicalConditions,
                    onValueChange = { medicalConditions = it },
                    label = { Text("Medical Conditions / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = existingMember?.copy(
                        name = name,
                        phone = phone,
                        whatsapp = phone.replace(" ", ""),
                        email = email,
                        heightCm = heightText.toFloatOrNull() ?: 170f,
                        weightKg = weightText.toFloatOrNull() ?: 70f,
                        membershipPlan = planName,
                        medicalConditions = medicalConditions,
                        photoUrl = capturedPhotoUrl
                    ) ?: Member(
                        name = name,
                        phone = phone,
                        whatsapp = phone.replace(" ", ""),
                        email = email,
                        dob = "1995-01-01",
                        gender = "Male",
                        heightCm = heightText.toFloatOrNull() ?: 170f,
                        weightKg = weightText.toFloatOrNull() ?: 70f,
                        address = "Main City Center",
                        emergencyContact = "+1 555-000-1111",
                        medicalConditions = medicalConditions,
                        membershipPlan = planName,
                        photoUrl = capturedPhotoUrl
                    )
                    onSave(m)
                },
                modifier = Modifier.testTag("save_member_confirm_button")
            ) {
                Text("Save Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
