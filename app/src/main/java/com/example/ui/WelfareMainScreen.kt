package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ContributionHelper
import com.example.data.model.Member
import com.example.data.model.MonthContribution
import com.example.data.model.WelfareCase
import com.example.ui.cases.CaseDetailSheet
import com.example.ui.cases.NewCaseDialog
import com.example.ui.member.MemberRegistrationDialog
import com.example.ui.reports.ExportReportsSheet
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusClosed
import com.example.ui.theme.StatusDisbursed
import com.example.ui.theme.StatusRejected
import com.example.ui.theme.StatusSubmitted
import com.example.ui.theme.StatusUnderReview
import com.example.ui.theme.WelfareBlue
import com.example.ui.theme.WelfareTeal
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelfareMainScreen(
    viewModel: WelfareViewModel
) {
    val context = LocalContext.current
    val currentMember by viewModel.currentMember.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val userCases by viewModel.userCases.collectAsState()
    val allCases by viewModel.allCases.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Cases, 1: Members (Admin only), 2: Reports, 3: About
    var showMemberDialog by remember { mutableStateOf(false) }
    var selectedMemberForEdit by remember { mutableStateOf<Member?>(null) }
    var showNewCaseDialog by remember { mutableStateOf(false) }
    var selectedCaseForDetail by remember { mutableStateOf<WelfareCase?>(null) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showAdminRestrictedDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    // First time prompt: If user has no member record registered, suggest prompt
    var promptRegistrationDismissed by remember { mutableStateOf(false) }

    val displayedCases = if (viewModel.isAdmin) allCases else userCases

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sifa_logo_circular),
                                contentDescription = "Sifa Welfare Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sifa Welfare",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        if (viewModel.isAdmin) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = WelfareBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    color = WelfareBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportSheet = true },
                        modifier = Modifier.testTag("open_export_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export Official PDF",
                            tint = WelfareBlue
                        )
                    }
                    if (viewModel.isAdmin) {
                        IconButton(
                            onClick = { showChangePinDialog = true },
                            modifier = Modifier.testTag("admin_pin_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin PIN Settings",
                                tint = WelfareTeal
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White.copy(alpha = 0.95f),
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.TrackChanges, contentDescription = "Cases") },
                    label = { Text("Case History") },
                    modifier = Modifier.testTag("tab_cases")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        if (viewModel.isAdmin) {
                            selectedTab = 1
                        } else {
                            showAdminRestrictedDialog = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (viewModel.isAdmin) Icons.Default.Group else Icons.Default.Lock,
                            contentDescription = "Members"
                        )
                    },
                    label = { Text(if (viewModel.isAdmin) "Members" else "Members 🔒") },
                    modifier = Modifier.testTag("tab_members")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Reports") },
                    label = { Text("Reports") },
                    modifier = Modifier.testTag("tab_analytics")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "About & Rules") },
                    label = { Text("About & Rules") },
                    modifier = Modifier.testTag("tab_about")
                )
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    FloatingActionButton(
                        onClick = {
                            if (currentMember == null && !viewModel.isAdmin) {
                                showMemberDialog = true
                            } else {
                                showNewCaseDialog = true
                            }
                        },
                        containerColor = WelfareBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_new_case")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Case")
                    }
                }
                1 -> {
                    if (viewModel.isAdmin) {
                        FloatingActionButton(
                            onClick = {
                                selectedMemberForEdit = null
                                showMemberDialog = true
                            },
                            containerColor = WelfareTeal,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("fab_register_member")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Register Member")
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE1F5FE), // Soft Sky Blue
                            Color(0xFFF0F8FE), // Frosted Sky
                            Color(0xFFFFFFFF)  // Base
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Unregistered Member Notice Banner
                if (currentMember == null && !promptRegistrationDismissed && !viewModel.isAdmin) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)),
                        border = BorderStroke(1.dp, Color(0xFF81D4FA).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Complete Member Registration",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Register your profile and welfare category to submit aid cases.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WelfareBlue,
                                modifier = Modifier.clickable {
                                    selectedMemberForEdit = null
                                    showMemberDialog = true
                                }
                            ) {
                                Text(
                                    text = "Register",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Tab Content
                when (selectedTab) {
                    0 -> CasesScreen(
                        currentMember = currentMember,
                        cases = displayedCases,
                        membersMap = allMembers.associateBy { it.id },
                        isAdmin = viewModel.isAdmin,
                        onCaseClick = { selectedCaseForDetail = it },
                        onNewCaseClick = { showNewCaseDialog = true },
                        onRecordContribution = { month ->
                            viewModel.recordMonthlyContribution(month)
                        }
                    )
                    1 -> {
                        if (viewModel.isAdmin) {
                            MembersScreen(
                                currentMember = currentMember,
                                allMembers = allMembers,
                                isAdmin = viewModel.isAdmin,
                                onEditProfile = {
                                    selectedMemberForEdit = it
                                    showMemberDialog = true
                                },
                                onRegisterClick = {
                                    selectedMemberForEdit = null
                                    showMemberDialog = true
                                },
                                onRecordMemberContribution = { memberId, month ->
                                    viewModel.recordMemberContribution(memberId, month)
                                }
                            )
                        }
                    }
                    2 -> ReportsAnalyticsScreen(
                        members = if (viewModel.isAdmin) allMembers else listOfNotNull(currentMember),
                        cases = displayedCases,
                        isAdmin = viewModel.isAdmin,
                        onDownloadPdf = { viewModel.exportOfficialPdf(context) }
                    )
                    3 -> com.example.ui.about.AboutWelfareScreen(
                        viewModel = viewModel,
                        isAdmin = viewModel.isAdmin,
                        onOpenChangePin = { showChangePinDialog = true }
                    )
                }
            }
        }
    }

    // Modal Sheets
    if (showMemberDialog) {
        MemberRegistrationDialog(
            existingMember = selectedMemberForEdit ?: currentMember,
            userEmail = viewModel.currentUserIdentifier,
            onDismiss = { showMemberDialog = false },
            onSave = { updatedMember ->
                viewModel.saveMemberProfile(updatedMember) {
                    showMemberDialog = false
                    promptRegistrationDismissed = true
                }
            }
        )
    }

    if (showNewCaseDialog) {
        val memberId = currentMember?.id ?: viewModel.currentUserId
        NewCaseDialog(
            memberId = memberId,
            userId = viewModel.currentUserId,
            onDismiss = { showNewCaseDialog = false },
            onSubmit = { newCase ->
                viewModel.submitNewCase(newCase) {
                    showNewCaseDialog = false
                }
            }
        )
    }

    selectedCaseForDetail?.let { caseItem ->
        CaseDetailSheet(
            welfareCase = caseItem,
            member = allMembers.find { it.id == caseItem.memberId } ?: currentMember,
            isAdmin = viewModel.isAdmin,
            onDismiss = { selectedCaseForDetail = null },
            onUpdateCase = { status, approvedAmount, notes, disbursementDate ->
                viewModel.updateCase(
                    caseId = caseItem.id,
                    status = status,
                    approvedAmount = approvedAmount,
                    officerNotes = notes,
                    disbursementDate = disbursementDate
                ) {
                    selectedCaseForDetail = null
                }
            }
        )
    }

    if (showExportSheet) {
        ExportReportsSheet(
            isAdmin = viewModel.isAdmin,
            onDismiss = { showExportSheet = false },
            onExportPdf = {
                showExportSheet = false
                viewModel.exportOfficialPdf(context)
            }
        )
    }

    if (showAdminRestrictedDialog) {
        AdminAccessRestrictedDialog(
            viewModel = viewModel,
            onDismiss = { showAdminRestrictedDialog = false },
            onUnlockAdmin = {
                showAdminRestrictedDialog = false
                selectedTab = 1
            }
        )
    }

    if (showChangePinDialog) {
        com.example.ui.about.ChangeAdminPinDialog(
            viewModel = viewModel,
            onDismiss = { showChangePinDialog = false }
        )
    }
}

