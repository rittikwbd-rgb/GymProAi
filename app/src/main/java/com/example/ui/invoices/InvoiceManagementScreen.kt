package com.example.ui.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GymInvoice
import com.example.data.model.GymMembershipPackage
import com.example.data.model.Member
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.components.GlassCard
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary
import com.example.ui.viewmodel.GymViewModel
import com.example.util.ShareUtils

@Composable
fun InvoiceManagementScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val invoices by viewModel.invoices.collectAsState()
    val members by viewModel.members.collectAsState()
    val packages by viewModel.membershipPackages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val gymName by viewModel.gymName.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Paid", "Pending"
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredInvoices = remember(invoices, searchQuery, selectedFilter) {
        invoices.filter { inv ->
            val matchesQuery = inv.memberName.contains(searchQuery, ignoreCase = true) ||
                    inv.id.contains(searchQuery, ignoreCase = true) ||
                    inv.memberPhone.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Paid" -> inv.paymentStatus.equals("Paid", ignoreCase = true)
                "Pending" -> inv.paymentStatus.equals("Pending", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    val totalAmountInvoiced = invoices.sumOf { it.totalAmount }
    val paidCount = invoices.count { it.paymentStatus.equals("Paid", ignoreCase = true) }
    val pendingDues = invoices.filter { it.paymentStatus.equals("Pending", ignoreCase = true) }.sumOf { it.totalAmount }

    val canCreateInvoice = currentUser.role == UserRole.GYM_OWNER || currentUser.role == UserRole.RECEPTIONIST || currentUser.role == UserRole.TRAINER

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top KPI Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = M3PurpleContainer,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Total Invoiced",
                        style = MaterialTheme.typography.labelSmall,
                        color = M3PurpleOnContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${totalAmountInvoiced.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = M3PurpleOnContainer
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = EmeraldPrimary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Paid Receipts",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$paidCount Paid",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Pending Dues",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${pendingDues.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions & Controls Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search invoice #, member...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_invoices_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = M3PurplePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            if (canCreateInvoice) {
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("create_invoice_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Invoice", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Paid", "Pending").forEach { filter ->
                FilterChip(
                    selected = (selectedFilter == filter),
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = M3PurpleContainer,
                        selectedLabelColor = M3PurpleOnContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Invoice History List
        if (filteredInvoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No invoices found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Create a new invoice to generate & share member bills.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredInvoices, key = { it.id }) { invoice ->
                    InvoiceHistoryCard(
                        invoice = invoice,
                        onWhatsAppClick = {
                            ShareUtils.shareInvoiceOnWhatsAppForInvoice(context, invoice, gymName)
                        },
                        onPdfClick = {
                            ShareUtils.shareInvoicePdfForInvoice(context, invoice, gymName)
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateInvoiceDialog(
            members = members,
            packages = packages,
            onDismiss = { showCreateDialog = false },
            onCreateInvoice = { member, pkg, amount, discount, tax, status, mode, notes ->
                val newInv = viewModel.createInvoice(
                    memberId = member.id,
                    memberName = member.name,
                    memberPhone = member.phone,
                    packageName = pkg.packageName,
                    amount = amount,
                    discount = discount,
                    taxAmount = tax,
                    paymentStatus = status,
                    paymentMode = mode,
                    notes = notes
                )
                showCreateDialog = false
                // Auto share options
                ShareUtils.shareInvoicePdfForInvoice(context, newInv, gymName)
            }
        )
    }
}

@Composable
fun InvoiceHistoryCard(
    invoice: GymInvoice,
    onWhatsAppClick: () -> Unit,
    onPdfClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaid = invoice.paymentStatus.equals("Paid", ignoreCase = true)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Invoice ID + Date + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = M3PurpleContainer
                    ) {
                        Text(
                            text = invoice.id,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = M3PurpleOnContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = invoice.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isPaid) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isPaid) "PAID" else "PENDING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPaid) EmeraldPrimary else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Member details & Package
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.memberName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${invoice.packageName} • ${invoice.paymentMode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${invoice.totalAmount.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (invoice.discount > 0) {
                        Text(
                            text = "Disc: ₹${invoice.discount.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By: ${invoice.createdByRole}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onWhatsAppClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp Share",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                    }

                    Button(
                        onClick = onPdfClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Invoice",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View PDF", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceDialog(
    members: List<Member>,
    packages: List<GymMembershipPackage>,
    onDismiss: () -> Unit,
    onCreateInvoice: (Member, GymMembershipPackage, Double, Double, Double, String, String, String) -> Unit
) {
    var selectedMember by remember { mutableStateOf(members.firstOrNull()) }
    var selectedPkg by remember { mutableStateOf(packages.firstOrNull()) }
    var baseAmountText by remember { mutableStateOf(selectedPkg?.price?.toInt()?.toString() ?: "2500") }
    var discountText by remember { mutableStateOf("0") }
    var applyGst by remember { mutableStateOf(true) }
    var paymentStatus by remember { mutableStateOf("Paid") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var notesText by remember { mutableStateOf("") }

    var expandedMemberDropdown by remember { mutableStateOf(false) }
    var expandedPkgDropdown by remember { mutableStateOf(false) }

    val baseAmount = baseAmountText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val taxAmount = if (applyGst) (baseAmount - discount).coerceAtLeast(0.0) * 0.18 else 0.0
    val totalAmount = (baseAmount - discount + taxAmount).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = M3PurplePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Tax Invoice", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Select Member Dropdown
                Text("Select Member", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expandedMemberDropdown,
                    onExpandedChange = { expandedMemberDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedMember?.name ?: "Select Member",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMemberDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMemberDropdown,
                        onDismissRequest = { expandedMemberDropdown = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.name} (${m.phone})") },
                                onClick = {
                                    selectedMember = m
                                    expandedMemberDropdown = false
                                }
                            )
                        }
                    }
                }

                // Select Package
                Text("Select Package", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expandedPkgDropdown,
                    onExpandedChange = { expandedPkgDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedPkg?.packageName ?: "Select Package",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPkgDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPkgDropdown,
                        onDismissRequest = { expandedPkgDropdown = false }
                    ) {
                        packages.forEach { pkg ->
                            DropdownMenuItem(
                                text = { Text("${pkg.packageName} - ₹${pkg.price.toInt()}") },
                                onClick = {
                                    selectedPkg = pkg
                                    baseAmountText = pkg.price.toInt().toString()
                                    expandedPkgDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = baseAmountText,
                        onValueChange = { baseAmountText = it },
                        label = { Text("Base Price (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Discount (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = applyGst, onCheckedChange = { applyGst = it })
                    Text("Apply 18% GST Tax", style = MaterialTheme.typography.bodyMedium)
                }

                // Payment Status & Mode
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Paid", "Pending").forEach { st ->
                                FilterChip(
                                    selected = (paymentStatus == st),
                                    onClick = { paymentStatus = st },
                                    label = { Text(st, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mode", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("UPI", "Cash", "Card").forEach { md ->
                                FilterChip(
                                    selected = (paymentMode == md),
                                    onClick = { paymentMode = md },
                                    label = { Text(md, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Internal Memo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Calculation Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = M3PurpleContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", style = MaterialTheme.typography.bodySmall, color = M3PurpleOnContainer)
                            Text("₹${baseAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = M3PurpleOnContainer)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount:", style = MaterialTheme.typography.bodySmall, color = M3PurpleOnContainer)
                            Text("-₹${discount.toInt()}", style = MaterialTheme.typography.bodySmall, color = M3PurpleOnContainer)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST (18%):", style = MaterialTheme.typography.bodySmall, color = M3PurpleOnContainer)
                            Text("+₹${taxAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = M3PurpleOnContainer)
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = M3PurpleOnContainer.copy(alpha = 0.2f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Final Total:", fontWeight = FontWeight.Bold, color = M3PurpleOnContainer)
                            Text("₹${totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = M3PurpleOnContainer)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = selectedMember ?: members.firstOrNull() ?: return@Button
                    val p = selectedPkg ?: packages.firstOrNull() ?: return@Button
                    onCreateInvoice(m, p, baseAmount, discount, taxAmount, paymentStatus, paymentMode, notesText)
                },
                enabled = selectedMember != null && selectedPkg != null,
                colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Generate & Save Invoice", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
