package com.tapattend.nfc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapattend.nfc.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionDialog(
    customCourses: List<CustomCourse>,
    onAddCustomCourse: (CustomCourse) -> Unit,
    onDismiss: () -> Unit,
    onCreate: (AttendanceSession) -> Unit
) {
    val defaultUni = KenyanUniversities.getById(KenyanUniversities.DEFAULT_UNIVERSITY_ID)

    var selectedUniId by remember { mutableStateOf(defaultUni.id) }
    var useCustomUni by remember { mutableStateOf(false) }
    var customUniName by remember { mutableStateOf("") }
    val selectedUni = remember(selectedUniId) { KenyanUniversities.getById(selectedUniId) }

    var selectedDept by remember(selectedUniId) {
        mutableStateOf(selectedUni.departments.firstOrNull()?.name ?: "Department of Computer Science")
    }
    var useCustomDept by remember { mutableStateOf(false) }
    var customDeptName by remember { mutableStateOf("") }

    val activeDeptName = if (useCustomDept && customDeptName.isNotBlank()) customDeptName.trim() else selectedDept

    val availableCourses = remember(selectedUniId, activeDeptName, customCourses, useCustomUni) {
        val presets = if (!useCustomUni) {
            selectedUni.departments.find { it.name.equals(activeDeptName, ignoreCase = true) }?.courses ?: emptyList()
        } else {
            emptyList()
        }
        val customs = customCourses
            .filter {
                it.universityId == (if (useCustomUni) "custom" else selectedUniId) &&
                    it.department.equals(activeDeptName, ignoreCase = true)
            }
            .map { PresetCourse(it.courseCode, it.courseName) }
        (presets + customs).distinctBy { it.name.lowercase() }
    }

    val selectedCourses = remember { mutableStateListOf<String>() }

    LaunchedEffect(availableCourses) {
        if (selectedCourses.isEmpty() && availableCourses.isNotEmpty()) {
            selectedCourses.add(availableCourses.first().name)
        }
    }

    var showAddCourseBox by remember { mutableStateOf(false) }
    var newCourseCode by remember { mutableStateOf("") }
    var newCourseName by remember { mutableStateOf("") }

    var uniDropdownOpen by remember { mutableStateOf(false) }
    var deptDropdownOpen by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("Lecture Hall 1") }
    var instructor by remember { mutableStateOf("Class Rep / Lecturer") }
    var selectedType by remember { mutableStateOf(SessionType.SEMESTER_WEEKLY) }
    var totalWeeks by remember { mutableIntStateOf(14) }

    // Customization state
    var isMultiSessionCustom by remember { mutableStateOf(true) }
    var customColumnInput by remember { mutableStateOf("") }
    val customColumnsList = remember { mutableStateListOf<String>() }
    var customSessionNamesText by remember {
        mutableStateOf("Session 1, Session 2, Midterm Lab, Session 4, Final Demo")
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
                Text(
                    "Create & Customize Sheet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Bind University, Department & Course(s) to block wrong-class scans",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 1. University Selector (default: DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY)
                Text("1. University / School Name (shown on Sheet & Logo)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = if (useCustomUni) "Other / Custom University" else selectedUni.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { uniDropdownOpen = true }
                    )
                    DropdownMenu(
                        expanded = uniDropdownOpen,
                        onDismissRequest = { uniDropdownOpen = false }
                    ) {
                        KenyanUniversities.list.forEach { uni ->
                            DropdownMenuItem(
                                text = { Text("${uni.shortName} — ${uni.name}", fontSize = 12.sp) },
                                onClick = {
                                    selectedUniId = uni.id
                                    useCustomUni = false
                                    useCustomDept = false
                                    selectedDept = uni.departments.firstOrNull()?.name ?: "General Department"
                                    selectedCourses.clear()
                                    uniDropdownOpen = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ Other / Custom University", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            onClick = {
                                useCustomUni = true
                                useCustomDept = true
                                selectedCourses.clear()
                                uniDropdownOpen = false
                            }
                        )
                    }
                }

                if (useCustomUni) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customUniName,
                        onValueChange = { customUniName = it.uppercase() },
                        label = { Text("Custom University Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Department Selector (BEFORE Course!)
                Text("2. Department / School (Select before Course)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (!useCustomUni) {
                    Box {
                        OutlinedTextField(
                            value = if (useCustomDept) "Other / Custom Department" else selectedDept,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { deptDropdownOpen = true }
                        )
                        DropdownMenu(
                            expanded = deptDropdownOpen,
                            onDismissRequest = { deptDropdownOpen = false }
                        ) {
                            selectedUni.departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept.name, fontSize = 12.sp) },
                                    onClick = {
                                        selectedDept = dept.name
                                        useCustomDept = false
                                        selectedCourses.clear()
                                        deptDropdownOpen = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Other / Custom Department", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                onClick = {
                                    useCustomDept = true
                                    selectedCourses.clear()
                                    deptDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                if (useCustomDept || useCustomUni) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customDeptName,
                        onValueChange = { customDeptName = it },
                        label = { Text("Department Name") },
                        placeholder = { Text("e.g. Department of Computer Science") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Allowed Courses (Multi-select for Common Units, 1 Sheet per Course)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3. Eligible Course(s) (Common Units OK)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = { showAddCourseBox = !showAddCourseBox },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text(if (showAddCourseBox) "Hide" else "+ Custom Course", fontSize = 11.sp)
                    }
                }

                if (showAddCourseBox) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = newCourseCode,
                                    onValueChange = { newCourseCode = it.uppercase() },
                                    label = { Text("Code", fontSize = 11.sp) },
                                    placeholder = { Text("C030") },
                                    modifier = Modifier.weight(0.35f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newCourseName,
                                    onValueChange = { newCourseName = it },
                                    label = { Text("Course Name", fontSize = 11.sp) },
                                    placeholder = { Text("B.Sc. AI & Robotics") },
                                    modifier = Modifier.weight(0.65f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    if (newCourseName.isNotBlank()) {
                                        val added = CustomCourse(
                                            universityId = if (useCustomUni) "custom" else selectedUniId,
                                            department = activeDeptName,
                                            courseCode = newCourseCode.ifBlank { "CUST" },
                                            courseName = newCourseName.trim()
                                        )
                                        onAddCustomCourse(added)
                                        if (!selectedCourses.contains(added.courseName)) {
                                            selectedCourses.add(added.courseName)
                                        }
                                        newCourseCode = ""
                                        newCourseName = ""
                                        showAddCourseBox = false
                                    }
                                },
                                enabled = newCourseName.isNotBlank(),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Add & Select", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (availableCourses.isEmpty()) {
                            Text(
                                "No preset courses in this custom department yet — tap '+ Custom Course' above to add one.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(4.dp)
                            )
                        } else {
                            availableCourses.forEach { c ->
                                val isChecked = selectedCourses.any { it.equals(c.name, ignoreCase = true) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) {
                                                selectedCourses.removeAll { it.equals(c.name, ignoreCase = true) }
                                            } else {
                                                selectedCourses.add(c.name)
                                            }
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked && !isChecked) {
                                                selectedCourses.add(c.name)
                                            } else if (!checked) {
                                                selectedCourses.removeAll { it.equals(c.name, ignoreCase = true) }
                                            }
                                        }
                                    )
                                    Column {
                                        Text(c.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(c.code, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedCourses.size > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Common Unit (${selectedCourses.size} courses): Printing/exporting will automatically split into ${selectedCourses.size} separate sheets (1 per course).",
                            fontSize = 11.sp,
                            color = Color(0xFF0369A1),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Sheet Occasion Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                TypeSelectionOption(
                    title = "Whole Semester Attendance",
                    description = "Multi-week semester matrix (Week 1..N) with 75% rule",
                    icon = Icons.Default.CalendarMonth,
                    isSelected = selectedType == SessionType.SEMESTER_WEEKLY,
                    onClick = { selectedType = SessionType.SEMESTER_WEEKLY }
                )

                TypeSelectionOption(
                    title = "Single Exam / Test Attendance",
                    description = "Records desk/time with digital signature attestation",
                    icon = Icons.Default.FactCheck,
                    isSelected = selectedType == SessionType.EXAM,
                    onClick = { selectedType = SessionType.EXAM }
                )

                TypeSelectionOption(
                    title = "Assignment / Lab Submission",
                    description = "Timestamps drop-offs, logs submission order #1, #2...",
                    icon = Icons.Default.UploadFile,
                    isSelected = selectedType == SessionType.SUBMISSION,
                    onClick = { selectedType = SessionType.SUBMISSION }
                )

                TypeSelectionOption(
                    title = "Single Occasion / Seminar",
                    description = "One-time roll call for workshops, talks or meetings",
                    icon = Icons.Default.HistoryEdu,
                    isSelected = selectedType == SessionType.SINGLE_EVENT,
                    onClick = { selectedType = SessionType.SINGLE_EVENT }
                )

                TypeSelectionOption(
                    title = "Custom Configured Sheet",
                    description = "Define custom session names, custom tracking fields & columns",
                    icon = Icons.Default.Tune,
                    isSelected = selectedType == SessionType.CUSTOM_SHEET,
                    onClick = { selectedType = SessionType.CUSTOM_SHEET }
                )

                if (selectedType == SessionType.CUSTOM_SHEET) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Custom Matrix Configuration", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isMultiSessionCustom,
                                    onCheckedChange = { isMultiSessionCustom = it }
                                )
                                Text("Multi-Session / Multi-Column Matrix", fontSize = 12.sp)
                            }
                            if (isMultiSessionCustom) {
                                OutlinedTextField(
                                    value = customSessionNamesText,
                                    onValueChange = { customSessionNamesText = it },
                                    label = { Text("Column Names (comma separated)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Custom Fields (e.g. Seat #, Remarks):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = customColumnInput,
                                    onValueChange = { customColumnInput = it },
                                    placeholder = { Text("e.g. Seat Number") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = {
                                        if (customColumnInput.isNotBlank() && !customColumnsList.contains(customColumnInput.trim())) {
                                            customColumnsList.add(customColumnInput.trim())
                                            customColumnInput = ""
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = "Add Column", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                if (selectedType == SessionType.SEMESTER_WEEKLY) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Number of Weeks: $totalWeeks", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Slider(
                        value = totalWeeks.toFloat(),
                        onValueChange = { totalWeeks = it.toInt() },
                        valueRange = 1f..16f,
                        steps = 14
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { courseCode = it.uppercase() },
                    label = { Text("Unit Code (e.g. CCS 2101)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Unit / Sheet Title") },
                    placeholder = { Text("e.g. Data Structures & Algorithms") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue / Room") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Lecturer / Class Rep Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalUniName = if (useCustomUni && customUniName.isNotBlank()) {
                                customUniName.trim()
                            } else {
                                selectedUni.name
                            }
                            val finalUniId = if (useCustomUni) "custom" else selectedUni.id
                            val finalCode = courseCode.ifBlank { "CCS 101" }
                            val finalTitle = title.ifBlank { "$finalCode Attendance" }

                            val sessionColNames = if (selectedType == SessionType.CUSTOM_SHEET && isMultiSessionCustom) {
                                customSessionNamesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            } else if (selectedType == SessionType.SEMESTER_WEEKLY) {
                                (1..totalWeeks).map { "Week $it" }
                            } else {
                                emptyList()
                            }

                            val calculatedWeeks = when {
                                sessionColNames.isNotEmpty() -> sessionColNames.size
                                selectedType == SessionType.SEMESTER_WEEKLY -> totalWeeks
                                else -> 1
                            }

                            val finalCourses = if (selectedCourses.isNotEmpty()) {
                                selectedCourses.toList()
                            } else if (availableCourses.isNotEmpty()) {
                                listOf(availableCourses.first().name)
                            } else {
                                listOf("General Programme")
                            }

                            val newSession = AttendanceSession(
                                title = finalTitle,
                                courseCode = finalCode,
                                universityId = finalUniId,
                                universityName = finalUniName,
                                department = activeDeptName,
                                allowedCourses = finalCourses,
                                sessionType = selectedType,
                                totalWeeks = calculatedWeeks,
                                venue = venue,
                                instructorName = instructor,
                                customColumns = customColumnsList.toList(),
                                sessionColumnNames = sessionColNames
                            )
                            onCreate(newSession)
                        }
                    ) {
                        Text("Create Sheet")
                    }
                }
            }
        }
    }
}

@Composable
fun TypeSelectionOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
