package com.agastyaone.crmai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.agastyaone.crmai.ui.AgastyaOneRoot
import com.agastyaone.crmai.ui.theme.AgastyaOneTheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("crash_reports", MODE_PRIVATE)
        val lastCrash = prefs.getString("last_crash", null)

        setContent {
            AgastyaOneTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AgastyaOneRoot()

                    if (!lastCrash.isNullOrBlank()) {
                        var showCrashDialog by remember { mutableStateOf(true) }
                        if (showCrashDialog) {
                            val clipboardManager = LocalClipboardManager.current
                            AlertDialog(
                                onDismissRequest = {
                                    showCrashDialog = false
                                    prefs.edit().remove("last_crash").apply()
                                },
                                title = { Text("App Diagnostics") },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp)
                                            .verticalScroll(rememberScrollState()),
                                    ) {
                                        Text(
                                            "The app logged this diagnostic trace from the previous run:",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text(
                                                lastCrash,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(8.dp),
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(lastCrash))
                                        showCrashDialog = false
                                        prefs.edit().remove("last_crash").apply()
                                    }) {
                                        Text("Copy & Dismiss")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showCrashDialog = false
                                        prefs.edit().remove("last_crash").apply()
                                    }) {
                                        Text("Dismiss")
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
