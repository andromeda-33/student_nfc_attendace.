package com.tapattend.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.lifecycleScope
import com.tapattend.nfc.data.*
import com.tapattend.nfc.nfc.NfcHelper
import com.tapattend.nfc.ui.*
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    LIVE_SCAN,
    SEMESTER_MATRIX,
    STUDENT_ROSTER
}

class MainActivity : ComponentActivity() {

    private lateinit var storage: AttendanceStorage
    private lateinit var nfcHelper: NfcHelper
    private lateinit var cloudSync: CloudDatabaseSync

    // Observable states for Compose
    private var currentScreen by mutableStateOf(AppScreen.HOME)
    private var activeSession by mutableStateOf<AttendanceSession?>(null)
    private var activeWeek by mutableIntStateOf(1)
    private var lastScannedUid by mutableStateOf<String?>(null)
    private var lastScanResult by mutableStateOf<ScanBannerState?>(null)
    private var pendingCardToEnroll by mutableStateOf<ParsedCardData?>(null)
    private var showCardRegisterDialog by mutableStateOf(false)
    private var pendingRegistrationCard by mutableStateOf<ParsedCardData?>(null)
    private var showCardInspector by mutableStateOf(false)
    private var inspectedCardData by mutableStateOf<ParsedCardData?>(null)
    private var showExportFormatDialog by mutableStateOf(false)
    private var isSyncing by mutableStateOf(false)
    private var refreshTrigger by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storage = AttendanceStorage(this)
        nfcHelper = NfcHelper(this)
        cloudSync = CloudDatabaseSync(this)

        if (!nfcHelper.isNfcSupported) {
            Toast.makeText(
                this,
                "Notice: NFC is not supported on this phone. Use 'Simulate Tap' or 'Manual Entry'.",
                Toast.LENGTH_LONG
            ).show()
        }

        setContent {
            TapAttendTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val students = remember(refreshTrigger) { storage.getStudents() }
                    val sessions = remember(refreshTrigger) { storage.getSessions() }
                    val records = remember(refreshTrigger) { storage.getRecords() }
                    val customCourses = remember(refreshTrigger) { storage.getCustomCourses() }

                    var showCreateDialog by remember { mutableStateOf(false) }

                    when (currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                sessions = sessions,
                                records = records,
                                studentCount = students.size,
                                isCloudConfigured = cloudSync.isConfigured(),
                                isSyncing = isSyncing,
                                lastSyncTime = cloudSync.getLastSyncTimeFormatted(),
                                currentSyncKey = cloudSync.getRefreshToken(),
                                onSelectSession = { session ->
                                    activeSession = session
                                    activeWeek = 1
                                    lastScanResult = null
                                    currentScreen = AppScreen.LIVE_SCAN
                                },
                                onCreateNewSession = { showCreateDialog = true },
                                onOpenRoster = { currentScreen = AppScreen.STUDENT_ROSTER },
                                onOpenCardRegistration = {
                                    pendingRegistrationCard = null
                                    showCardRegisterDialog = true
                                },
                                onOpenCardInspector = {
                                    inspectedCardData = null
                                    showCardInspector = true
                                },
                                onUpdateDatabase = {
                                    performDatabaseSync(
                                        activeSession?.universityId ?: KenyanUniversities.DEFAULT_UNIVERSITY_ID
                                    )
                                },
                                onSaveSyncKey = { key ->
                                    cloudSync.saveRefreshToken(key)
                                    refreshTrigger++
                                    Toast.makeText(this@MainActivity, "Cloud Sync Key saved", Toast.LENGTH_SHORT).show()
                                },
                                onDeleteSession = { session ->
                                    storage.deleteSession(session.id)
                                    refreshTrigger++
                                }
                            )

                            if (showCreateDialog) {
                                CreateSessionDialog(
                                    customCourses = customCourses,
                                    onAddCustomCourse = { course ->
                                        val saved = storage.addCustomCourse(course)
                                        refreshTrigger++
                                        lifecycleScope.launch {
                                            cloudSync.pushCustomCourse(saved)
                                        }
                                    },
                                    onDismiss = { showCreateDialog = false },
                                    onCreate = { newSession ->
                                        storage.addSession(newSession)
                                        showCreateDialog = false
                                        refreshTrigger++
                                        activeSession = newSession
                                        activeWeek = 1
                                        lastScanResult = null
                                        currentScreen = AppScreen.LIVE_SCAN
                                    }
                                )
                            }
                        }

