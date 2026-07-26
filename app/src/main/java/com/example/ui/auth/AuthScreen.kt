package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.ui.platform.LocalContext
import com.example.data.auth.AuthAccountManager
import com.example.data.auth.RegisteredAccount
import com.example.data.model.UserRole
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.M3PurpleContainer
import com.example.ui.theme.M3PurpleOnContainer
import com.example.ui.theme.M3PurplePrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onLoginSuccess: (UserRole, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Auth Mode: 0 = Sign In, 1 = Sign Up
    var authFlowMode by remember { mutableStateOf(0) }

    // Account state persistence
    var registeredAccounts by remember {
        mutableStateOf(AuthAccountManager.getRegisteredAccounts(context))
    }
    var lastLoggedInAccount by remember {
        mutableStateOf(AuthAccountManager.getLastLoggedInAccount(context) ?: registeredAccounts.firstOrNull())
    }

    // Sign In Fields
    var signInInput by remember {
        mutableStateOf(
            lastLoggedInAccount?.mobile?.ifBlank { lastLoggedInAccount?.email } ?: "+91 98765 43210"
        )
    }
    var signInPassword by remember { mutableStateOf("••••••••") }

    // Derived detected account based on signInInput
    val detectedAccount = remember(signInInput, registeredAccounts) {
        AuthAccountManager.findAccountForInput(context, signInInput)
    }

    // Sign Up Fields
    var signUpGymName by remember { mutableStateOf("Metro Fitness Club") }
    var signUpOwnerName by remember { mutableStateOf("Alex Vance") }
    var signUpMobile by remember { mutableStateOf("+91 98765 43210") }
    var signUpEmail by remember { mutableStateOf("alex.vance@gymai.pro") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpCrewCount by remember { mutableStateOf("12") }

    var signUpRole by remember { mutableStateOf(UserRole.GYM_OWNER) }

    // Biometric Modal State (ONLY in Sign In flow)
    var showBiometricModal by remember { mutableStateOf(false) }
    var isBiometricScanning by remember { mutableStateOf(false) }
    var biometricSuccessMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    // Fingerprint pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(EmeraldPrimary, IndigoSecondary)
                        )
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fitopsailogo),
                    contentDescription = "FitOps AI Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "FitOps AI",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Gym Operating System & Admin Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // MAIN LOGIN CONTAINER
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // FLOW SWITCHER: SIGN IN vs SIGN UP
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = authFlowMode == 0,
                            onClick = { authFlowMode = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        SegmentedButton(
                            selected = authFlowMode == 1,
                            onClick = { authFlowMode = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Sign Up", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (authFlowMode == 0) {
                        // ================= SIGN IN FLOW =================
                        Text(
                            text = "Account Sign In",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Account Selector / Registered Accounts Chips
                        Text(
                            text = "Quick Select Account:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(registeredAccounts.size) { idx ->
                                val acc = registeredAccounts[idx]
                                val isSelected = (acc.email.equals(detectedAccount.email, ignoreCase = true) ||
                                        (acc.mobile.isNotBlank() && acc.mobile.replace(" ", "") == detectedAccount.mobile.replace(" ", "")))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) M3PurpleContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, M3PurplePrimary) else null,
                                    modifier = Modifier.clickable {
                                        signInInput = if (acc.mobile.isNotBlank()) acc.mobile else acc.email
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (acc.role) {
                                                        UserRole.GYM_OWNER -> M3PurplePrimary
                                                        UserRole.TRAINER -> EmeraldPrimary
                                                        UserRole.RECEPTIONIST -> Color(0xFF3B82F6)
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${acc.name} (${acc.role.displayName})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) M3PurpleOnContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = signInInput,
                            onValueChange = { signInInput = it },
                            label = { Text("Mobile Number or Email") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signin_mobile_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = signInPassword,
                            onValueChange = { signInPassword = it },
                            label = { Text("Password / PIN") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signin_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // REMEMBERED ROLE AUTO-DETECTED BADGER
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = M3PurpleContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, M3PurplePrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Account Role Auto-Detected",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = M3PurpleOnContainer
                                    )
                                    Text(
                                        text = "${detectedAccount.name} • ${detectedAccount.role.displayName} (${detectedAccount.gymName})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val targetAccount = AuthAccountManager.findAccountForInput(context, signInInput)
                                AuthAccountManager.setLastLoggedInAccount(context, targetAccount)
                                onLoginSuccess(
                                    targetAccount.role,
                                    targetAccount.name,
                                    if (targetAccount.email.isNotBlank()) targetAccount.email else signInInput,
                                    targetAccount.gymName
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("signin_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Sign In as ${detectedAccount.role.displayName}", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // BIOMETRIC OPTION (STRICTLY IN SIGN IN FLOW ONLY!)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Or Sign In with Biometric Pass",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(EmeraldPrimary.copy(alpha = 0.3f), Color.Transparent)
                                            )
                                        )
                                        .border(2.dp, EmeraldPrimary, CircleShape)
                                        .clickable {
                                            showBiometricModal = true
                                        }
                                        .padding(12.dp)
                                        .testTag("fingerprint_sensor_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Scan Fingerprint",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Touch Fingerprint / Face ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // ================= SIGN UP FLOW =================
                        Text(
                            text = "Register New Gym Account",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = signUpGymName,
                            onValueChange = { signUpGymName = it },
                            label = { Text("Gym Name") },
                            leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_gym_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpOwnerName,
                            onValueChange = { signUpOwnerName = it },
                            label = { Text("Admin / Owner Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpMobile,
                            onValueChange = { signUpMobile = it },
                            label = { Text("Mobile Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpEmail,
                            onValueChange = { signUpEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpPassword,
                            onValueChange = { signUpPassword = it },
                            label = { Text("Create Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpCrewCount,
                            onValueChange = { signUpCrewCount = it },
                            label = { Text("Number of Crew / Staff") },
                            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("5", "12", "25", "50+").forEach { preset ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (signUpCrewCount == preset) M3PurpleContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { signUpCrewCount = preset }
                                ) {
                                    Text(
                                        text = "$preset Crew",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (signUpCrewCount == preset) M3PurpleOnContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Select Your Designation / Role",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple(UserRole.RECEPTIONIST, "Receptionist", "Add members, manage packages, renewals & invoices"),
                                Triple(UserRole.TRAINER, "Gym Trainer", "Workout logging, AI fitness & member plans"),
                                Triple(UserRole.GYM_OWNER, "Gym Owner", "Full access (Revenue, Analytics, Members & AI)")
                            ).forEach { (role, title, desc) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (signUpRole == role) M3PurpleContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (signUpRole == role) androidx.compose.foundation.BorderStroke(1.5.dp, M3PurplePrimary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { signUpRole = role }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.compose.material3.RadioButton(
                                            selected = (signUpRole == role),
                                            onClick = { signUpRole = role },
                                            colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = M3PurplePrimary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (signUpRole == role) M3PurpleOnContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (signUpRole == role) M3PurpleOnContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val name = if (signUpOwnerName.isNotBlank()) signUpOwnerName else "Gym Admin"
                                val email = if (signUpEmail.isNotBlank()) signUpEmail else "admin@fitops.ai"
                                val gym = if (signUpGymName.isNotBlank()) signUpGymName else "Metro Fitness Club"

                                val newAccount = RegisteredAccount(
                                    email = email,
                                    mobile = signUpMobile,
                                    name = name,
                                    role = signUpRole,
                                    gymName = gym
                                )

                                AuthAccountManager.registerAccount(context, newAccount)
                                registeredAccounts = AuthAccountManager.getRegisteredAccounts(context)

                                onLoginSuccess(signUpRole, name, email, gym)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("signup_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary)
                        ) {
                            Text("Create Account & Sign Up", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // BIOMETRIC PROMPT MODAL DIALOG (STRICTLY FOR SIGN IN FLOW)
    if (showBiometricModal) {
        AlertDialog(
            onDismissRequest = {
                if (!isBiometricScanning) showBiometricModal = false
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Prompt",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Biometric Sign In",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Touch the fingerprint sensor or scan Face ID to log in.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    if (biometricSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(biometricSuccessMessage!!, color = EmeraldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBiometricModal = false
                        val targetAccount = AuthAccountManager.findAccountForInput(context, signInInput)
                        AuthAccountManager.setLastLoggedInAccount(context, targetAccount)
                        onLoginSuccess(
                            targetAccount.role,
                            targetAccount.name,
                            if (targetAccount.email.isNotBlank()) targetAccount.email else signInInput,
                            targetAccount.gymName
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Scan & Sign In", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBiometricModal = false },
                    enabled = !isBiometricScanning
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
