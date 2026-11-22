package com.tapattend.nfc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapattend.nfc.data.*

private data class CourseSlotState(
    val department: String,
    val useCustomDept: Boolean = false,
    val customDept: String = "",
    val courseName: String,
    val showCustomCourseBox: Boolean = false,
    val customCourseCode: String = "",
    val customCourseName: String = ""
)

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

    val allDepts = remember(selectedUniId, customCourses, useCustomUni) {
        if (useCustomUni) {
            KenyanUniversities.allKenyanDepartments
        } else {
            selectedUni.departments
        }
    }

    val firstDeptName = allDepts.firstOrNull()?.name ?: "Department of Computer Science"
    val firstCourseName = allDepts.firstOrNull()?.courses?.firstOrNull()?.name ?: "Bachelor of Science in Computer Science"

    val courseSlots = remember {
        mutableStateListOf(
            CourseSlotState(
                department = firstDeptName,
                courseName = firstCourseName
            )
        )
    }

    fun updateSlotCount(newCount: Int) {
        val clamped = newCount.coerceIn(1, 12)
        while (courseSlots.size < clamped) {
            val baseSlot = courseSlots.last()
            val deptCourses = allDepts.find { it.name.equals(baseSlot.department, ignoreCase = true) }?.courses ?: emptyList()
            val nextUnused = deptCourses.firstOrNull { c ->
                courseSlots.none { it.courseName.equals(c.name, ignoreCase = true) }
            }?.name ?: baseSlot.courseName
            courseSlots.add(
                CourseSlotState(
                    department = baseSlot.department,
                    useCustomDept = baseSlot.useCustomDept,
                    customDept = baseSlot.customDept,
                    courseName = nextUnused
                )
            )
        }
        while (courseSlots.size > clamped && courseSlots.size > 1) {
            courseSlots.removeAt(courseSlots.lastIndex)
        }
    }

    var uniDropdownOpen by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("Lecture Hall 1") }
    var instructor by remember { mutableStateOf("Class Rep / Lecturer") }
    var selectedType by remember { mutableStateOf(SessionType.SEMESTER_WEEKLY) }
    var totalWeeks by remember { mutableIntStateOf(14) }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val primaryColor = Color((0xFF000000L or (selectedUni.primaryColorHex and 0xFFFFFFL)))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            selectedUni.shortName.take(5).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Create & Customize Sheet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Select courses & departments for single or common classes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. University Selector
                Text("1. University / Institution", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    val newDepts = uni.departments
                                    val d0 = newDepts.firstOrNull()?.name ?: "Department of Computer Science"
                                    val c0 = newDepts.firstOrNull()?.courses?.firstOrNull()?.name ?: "Bachelor of Science in Computer Science"
                                    courseSlots.clear()
                                    courseSlots.add(CourseSlotState(department = d0, courseName = c0))
                                    uniDropdownOpen = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ Other / Custom University", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            onClick = {
                                useCustomUni = true
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

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Ask How Many Different Courses are in this Session
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "2. How many different courses are in this session?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Use 1 for a single class, or 2+ for common units (same or different departments)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { updateSlotCount(courseSlots.size - 1) },
                                enabled = courseSlots.size > 1,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("−", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    "${courseSlots.size} Course${if (courseSlots.size > 1) "s" else ""}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                            OutlinedButton(
                                onClick = { updateSlotCount(courseSlots.size + 1) },
                                enabled = courseSlots.size < 12,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Per-Course Department & Course Pickers
                courseSlots.forEachIndexed { index, slot ->
                    var deptMenuOpen by remember { mutableStateOf(false) }
                    var courseMenuOpen by remember { mutableStateOf(false) }

                    val resolvedSlotDept = if (slot.useCustomDept && slot.customDept.isNotBlank()) {
                        slot.customDept.trim()
                    } else {
                        slot.department
                    }

                    val slotCourses = remember(selectedUniId, resolvedSlotDept, customCourses, useCustomUni) {
                        val presets = allDepts.find { it.name.equals(resolvedSlotDept, ignoreCase = true) }?.courses ?: emptyList()
                        val customs = customCourses
                            .filter {
                                it.universityId == (if (useCustomUni) "custom" else selectedUniId) &&
                                    it.department.equals(resolvedSlotDept, ignoreCase = true)
                            }
                            .map { PresetCourse(it.courseCode, it.courseName) }
                        (presets + customs).distinctBy { it.name.lowercase() }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Course #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (index > 0) {
                                        TextButton(
                                            onClick = {
                                                val s0 = courseSlots[0]
                                                val s0Courses = allDepts.find { it.name.equals(s0.department, ignoreCase = true) }?.courses ?: emptyList()
                                                courseSlots[index] = slot.copy(
                                                    department = s0.department,
                                                    useCustomDept = s0.useCustomDept,
                                                    customDept = s0.customDept,
                                                    courseName = s0Courses.firstOrNull { c ->
                                                        !c.name.equals(s0.courseName, ignoreCase = true)
                                                    }?.name ?: slot.courseName
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                        ) {
                                            Text("Same Dept as #1", fontSize = 10.sp)
                                        }
                                        IconButton(
                                            onClick = { courseSlots.removeAt(index) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove Course",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Department dropdown
                            Box {
                                OutlinedTextField(
                                    value = if (slot.useCustomDept) "Custom Department" else slot.department,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Department", fontSize = 11.sp) },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { deptMenuOpen = true }
                                )
                                DropdownMenu(
                                    expanded = deptMenuOpen,
                                    onDismissRequest = { deptMenuOpen = false }
                                ) {
                                    allDepts.forEach { d ->
                                        DropdownMenuItem(
                                            text = { Text(d.name, fontSize = 12.sp) },
                                            onClick = {
                                                courseSlots[index] = slot.copy(
                                                    department = d.name,
                                                    useCustomDept = false,
                                                    courseName = d.courses.firstOrNull()?.name ?: slot.courseName
                                                )
                                                deptMenuOpen = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("+ Custom Department", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                        onClick = {
                                            courseSlots[index] = slot.copy(useCustomDept = true)
                                            deptMenuOpen = false
                                        }
                                    )
                                }
                            }

                            if (slot.useCustomDept) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = slot.customDept,
                                    onValueChange = { courseSlots[index] = slot.copy(customDept = it) },
                                    label = { Text("Enter Department Name", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Course dropdown
                            Box {
                                OutlinedTextField(
                                    value = slot.courseName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Course / Programme", fontSize = 11.sp) },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { courseMenuOpen = true }
                                )
                                DropdownMenu(
                                    expanded = courseMenuOpen,
                                    onDismissRequest = { courseMenuOpen = false }
                                ) {
                                    slotCourses.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.code} — ${c.name}", fontSize = 12.sp) },
                                            onClick = {
                                                courseSlots[index] = slot.copy(courseName = c.name)
                                                courseMenuOpen = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("+ Add Custom Course", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                        onClick = {
                                            courseSlots[index] = slot.copy(showCustomCourseBox = true)
                                            courseMenuOpen = false
                                        }
                                    )
                                }
                            }

                            TextButton(
                                onClick = { courseSlots[index] = slot.copy(showCustomCourseBox = !slot.showCustomCourseBox) },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    if (slot.showCustomCourseBox) "Hide Custom Course" else "+ Course not listed? Add Custom Course",
                                    fontSize = 11.sp
                                )
                            }

                            if (slot.showCustomCourseBox) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = slot.customCourseCode,
                                        onValueChange = { courseSlots[index] = slot.copy(customCourseCode = it.uppercase()) },
                                        label = { Text("Code", fontSize = 10.sp) },
                                        placeholder = { Text("E021") },
                                        modifier = Modifier.weight(0.32f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = slot.customCourseName,
                                        onValueChange = { courseSlots[index] = slot.copy(customCourseName = it) },
                                        label = { Text("Course Name", fontSize = 10.sp) },
                                        placeholder = { Text("Bachelor of Science in ...") },
                                        modifier = Modifier.weight(0.68f),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        if (slot.customCourseName.isNotBlank()) {
                                            val added = CustomCourse(
                                                universityId = if (useCustomUni) "custom" else selectedUniId,
                                                department = resolvedSlotDept,
                                                courseCode = slot.customCourseCode.ifBlank { "CUST" },
                                                courseName = slot.customCourseName.trim()
                                            )
                                            onAddCustomCourse(added)
                                            courseSlots[index] = slot.copy(
                                                courseName = added.courseName,
                                                showCustomCourseBox = false,
                                                customCourseCode = "",
                                                customCourseName = ""
                                            )
                                        }
                                    },
                                    enabled = slot.customCourseName.isNotBlank()
                                ) {
                                    Text("Use Custom Course", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Quick Common-Unit Add Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val base = courseSlots.first()
                            val deptCourses = allDepts.find { it.name.equals(base.department, ignoreCase = true) }?.courses ?: emptyList()
                            val nextUnused = deptCourses.firstOrNull { c ->
                                courseSlots.none { it.courseName.equals(c.name, ignoreCase = true) }
                            }?.name ?: base.courseName
                            courseSlots.add(
                                CourseSlotState(
                                    department = base.department,
                                    useCustomDept = base.useCustomDept,
                                    customDept = base.customDept,
                                    courseName = nextUnused
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("+ Add (Same Dept)", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            val usedDepts = courseSlots.map { it.department.lowercase() }.toSet()
                            val nextDept = allDepts.firstOrNull { it.name.lowercase() !in usedDepts } ?: allDepts.first()
                            courseSlots.add(
                                CourseSlotState(
                                    department = nextDept.name,
                                    courseName = nextDept.courses.firstOrNull()?.name ?: "Bachelor of Science in Computer Science"
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("+ Add (Other Dept)", fontSize = 11.sp)
                    }
                }

                if (courseSlots.size > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Common Class (${courseSlots.size} courses): Exports 1 separate signed sheet per course automatically.",
                            fontSize = 11.sp,
                            color = Color(0xFF0369A1),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "3. Sheet Occasion Type",
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
                    placeholder = { Text("e.g. Engineering Mathematics III") },
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

                            val resolvedDepts = courseSlots.map { s ->
                                if (s.useCustomDept && s.customDept.isNotBlank()) s.customDept.trim() else s.department
                            }.distinct()

                            val finalCourses = courseSlots.map { s ->
                                s.courseName.trim()
                            }.filter { it.isNotBlank() }.distinct()

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

                            val newSession = AttendanceSession(
                                title = finalTitle,
                                courseCode = finalCode,
                                universityId = finalUniId,
                                universityName = finalUniName,
                                department = resolvedDepts.joinToString(" / "),
                                allowedCourses = finalCourses.ifEmpty { listOf("General Programme") },
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
