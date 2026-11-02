package com.example.ui.about

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.WelfareViewModel
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.WelfareBlue
import com.example.ui.theme.WelfareTeal
import com.example.util.ExportUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AboutWelfareScreen(
    viewModel: WelfareViewModel,
    isAdmin: Boolean,
    onOpenChangePin: () -> Unit
) {
    val context = LocalContext.current
    var showChangePinDialog by remember { mutableStateOf(false) }

    // PDF upload launcher for Admin
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = queryFileName(context, uri) ?: "sifa_welfare_rules.pdf"
            val dateStr = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
            viewModel.saveConstitutionPdfInfo(
                title = "Sifa Welfare Constitution & Bylaws",
                filename = fileName,
                date = dateStr,
                uriString = uri.toString()
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Brand Header
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                border = BorderStroke(1.dp, Color(0xFF81D4FA).copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(90.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sifa_logo_circular),
                            contentDescription = "Sifa Welfare Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sifa Welfare",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0077C2)
                    )

                    Text(
                        text = "Star Initiative for Social and Economic Assistance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE0F7FA)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = WelfareTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Registered Welfare Society • Founded 2022",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WelfareTeal
                            )
                        }
                    }
                }
            }
        }

        // 2. Welfare History & Background
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                border = BorderStroke(1.dp, Color(0xFF81D4FA).copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = WelfareBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = WelfareBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "History of Sifa Welfare",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Sifa Welfare (Star Initiative for Social & Economic Assistance) was established with a singular vision: to ensure that no member faces life's unforeseen vulnerabilities alone. Over the years, our community has grown into a strong, self-sustaining family.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 21.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Through an equitable monthly contribution model of Ksh 100 per member, collected from January through December, the fund maintains high liquidity to promptly disburse medical assistance grants, bereavement support, and children's education aid.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 21.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Official Constitution & Rules PDF Placeholder
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                border = BorderStroke(1.5.dp, Color(0xFF4FC3F7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Welfare Constitution & Rules",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Official bylaws, member rights & aid rules in PDF",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PDF Document details card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F8FE),
                        border = BorderStroke(1.dp, Color(0xFFB3E5FC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = viewModel.getConstitutionDocTitle(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0077C2)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "File: ${viewModel.getConstitutionDocFileName()} • Updated: ${viewModel.getConstitutionDocDate()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val uri = ExportUtils.generateConstitutionRulesPdf(context)
                                ExportUtils.shareReport(
                                    context = context,
                                    uri = uri,
                                    mimeType = "application/pdf",
                                    title = "Sifa Welfare Constitution & Bylaws (PDF)"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WelfareBlue),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_constitution_pdf_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download PDF")
                        }

                        if (isAdmin) {
                            OutlinedButton(
                                onClick = {
                                    pdfPickerLauncher.launch("application/pdf")
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_upload_pdf_button")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload PDF")
                            }
                        }
                    }

                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Admin Note: You can tap 'Upload PDF' to replace the constitution document with new executive revisions.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // 4. Admin Security Controls (Secret PIN Change)
        if (isAdmin) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                    border = BorderStroke(1.dp, Color(0xFF81D4FA).copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEDE7F6),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = Color(0xFF5E35B1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Welfare Administrator Security",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Manage your secret administrator credentials",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showChangePinDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("change_admin_pin_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Secret Admin PIN")
                        }
                    }
                }
            }
        }

        // 5. Contact & Secretariat Info
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
                border = BorderStroke(1.dp, Color(0xFF81D4FA).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Secretariat & Payment Coordinates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0077C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• M-Pesa Paybill: 400200", style = MaterialTheme.typography.bodySmall)
                    Text("• Account Number: SIFA-[Your Member Number]", style = MaterialTheme.typography.bodySmall)
                    Text("• Monthly Contribution: Ksh 100", style = MaterialTheme.typography.bodySmall)
                    Text("• Central Secretariat: Nairobi, Kenya", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    // Modal Dialog: Change Admin PIN
    if (showChangePinDialog) {
        ChangeAdminPinDialog(
            viewModel = viewModel,
            onDismiss = { showChangePinDialog = false }
        )
    }
}

@Composable
fun ChangeAdminPinDialog(
    viewModel: WelfareViewModel,
    onDismiss: () -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = WelfareBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Admin Secret PIN")
            }
        },
        text = {
            Column {
                Text(
                    text = "Update your welfare administrator master PIN from default to your preferred secret passcode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = currentPin,
                    onValueChange = {
                        currentPin = it
                        errorMessage = null
                    },
                    label = { Text("Current PIN *") },
                    placeholder = { Text("Default: 9922") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_pin_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        errorMessage = null
                    },
                    label = { Text("New Secret PIN (4-8 digits) *") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_pin_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmNewPin,
                    onValueChange = {
                        confirmNewPin = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New PIN *") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_new_pin_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentPin.isBlank() || newPin.isBlank()) {
                        errorMessage = "Please fill in all PIN fields."
                        return@Button
                    }
                    if (newPin.trim().length < 4) {
                        errorMessage = "New PIN must be at least 4 digits."
                        return@Button
                    }
                    if (newPin != confirmNewPin) {
                        errorMessage = "New PIN and confirmation PIN do not match."
                        return@Button
                    }
                    viewModel.changeAdminPin(
                        currentPin = currentPin,
                        newPin = newPin
                    ) {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WelfareBlue),
                modifier = Modifier.testTag("save_new_pin_button")
            ) {
                Text("Save Secret PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun queryFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }
    return name
}
