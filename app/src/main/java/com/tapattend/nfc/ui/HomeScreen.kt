package com.tapattend.nfc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapattend.nfc.data.AttendanceRecord
import com.tapattend.nfc.data.AttendanceSession
import com.tapattend.nfc.data.SessionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sessions: List<AttendanceSession>,
    records: List<AttendanceRecord>,
    studentCount: Int,
    isCloudConfigured: Boolean,
    isSyncing: Boolean,
    lastSyncTime: String?,
    currentSyncKey: String,
    onSelectSession: (AttendanceSession) -> Unit,
    onCreateNewSession: () -> Unit,
    onOpenRoster: () -> Unit,
    onOpenCardRegistration: () -> Unit,
    onUpdateDatabase: () -> Unit,
    onSaveSyncKey: (String) -> Unit,
    onDeleteSession: (AttendanceSession) -> Unit
) {
    var showSyncKeyDialog by remember { mutableStateOf(false) }
    var syncKeyInput by remember(currentSyncKey) { mutableStateOf(currentSyncKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TapAttend NFC", fontWeight = FontWeight.Bold)
                        Text(
                            "Kenyan University Roll Call & Cloud Sync",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSyncKeyDialog = true }) {
                        Icon(
                            Icons.Default.VpnKey,
                            contentDescription = "Cloud Sync Key",
                            tint = if (isCloudConfigured) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onOpenRoster) {
                        Icon(Icons.Default.People, contentDescription = "Student Roster")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNewSession,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Sheet / Session") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stats Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Enrolled Students",
                    value = studentCount.toString(),
                    icon = Icons.Default.Badge,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF0284C7)
                )
                StatCard(
                    title = "Active Sheets",
                    value = sessions.size.toString(),
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF059669)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Cloud & NFC Registration Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenCardRegistration,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tap & Register", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = {
                        if (!isCloudConfigured) {
                            showSyncKeyDialog = true
                        } else {
                            onUpdateDatabase()
                        }
                    },
                    enabled = !isSyncing,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        when {
                            isSyncing -> "Updating..."
                            lastSyncTime != null -> "Update ($lastSyncTime)"
                            else -> "Update Roster"
                        },
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Attendance Sessions & Sheets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Contactless,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No attendance sheets created yet",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Tap '+ New Sheet / Session' to begin roll call",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(sessions) { session ->
                        val sessionRecords = records.filter { it.sessionId == session.id }
                        SessionItemCard(
                            session = session,
                            attendanceCount = sessionRecords.size,
                            onClick = { onSelectSession(session) },
                            onDelete = { onDeleteSession(session) }
                        )
                    }
                }
            }
        }
    }

    if (showSyncKeyDialog) {
        AlertDialog(
            onDismissRequest = { showSyncKeyDialog = false },
            title = { Text("Cloud Database Sync Key", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Paste the Sync Key from the TapAttend Web App (sign in -> 'APK Sync Key') to sync registered student cards across devices and future APK builds.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = syncKeyInput,
                        onValueChange = { syncKeyInput = it },
                        label = { Text("Firebase Sync Key") },
                        placeholder = { Text("Paste key here...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveSyncKey(syncKeyInput.trim())
                        showSyncKeyDialog = false
                        if (syncKeyInput.isNotBlank()) {
                            onUpdateDatabase()
                        }
                    }
                ) {
                    Text("Save & Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncKeyDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SessionItemCard(
    session: AttendanceSession,
    attendanceCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val typeBadgeColor = when (session.sessionType) {
        SessionType.EXAM -> Color(0xFFDC2626)
        SessionType.SUBMISSION -> Color(0xFF7C3AED)
        SessionType.SEMESTER_WEEKLY -> Color(0xFF0284C7)
        SessionType.SINGLE_EVENT -> Color(0xFF059669)
        SessionType.CUSTOM_SHEET -> Color(0xFFD97706)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeBadgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = session.sessionType.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = typeBadgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = session.universityName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B6623)
            )
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${session.courseCode} • ${session.venue}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (session.allowedCourses.isNotEmpty()) {
                Text(
                    text = "Courses (${session.allowedCourses.size}): ${session.allowedCourses.joinToString(", ")}",
                    fontSize = 11.sp,
                    color = Color(0xFF0369A1),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (session.isMultiSession()) {
                            "$attendanceCount total taps (${session.getTotalSessionsCount()} cols)"
                        } else {
                            "$attendanceCount students signed in"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Open Sheet",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
