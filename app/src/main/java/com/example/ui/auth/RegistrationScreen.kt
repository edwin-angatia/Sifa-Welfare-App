package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dependant
import com.example.data.model.NextOfKin
import com.example.data.repository.WelfareRepository
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.WelfareBlue
import com.example.ui.theme.WelfareTeal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    repository: WelfareRepository,
    onRegistrationSuccess: () -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var step by remember { mutableIntStateOf(1) } // 1: Personal Credentials, 2: Dependants & Next of Kin
    var isLoading by remember { mutableStateOf(false) }

    // Step 1 Fields
    var idNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var fullNames by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Field Validation States
    var idError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Step 2 Fields
    var dependants by remember { mutableStateOf<List<Dependant>>(emptyList()) }
    var showAddDependantDialog by remember { mutableStateOf(false) }

    var kinName by remember { mutableStateOf("") }
    var kinRelationship by remember { mutableStateOf("Spouse") }
    var kinPhone by remember { mutableStateOf("") }
    var kinAddress by remember { mutableStateOf("") }

    var category by remember { mutableStateOf("Low Income Family") }
    var residentialAddress by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Low Income Family",
        "Senior Citizen",
        "Disability Assistance",
        "Healthcare Relief",
        "Education Aid",
        "General Member"
    )

    fun validateStep1(): Boolean {
        var valid = true
        if (idNumber.trim().length < 4) {
            idError = "Please enter a valid National ID Number"
            valid = false
        } else {
            idError = null
        }

        if (phoneNumber.trim().length < 9) {
            phoneError = "Please enter a valid Phone Number"
            valid = false
        } else {
            phoneError = null
        }

        if (fullNames.trim().length < 3) {
            nameError = "Please enter your full legal names"
            valid = false
        } else {
            nameError = null
        }

        if (password.length < 4) {
            passwordError = "Password must be at least 4 characters"
            valid = false
        } else if (password != confirmPassword) {
            passwordError = "Passwords do not match"
            valid = false
        } else {
            passwordError = null
        }

        return valid
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            WelfareBlue,
                            Color(0xFF0A2E6B),
                            Color(0xFF061A3B)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (step == 2) step = 1 else onNavigateBackToLogin()
                        },
                        modifier = Modifier.testTag("reg_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Member Registration",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (step == 1) "Personal ID & Number Sign Up" else "Dependants & Next of Kin Record",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step Indicator Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step 1 Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (step == 1) Color.White else StatusApproved,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            if (step > 1) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Personal Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Text("1. Personal Details", color = WelfareBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .background(Color.White.copy(alpha = 0.4f))
                    )

                    // Step 2 Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (step == 2) Color.White else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "2. Family & Kin",
                                color = if (step == 2) WelfareBlue else Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Card Container with Animated Content Transition
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "RegistrationStepTransition"
                ) { currentStep ->
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("registration_card_step_$currentStep")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            if (currentStep == 1) {
                                // STEP 1: ID number, Phone number, Full names, Password
                                Text(
                                    text = "Member Credentials",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Enter your ID number and phone number for secure authentication without requiring an email.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // National ID Number Input
                                OutlinedTextField(
                                    value = idNumber,
                                    onValueChange = {
                                        idNumber = it
                                        if (idError != null) idError = null
                                    },
                                    label = { Text("National ID Number *") },
                                    placeholder = { Text("e.g. 28475931") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Badge, contentDescription = null, tint = WelfareBlue)
                                    },
                                    isError = idError != null,
                                    supportingText = idError?.let { { Text(it) } },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_id_number_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Phone Number Input
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = {
                                        phoneNumber = it
                                        if (phoneError != null) phoneError = null
                                    },
                                    label = { Text("Phone Number *") },
                                    placeholder = { Text("e.g. 0712345678") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = WelfareBlue)
                                    },
                                    isError = phoneError != null,
                                    supportingText = phoneError?.let { { Text(it) } },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_phone_number_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Full Names Input
                                OutlinedTextField(
                                    value = fullNames,
                                    onValueChange = {
                                        fullNames = it
                                        if (nameError != null) nameError = null
                                    },
                                    label = { Text("Full Legal Names *") },
                                    placeholder = { Text("e.g. Samuel Kiprono Koech") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = WelfareBlue)
                                    },
                                    isError = nameError != null,
                                    supportingText = nameError?.let { { Text(it) } },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_full_names_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Login Password
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        if (passwordError != null) passwordError = null
                                    },
                                    label = { Text("Set Login Password *") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = WelfareBlue)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_password_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Confirm Password
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = {
                                        confirmPassword = it
                                        if (passwordError != null) passwordError = null
                                    },
                                    label = { Text("Confirm Password *") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = WelfareBlue)
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    isError = passwordError != null,
                                    supportingText = passwordError?.let { { Text(it) } },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_confirm_password_input")
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        if (validateStep1()) {
                                            step = 2
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WelfareBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("continue_to_step_2_button")
                                ) {
                                    Text("Continue: Dependants & Next of Kin", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                TextButton(
                                    onClick = onNavigateBackToLogin,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("nav_to_login_button")
                                ) {
                                    Text("Already a member? Sign in with ID/Phone", color = MaterialTheme.colorScheme.outline)
                                }
                            } else {
                                // STEP 2: Dependants & Next of Kin
                                Text(
                                    text = "Dependants & Next of Kin",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Record family members relying on your welfare profile and your emergency next of kin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Dependants Section
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = WelfareTeal, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Dependants (${dependants.size})",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                            }

                                            Button(
                                                onClick = { showAddDependantDialog = true },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = WelfareTeal),
                                                modifier = Modifier.testTag("add_dependant_open_dialog_button")
                                            ) {
                                                Text("+ Add Dependant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        if (dependants.isEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "No dependants added yet. Tap '+ Add Dependant' to include children, elders, or spouse.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            dependants.forEachIndexed { index, dep ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(dep.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                            Text(
                                                                text = "${dep.relationship} • Age: ${dep.age}" + if (dep.specialNeeds.isNotBlank()) " • Needs: ${dep.specialNeeds}" else "",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.outline
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                val updated = dependants.toMutableList()
                                                                updated.removeAt(index)
                                                                dependants = updated
                                                            }
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Delete dependant", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Next of Kin Section
                                Text(
                                    text = "Next of Kin / Contact",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = kinName,
                                    onValueChange = { kinName = it },
                                    label = { Text("Next of Kin Full Name *") },
                                    placeholder = { Text("e.g. Grace Wanjiku") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("kin_fullname_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = kinRelationship,
                                        onValueChange = { kinRelationship = it },
                                        label = { Text("Relationship *") },
                                        placeholder = { Text("e.g. Spouse") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = kinPhone,
                                        onValueChange = { kinPhone = it },
                                        label = { Text("Kin Phone *") },
                                        placeholder = { Text("e.g. 0722000111") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("kin_phone_number_input")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = kinAddress,
                                    onValueChange = { kinAddress = it },
                                    label = { Text("Next of Kin Address") },
                                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Welfare Category
                                ExposedDropdownMenuBox(
                                    expanded = categoryExpanded,
                                    onExpandedChange = { categoryExpanded = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = category,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Welfare Relief Category") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = categoryExpanded,
                                        onDismissRequest = { categoryExpanded = false }
                                    ) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    category = cat
                                                    categoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = residentialAddress,
                                    onValueChange = { residentialAddress = it },
                                    label = { Text("Your Physical / Village Address") },
                                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        if (kinName.isBlank() || kinPhone.isBlank()) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Please provide Next of Kin name and phone number.")
                                            }
                                            return@Button
                                        }

                                        isLoading = true
                                        val nextOfKin = NextOfKin(
                                            name = kinName.trim(),
                                            relationship = kinRelationship.trim(),
                                            phone = kinPhone.trim(),
                                            address = kinAddress.trim()
                                        )

                                        val result = repository.registerMemberWithDetails(
                                            idNumber = idNumber.trim(),
                                            phone = phoneNumber.trim(),
                                            fullName = fullNames.trim(),
                                            password = password,
                                            dependants = dependants,
                                            nextOfKin = nextOfKin,
                                            category = category,
                                            address = residentialAddress.trim()
                                        )
                                        isLoading = false

                                        if (result.isSuccess) {
                                            onRegistrationSuccess()
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Registration error")
                                            }
                                        }
                                    },
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WelfareBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("submit_registration_button")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Complete Registration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = { step = 1 },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Back to Personal Details")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Encrypted Community Records Database",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }

    // Modal: Add Dependant Form Dialog
    if (showAddDependantDialog) {
        var depName by remember { mutableStateOf("") }
        var depRelation by remember { mutableStateOf("Child") }
        var depAge by remember { mutableStateOf("") }
        var depSpecialNeeds by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDependantDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = WelfareTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Family Dependant")
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = depName,
                        onValueChange = { depName = it },
                        label = { Text("Dependant Full Name *") },
                        placeholder = { Text("e.g. Kelvin Koech") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_dep_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = depRelation,
                            onValueChange = { depRelation = it },
                            label = { Text("Relationship") },
                            placeholder = { Text("Child, Parent, etc.") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = depAge,
                            onValueChange = { depAge = it },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("dialog_dep_age_input")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = depSpecialNeeds,
                        onValueChange = { depSpecialNeeds = it },
                        label = { Text("Special Needs or Notes (Optional)") },
                        placeholder = { Text("e.g. Asthma, School Aid") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (depName.isNotBlank()) {
                            val newDep = Dependant(
                                name = depName.trim(),
                                relationship = depRelation.trim(),
                                age = depAge.toIntOrNull() ?: 0,
                                specialNeeds = depSpecialNeeds.trim()
                            )
                            dependants = dependants + newDep
                            showAddDependantDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WelfareTeal),
                    modifier = Modifier.testTag("dialog_add_dependant_confirm_button")
                ) {
                    Text("Add Dependant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDependantDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
