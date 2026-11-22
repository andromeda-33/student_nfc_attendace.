package com.tapattend.nfc.ui

import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRosterScreen(
    students: List<Student>,
    customCourses: List<CustomCourse>,
    onBack: () -> Unit,
    onAddStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onImportCsv: (String, String, String, String, String) -> Unit,
    lastScannedUid: String?
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = students.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.regNumber.contains(searchQuery, ignoreCase = true) ||
            it.cardUid.contains(searchQuery, ignoreCase = true) ||
            it.courseName.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Directory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Import CSV Roster")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Student") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name, reg no, course, dept or UID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Saved: ${students.size} students",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (lastScannedUid != null) {
                    Text(
                        "Last Card: $lastScannedUid",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                items(filtered) { s ->
                    val hasCard = s.cardUid.isNotBlank()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (hasCard) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = if (hasCard) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "${s.regNumber} • ${s.courseName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${s.department} • ${s.universityName}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF0B6623)
                                )
                                if (hasCard) {
                                    Text(
                                        "UID: ${s.cardUid}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            IconButton(onClick = { onDeleteStudent(s) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudentDialog(
            prefilledCardUid = lastScannedUid ?: "",
            customCourses = customCourses,
            onDismiss = { showAddDialog = false },
            onAdd = {
                onAddStudent(it)
                showAddDialog = false
            }
        )
    }

    if (showImportDialog) {
        ImportRosterCsvDialog(
            onDismiss = { showImportDialog = false },
            onImport = { csv, dept, code, course ->
                onImportCsv(csv, KenyanUniversities.DEFAULT_UNIVERSITY_ID, dept, code, course)
                showImportDialog = false
            }
        )
    }
}

@Composable
fun AddStudentDialog(
    prefilledCardUid: String,
    customCourses: List<CustomCourse>,
    onDismiss: () -> Unit,
    onAdd: (Student) -> Unit
) {
    val defaultUni = KenyanUniversities.getById(KenyanUniversities.DEFAULT_UNIVERSITY_ID)
    var selectedUniId by remember { mutableStateOf(defaultUni.id) }
    val selectedUni = remember(selectedUniId) { KenyanUniversities.getById(selectedUniId) }
    val allDepts = remember(selectedUniId) {
        (selectedUni.departments + KenyanUniversities.allKenyanDepartments).distinctBy { it.name.lowercase() }
    }
    var selectedDept by remember(selectedUniId) {
        mutableStateOf(allDepts.firstOrNull()?.name ?: "Department of Computer Science")
    }

    val availableCourses = remember(selectedUniId, selectedDept, customCourses) {
        val presets = allDepts.find { it.name.equals(selectedDept, ignoreCase = true) }?.courses ?: emptyList()
        val customs = customCourses
            .filter { it.universityId == selectedUniId && it.department.equals(selectedDept, ignoreCase = true) }
            .map { PresetCourse(it.courseCode, it.courseName) }
        (presets + customs).distinctBy { it.name.lowercase() }
    }

    var selectedCourse by remember(availableCourses) {
        mutableStateOf(availableCourses.firstOrNull()?.name ?: "Bachelor of Science in Computer Science")
    }
    var selectedCourseCode by remember(availableCourses) {
        mutableStateOf(availableCourses.firstOrNull()?.code ?: "CSC")
    }

    var cardUid by remember { mutableStateOf(prefilledCardUid) }
    var regNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    var uniMenu by remember { mutableStateOf(false) }
    var deptMenu by remember { mutableStateOf(false) }
    var courseMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add Student to Directory", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Select University -> Department -> Course", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(10.dp))

                Box {
                    OutlinedTextField(
                        value = selectedUni.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("1. University") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { uniMenu = true })
                    DropdownMenu(expanded = uniMenu, onDismissRequest = { uniMenu = false }) {
                        KenyanUniversities.list.forEach { uni ->
                            DropdownMenuItem(
                                text = { Text("${uni.shortName} — ${uni.name}", fontSize = 12.sp) },
                                onClick = {
                                    selectedUniId = uni.id
                                    selectedDept = uni.departments.firstOrNull()?.name ?: "Department of Computer Science"
                                    uniMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = selectedDept,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("2. Department (before Course)") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { deptMenu = true })
                    DropdownMenu(expanded = deptMenu, onDismissRequest = { deptMenu = false }) {
                        allDepts.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.name, fontSize = 12.sp) },
                                onClick = {
                                    selectedDept = d.name
                                    deptMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = selectedCourse,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("3. Course / Programme") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { courseMenu = true })
                    DropdownMenu(expanded = courseMenu, onDismissRequest = { courseMenu = false }) {
                        availableCourses.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.code} — ${c.name}", fontSize = 12.sp) },
                                onClick = {
                                    selectedCourse = c.name
                                    selectedCourseCode = c.code
                                    courseMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cardUid,
                    onValueChange = { cardUid = it.uppercase() },
                    label = { Text("NFC Card UID (Hex, optional)") },
                    placeholder = { Text("e.g. 86A098BF") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it.uppercase() },
                    label = { Text("Registration Number *") },
                    placeholder = { Text("e.g. E021-01-1034/2024") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Joseph Karoki Mwangi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (regNumber.isNotBlank() && fullName.isNotBlank()) {
                                onAdd(
                                    Student(
                                        cardUid = cardUid.trim(),
                                        regNumber = regNumber.trim(),
                                        fullName = fullName.trim(),
                                        universityId = selectedUni.id,
                                        universityName = selectedUni.name,
                                        department = selectedDept,
                                        courseCode = selectedCourseCode,
                                        courseName = selectedCourse
                                    )
                                )
                            }
                        },
                        enabled = regNumber.isNotBlank() && fullName.isNotBlank()
                    ) {
                        Text("Save Student")
                    }
                }
            }
        }
    }
}

@Composable
fun ImportRosterCsvDialog(
    onDismiss: () -> Unit,
    onImport: (String, String, String, String) -> Unit
) {
    var defaultDept by remember { mutableStateOf("Department of Computer Science") }
    var defaultCourse by remember { mutableStateOf("Bachelor of Science in Computer Science") }
    var csvText by remember {
        mutableStateOf("C026-01-0101/2024, John Kamau, Department of Computer Science, Bachelor of Science in Computer Science\nE021-01-0102/2024, Alice Wanjiru, Department of Electrical & Electronic Engineering, Bachelor of Science in Electrical and Electronics Engineering")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Bulk Import Class List", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Format: RegNo, FullName, [Department], [Course], [CardUID]", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = defaultDept,
                    onValueChange = { defaultDept = it },
                    label = { Text("Default Department") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = defaultCourse,
                    onValueChange = { defaultCourse = it },
                    label = { Text("Default Course") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    label = { Text("CSV / Text Roster") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 12
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (csvText.isNotBlank()) {
                                onImport(csvText, defaultDept, "GEN", defaultCourse)
                            }
                        }
                    ) {
                        Text("Import Roster")
                    }
                }
            }
        }
    }
}
