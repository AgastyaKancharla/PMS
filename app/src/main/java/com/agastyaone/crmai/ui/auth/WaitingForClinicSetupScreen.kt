package com.agastyaone.crmai.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agastyaone.crmai.core.ServiceLocator
import kotlinx.coroutines.launch

/**
 * Shown when an authenticated user has no resolved clinic claim yet.
 * Provides clear recovery pathways: activate invite, setup clinic, or explore demo.
 */
@Composable
fun WaitingForClinicSetupScreen(onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    val functionsRepository = ServiceLocator.cloudFunctionsRepository
    val authRepository = ServiceLocator.authRepository

    var isBusy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showClinicDialog by remember { mutableStateOf(false) }

    var clinicName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⏳", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Clinic Link Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Your account is signed in, but is not attached to an active dental clinic yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            isBusy = true
                            errorMessage = null
                            scope.launch {
                                runCatching {
                                    functionsRepository.acceptInvite()
                                    authRepository.refreshSession()
                                }.onFailure {
                                    errorMessage = it.message ?: "No pending invite found matching your account credentials."
                                }
                                isBusy = false
                            }
                        },
                    ) {
                        Text(if (isBusy) "Checking invites..." else "Activate My Staff Invite")
                    }

                    OutlinedButton(
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showClinicDialog = true },
                    ) {
                        Text("Set Up Practice as Doctor / Owner")
                    }

                    FilledTonalButton(
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { authRepository.setDemoSession() },
                    ) {
                        Text("Explore Live Demo Clinic (Instant)")
                    }
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onSignOut) {
                Text(
                    "Sign out of this account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showClinicDialog) {
        val formValid = clinicName.isNotBlank() && address.isNotBlank() && city.isNotBlank() && state.isNotBlank() && ownerName.isNotBlank()
        AlertDialog(
            onDismissRequest = { showClinicDialog = false },
            title = {
                Text(
                    "Set up your practice",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Enter your practice details to initialize your workspace:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = clinicName,
                        onValueChange = { clinicName = it },
                        label = { Text("Clinic name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Street address *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Doctor name (Owner) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isBusy && formValid,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    onClick = {
                        isBusy = true
                        errorMessage = null
                        showClinicDialog = false
                        scope.launch {
                            runCatching {
                                val user = authRepository.currentUser
                                functionsRepository.createClinicAndOwner(
                                    clinicName = clinicName,
                                    address = address,
                                    city = city,
                                    state = state,
                                    gstin = null,
                                    ownerName = ownerName,
                                    ownerPhone = user?.phoneNumber,
                                    ownerEmail = user?.email,
                                )
                                authRepository.refreshSession()
                            }.onFailure { err ->
                                errorMessage = "${err.message}\n(Tip: use 'Explore Live Demo Clinic' for immediate review)"
                            }
                            isBusy = false
                        }
                    },
                ) {
                    Text("Create Practice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClinicDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