                        AppScreen.LIVE_SCAN -> {
                            activeSession?.let { session ->
                                val sessionRecords = records.filter {
                                    it.sessionId == session.id &&
                                        (!session.isMultiSession() || it.weekNumber == activeWeek)
                                }

                                LiveScanScreen(
                                    session = session,
                                    records = sessionRecords,
                                    students = students,
                                    customCourses = customCourses,
                                    selectedWeek = activeWeek,
                                    isSyncing = isSyncing,
                                    onWeekChanged = { activeWeek = it },
                                    onBack = { currentScreen = AppScreen.HOME },
                                    onOpenMatrix = { currentScreen = AppScreen.SEMESTER_MATRIX },
                                    onUpdateDatabase = {
                                        performDatabaseSync(session.universityId)
                                    },
                                    onExport = { showExportFormatDialog = true },
                                    lastScannedResult = lastScanResult,
                                    onManualCheckIn = { student ->
                                        val weekNum = if (session.isMultiSession()) activeWeek else null
                                        val res = storage.manualCheckInStudent(session, student, weekNum)
                                        handleRecordResult(res, ParsedCardData(student.cardUid))
                                    },
                                    onSimulateScan = {
                                        if (students.isNotEmpty()) {
                                            val randomStudent = students.random()
                                            if (randomStudent.cardUid.isNotBlank()) {
                                                processCardUid(randomStudent.cardUid)
                                            } else {
                                                val fakeUid = "04%08X".format((0x10000000..0x7FFFFFFF).random())
                                                processCardData(ParsedCardData(fakeUid))
                                            }
                                        } else {
                                            val fakeUid = "04%08X".format((0x10000000..0x7FFFFFFF).random())
                                            processCardData(ParsedCardData(fakeUid))
                                        }
                                    },
                                    pendingCardToEnroll = pendingCardToEnroll,
                                    onDismissEnrollDialog = { pendingCardToEnroll = null },
                                    onSaveAndEnrollTap = { cardUid, existingId, reg, name, uniId, uniName, dept, crsCode, crsName, customVals ->
                                        val res = storage.enrollAndRecordTap(
                                            session = session,
                                            cardUid = cardUid,
                                            weekNumber = if (session.isMultiSession()) activeWeek else null,
                                            existingStudentIdToLink = existingId,
                                            newRegNumber = reg,
                                            newFullName = name,
                                            universityId = uniId,
                                            universityName = uniName,
                                            department = dept,
                                            courseCode = crsCode,
                                            courseName = crsName,
                                            customValues = customVals
                                        )
                                        handleRecordResult(res, ParsedCardData(cardUid))
                                        if (res is AttendanceStorage.RecordResult.Success) {
                                            lifecycleScope.launch {
                                                cloudSync.pushStudent(res.student)
                                            }
                                        }
                                    }
                                )

                                if (showExportFormatDialog) {
                                    val sheetCount = SheetExporter.getCourseGroups(session, sessionRecords).size
                                    ExportFormatChooserDialog(
                                        sheetCount = sheetCount,
                                        universityName = session.universityName,
                                        onDismiss = { showExportFormatDialog = false },
                                        onSelectFormat = { format ->
                                            showExportFormatDialog = false
                                            handleExportFormat(session, sessionRecords, format)
                                        }
                                    )
                                }
                            }
                        }

