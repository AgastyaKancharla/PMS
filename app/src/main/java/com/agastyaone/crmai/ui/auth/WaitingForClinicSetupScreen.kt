package com.agastyaone.crmai.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agastyaone.crmai.core.ServiceLocator
import kotlinx.coroutines.launch

/**
 * Shown for a signed-in user whose ID token has no `role`/`clinicId` claim yet.
 * That's either someone who was invited but hasn't accepted, or a stray account
 * (e.g. someone who signed up without going through owner-signup or an invite).
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

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Waiting for clinic setup", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Your account is signed in, but isn't linked to a clinic yet. You can activate an invite, register a new clinic, or explore in demo mode.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            Button(
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    isBusy = true
                    errorMessage = null
                    scope.launch {
                        runCatching {
                            functionsRepository.acceptInvite()
                            authRepository.refreshSession()
                        }.onFailure {
                            errorMessage = it.message ?: "No pending invite found for your phone/email."
                        }
                        isBusy = false
                    }
                },
            ) { Text("I have a staff invite") }

            OutlinedButton(
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                onClick = { showClinicDialog = true },
            ) { Text("Set up a new clinic as Doctor / Owner") }

            FilledTonalButton(
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                onClick = { authRepository.setDemoSession() },
            ) { Text("Explore Demo Clinic (Quick Test)") }

            if (isBusy) CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            errorMessage?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp)) }

            TextButton(onClick = onSignOut, modifier = Modifier.padding(top = 24.dp)) {
                Text("Sign out")
            }
        }
    }

    if (showClinicDialog) {
        val formValid = clinicName.isNotBlank() && address.isNotBlank() && city.isNotBlank() && state.isNotBlank() && ownerName.isNotBlank()
        AlertDialog(
            onDismissRequest = { showClinicDialog = false },
            title = { Text("Set up your clinic") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = clinicName,
                        onValueChange = { clinicName = it },
                        label = { Text("Clinic name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Doctor name (Owner)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isBusy && formValid,
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
                                errorMessage = "${err.message}\n(Tip: use 'Explore Demo Clinic' to test all screens immediately)"
                            }
                            isBusy = false
                        }
                    },
                ) { Text("Create clinic") }
            },
            dismissButton = {
                TextButton(onClick = { showClinicDialog = false }) { Text("Cancel") }
            },
        )
    }
}
