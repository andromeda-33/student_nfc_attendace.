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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapattend.nfc.data.*

@Composable
fun CardRegistrationDialog(
    capturedCardData: ParsedCardData?,
    customCourses: List<CustomCourse>,
    isCloudConfigured: Boolean,
    onSimulateCardUid: (String) -> Unit,
    onClearCardUid: () -> Unit,
    onAddCustomCourse: (CustomCourse) -> Unit,
    onSaveStudent: (Student) -> Unit,
    onDismiss: () -> Unit
) {
    val capturedCardUid = capturedCardData?.cardUid
    val defaultUni = KenyanUniversities.getById(KenyanUniversities.DEFAULT_UNIVERSITY_ID)

    var selectedUniId by remember { mutableStateOf(defaultUni.id) }
    var customUniName by remember { mutableStateOf("") }
    var useCustomUni by remember { mutableStateOf(false) }

    val selectedUni = remember(selectedUniId) { KenyanUniversities.getById(selectedUniId) }

    var selectedDept by remember(selectedUniId) {
        mutableStateOf(selectedUni.departments.firstOrNull()?.name ?: "Department of Computer Science")
    }
    var customDeptName by remember { mutableStateOf("") }
    var useCustomDept by remember { mutableStateOf(false) }

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

    var selectedCourseName by remember(availableCourses) {
        mutableStateOf(availableCourses.firstOrNull()?.name ?: "")
    }
    var selectedCourseCode by remember(availableCourses) {
        mutableStateOf(availableCourses.firstOrNull()?.code ?: "GEN")
    }

    var showAddCourseBox by remember { mutableStateOf(false) }
    var newCourseCode by remember { mutableStateOf("") }
    var newCourseName by remember { mutableStateOf("") }

    var regNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    LaunchedEffect(capturedCardData) {
        if (capturedCardData != null) {
            if (!capturedCardData.suggestedRegNumber.isNullOrBlank()) {
                regNumber = capturedCardData.suggestedRegNumber
            }
            if (!capturedCardData.suggestedName.isNullOrBlank()) {
                fullName = capturedCardData.suggestedName
            }
            if (!capturedCardData.suggestedDepartment.isNullOrBlank()) {
                val matchedDept = selectedUni.departments.find {
                    it.name.equals(capturedCardData.suggestedDepartment, ignoreCase = true)
                }
                if (matchedDept != null) {
                    useCustomDept = false
                    selectedDept = matchedDept.name
                } else {
                    useCustomDept = true
                    customDeptName = capturedCardData.suggestedDepartment
                }
            }
            if (!capturedCardData.suggestedCourse.isNullOrBlank()) {
                selectedCourseName = capturedCardData.suggestedCourse
                if (!capturedCardData.suggestedCourseCode.isNullOrBlank()) {
                    selectedCourseCode = capturedCardData.suggestedCourseCode
                }
            }
        }
    }

    var uniDropdownOpen by remember { mutableStateOf(false) }
    var deptDropdownOpen by remember { mutableStateOf(false) }
    var courseDropdownOpen by remember { mutableStateOf(false) }

    val hasCard = !capturedCardUid.isNullOrBlank()

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Tap & Register Card", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(
                                if (isCloudConfigured) "Saves locally & syncs to Cloud DB" else "Saves to local database",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // NFC Card Capture Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasCard) Color(0xFFECFDF5) else Color(0xFFE0F2FE),
                    border = BorderStroke(
                        1.5.dp,
                        if (hasCard) Color(0xFF10B981) else Color(0xFF0284C7)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (hasCard) Icons.Default.CheckCircle else Icons.Default.Contactless,
                                    contentDescription = null,
                                    tint = if (hasCard) Color(0xFF059669) else Color(0xFF0284C7),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        if (hasCard) {
                                            if (capturedCardData?.hasLocalStudentData == true) {
                                                "Card Read Locally (${capturedCardData.memorySource})"
                                            } else {
                                                "Card Captured! (${capturedCardData?.memorySource ?: "UID Only"})"
                                            }
                                        } else {
                                            "Bring Student Card Closer"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (hasCard) Color(0xFF065F46) else Color(0xFF0C4A6E)
                                    )
                                    Text(
                                        if (hasCard) "UID: $capturedCardUid" else "Hold NFC student ID card to back of phone",
                                        fontSize = 11.sp,
                                        fontFamily = if (hasCard) FontFamily.Monospace else FontFamily.Default,
                                        color = if (hasCard) Color(0xFF047857) else Color(0xFF0369A1)
                                    )
                                }
                            }
                            if (hasCard) {
                                TextButton(onClick = onClearCardUid) {
                                    Text("Re-tap", fontSize = 11.sp)
                                }
                            }
                        }
                        if (!hasCard) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    val fakeUid = "04%08X".format((0x10000000..0x7FFFFFFF).random())
                                    onSimulateCardUid(fakeUid)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simulate Card Tap (Test)", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Toggled Registration Form once NFC card is detected
                if (hasCard) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. University Selector (default: DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY)
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
                                        useCustomDept = false
                                        selectedDept = uni.departments.firstOrNull()?.name ?: "General Department"
                                        uniDropdownOpen = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Other / Custom University", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                onClick = {
                                    useCustomUni = true
                                    useCustomDept = true
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

                    // 2. Department Selector (BEFORE Course as required)
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
                                            deptDropdownOpen = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("+ Other / Custom Department", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    onClick = {
                                        useCustomDept = true
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

                    // 3. Course Selector + Inline Custom Course Creator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("3. Course / Programme", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = { showAddCourseBox = !showAddCourseBox },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(if (showAddCourseBox) "Hide" else "+ Add Custom Course", fontSize = 11.sp)
                        }
                    }

                    if (showAddCourseBox) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Add Course to Department", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
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
                                            selectedCourseName = added.courseName
                                            selectedCourseCode = added.courseCode
                                            newCourseCode = ""
                                            newCourseName = ""
                                            showAddCourseBox = false
                                        }
                                    },
                                    enabled = newCourseName.isNotBlank(),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Save Course", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    if (availableCourses.isNotEmpty()) {
                        Box {
                            OutlinedTextField(
                                value = selectedCourseName.ifBlank { availableCourses.first().name },
                                onValueChange = {},
                                readOnly = true,
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
                                availableCourses.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.code} — ${c.name}", fontSize = 12.sp) },
                                        onClick = {
                                            selectedCourseName = c.name
                                            selectedCourseCode = c.code
                                            courseDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = selectedCourseName,
                            onValueChange = { selectedCourseName = it },
                            label = { Text("Course / Programme Name") },
                            placeholder = { Text("e.g. B.Sc. Computer Science") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Registration Number & Full Name
                    OutlinedTextField(
                        value = regNumber,
                        onValueChange = { regNumber = it.uppercase() },
                        label = { Text("4. Registration Number") },
                        placeholder = { Text("e.g. C026-01-0123/2024") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("5. Student Full Name") },
                        placeholder = { Text("e.g. Jane Wambui") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Close") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val finalUniName = if (useCustomUni && customUniName.isNotBlank()) {
                                    customUniName.trim()
                                } else {
                                    selectedUni.name
                                }
                                val finalUniId = if (useCustomUni) "custom" else selectedUni.id
                                val finalCourse = selectedCourseName.trim().ifBlank { "General Programme" }
                                val student = Student(
                                    cardUid = capturedCardUid ?: "",
                                    regNumber = regNumber.trim(),
                                    fullName = fullName.trim(),
                                    universityId = finalUniId,
                                    universityName = finalUniName,
                                    department = activeDeptName,
                                    courseCode = selectedCourseCode.ifBlank { "GEN" },
                                    courseName = finalCourse
                                )
                                onSaveStudent(student)
                                regNumber = ""
                                fullName = ""
                            },
                            enabled = regNumber.isNotBlank() && fullName.isNotBlank() && selectedCourseName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to DB")
                        }
                    }
                }
            }
        }
    }
}
