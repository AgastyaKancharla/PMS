package com.agastyaone.crmai.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DashboardTile(
    val label: String,
    val subtitle: String? = null,
    val badge: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
)

/**
 * Modern Clinical Command Center Scaffold for AgastyaOne.
 * Replaces generic tiles with high-density, context-rich dental practice modules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScaffold(
    title: String,
    subtitle: String,
    tiles: List<DashboardTile>,
    onSignOut: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "✚",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Text(
                                        text = subtitle.uppercase(),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        letterSpacing = 0.5.sp,
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "• Online",
                                    fontSize = 11.sp,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "Operatory Clinical Workstation",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Chairside scheduling, EMR, FDI odontogram & billing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🦷", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "CLINIC WORKSPACES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                )
            }

            items(tiles) { tile ->
                val meta = getTileMetadata(tile.label)
                val isClickable = tile.enabled && tile.onClick != {}

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (isClickable) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = isClickable, onClick = tile.onClick),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isClickable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isClickable) meta.iconBg else Color(0xFFF1F5F9),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(meta.iconEmoji, fontSize = 18.sp)
                                }
                            }

                            if (tile.badge != null || !isClickable) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isClickable) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9),
                                ) {
                                    Text(
                                        text = tile.badge ?: "SOON",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isClickable) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF94A3B8),
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = tile.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isClickable) MaterialTheme.colorScheme.onSurface else Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tile.subtitle ?: meta.defaultSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.5.sp,
                                color = if (isClickable) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TileMeta(val iconEmoji: String, val iconBg: Color, val defaultSubtitle: String)

private fun getTileMetadata(label: String): TileMeta {
    return when (label.lowercase()) {
        "schedule" -> TileMeta("📅", Color(0xFFE0F2FE), "Operatories & chairs")
        "patients" -> TileMeta("👥", Color(0xFFE6F4F1), "Directory, EMR & charts")
        "clinical" -> TileMeta("🩺", Color(0xFFF3E8FF), "Charting & diagnoses")
        "billing" -> TileMeta("💳", Color(0xFFECFDF5), "GST invoices & receipts")
        "reports" -> TileMeta("📊", Color(0xFFFEF3C7), "Practice analytics")
        "inventory" -> TileMeta("📦", Color(0xFFF1F5F9), "Dental consumables")
        "lab" -> TileMeta("🔬", Color(0xFFEDE9FE), "Lab slips & tracking")
        "staff" -> TileMeta("🧑‍⚕️", Color(0xFFFCE7F3), "Invite & role access")
        "data requests" -> TileMeta("🔒", Color(0xFFFFF1F2), "DPDP patient requests")
        "clinics" -> TileMeta("🏢", Color(0xFFE6F4F1), "Tenant management")
        "support" -> TileMeta("🎧", Color(0xFFE0F2FE), "System assistance")
        else -> TileMeta("📁", Color(0xFFF1F5F9), "Workspace module")
    }
}