@Composable
fun CasesScreen(
    currentMember: Member?,
    cases: List<WelfareCase>,
    membersMap: Map<String, Member>,
    isAdmin: Boolean,
    onCaseClick: (WelfareCase) -> Unit,
    onNewCaseClick: () -> Unit,
    onRecordContribution: (Int) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Submitted", "Under Review", "Approved", "Disbursed", "Closed", "Rejected")

    var showMpesaDialogForMonth by remember { mutableStateOf<Int?>(null) }
    var showAdminConfirmDialogForMonth by remember { mutableStateOf<Int?>(null) }

    val filteredCases = if (selectedFilter == "All") {
        cases
    } else {
        cases.filter { it.status.equals(selectedFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Dynamic Salutation Hero Card (Auto First Name + Time Emoji)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.90f)
                ),
                border = BorderStroke(1.dp, Color(0xFF81D4FA).copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ContributionHelper.getSalutation(currentMember?.fullName),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0077C2)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Sifa Welfare • ${currentMember?.memberNumber ?: "SIFA-001"} • Star Initiative",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sifa_logo_circular),
                            contentDescription = "Sifa Welfare Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }

        // 2. Monthly Contributions Tracker (Jan to Dec, Ksh 100/mo)
        item {
            val paidMonthsList = currentMember?.paidMonths ?: (1..11).toList()
            val monthItems = ContributionHelper.getYearMonths(paidMonthsList)
            val paidCount = monthItems.count { it.isPaid }
            val totalPaidAmount = paidCount * (currentMember?.contributionRateKsh ?: 100)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = WelfareBlue.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = WelfareBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Monthly Contributions 2026",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ksh 100 / month • Jan to Dec",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$paidCount of 12 Months Paid",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = WelfareBlue
                        )
                        Text(
                            text = "Ksh $totalPaidAmount of Ksh 1,200",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { paidCount / 12f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = WelfareBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 12 Months Grid (Row 1: Jan - Jun, Row 2: Jul - Dec)
                    val firstHalf = monthItems.take(6)
                    val secondHalf = monthItems.drop(6)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        firstHalf.forEach { item ->
                            MonthContributionChip(
                                item = item,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (isAdmin) {
                                        showAdminConfirmDialogForMonth = item.monthIndex
                                    } else {
                                        showMpesaDialogForMonth = item.monthIndex
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        secondHalf.forEach { item ->
                            MonthContributionChip(
                                item = item,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (isAdmin) {
                                        showAdminConfirmDialogForMonth = item.monthIndex
                                    } else {
                                        showMpesaDialogForMonth = item.monthIndex
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Welfare Case Histories Header & Filter Chips
        item {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WELFARE CASE HISTORIES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WelfareBlue
                    )
                    Text(
                        text = "${filteredCases.size} total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { f ->
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(f) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WelfareBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // 4. Cases List or Empty State
        if (filteredCases.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TrackChanges,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (selectedFilter == "All") "No Case Histories Logged" else "No $selectedFilter Cases",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Submit a case application to request welfare relief or review past support claims.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredCases, key = { it.id.ifBlank { it.caseNumber } }) { caseItem ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    CaseItemCard(
                        welfareCase = caseItem,
                        member = membersMap[caseItem.memberId],
                        onClick = { onCaseClick(caseItem) }
                    )
                }
            }
        }
    }

    if (showMpesaDialogForMonth != null) {
        val monthIdx = showMpesaDialogForMonth!!
        MpesaPaymentDialog(
            monthIndex = monthIdx,
            monthName = ContributionHelper.getMonthName(monthIdx),
            member = currentMember,
            onDismiss = { showMpesaDialogForMonth = null },
            onConfirmMpesaPayment = {
                onRecordContribution(monthIdx)
                showMpesaDialogForMonth = null
            }
        )
    }

    if (showAdminConfirmDialogForMonth != null) {
        val monthIdx = showAdminConfirmDialogForMonth!!
        AdminConfirmMonthPaymentDialog(
            monthIndex = monthIdx,
            monthName = ContributionHelper.getMonthName(monthIdx),
            member = currentMember,
            onDismiss = { showAdminConfirmDialogForMonth = null },
            onConfirmPayment = {
                onRecordContribution(monthIdx)
                showAdminConfirmDialogForMonth = null
            }
        )
    }
}

@Composable
fun MonthContributionChip(
    item: MonthContribution,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        item.isPaid -> Color(0xFFE0F2FE)
        item.isCurrentMonth -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = when {
        item.isPaid -> Color(0xFF0284C7)
        item.isCurrentMonth -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.monthShort,
                fontSize = 11.sp,
                fontWeight = if (item.isCurrentMonth) FontWeight.ExtraBold else FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (item.isPaid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Paid",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Ksh100",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0284C7)
                )
            } else {
                Text(
                    text = "Pay",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = WelfareBlue
                )
            }
        }
    }
}

@Composable
fun CaseItemCard(
    welfareCase: WelfareCase,
    member: Member?,
    onClick: () -> Unit
) {
    val statusColor = when (welfareCase.status) {
        "Approved" -> StatusApproved
        "Disbursed" -> StatusDisbursed
        "Under Review" -> StatusUnderReview
        "Rejected" -> StatusRejected
        "Closed" -> StatusClosed
        else -> StatusSubmitted
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("case_item_${welfareCase.caseNumber}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = welfareCase.caseNumber,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${welfareCase.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = WelfareTeal
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = welfareCase.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = welfareCase.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (welfareCase.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = welfareCase.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = member?.fullName ?: "Member ${welfareCase.userId.take(6)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (welfareCase.approvedAmount > 0) {
                            "Approved: Ksh ${String.format(Locale.US, "%,.0f", welfareCase.approvedAmount)}"
                        } else {
                            "Requested: Ksh ${String.format(Locale.US, "%,.0f", welfareCase.requestedAmount)}"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (welfareCase.approvedAmount > 0) StatusApproved else WelfareBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MembersScreen(
    currentMember: Member?,
    allMembers: List<Member>,
    isAdmin: Boolean,
    onEditProfile: (Member) -> Unit,
    onRegisterClick: () -> Unit,
    onRecordMemberContribution: (String, Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMemberForPayment by remember { mutableStateOf<Member?>(null) }

    val membersToShow = if (isAdmin) {
        if (searchQuery.isBlank()) allMembers
        else allMembers.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.memberNumber.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    } else {
        listOfNotNull(currentMember)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isAdmin) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search members by name, ID, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentMember != null && !isAdmin) {
                item {
                    Text(
                        text = "YOUR REGISTERED RECORD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WelfareBlue
                    )
                }
            }

            if (membersToShow.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Member Records Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Register Member' to create a new welfare profile.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(membersToShow, key = { it.id.ifBlank { it.memberNumber } }) { member ->
                    MemberItemCard(
                        member = member,
                        isAdmin = isAdmin,
                        onEdit = { onEditProfile(member) },
                        onRecordPayment = {
                            selectedMemberForPayment = member
                        }
                    )
                }
            }
        }
    }

    // Admin dialog to record member contribution
    selectedMemberForPayment?.let { member ->
        val paidMonths = member.paidMonths
        val nextUnpaidMonth = (1..12).firstOrNull { !paidMonths.contains(it) } ?: 12
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedMemberForPayment = null },
            title = {
                Text("Record Monthly Contribution")
            },
            text = {
                Column {
                    Text("Member: ${member.fullName} (${member.memberNumber})")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Rate: Ksh ${member.contributionRateKsh} / month",
                        fontWeight = FontWeight.Bold,
                        color = WelfareBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Status: ${member.getActiveUntilText()}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select month to record as paid (Ksh 100):")
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items((1..12).toList()) { m ->
                            val isPaid = paidMonths.contains(m)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPaid) Color(0xFFE8F5E9) else if (m == nextUnpaidMonth) WelfareBlue else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable(enabled = !isPaid) {
                                    onRecordMemberContribution(member.id, m)
                                    selectedMemberForPayment = null
                                }
                            ) {
                                Text(
                                    text = ContributionHelper.MONTH_SHORTS[m - 1] + if (isPaid) " ✓" else "",
                                    color = if (isPaid) Color(0xFF2E7D32) else if (m == nextUnpaidMonth) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val firstUnpaid = (1..12).firstOrNull { !paidMonths.contains(it) }
                        if (firstUnpaid != null) {
                            onRecordMemberContribution(member.id, firstUnpaid)
                        }
                        selectedMemberForPayment = null
                    }
                ) {
                    Text("Pay Next Unpaid Month")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { selectedMemberForPayment = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun MemberItemCard(
    member: Member,
    isAdmin: Boolean = false,
    onEdit: () -> Unit,
    onRecordPayment: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = WelfareBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = member.fullName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = WelfareBlue,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = member.memberNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Member Record",
                        tint = WelfareBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Monthly Contributions Status Row
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE1F5FE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = WelfareBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${member.paidMonths.size}/12 Months (Ksh ${member.paidMonths.size * member.contributionRateKsh})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WelfareBlue
                            )
                        }
                        Text(
                            text = member.getActiveUntilText(),
                            fontSize = 11.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WelfareBlue,
                            modifier = Modifier.clickable(onClick = onRecordPayment)
                        ) {
                            Text(
                                text = "+ Record",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WelfareTeal.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = member.category,
                        color = WelfareTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusApproved.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = member.status,
                        color = StatusApproved,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (member.phone.isNotBlank()) {
                    Text(
                        text = "Phone: ${member.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Household: ${member.householdSize} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (member.idNumber.isNotBlank()) {
                Text(
                    text = "National ID: ${member.idNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            if (member.address.isNotBlank()) {
                Text(
                    text = "Address: ${member.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            if (member.dependants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Dependants (${member.dependants.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WelfareTeal
                        )
                        member.dependants.forEach { dep ->
                            Text(
                                text = "• ${dep.name} (${dep.relationship}, age ${dep.age})" +
                                        if (dep.specialNeeds.isNotBlank()) " - ${dep.specialNeeds}" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            member.nextOfKin?.let { kin ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Next of Kin: ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${kin.name} (${kin.relationship}) • ${kin.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReportsAnalyticsScreen(
    members: List<Member>,
    cases: List<WelfareCase>,
    isAdmin: Boolean,
    onDownloadPdf: () -> Unit
) {
    val totalApproved = cases.sumOf { it.approvedAmount }
    val totalRequested = cases.sumOf { it.requestedAmount }
    val approvedCount = cases.count { it.status == "Approved" || it.status == "Disbursed" }
    val totalContributionsCollected = members.sumOf { it.paidMonths.size * it.contributionRateKsh }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "COMMUNITY WELFARE ANALYTICS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = WelfareBlue
            )
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Members",
                    value = "${members.size}",
                    subtitle = "Registered records",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Contributions Pool",
                    value = "Ksh $totalContributionsCollected",
                    subtitle = "Jan-Dec Ksh100/mo",
                    valueColor = WelfareBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Active Cases",
                    value = "${cases.size}",
                    subtitle = "$approvedCount approved/disbursed",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Disbursed Aid",
                    value = "Ksh ${String.format(Locale.US, "%,.0f", totalApproved)}",
                    subtitle = "Allocated assistance",
                    valueColor = StatusApproved,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category distribution
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cases By Support Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val categories = cases.groupBy { it.category }
                    if (categories.isEmpty()) {
                        Text(
                            text = "No cases recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        categories.forEach { (cat, list) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${list.size} cases",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WelfareBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export Download Actions (Certified PDF Document only)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                border = BorderStroke(1.dp, Color(0xFF81D4FA).copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = WelfareBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = WelfareBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Official Welfare PDF Report",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Certified membership & financial statement",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Generate and download the official Sifa Welfare statement in PDF format, complete with member details, annual monthly contributions, and case histories.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDownloadPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = WelfareBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_pdf_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download Official PDF Report",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color = WelfareBlue,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = valueColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun AdminAccessRestrictedDialog(
    viewModel: WelfareViewModel,
    onDismiss: () -> Unit,
    onUnlockAdmin: () -> Unit
) {
    var adminPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = WelfareBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Restricted",
                        tint = WelfareBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Administrator Access Only",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "The Members directory is restricted to welfare officials and administrators. Please enter your secret Admin PIN to view and manage community members.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = adminPin,
                    onValueChange = {
                        if (it.length <= 8) {
                            adminPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Secret Admin PIN") },
                    placeholder = { Text("Default: 9922") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let { msg ->
                            Text(msg, color = MaterialTheme.colorScheme.error)
                        } ?: Text("Default PIN is 9922 (can be customized in About & Rules)", fontSize = 11.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (viewModel.verifyAdminPin(adminPin.trim())) {
                        viewModel.setAdminSession(true)
                        onUnlockAdmin()
                    } else {
                        errorMessage = "Incorrect Admin PIN. Try 9922 or your custom PIN."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WelfareBlue),
                modifier = Modifier.testTag("admin_unlock_button")
            ) {
                Text("Unlock Access", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MpesaPaymentDialog(
    monthIndex: Int,
    monthName: String,
    member: Member?,
    onDismiss: () -> Unit,
    onConfirmMpesaPayment: () -> Unit
) {
    val context = LocalContext.current
    var transactionCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }

    val accountNo = "SIFA-${member?.idNumber?.ifBlank { member.memberNumber } ?: "001"}"
    val amountDueKsh = member?.contributionRateKsh ?: 100

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFF0077C2).copy(alpha = 0.12f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "M-Pesa Payment",
                        tint = Color(0xFF0077C2),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Pay Contribution: $monthName 2026",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "Monthly welfare contribution is Ksh $amountDueKsh per member. Use your SIM Toolkit or M-Pesa to complete the contribution:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // M-Pesa Paybill instructions Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                    border = BorderStroke(1.dp, Color(0xFF81D4FA).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Service:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF01579B))
                            Text("M-PESA Paybill", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Business No:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF01579B))
                            Text("400200", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Account No:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF01579B))
                            Text(accountNo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF01579B))
                            Text("Ksh $amountDueKsh", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0077C2), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SIM Toolkit launcher button
                OutlinedButton(
                    onClick = {
                        try {
                            // Try launching SIM Toolkit package
                            val stkIntent = context.packageManager.getLaunchIntentForPackage("com.android.stk")
                            if (stkIntent != null) {
                                context.startActivity(stkIntent)
                            } else {
                                // Fallback to Safaricom M-Pesa USSD dialer *334#
                                val ussdIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*334%23"))
                                context.startActivity(ussdIntent)
                            }
                        } catch (e: Exception) {
                            val ussdIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*334%23"))
                            context.startActivity(ussdIntent)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_sim_toolkit_button")
                ) {
                    Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open SIM Toolkit / Dial *334#", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = transactionCode,
                    onValueChange = {
                        transactionCode = it.uppercase()
                        codeError = null
                    },
                    label = { Text("M-Pesa SMS Confirmation Code") },
                    placeholder = { Text("e.g. SI84PQ92K1") },
                    singleLine = true,
                    isError = codeError != null,
                    supportingText = {
                        codeError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        } ?: Text("Enter code received via SMS to confirm payment", fontSize = 11.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mpesa_code_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (transactionCode.trim().length >= 4) {
                        onConfirmMpesaPayment()
                    } else {
                        codeError = "Please enter the M-Pesa confirmation code"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077C2)),
                modifier = Modifier.testTag("confirm_mpesa_payment_button")
            ) {
                Text("Confirm Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminConfirmMonthPaymentDialog(
    monthIndex: Int,
    monthName: String,
    member: Member?,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    val rate = member?.contributionRateKsh ?: 100
    val memberTitle = member?.fullName ?: "Member"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = WelfareTeal.copy(alpha = 0.12f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Verification",
                        tint = WelfareTeal,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Admin Contribution Verification",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "As Welfare Admin, you can verify and record the monthly contribution for this member upon receiving M-Pesa or bank receipt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Member: $memberTitle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Month: $monthName 2026", fontSize = 12.sp)
                        Text("Amount: Ksh $rate", fontWeight = FontWeight.Bold, color = WelfareBlue, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmPayment,
                colors = ButtonDefaults.buttonColors(containerColor = WelfareTeal),
                modifier = Modifier.testTag("admin_confirm_month_button")
            ) {
                Text("Record Ksh $rate Paid", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
