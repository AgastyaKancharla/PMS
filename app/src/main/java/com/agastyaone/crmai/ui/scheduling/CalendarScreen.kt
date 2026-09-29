package com.agastyaone.crmai.ui.scheduling

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agastyaone.crmai.core.Role
import com.agastyaone.crmai.core.ServiceLocator
import com.agastyaone.crmai.data.scheduling.Appointment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private enum class CalendarViewMode { DAY, WEEK }

private val TIMELINE_HOURS = 8..19
private val DAY_HEADER_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    clinicId: String,
    role: Role,
    onBack: () -> Unit,
    onOpenAppointment: (String) -> Unit,
    onAddWalkIn: () -> Unit,
    onOpenWaitlist: () -> Unit,
) {
    val repository = ServiceLocator.scheduleRepository
    val canEdit = role == Role.OWNER || role == Role.RECEPTIONIST

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.DAY) }
    var rescheduleTarget by remember { mutableStateOf<Appointment?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Operatory Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (canEdit) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenWaitlist),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                "Waitlist",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = onAddWalkIn,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Walk-in quick add")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (viewMode == CalendarViewMode.DAY) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    modifier = Modifier.clickable { viewMode = CalendarViewMode.DAY },
                                ) {
                                    Text(
                                        "Day Timeline",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (viewMode == CalendarViewMode.DAY) FontWeight.Bold else FontWeight.Normal,
                                        color = if (viewMode == CalendarViewMode.DAY) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (viewMode == CalendarViewMode.WEEK) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    modifier = Modifier.clickable { viewMode = CalendarViewMode.WEEK },
                                ) {
                                    Text(
                                        "Week Overview",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (viewMode == CalendarViewMode.WEEK) FontWeight.Bold else FontWeight.Normal,
                                        color = if (viewMode == CalendarViewMode.WEEK) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        if (selectedDate != LocalDate.now()) {
                            OutlinedButton(
                                onClick = { selectedDate = LocalDate.now() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            ) {
                                Text("Today", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    if (viewMode == CalendarViewMode.DAY) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            IconButton(
                                onClick = { selectedDate = selectedDate.minusDays(1) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Text("◀", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                selectedDate.format(DAY_HEADER_FORMAT),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            IconButton(
                                onClick = { selectedDate = selectedDate.plusDays(1) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Text("▶", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            if (viewMode == CalendarViewMode.DAY) {
                val appointments by remember(selectedDate) {
                    repository.observeAppointmentsInRange(
                        clinicId,
                        selectedDate.startOfDayTimestamp(),
                        selectedDate.endOfDayTimestamp(),
                    )
                }.collectAsState(initial = emptyList())

                DayTimeline(
                    appointments = appointments,
                    canEdit = canEdit,
                    onOpenAppointment = onOpenAppointment,
                    onLongPressAppointment = { rescheduleTarget = it },
                )
            } else {
                WeekOverview(
                    clinicId = clinicId,
                    anchorDate = selectedDate,
                    onSelectDay = { day ->
                        selectedDate = day
                        viewMode = CalendarViewMode.DAY
                    },
                )
            }
        }
    }

    rescheduleTarget?.let { appointment ->
        RescheduleDialog(
            clinicId = clinicId,
            appointment = appointment,
            onDismiss = { rescheduleTarget = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayTimeline(
    appointments: List<Appointment>,
    canEdit: Boolean,
    onOpenAppointment: (String) -> Unit,
    onLongPressAppointment: (Appointment) -> Unit,
) {
    val displayHours = remember(appointments) {
        val appointmentHours = appointments.mapNotNull { it.startTime?.toLocalDateTime()?.hour }
        val minHour = minOf(TIMELINE_HOURS.first, appointmentHours.minOrNull() ?: TIMELINE_HOURS.first)
        val maxHour = maxOf(TIMELINE_HOURS.last, appointmentHours.maxOrNull() ?: TIMELINE_HOURS.last)
        (minHour..maxHour).toList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        items(displayHours) { hour ->
            val hourAppointments = appointments.filter { it.startTime?.toLocalDateTime()?.hour == hour }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = String.format(Locale.US, "%02d:00", hour),
                    modifier = Modifier
                        .width(48.dp)
                        .padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (hourAppointments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(4.dp)
                                )
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                        )
                    } else {
                        for (appointment in hourAppointments) {
                            val statusColor = colorForStatus(appointment.status)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { onOpenAppointment(appointment.id) },
                                        onLongClick = {
                                            if (canEdit && !isTerminalStatus(appointment.status)) {
                                                onLongPressAppointment(appointment)
                                            }
                                        },
                                    ),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .height(64.dp)
                                            .background(statusColor),
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                appointment.patientName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = statusColor.copy(alpha = 0.2f),
                                            ) {
                                                Text(
                                                    text = labelForStatus(appointment.status),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "🕒 ${appointment.startTime?.formattedTime()} - ${appointment.endTime?.formattedTime()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekOverview(
    clinicId: String,
    anchorDate: LocalDate,
    onSelectDay: (LocalDate) -> Unit,
) {
    val repository = ServiceLocator.scheduleRepository
    val weekStart = anchorDate.with(DayOfWeek.MONDAY)
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }

    val weekAppointments by remember(weekStart) {
        repository.observeAppointmentsInRange(
            clinicId,
            weekStart.startOfDayTimestamp(),
            weekStart.plusDays(7).startOfDayTimestamp(),
        )
    }.collectAsState(initial = emptyList())

    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(days) { day ->
            val count = weekAppointments.count { it.startTime?.toLocalDateTime()?.toLocalDate() == day }
            val isToday = day == LocalDate.now()
            Card(
                modifier = Modifier
                    .width(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        if (isToday) 2.dp else 1.dp,
                        if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectDay(day) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        day.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        day.format(DateTimeFormatter.ofPattern("d MMM")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = "$count booked",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (count > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
