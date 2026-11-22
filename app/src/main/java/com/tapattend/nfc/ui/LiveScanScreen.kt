package com.tapattend.nfc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapattend.nfc.data.*

data class ScanBannerState(
    val status: String,
    val record: AttendanceRecord? = null,
    val blockedStudent: Student? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScanScreen(
    session: AttendanceSession,
    records: List<AttendanceRecord>,
    students: List<Student>,
    customCourses: List<CustomCourse>,
    selectedWeek: Int,
    onWeekChanged: (Int) -> Unit,
    onBack: () -> Unit,
    onOpenMatrix: () -> Unit,
    onExport: () -> Unit,
    lastScannedResult: ScanBannerState?,
    onManualCheckIn: (Student) -> Unit,
    onFastManualCheckIn: (
        regNumber: String,
        fullName: String,
        courseName: String,
        department: String,
        customValues: Map<String, String>
    ) -> Unit,
    pendingCardToEnroll: ParsedCardData?,
    onDismissEnrollDialog: () -> Unit,
    onSaveAndEnrollTap: (
        cardUid: String,
        existingStudentId: String?,
        newReg: String?,
        newName: String?,
        universityId: String,
        universityName: String,
        dept: String,
        courseCode: String,
        courseName: String,
        customValues: Map<String, String>
    ) -> Unit
) {
    var showManualDialog by remember { mutableStateOf(false) }
    val totalSessions = session.getTotalSessionsCount()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UniversityCrestBadge(universityId = session.universityId, size = 32.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(session.courseCode, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(
                                "${session.universityName} • ${session.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (session.isMultiSession()) {
                        IconButton(onClick = onOpenMatrix) {
                            Icon(Icons.Default.GridOn, contentDescription = "Matrix View")
                        }
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.Share, contentDescription = "Export Sheet")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (session.allowedCourses.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE0F2FE),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Eligible (${session.allowedCourses.size}): ${session.allowedCourses.joinToString(", ")}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (session.isMultiSession()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Active Column:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (selectedWeek > 1) onWeekChanged(selectedWeek - 1) },
                            enabled = selectedWeek > 1
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                session.getColumnName(selectedWeek),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = { if (selectedWeek < totalSessions) onWeekChanged(selectedWeek + 1) },
                            enabled = selectedWeek < totalSessions
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isSuccess = lastScannedResult?.status?.startsWith("VERIFIED") == true
            val isDuplicate = lastScannedResult?.status?.startsWith("ALREADY") == true
            val isWrongCourse = lastScannedResult?.status?.startsWith("WRONG_COURSE") == true
            val isUnknown = lastScannedResult?.status?.startsWith("UNKNOWN") == true

            val zoneColor = when {
                isSuccess -> Color(0xFF10B981)
                isDuplicate -> Color(0xFFF59E0B)
                isWrongCourse || isUnknown -> Color(0xFFDC2626)
                else -> MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .size(122.dp)
                    .clip(CircleShape)
                    .background(zoneColor.copy(alpha = 0.12f))
                    .border(3.dp, zoneColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        when {
                            isSuccess -> Icons.Default.Check
                            isDuplicate -> Icons.Default.Warning
                            isWrongCourse || isUnknown -> Icons.Default.Block
                            else -> Icons.Default.Nfc
                        },
                        contentDescription = null,
                        modifier = Modifier.size(46.dp),
                        tint = zoneColor
                    )
                    Text(
                        when {
                            isSuccess -> "TAP OK"
                            isDuplicate -> "DUPLICATE"
                            isWrongCourse -> "WRONG CLASS"
                            isUnknown -> "UNKNOWN"
                            else -> "READY"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = zoneColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Tap student ID card to back of phone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Present: ${records.size} Students",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isWrongCourse && lastScannedResult?.blockedStudent != null) {
                val blocked = lastScannedResult.blockedStudent
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.GppBad,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Blocked: Wrong Course (${blocked.fullName})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                "Card course: ${blocked.courseName}",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C)
                            )
                            Text(
                                "Sheet allows: ${session.allowedCourses.joinToString(", ")}",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (lastScannedResult?.record != null) {
                val record = lastScannedResult.record
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDuplicate) Color(0xFFFFFBEB) else Color(0xFFECFDF5)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDuplicate) Color(0xFFFDE68A) else Color(0xFFA7F3D0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isDuplicate) Color(0xFFF59E0B) else Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                record.studentName.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(record.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            Text("${record.regNumber} • ${record.studentCourse}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                            Text(
                                "SHA-256: ${record.attestationToken} • ${record.getFormattedTime()}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Button(
                onClick = { showManualDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fast Manual Check-In (Name & Reg No)", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Live Roll Call (${records.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(records.asReversed()) { r ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(r.studentName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(
                                    "${r.regNumber} • ${r.studentCourse} • ${r.getFormattedTime()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (r.customValues.isNotEmpty()) {
                                    val customSummary = r.customValues.entries.joinToString(" • ") { "${it.key}: ${it.value}" }
                                    Text(customSummary, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(
                                "✓ ${r.status}",
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showManualDialog) {
        ManualCheckInDialog(
            session = session,
            students = students,
            alreadyPresentUids = records.map { it.studentCardUid.replace(":", "").uppercase() }.toSet(),
            alreadyPresentRegs = records.map { it.regNumber.trim().uppercase() }.toSet(),
            onDismiss = { showManualDialog = false },
            onFastCheckIn = { reg, name, course, dept, customVals ->
                onFastManualCheckIn(reg, name, course, dept, customVals)
                showManualDialog = false
            },
            onSelectExisting = {
                onManualCheckIn(it)
                showManualDialog = false
            }
        )
    }

    pendingCardToEnroll?.let { cardData ->
        QuickEnrollOnTapDialog(
            cardData = cardData,
            session = session,
            students = students,
            customCourses = customCourses,
            onDismiss = onDismissEnrollDialog,
            onSaveAndCheckIn = { existingId, reg, name, uniId, uniName, dept, crsCode, crsName, customVals ->
                onSaveAndEnrollTap(
                    cardData.cardUid,
                    existingId,
                    reg,
                    name,
                    uniId,
                    uniName,
                    dept,
                    crsCode,
                    crsName,
                    customVals
                )
                onDismissEnrollDialog()
            }
        )
    }
}

@Composable
fun QuickEnrollOnTapDialog(
    cardData: ParsedCardData,
    session: AttendanceSession,
    students: List<Student>,
    customCourses: List<CustomCourse>,
    onDismiss: () -> Unit,
    onSaveAndCheckIn: (
        existingStudentId: String?,
        reg: String?,
        name: String?,
        universityId: String,
        universityName: String,
        dept: String,
        courseCode: String,
        courseName: String,
        customVals: Map<String, String>
    ) -> Unit
) {
    var mode by remember { mutableIntStateOf(0) }
    var regNumber by remember { mutableStateOf(cardData.suggestedRegNumber ?: "") }
    var fullName by remember { mutableStateOf(cardData.suggestedName ?: "") }

    val uni = remember(session.universityId) { KenyanUniversities.getById(session.universityId) }
    var department by remember { mutableStateOf(cardData.suggestedDepartment ?: session.department) }
    val sessionCourses = remember(session, customCourses) {
        val allowed = session.allowedCourses
        if (allowed.isNotEmpty()) allowed else listOf("General Programme")
    }
    var selectedCourse by remember {
        val matched = cardData.suggestedCourse?.let { sc ->
            sessionCourses.find { CourseMatcher.isCourseMatch(it, sc) }
        }
        mutableStateOf(matched ?: sessionCourses.first())
    }
    var courseDropdownOpen by remember { mutableStateOf(false) }

    var selectedStudentId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val customValues = remember { mutableStateMapOf<String, String>() }

    val unmappedStudents = students.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) || it.regNumber.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Nfc, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Card UID Not Mapped Yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("UID: ${cardData.cardUid}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(selectedTabIndex = mode) {
                    Tab(
                        selected = mode == 0,
                        onClick = { mode = 0 },
                        text = { Text("Enter Name & Reg", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = mode == 1,
                        onClick = { mode = 1 },
                        text = { Text("Link Existing", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (mode == 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFECFDF5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                session.universityName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B6623)
                            )
                            Text(
                                "Dept: $department",
                                fontSize = 11.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (sessionCourses.size == 1) {
                        OutlinedTextField(
                            value = selectedCourse,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Course (Pre-filled from Sheet)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Box {
                            OutlinedTextField(
                                value = selectedCourse,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Course (From Sheet)") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { courseDropdownOpen = true }
                            )
                            DropdownMenu(
                                expanded = courseDropdownOpen,
                                onDismissRequest = { courseDropdownOpen = false }
                            ) {
                                sessionCourses.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c, fontSize = 12.sp) },
                                        onClick = {
                                            selectedCourse = c
                                            department = KenyanUniversities.inferDepartmentFromCourse(c) ?: session.department
                                            courseDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regNumber,
                        onValueChange = { regNumber = it.uppercase() },
                        label = { Text("Registration Number *") },
                        placeholder = { Text("e.g. E021-01-1034/2024") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Student Full Name *") },
                        placeholder = { Text("e.g. Joseph Karoki Mwangi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search roster name or reg...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(140.dp)) {
                        items(unmappedStudents) { s ->
                            val isSelected = selectedStudentId == s.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStudentId = s.id }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(s.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${s.regNumber} • ${s.courseName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (session.customColumns.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Session Fields:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    session.customColumns.forEach { col ->
                        var colVal by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = colVal,
                            onValueChange = {
                                colVal = it
                                customValues[col] = it
                            },
                            label = { Text(col, fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val matchedCourse = uni.departments
                                .flatMap { it.courses }
                                .find { CourseMatcher.isCourseMatch(it.name, selectedCourse) }
                            val crsCode = matchedCourse?.code ?: session.courseCode

                            if (mode == 0 && fullName.isNotBlank() && regNumber.isNotBlank()) {
                                onSaveAndCheckIn(
                                    null,
                                    regNumber.trim(),
                                    fullName.trim(),
                                    session.universityId,
                                    session.universityName,
                                    department.trim(),
                                    crsCode,
                                    selectedCourse,
                                    customValues.toMap()
                                )
                            } else if (mode == 1 && selectedStudentId != null) {
                                onSaveAndCheckIn(
                                    selectedStudentId,
                                    null,
                                    null,
                                    session.universityId,
                                    session.universityName,
                                    department.trim(),
                                    crsCode,
                                    selectedCourse,
                                    customValues.toMap()
                                )
                            }
                        },
                        enabled = (mode == 0 && fullName.isNotBlank() && regNumber.isNotBlank()) ||
                            (mode == 1 && selectedStudentId != null)
                    ) {
                        Text("Save & Mark Present")
                    }
                }
            }
        }
    }
}

@Composable
fun ManualCheckInDialog(
    session: AttendanceSession,
    students: List<Student>,
    alreadyPresentUids: Set<String>,
    alreadyPresentRegs: Set<String>,
    onDismiss: () -> Unit,
    onFastCheckIn: (
        regNumber: String,
        fullName: String,
        courseName: String,
        department: String,
        customValues: Map<String, String>
    ) -> Unit,
    onSelectExisting: (Student) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var regNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    val sessionCourses = remember(session) {
        if (session.allowedCourses.isNotEmpty()) session.allowedCourses else listOf("General Programme")
    }
    var selectedCourse by remember(session) { mutableStateOf(sessionCourses.first()) }
    var selectedDept by remember(selectedCourse, session) {
        mutableStateOf(KenyanUniversities.inferDepartmentFromCourse(selectedCourse) ?: session.department)
    }
    var courseDropdownOpen by remember { mutableStateOf(false) }
    val customValues = remember { mutableStateMapOf<String, String>() }

    var query by remember { mutableStateOf("") }
    val filtered = students.filter {
        it.fullName.contains(query, ignoreCase = true) ||
            it.regNumber.contains(query, ignoreCase = true) ||
            it.courseName.contains(query, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manual Check-In", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                TabRow(selectedTabIndex = tab) {
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text("Quick Entry (Name & Reg)", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text("Saved Roster", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (tab == 0) {
                    // Pre-filled university, department & course badge from sheet
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0F9FF),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Pre-filled from Sheet",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                            Text(session.universityName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Department: $selectedDept", fontSize = 11.sp, color = Color(0xFF334155))
                            if (sessionCourses.size == 1) {
                                Text("Course: $selectedCourse", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }
                    }

                    if (sessionCourses.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            OutlinedTextField(
                                value = selectedCourse,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Course (Pick from Sheet)") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { courseDropdownOpen = true }
                            )
                            DropdownMenu(
                                expanded = courseDropdownOpen,
                                onDismissRequest = { courseDropdownOpen = false }
                            ) {
                                sessionCourses.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c, fontSize = 12.sp) },
                                        onClick = {
                                            selectedCourse = c
                                            selectedDept = KenyanUniversities.inferDepartmentFromCourse(c) ?: session.department
                                            courseDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Student Full Name *") },
                        placeholder = { Text("e.g. Joseph Karoki Mwangi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regNumber,
                        onValueChange = { regNumber = it.uppercase() },
                        label = { Text("Registration Number *") },
                        placeholder = { Text("e.g. E021-01-1034/2024") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (session.customColumns.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        session.customColumns.forEach { col ->
                            var colVal by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = colVal,
                                onValueChange = {
                                    colVal = it
                                    customValues[col] = it
                                },
                                label = { Text(col, fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (fullName.isNotBlank() && regNumber.isNotBlank()) {
                                    onFastCheckIn(
                                        regNumber.trim(),
                                        fullName.trim(),
                                        selectedCourse,
                                        selectedDept,
                                        customValues.toMap()
                                    )
                                }
                            },
                            enabled = fullName.isNotBlank() && regNumber.isNotBlank()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Present")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search Name, Reg No or Course...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.height(230.dp)) {
                        items(filtered) { s ->
                            val cleanUid = s.cardUid.replace(":", "").trim().uppercase()
                            val isPresent = (cleanUid.isNotBlank() && alreadyPresentUids.contains(cleanUid)) ||
                                alreadyPresentRegs.contains(s.regNumber.trim().uppercase())
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${s.regNumber} • ${s.courseName}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (isPresent) {
                                    Text("Present", color = Color(0xFF059669), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Button(
                                        onClick = { onSelectExisting(s) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Mark Present", fontSize = 11.sp)
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