                        AppScreen.SEMESTER_MATRIX -> {
                            activeSession?.let { session ->
                                var selectedMatrixCourse by remember(session.id) {
                                    mutableStateOf(session.allowedCourses.firstOrNull())
                                }
                                val matrix = remember(refreshTrigger, session.id, selectedMatrixCourse) {
                                    storage.computeSemesterMatrix(session, selectedMatrixCourse)
                                }
                                SemesterMatrixScreen(
                                    session = session,
                                    matrix = matrix,
                                    selectedCourse = selectedMatrixCourse,
                                    onSelectCourse = { selectedMatrixCourse = it },
                                    totalWeeks = session.getTotalSessionsCount(),
                                    onBack = { currentScreen = AppScreen.LIVE_SCAN },
                                    onExportCsv = {
                                        val file = SheetExporter.exportSemesterMatrixCsv(
                                            this@MainActivity,
                                            session,
                                            matrix,
                                            selectedMatrixCourse
                                        )
                                        SheetExporter.shareFile(this@MainActivity, file)
                                    },
                                    onAddSessionColumn = { colName ->
                                        storage.addSessionColumn(session.id, colName)
                                        activeSession = storage.getSessions().find { it.id == session.id }
                                        refreshTrigger++
                                    }
                                )
                            }
                        }

