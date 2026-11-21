package com.tapattend.nfc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapattend.nfc.data.AttendanceSession
import com.tapattend.nfc.data.StudentMatrixRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterMatrixScreen(
    session: AttendanceSession,
    matrix: List<StudentMatrixRow>,
    selectedCourse: String?,
    onSelectCourse: (String) -> Unit,
    totalWeeks: Int,
    onBack: () -> Unit,
    onExportCsv: () -> Unit,
    onAddSessionColumn: (String) -> Unit
) {
    var showOnlyDeficit by remember { mutableStateOf(false) }
    var showAddColDialog by remember { mutableStateOf(false) }
    var newColName by remember { mutableStateOf("") }

    val displayedRows = if (showOnlyDeficit) {
        matrix.filter { it.attendancePercentage < 75f }
    } else {
        matrix
    }

    val totalSessions = session.getTotalSessionsCount()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${session.courseCode} - Attendance Matrix", fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            "${session.universityName} • $totalSessions Sessions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddColDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Column")
                    }
                    IconButton(onClick = onExportCsv) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Single-Course Matrix CSV")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Single-Course Sheet Tabs for Common Units
            if (session.allowedCourses.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Course Sheet:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    session.allowedCourses.forEach { course ->
                        FilterChip(
                            selected = selectedCourse.equals(course, ignoreCase = true),
                            onClick = { onSelectCourse(course) },
                            label = { Text(course, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Filter Bar & Legend
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = showOnlyDeficit,
                            onClick = { showOnlyDeficit = !showOnlyDeficit },
                            label = { Text("Deficit Only (<75%): ${matrix.count { it.attendancePercentage < 75f }}", fontSize = 11.sp) }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF10B981)) {
                            Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Text(" Present  ", fontSize = 11.sp)
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE2E8F0)) {
                            Text("A", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Text(" Absent", fontSize = 11.sp)
                    }
                }
            }

            val horizontalScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("S/N", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(36.dp))
                        Text("Registration No.", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(125.dp))
                        Text("Full Name", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(140.dp))

                        for (w in 1..totalSessions) {
                            Text(
                                session.getColumnName(w),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.width(70.dp),
                                maxLines = 1
                            )
                        }
                        Text("Attended", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(65.dp))
                        Text("%", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(55.dp))
                        Text("Exam Status", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(85.dp))
                    }

                    HorizontalDivider()

                    LazyColumn(modifier = Modifier.fillMaxHeight()) {
                        itemsIndexed(displayedRows) { index, row ->
                            val isEligible = row.attendancePercentage >= 75f
                            Row(
                                modifier = Modifier
                                    .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}", fontSize = 12.sp, modifier = Modifier.width(36.dp))
                                Text(row.student.regNumber, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(125.dp))
                                Text(row.student.fullName, fontSize = 12.sp, modifier = Modifier.width(140.dp), maxLines = 1)

                                for (w in 1..totalSessions) {
                                    val isPresent = row.attendanceBySession[w] == true
                                    Box(
                                        modifier = Modifier.width(70.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPresent) {
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    "P",
                                                    color = Color(0xFF15803D),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else {
                                            Text("-", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Text("${row.totalPresent}/${row.totalSessionsHeld}", fontSize = 12.sp, modifier = Modifier.width(65.dp))
                                Text(
                                    "%.0f%%".format(row.attendancePercentage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isEligible) Color(0xFF15803D) else Color(0xFFDC2626),
                                    modifier = Modifier.width(55.dp)
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isEligible) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                    modifier = Modifier.width(85.dp)
                                ) {
                                    Text(
                                        if (isEligible) "ELIGIBLE" else "DEFICIT",
                                        color = if (isEligible) Color(0xFF15803D) else Color(0xFFB91C1C),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }

    if (showAddColDialog) {
        AlertDialog(
            onDismissRequest = { showAddColDialog = false },
            title = { Text("Add Session / Column") },
            text = {
                Column {
                    Text("Add a new session column to this attendance matrix on the fly:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        label = { Text("Column Header (e.g. Week 5, Lab 3)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newColName.ifBlank { "Session ${totalSessions + 1}" }
                        onAddSessionColumn(name)
                        newColName = ""
                        showAddColDialog = false
                    }
                ) {
                    Text("Add Column")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddColDialog = false }) { Text("Cancel") }
            }
        )
    }
}
