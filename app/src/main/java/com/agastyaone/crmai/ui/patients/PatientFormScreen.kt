package com.agastyaone.crmai.ui.patients

import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agastyaone.crmai.core.ServiceLocator
import com.agastyaone.crmai.data.patients.Patient
import com.agastyaone.crmai.data.patients.PatientDemographics
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Modern clinical patient registration & demographics editing.
 * Features an intuitive calendar DatePicker dialog for DOB,
 * one-tap Gender selection (Male / Female / Other),
 * and reliable offline/demo store integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFormScreen(
    clinicId: String,
    uid: String,
    existingPatient: Patient?,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = ServiceLocator.patientRepository
    val isEditing = existingPatient != null

    var name by remember { mutableStateOf(existingPatient?.name ?: "") }
    var dob by remember { mutableStateOf(existingPatient?.dob ?: "") }
    var gender by remember { mutableStateOf(existingPatient?.gender ?: "Male") }
    var phone by remember { mutableStateOf(existingPatient?.phone ?: "") }
    var email by remember { mutableStateOf(existingPatient?.email ?: "") }
    var address by remember { mutableStateOf(existingPatient?.address ?: "") }
    var emergencyContactName by remember { mutableStateOf(existingPatient?.emergencyContactName ?: "") }
    var emergencyContactPhone by remember { mutableStateOf(existingPatient?.emergencyContactPhone ?: "") }
    var bloodGroup by remember { mutableStateOf(existingPatient?.bloodGroup ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // DatePicker Dialog setup
    val calendar = Calendar.getInstance()
    var initYear = calendar.get(Calendar.YEAR) - 25
    var initMonth = calendar.get(Calendar.MONTH)
    var initDay = calendar.get(Calendar.DAY_OF_MONTH)

    if (dob.isNotBlank()) {
        try {
            val parsed = LocalDate.parse(dob, DateTimeFormatter.ISO_LOCAL_DATE)
            initYear = parsed.year
            initMonth = parsed.monthValue - 1
            initDay = parsed.dayOfMonth
        } catch (_: Exception) {}
    }

    val datePickerDialog = remember(context, dob) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
                dob = pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            },
            initYear,
            initMonth,
            initDay,
        ).apply {
            datePicker.maxDate = System.currentTimeMillis() // Do not allow future DOB
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Patient Demographics" else "Register New Patient",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Patient Identification Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        "Primary Demographics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Sumanth Reddy") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    )

                    // Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Phone Number *") },
                        placeholder = { Text("10-digit mobile number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )

                    // Date of Birth (Interactive Calendar Picker)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = dob.ifBlank { "" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date of Birth") },
                            placeholder = { Text("Tap to select date from calendar") },
                            trailingIcon = {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Text("📅", fontSize = 20.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        // Click overlay so clicking anywhere on the text field triggers the calendar
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { datePickerDialog.show() }
                        )
                    }

                    // Gender Selection (Male / Female / Other)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Gender *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            val genderOptions = listOf(
                                Triple("Male", "♂", "Male"),
                                Triple("Female", "♀", "Female"),
                                Triple("Other", "⚧", "Other"),
                            )

                            genderOptions.forEach { (option, icon, label) ->
                                val isSelected = gender.equals(option, ignoreCase = true)

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(8.dp),
                                        )
                                        .clickable { gender = option },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Text(
                                            text = icon,
                                            fontSize = 16.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Blood Group Selection Chips
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Blood Group (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )

                        val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            bloodGroups.take(4).forEach { bg ->
                                val isSelected = bloodGroup.equals(bg, ignoreCase = true)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(6.dp),
                                        )
                                        .clickable { bloodGroup = if (isSelected) "" else bg },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = bg,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            bloodGroups.drop(4).forEach { bg ->
                                val isSelected = bloodGroup.equals(bg, ignoreCase = true)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(6.dp),
                                        )
                                        .clickable { bloodGroup = if (isSelected) "" else bg },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = bg,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contact & Emergency Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        "Contact & Emergency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("patient@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address") },
                        placeholder = { Text("Flat, Street, Area, City") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                    )

                    OutlinedTextField(
                        value = emergencyContactName,
                        onValueChange = { emergencyContactName = it },
                        label = { Text("Emergency Contact Name") },
                        placeholder = { Text("Spouse / Parent / Guardian") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    )

                    OutlinedTextField(
                        value = emergencyContactPhone,
                        onValueChange = { emergencyContactPhone = it },
                        label = { Text("Emergency Contact Phone") },
                        placeholder = { Text("Emergency contact phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            // Save / Submit Button
            val isFormValid = name.isNotBlank() && phone.isNotBlank()

            Button(
                enabled = !isSaving && isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    isSaving = true
                    errorMessage = null
                    val demographics = PatientDemographics(
                        name = name.trim(),
                        dob = dob.ifBlank { null },
                        gender = gender.ifBlank { null },
                        phone = phone.trim(),
                        email = email.ifBlank { null },
                        address = address.ifBlank { null },
                        emergencyContactName = emergencyContactName.ifBlank { null },
                        emergencyContactPhone = emergencyContactPhone.ifBlank { null },
                        bloodGroup = bloodGroup.ifBlank { null },
                    )
                    scope.launch {
                        runCatching {
                            if (isEditing) {
                                repository.updateDemographics(clinicId, existingPatient!!.id, uid, demographics)
                                existingPatient.id
                            } else {
                                repository.createPatient(clinicId, uid, demographics)
                            }
                        }.onSuccess { patientId ->
                            isSaving = false
                            onSaved(patientId)
                        }.onFailure { e ->
                            isSaving = false
                            errorMessage = e.message ?: "Failed to save patient. Please retry."
                        }
                    }
                },
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Saving Patient Record...", fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = if (isEditing) "Save Changes" else "Add Patient to Directory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
