package com.example.ui.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Dependant
import com.example.data.model.Member
import com.example.data.model.NextOfKin
import com.example.ui.theme.WelfareBlue
import com.example.ui.theme.WelfareTeal
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberRegistrationDialog(
    existingMember: Member?,
    userEmail: String,
    onDismiss: () -> Unit,
    onSave: (Member) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val randomSuffix = remember { 1000 + Random().nextInt(9000) }
    var fullName by remember { mutableStateOf(existingMember?.fullName ?: "") }
    var idNumber by remember { mutableStateOf(existingMember?.idNumber ?: "") }
    var memberNumber by remember { mutableStateOf(existingMember?.memberNumber ?: "WF-2026-$randomSuffix") }
    var phone by remember { mutableStateOf(existingMember?.phone ?: "") }
    var category by remember { mutableStateOf(existingMember?.category ?: "Low Income Family") }
    var monthlyIncome by remember { mutableStateOf(existingMember?.monthlyIncome?.toString() ?: "1200") }
    var address by remember { mutableStateOf(existingMember?.address ?: "") }
    var notes by remember { mutableStateOf(existingMember?.notes ?: "") }

    // Dependants & Kin
    var dependants by remember { mutableStateOf(existingMember?.dependants ?: emptyList()) }
    var showAddDepDialog by remember { mutableStateOf(false) }

    var kinName by remember { mutableStateOf(existingMember?.nextOfKin?.name ?: "") }
    var kinRelationship by remember { mutableStateOf(existingMember?.nextOfKin?.relationship ?: "Spouse") }
    var kinPhone by remember { mutableStateOf(existingMember?.nextOfKin?.phone ?: "") }
    var kinAddress by remember { mutableStateOf(existingMember?.nextOfKin?.address ?: "") }

    val categories = listOf(
        "Low Income Family",
        "Senior Citizen",
        "Disability Assistance",
        "Healthcare Relief",
        "Education Aid",
        "General Member"
    )
    var categoryExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (existingMember == null) "Member Registration" else "Edit Member Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Official Welfare Database Record",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Legal Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("member_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = idNumber,
                    onValueChange = { idNumber = it },
                    label = { Text("National ID Number *") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assistance Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Residential Address") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dependants Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = WelfareTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Registered Dependants (${dependants.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = { showAddDepDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WelfareTeal)
                        ) {
                            Text("+ Add", fontSize = 12.sp)
                        }
                    }

                    if (dependants.isEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No dependants listed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        dependants.forEachIndexed { i, d ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(d.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        Text("${d.relationship} • Age: ${d.age}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    IconButton(onClick = {
                                        val list = dependants.toMutableList()
                                        list.removeAt(i)
                                        dependants = list
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next of Kin Card
            Text("Next of Kin / Emergency Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = kinName,
                onValueChange = { kinName = it },
                label = { Text("Next of Kin Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = kinRelationship,
                    onValueChange = { kinRelationship = it },
                    label = { Text("Relationship") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = kinPhone,
                    onValueChange = { kinPhone = it },
                    label = { Text("Kin Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val incomeDbl = monthlyIncome.toDoubleOrNull() ?: 0.0
                    val nextOfKin = if (kinName.isNotBlank()) {
                        NextOfKin(name = kinName.trim(), relationship = kinRelationship.trim(), phone = kinPhone.trim(), address = kinAddress.trim())
                    } else existingMember?.nextOfKin

                    val member = (existingMember ?: Member()).copy(
                        fullName = fullName.trim(),
                        idNumber = idNumber.trim(),
                        memberNumber = memberNumber.trim(),
                        phone = phone.trim(),
                        category = category,
                        householdSize = (dependants.size + 1).toLong(),
                        monthlyIncome = incomeDbl,
                        address = address.trim(),
                        notes = notes.trim(),
                        dependants = dependants,
                        nextOfKin = nextOfKin,
                        status = existingMember?.status ?: "Active"
                    )
                    onSave(member)
                },
                enabled = fullName.isNotBlank() && phone.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WelfareBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_member_button")
            ) {
                Text(text = if (existingMember == null) "Complete Registration" else "Save Changes", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showAddDepDialog) {
        var depName by remember { mutableStateOf("") }
        var depRel by remember { mutableStateOf("Child") }
        var depAge by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDepDialog = false },
            title = { Text("Add Dependant") },
            text = {
                Column {
                    OutlinedTextField(
                        value = depName,
                        onValueChange = { depName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = depRel,
                        onValueChange = { depRel = it },
                        label = { Text("Relationship (Child, Spouse, etc.)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = depAge,
                        onValueChange = { depAge = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (depName.isNotBlank()) {
                        dependants = dependants + Dependant(name = depName.trim(), relationship = depRel.trim(), age = depAge.toIntOrNull() ?: 0)
                        showAddDepDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDepDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