                        AppScreen.STUDENT_ROSTER -> {
                            StudentRosterScreen(
                                students = students,
                                customCourses = customCourses,
                                isSyncing = isSyncing,
                                onBack = { currentScreen = AppScreen.HOME },
                                onUpdateDatabase = {
                                    performDatabaseSync(
                                        activeSession?.universityId ?: KenyanUniversities.DEFAULT_UNIVERSITY_ID
                                    )
                                },
                                onOpenCardRegistration = {
                                    pendingRegistrationCard = null
                                    showCardRegisterDialog = true
                                },
                                onAddStudent = { newStudent ->
                                    val saved = storage.addOrUpdateStudent(newStudent)
                                    refreshTrigger++
                                    lifecycleScope.launch {
                                        cloudSync.pushStudent(saved)
                                    }
                                    Toast.makeText(this@MainActivity, "Saved: ${saved.fullName}", Toast.LENGTH_SHORT).show()
                                },
                                onDeleteStudent = { student ->
                                    val updated = students.filter { it.id != student.id }
                                    storage.saveStudents(updated)
                                    refreshTrigger++
                                },
                                onImportCsv = { csvText, uniId, dept, code, course ->
                                    val count = storage.importStudentsFromCsv(csvText, uniId, dept, code, course)
                                    refreshTrigger++
                                    Toast.makeText(this@MainActivity, "Imported $count students!", Toast.LENGTH_LONG).show()
                                },
                                lastScannedUid = lastScannedUid
                            )
                        }
                    }

                    if (showCardRegisterDialog) {
                        CardRegistrationDialog(
                            capturedCardData = pendingRegistrationCard,
                            customCourses = customCourses,
                            isCloudConfigured = cloudSync.isConfigured(),
                            onSimulateCardUid = { fakeUid ->
                                nfcHelper.playSuccessFeedback()
                                pendingRegistrationCard = nfcHelper.parseTextIntoCardData(
                                    cardUid = fakeUid,
                                    rawText = null,
                                    memorySource = "Simulated UID Card"
                                )
                            },
                            onClearCardUid = { pendingRegistrationCard = null },
                            onAddCustomCourse = { course ->
                                val saved = storage.addCustomCourse(course)
                                refreshTrigger++
                                lifecycleScope.launch {
                                    cloudSync.pushCustomCourse(saved)
                                }
                            },
                            onSaveStudent = { student ->
                                val saved = storage.addOrUpdateStudent(student)
                                refreshTrigger++
                                pendingRegistrationCard = null
                                lifecycleScope.launch {
                                    val cloudRes = cloudSync.pushStudent(saved)
                                    val msg = if (cloudRes.isSuccess && cloudSync.isConfigured()) {
                                        "Saved & synced to Cloud DB: ${saved.fullName}"
                                    } else {
                                        "Saved locally: ${saved.fullName}"
                                    }
                                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDismiss = {
                                showCardRegisterDialog = false
                                pendingRegistrationCard = null
                            }
                        )
                    }

                    if (showCardInspector) {
                        CardInspectorDialog(
                            inspectedCard = inspectedCardData,
                            onSimulateLocalCard = {
                                val fakeUid = "04%08X".format((0x10000000..0x7FFFFFFF).random())
                                val sampleText = "Name: Faith Nyambura\nReg: C026-01-1142/2023\nDepartment: Department of Computer Science\nCourse: B.Sc. Computer Science"
                                inspectedCardData = nfcHelper.parseTextIntoCardData(
                                    cardUid = fakeUid,
                                    rawText = sampleText,
                                    techList = listOf("NfcA", "MifareUltralight", "Ndef"),
                                    memorySource = "Simulated NDEF Payload",
                                    rawDump = "Card UID: $fakeUid\nTechnologies: NfcA, MifareUltralight, Ndef\n\n$sampleText"
                                )
                                nfcHelper.playSuccessFeedback()
                            },
                            onClear = { inspectedCardData = null },
                            onSendToRegistration = { parsed ->
                                showCardInspector = false
                                pendingRegistrationCard = parsed
                                showCardRegisterDialog = true
                            },
                            onDismiss = {
                                showCardInspector = false
                                inspectedCardData = null
                            }
                        )
                    }
                }
            }
        }
    }

    private fun performDatabaseSync(universityId: String) {
        if (isSyncing) return
        isSyncing = true
        lifecycleScope.launch {
            val summary = cloudSync.syncRoster(universityId, storage)
            isSyncing = false
            refreshTrigger++
            Toast.makeText(this@MainActivity, summary.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun handleExportFormat(
        session: AttendanceSession,
        records: List<AttendanceRecord>,
        format: ExportFormat
    ) {
        val files = when (format) {
            ExportFormat.EXCEL -> SheetExporter.exportMultiCourseExcel(this, session, records)
            ExportFormat.PDF -> listOf(SheetExporter.exportPdf(this, session, records))
            ExportFormat.WORD -> SheetExporter.exportMultiCourseWord(this, session, records)
            ExportFormat.CSV -> SheetExporter.exportMultiCourseCsv(this, session, records)
        }
        SheetExporter.shareFiles(this, files, format.mimeType)
    }

    override fun onResume() {
        super.onResume()
        nfcHelper.enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcHelper.disableForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    @Suppress("DEPRECATION")
    private fun handleNfcIntent(intent: Intent) {
        val action = intent.action ?: return
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            } ?: return

            val parsedData = nfcHelper.parseStudentCard(tag, intent)
            lastScannedUid = parsedData.cardUid

            if (showCardInspector) {
                inspectedCardData = parsedData
                if (parsedData.hasLocalStudentData || !parsedData.ndefText.isNullOrBlank()) {
                    nfcHelper.playSuccessFeedback()
                    Toast.makeText(this, "Local card data read: ${parsedData.memorySource}", Toast.LENGTH_SHORT).show()
                } else {
                    nfcHelper.playDuplicateWarningFeedback()
                    Toast.makeText(this, "Card UID read (${parsedData.cardUid}) — memory locked/empty", Toast.LENGTH_SHORT).show()
                }
                return
            }

            if (showCardRegisterDialog) {
                nfcHelper.playSuccessFeedback()
                pendingRegistrationCard = parsedData
                Toast.makeText(this, "Card captured: ${parsedData.cardUid}", Toast.LENGTH_SHORT).show()
                return
            }

            processCardData(parsedData)
        }
    }

    private fun processCardUid(cardUid: String) {
        processCardData(ParsedCardData(cardUid))
    }

    private fun processCardData(cardData: ParsedCardData) {
        val session = activeSession
        val cardUid = cardData.cardUid

        if (session == null || currentScreen != AppScreen.LIVE_SCAN) {
            val summary = if (cardData.hasLocalStudentData) {
                "Read locally: ${cardData.suggestedName} (${cardData.suggestedRegNumber})"
            } else {
                "NFC Card Scanned: $cardUid"
            }
            Toast.makeText(this, summary, Toast.LENGTH_SHORT).show()
            return
        }

        val weekNum = if (session.isMultiSession()) activeWeek else null
        val result = storage.recordTapForSession(session, cardUid, weekNum)
        handleRecordResult(result, cardData)
    }

    private fun handleRecordResult(
        result: AttendanceStorage.RecordResult,
        cardData: ParsedCardData,
        allowAutoEnrollFromCard: Boolean = true
    ) {
        when (result) {
            is AttendanceStorage.RecordResult.Success -> {
                nfcHelper.playSuccessFeedback()
                lastScanResult = ScanBannerState("VERIFIED: ${result.student.fullName}", result.record)
                refreshTrigger++
            }
            is AttendanceStorage.RecordResult.Duplicate -> {
                nfcHelper.playDuplicateWarningFeedback()
                lastScanResult = ScanBannerState("ALREADY SCANNED TODAY", result.record)
                Toast.makeText(this, "${result.student.fullName} is already marked present!", Toast.LENGTH_SHORT).show()
            }
            is AttendanceStorage.RecordResult.WrongCourse -> {
                nfcHelper.playUnrecognizedStudentFeedback()
                lastScanResult = ScanBannerState(
                    status = "WRONG_COURSE: ${result.student.courseName}",
                    record = null,
                    blockedStudent = result.student
                )
                Toast.makeText(
                    this,
                    "Blocked: ${result.student.fullName} belongs to ${result.student.courseName}",
                    Toast.LENGTH_LONG
                ).show()
            }
            is AttendanceStorage.RecordResult.UnknownCard -> {
                val session = activeSession
                if (allowAutoEnrollFromCard && session != null && cardData.hasLocalStudentData) {
                    val weekNum = if (session.isMultiSession()) activeWeek else null
                    val autoRes = storage.enrollAndRecordTap(
                        session = session,
                        cardUid = cardData.cardUid,
                        weekNumber = weekNum,
                        newRegNumber = cardData.suggestedRegNumber,
                        newFullName = cardData.suggestedName,
                        universityId = session.universityId,
                        universityName = session.universityName,
                        department = cardData.suggestedDepartment ?: session.department,
                        courseCode = cardData.suggestedCourseCode ?: session.courseCode,
                        courseName = cardData.suggestedCourse ?: session.allowedCourses.firstOrNull() ?: "General Programme"
                    )
                    if (autoRes is AttendanceStorage.RecordResult.Success) {
                        lifecycleScope.launch { cloudSync.pushStudent(autoRes.student) }
                    }
                    handleRecordResult(autoRes, cardData, allowAutoEnrollFromCard = false)
                } else {
                    nfcHelper.playUnrecognizedStudentFeedback()
                    lastScanResult = ScanBannerState("UNKNOWN CARD: ${result.cardUid}", null)
                    pendingCardToEnroll = cardData
                }
            }
        }
    }
}

@Composable
fun ExportFormatChooserDialog(
    sheetCount: Int,
    universityName: String,
    onDismiss: () -> Unit,
    onSelectFormat: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Attendance Sheet(s)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Generates $sheetCount separate sheet${if (sheetCount > 1) "s" else ""} (1 per course) with $universityName crest logo:",
                    fontSize = 12.sp
                )

                ExportOptionItem(
                    title = "PDF Document (.pdf)",
                    subtitle = "$sheetCount A4 sheet${if (sheetCount > 1) "s" else ""} with university crest & signature lines",
                    icon = Icons.Default.Print,
                    color = Color(0xFFDC2626),
                    onClick = { onSelectFormat(ExportFormat.PDF) }
                )

                ExportOptionItem(
                    title = "Excel Spreadsheet (.xls)",
                    subtitle = "$sheetCount formatted file${if (sheetCount > 1) "s" else ""} (1 per course)",
                    icon = Icons.Default.DateRange,
                    color = Color(0xFF107C41),
                    onClick = { onSelectFormat(ExportFormat.EXCEL) }
                )

                ExportOptionItem(
                    title = "Microsoft Word (.doc)",
                    subtitle = "$sheetCount editable doc${if (sheetCount > 1) "s" else ""} with crest & approvals",
                    icon = Icons.Default.Description,
                    color = Color(0xFF185ABD),
                    onClick = { onSelectFormat(ExportFormat.WORD) }
                )

                ExportOptionItem(
                    title = "Standard CSV (.csv)",
                    subtitle = "$sheetCount comma-separated file${if (sheetCount > 1) "s" else ""}",
                    icon = Icons.Default.List,
                    color = Color(0xFF0284C7),
                    onClick = { onSelectFormat(ExportFormat.CSV) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ExportOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = color,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }
    }
}
