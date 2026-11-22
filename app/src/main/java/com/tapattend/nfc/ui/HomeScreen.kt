package com.tapattend.nfc.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapattend.nfc.data.AttendanceRecord
import com.tapattend.nfc.data.AttendanceSession
import com.tapattend.nfc.data.KenyanUniversities
import com.tapattend.nfc.data.SessionType
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sessions: List<AttendanceSession>,
    records: List<AttendanceRecord>,
    studentCount: Int,
    onSelectSession: (AttendanceSession) -> Unit,
    onCreateNewSession: () -> Unit,
    onOpenRoster: () -> Unit,
    onOpenCardInspector: () -> Unit,
    onDeleteSession: (AttendanceSession) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UniversityCrestBadge(
                            universityId = KenyanUniversities.DEFAULT_UNIVERSITY_ID,
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("TapAttend NFC", fontWeight = FontWeight.Bold)
                            Text(
                                "Offline NFC & Kenyan University Attendance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
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

            OutlinedButton(
                onClick = onOpenCardInspector,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF7C3AED)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Inspect Physical NFC Card (Local Chip Reader)",
                    fontSize = 12.sp,
                    color = Color(0xFF6D28D9)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

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
}

@Composable
fun UniversityCrestBadge(
    universityId: String,
    size: Dp = 36.dp
) {
    val uni = remember(universityId) { KenyanUniversities.getById(universityId) }
    val primary = Color(uni.primaryColorHex)
    val accent = Color(uni.accentColorHex)

    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f

        // Heraldic shield silhouette
        val shield = Path().apply {
            moveTo(w * 0.14f, h * 0.10f)
            lineTo(w * 0.86f, h * 0.10f)
            lineTo(w * 0.86f, h * 0.56f)
            quadraticBezierTo(w * 0.86f, h * 0.82f, cx, h * 0.93f)
            quadraticBezierTo(w * 0.14f, h * 0.82f, w * 0.14f, h * 0.56f)
            close()
        }
        drawPath(path = shield, color = Color(0xFFFCFDF9))
        drawPath(path = shield, color = primary, style = Stroke(width = w * 0.055f))

        if (uni.id == "dekut") {
            // Rising gold sun
            drawCircle(
                color = accent,
                radius = w * 0.10f,
                center = Offset(cx, h * 0.25f)
            )
            // Twin Mount Kenya peaks in forest green
            val peaks = Path().apply {
                moveTo(w * 0.22f, h * 0.54f)
                lineTo(w * 0.40f, h * 0.31f)
                lineTo(w * 0.50f, h * 0.42f)
                lineTo(w * 0.62f, h * 0.27f)
                lineTo(w * 0.78f, h * 0.54f)
                close()
            }
            drawPath(path = peaks, color = primary)

            // Snow caps
            val leftSnow = Path().apply {
                moveTo(w * 0.35f, h * 0.37f)
                lineTo(w * 0.40f, h * 0.31f)
                lineTo(w * 0.44f, h * 0.36f)
                close()
            }
            val rightSnow = Path().apply {
                moveTo(w * 0.57f, h * 0.34f)
                lineTo(w * 0.62f, h * 0.27f)
                lineTo(w * 0.67f, h * 0.35f)
                close()
            }
            drawPath(path = leftSnow, color = Color.White)
            drawPath(path = rightSnow, color = Color.White)

            // 8-tooth crimson/gold gear wheel at base of shield
            val gearCenter = Offset(cx, h * 0.68f)
            val gearR = w * 0.12f
            for (i in 0 until 8) {
                val angle = i * 45f
                rotate(degrees = angle, pivot = gearCenter) {
                    drawRect(
                        color = Color(0xFFB91C1C),
                        topLeft = Offset(cx - w * 0.03f, gearCenter.y - gearR - w * 0.03f),
                        size = Size(w * 0.06f, w * 0.05f)
                    )
                }
            }
            drawCircle(color = Color(0xFFB91C1C), radius = gearR, center = gearCenter)
            drawCircle(color = accent, radius = gearR * 0.7f, center = gearCenter)
            drawCircle(color = Color.White, radius = gearR * 0.34f, center = gearCenter)
        } else {
            // Academic chevron + open book + flame for other Kenyan universities
            val band = Path().apply {
                moveTo(w * 0.22f, h * 0.34f)
                lineTo(cx, h * 0.23f)
                lineTo(w * 0.78f, h * 0.34f)
                lineTo(w * 0.78f, h * 0.46f)
                lineTo(cx, h * 0.35f)
                lineTo(w * 0.22f, h * 0.46f)
                close()
            }
            drawPath(path = band, color = primary)

            val bookLeft = Path().apply {
                moveTo(w * 0.27f, h * 0.56f)
                lineTo(cx - w * 0.02f, h * 0.60f)
                lineTo(cx - w * 0.02f, h * 0.75f)
                lineTo(w * 0.27f, h * 0.70f)
                close()
            }
            val bookRight = Path().apply {
                moveTo(w * 0.73f, h * 0.56f)
                lineTo(cx + w * 0.02f, h * 0.60f)
                lineTo(cx + w * 0.02f, h * 0.75f)
                lineTo(w * 0.73f, h * 0.70f)
                close()
            }
            drawPath(path = bookLeft, color = accent)
            drawPath(path = bookRight, color = accent)

            // Rays
            for (k in 0..4) {
                val rad = Math.toRadians((200 + k * 35).toDouble())
                val x1 = cx + (w * 0.08f * cos(rad)).toFloat()
                val y1 = h * 0.21f + (h * 0.08f * sin(rad)).toFloat()
                drawCircle(color = accent, radius = w * 0.022f, center = Offset(x1, y1))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UniversityCrestBadge(universityId = session.universityId, size = 28.dp)
                    Spacer(modifier = Modifier.width(8.dp))
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
