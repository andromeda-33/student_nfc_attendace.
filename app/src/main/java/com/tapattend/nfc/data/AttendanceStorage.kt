package com.tapattend.nfc.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AttendanceStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tapattend_prefs_v2", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val KEY_STUDENTS = "key_students_v2"
    private val KEY_SESSIONS = "key_sessions_v2"
    private val KEY_RECORDS = "key_records_v2"
    private val KEY_CUSTOM_COURSES = "key_custom_courses_v2"

    init {
        val dekut = KenyanUniversities.getById(KenyanUniversities.DEFAULT_UNIVERSITY_ID)

        if (getStudents().isEmpty()) {
            val samples = listOf(
                Student(
                    id = "stu-dekut-1",
                    cardUid = "04A23B5C89",
                    regNumber = "C026-01-0901/2023",
                    fullName = "Alexander Kamau",
                    universityId = dekut.id,
                    universityName = dekut.name,
                    department = "Department of Computer Science",
                    courseCode = "C026",
                    courseName = "B.Sc. Computer Science"
                ),
                Student(
                    id = "stu-dekut-2",
                    cardUid = "04F18290EA",
                    regNumber = "C026-01-0914/2023",
                    fullName = "Beatrice Mwangi",
                    universityId = dekut.id,
                    universityName = dekut.name,
                    department = "Department of Computer Science",
                    courseCode = "C026",
                    courseName = "B.Sc. Computer Science"
                ),
                Student(
                    id = "stu-dekut-3",
                    cardUid = "04C398110D",
                    regNumber = "C027-01-0427/2023",
                    fullName = "Brian Kipchoge",
                    universityId = dekut.id,
                    universityName = dekut.name,
                    department = "Department of Information Technology",
                    courseCode = "C027",
                    courseName = "B.Sc. Information Technology"
                ),
                Student(
                    id = "stu-dekut-4",
                    cardUid = "04561234AB",
                    regNumber = "C027-01-0433/2023",
                    fullName = "Faith Wanjiku",
                    universityId = dekut.id,
                    universityName = dekut.name,
                    department = "Department of Information Technology",
                    courseCode = "C027",
                    courseName = "B.Sc. Information Technology"
                ),
                Student(
                    id = "stu-dekut-5",
                    cardUid = "047890CDEF",
                    regNumber = "E022-01-0148/2023",
                    fullName = "Daniel Omondi",
                    universityId = dekut.id,
                    universityName = dekut.name,
                    department = "Department of Mechatronic Engineering",
                    courseCode = "E022",
                    courseName = "B.Sc. Mechatronic Engineering"
                ),
                Student(
                    id = "stu-dekut-6",
                    cardUid = "0499AA1234",
                    regNumber = "C029-01-0255/2023",
                    fullName = "Grace Achieng",
                    universityId = dekut.id,
                    universityName = dekut.name,
                    department = "Department of Computer Science",
                    courseCode = "C029",
                    courseName = "B.Sc. Software Engineering"
                )
            )
            saveStudents(samples)
        }

        if (getSessions().isEmpty()) {
            val commonUnitSession = AttendanceSession(
                id = "sess-ccs2101",
                title = "CCS 2101: Data Structures & Algorithms (Common Unit)",
                courseCode = "CCS 2101",
                universityId = dekut.id,
                universityName = dekut.name,
                department = "Department of Computer Science",
                allowedCourses = listOf(
                    "B.Sc. Computer Science",
                    "B.Sc. Information Technology"
                ),
                sessionType = SessionType.SEMESTER_WEEKLY,
                totalWeeks = 14,
                venue = "Resource Centre Hall 2",
                instructorName = "Dr. Mwangi / Class Rep",
                sessionColumnNames = (1..14).map { "Week $it" }
            )
            val examSession = AttendanceSession(
                id = "sess-igs1102",
                title = "IGS 1102: Communication Skills Exam",
                courseCode = "IGS 1102",
                universityId = dekut.id,
                universityName = dekut.name,
                department = "Department of Computer Science",
                allowedCourses = listOf("B.Sc. Computer Science"),
                sessionType = SessionType.EXAM,
                totalWeeks = 1,
                venue = "Freedom Hall",
                instructorName = "Prof. Njoroge",
                customColumns = listOf("Seat #")
            )
            saveSessions(listOf(commonUnitSession, examSession))
        }
    }

    fun normalizeUid(raw: String): String {
        return raw.replace(":", "").replace(" ", "").trim().uppercase()
    }

    // --- Students ---
    fun getStudents(): List<Student> {
        val json = prefs.getString(KEY_STUDENTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Student>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @Synchronized
    fun saveStudents(students: List<Student>) {
        prefs.edit().putString(KEY_STUDENTS, gson.toJson(students)).apply()
    }

    fun findStudentByCardUid(cardUid: String): Student? {
        val normalized = normalizeUid(cardUid)
        if (normalized.isBlank()) return null
        return getStudents().find { normalizeUid(it.cardUid) == normalized }
    }

    fun addOrUpdateStudent(student: Student): Student {
        val students = getStudents().toMutableList()
        val cleanUid = normalizeUid(student.cardUid)
        val idx = students.indexOfFirst {
            it.id == student.id ||
                (cleanUid.isNotBlank() && normalizeUid(it.cardUid) == cleanUid) ||
                it.regNumber.equals(student.regNumber.trim(), ignoreCase = true)
        }
        val cleanStudent = student.copy(
            id = if (idx >= 0) students[idx].id else student.id,
            cardUid = cleanUid,
            regNumber = student.regNumber.trim().uppercase(),
            fullName = student.fullName.trim(),
            department = student.department.trim().ifBlank { "Department of Computer Science" },
            courseCode = student.courseCode.trim().uppercase().ifBlank { "GEN" },
            courseName = student.courseName.trim().ifBlank { "General Programme" },
            updatedAt = System.currentTimeMillis()
        )
        if (idx >= 0) {
            students[idx] = cleanStudent
        } else {
            students.add(cleanStudent)
        }
        saveStudents(students)
        return cleanStudent
    }

    @Synchronized
    fun mergeCloudStudents(cloudStudents: List<Student>): Int {
        val local = getStudents().toMutableList()
        var added = 0
        for (cs in cloudStudents) {
            val cleanUid = normalizeUid(cs.cardUid)
            val idx = local.indexOfFirst { l ->
                l.id == cs.id ||
                    (cleanUid.isNotBlank() && normalizeUid(l.cardUid) == cleanUid) ||
                    l.regNumber.equals(cs.regNumber.trim(), ignoreCase = true)
            }
            val cleaned = cs.copy(
                id = if (idx >= 0) local[idx].id else cs.id,
                cardUid = cleanUid
            )
            if (idx < 0) {
                local.add(cleaned)
                added++
            } else if (cs.updatedAt >= local[idx].updatedAt) {
                local[idx] = cleaned
            }
        }
        saveStudents(local)
        return added
    }

    fun importStudentsFromCsv(
        csvText: String,
        defaultUniversityId: String = KenyanUniversities.DEFAULT_UNIVERSITY_ID,
        defaultDepartment: String = "Department of Computer Science",
        defaultCourseCode: String = "C026",
        defaultCourseName: String = "B.Sc. Computer Science"
    ): Int {
        var count = 0
        val uni = KenyanUniversities.getById(defaultUniversityId)
        val lines = csvText.lines()
        val currentList = getStudents().toMutableList()

        for (line in lines) {
            val parts = line.split(",").map { it.trim().trim('"') }
            if (parts.size >= 2) {
                val reg = parts[0]
                val name = parts[1]
                val dept = if (parts.size >= 3 && parts[2].isNotBlank()) parts[2] else defaultDepartment
                val course = if (parts.size >= 4 && parts[3].isNotBlank()) parts[3] else defaultCourseName
                val uid = if (parts.size >= 5) normalizeUid(parts[4]) else ""

                if (reg.isNotBlank() && name.isNotBlank() && !reg.equals("Registration Number", ignoreCase = true)) {
                    val existingIdx = currentList.indexOfFirst { it.regNumber.equals(reg, ignoreCase = true) }
                    val newStudent = Student(
                        id = if (existingIdx >= 0) currentList[existingIdx].id else java.util.UUID.randomUUID().toString(),
                        cardUid = uid.ifBlank { if (existingIdx >= 0) currentList[existingIdx].cardUid else "" },
                        regNumber = reg.uppercase(),
                        fullName = name,
                        universityId = uni.id,
                        universityName = uni.name,
                        department = dept,
                        courseCode = defaultCourseCode,
                        courseName = course
                    )
                    if (existingIdx >= 0) {
                        currentList[existingIdx] = newStudent
                    } else {
                        currentList.add(newStudent)
                    }
                    count++
                }
            }
        }
        saveStudents(currentList)
        return count
    }

    // --- Custom Courses ---
    fun getCustomCourses(): List<CustomCourse> {
        val json = prefs.getString(KEY_CUSTOM_COURSES, null) ?: return emptyList()
        val type = object : TypeToken<List<CustomCourse>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @Synchronized
    fun saveCustomCourses(courses: List<CustomCourse>) {
        prefs.edit().putString(KEY_CUSTOM_COURSES, gson.toJson(courses)).apply()
    }

    fun addCustomCourse(course: CustomCourse): CustomCourse {
        val current = getCustomCourses().toMutableList()
        val existing = current.find {
            it.universityId == course.universityId &&
                it.department.equals(course.department.trim(), ignoreCase = true) &&
                it.courseName.equals(course.courseName.trim(), ignoreCase = true)
        }
        if (existing != null) return existing
        val clean = course.copy(
            department = course.department.trim(),
            courseCode = course.courseCode.trim().uppercase().ifBlank { "CUST" },
            courseName = course.courseName.trim()
        )
        current.add(clean)
        saveCustomCourses(current)
        return clean
    }

    // --- Sessions ---
    fun getSessions(): List<AttendanceSession> {
        val json = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<AttendanceSession>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @Synchronized
    fun saveSessions(sessions: List<AttendanceSession>) {
        prefs.edit().putString(KEY_SESSIONS, gson.toJson(sessions)).apply()
    }

    fun addSession(session: AttendanceSession) {
        val sessions = getSessions().toMutableList()
        sessions.add(0, session)
        saveSessions(sessions)
    }

    fun addSessionColumn(sessionId: String, columnName: String) {
        val sessions = getSessions().toMutableList()
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx >= 0) {
            val s = sessions[idx]
            val newColumns = s.sessionColumnNames.toMutableList()
            if (newColumns.isEmpty()) {
                for (w in 1..s.totalWeeks) newColumns.add("Week $w")
            }
            newColumns.add(columnName)
            sessions[idx] = s.copy(
                sessionColumnNames = newColumns,
                totalWeeks = newColumns.size
            )
            saveSessions(sessions)
        }
    }

    fun deleteSession(sessionId: String) {
        val sessions = getSessions().filter { it.id != sessionId }
        saveSessions(sessions)
        val records = getRecords().filter { it.sessionId != sessionId }
        saveRecords(records)
    }

    // --- Attendance Records ---
    fun getRecords(): List<AttendanceRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<AttendanceRecord>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @Synchronized
    fun saveRecords(records: List<AttendanceRecord>) {
        prefs.edit().putString(KEY_RECORDS, gson.toJson(records)).apply()
    }

    fun getRecordsForSession(sessionId: String, weekNumber: Int? = null): List<AttendanceRecord> {
        return getRecords().filter { record ->
            record.sessionId == sessionId && (weekNumber == null || record.weekNumber == weekNumber)
        }
    }

    sealed class RecordResult {
        data class Success(val record: AttendanceRecord, val student: Student) : RecordResult()
        data class Duplicate(val record: AttendanceRecord, val student: Student) : RecordResult()
        data class WrongCourse(val student: Student, val allowedCourses: List<String>) : RecordResult()
        data class UnknownCard(val cardUid: String) : RecordResult()
    }

    fun recordTap(
        sessionId: String,
        cardUid: String,
        weekNumber: Int? = null,
        customValues: Map<String, String> = emptyMap()
    ): RecordResult {
        val session = getSessions().find { it.id == sessionId }
        return if (session != null) {
            recordTapForSession(session, cardUid, weekNumber, customValues)
        } else {
            RecordResult.UnknownCard(normalizeUid(cardUid))
        }
    }

    /**
     * Records a card tap with course enforcement & duplicate protection
     */
    fun recordTapForSession(
        session: AttendanceSession,
        cardUid: String,
        weekNumber: Int? = null,
        customValues: Map<String, String> = emptyMap(),
        allowCourseOverride: Boolean = false
    ): RecordResult {
        val cleanUid = normalizeUid(cardUid)
        val student = findStudentByCardUid(cleanUid) ?: return RecordResult.UnknownCard(cleanUid)
        return markStudentPresent(session, student, cleanUid, weekNumber, customValues, allowCourseOverride)
    }

    fun manualCheckInStudent(
        session: AttendanceSession,
        student: Student,
        weekNumber: Int? = null,
        customValues: Map<String, String> = emptyMap()
    ): RecordResult {
        val effectiveUid = normalizeUid(student.cardUid).ifBlank { "MANUAL-${student.regNumber.uppercase()}" }
        return markStudentPresent(session, student, effectiveUid, weekNumber, customValues, allowCourseOverride = false)
    }

    private fun markStudentPresent(
        session: AttendanceSession,
        student: Student,
        effectiveUid: String,
        weekNumber: Int?,
        customValues: Map<String, String>,
        allowCourseOverride: Boolean
    ): RecordResult {
        if (!allowCourseOverride && session.allowedCourses.isNotEmpty()) {
            val isAllowed = session.allowedCourses.any {
                it.trim().equals(student.courseName.trim(), ignoreCase = true)
            }
            if (!isAllowed) {
                return RecordResult.WrongCourse(student, session.allowedCourses)
            }
        }

        val existingRecords = getRecordsForSession(session.id, weekNumber)
        val duplicate = existingRecords.find {
            normalizeUid(it.studentCardUid) == effectiveUid ||
                it.regNumber.equals(student.regNumber, ignoreCase = true)
        }
        if (duplicate != null) {
            return RecordResult.Duplicate(duplicate, student)
        }

        val allSessionRecords = getRecordsForSession(session.id)
        val newRecord = AttendanceRecord(
            sessionId = session.id,
            studentCardUid = effectiveUid,
            regNumber = student.regNumber,
            studentName = student.fullName,
            studentCourse = student.courseName,
            studentDepartment = student.department,
            weekNumber = weekNumber,
            timestamp = System.currentTimeMillis(),
            status = "PRESENT",
            orderNumber = allSessionRecords.size + 1,
            customValues = customValues
        )

        val allRecords = getRecords().toMutableList()
        allRecords.add(newRecord)
        saveRecords(allRecords)

        return RecordResult.Success(newRecord, student)
    }

    /**
     * Enrolls an unrecognized card directly on tap and marks attendance
     */
    fun enrollAndRecordTap(
        session: AttendanceSession,
        cardUid: String,
        weekNumber: Int? = null,
        existingStudentIdToLink: String? = null,
        newRegNumber: String? = null,
        newFullName: String? = null,
        universityId: String = session.universityId,
        universityName: String = session.universityName,
        department: String = session.department,
        courseCode: String = session.courseCode,
        courseName: String = session.allowedCourses.firstOrNull() ?: "General Programme",
        customValues: Map<String, String> = emptyMap()
    ): RecordResult {
        val cleanUid = normalizeUid(cardUid)

        if (existingStudentIdToLink != null) {
            val students = getStudents().toMutableList()
            val idx = students.indexOfFirst { it.id == existingStudentIdToLink }
            if (idx >= 0) {
                students[idx] = students[idx].copy(
                    cardUid = cleanUid,
                    updatedAt = System.currentTimeMillis()
                )
                saveStudents(students)
            }
        } else if (!newRegNumber.isNullOrBlank() && !newFullName.isNullOrBlank()) {
            addOrUpdateStudent(
                Student(
                    cardUid = cleanUid,
                    regNumber = newRegNumber.trim(),
                    fullName = newFullName.trim(),
                    universityId = universityId,
                    universityName = universityName,
                    department = department.trim(),
                    courseCode = courseCode.trim(),
                    courseName = courseName.trim()
                )
            )
        }

        return recordTapForSession(session, cleanUid, weekNumber, customValues)
    }

    /**
     * Computes multi-session/semester attendance matrix filtered by course (1 sheet per course)
     */
    fun computeSemesterMatrix(
        session: AttendanceSession,
        filterCourse: String? = null
    ): List<StudentMatrixRow> {
        val allStudents = getStudents().filter { s ->
            when {
                !filterCourse.isNullOrBlank() ->
                    s.courseName.trim().equals(filterCourse.trim(), ignoreCase = true)
                session.allowedCourses.isNotEmpty() ->
                    session.allowedCourses.any { it.trim().equals(s.courseName.trim(), ignoreCase = true) }
                else -> true
            }
        }
        val records = getRecordsForSession(session.id)
        val totalSessions = session.getTotalSessionsCount()

        val sessionsWithAttendance = records.mapNotNull { it.weekNumber }.toSet()
        val sessionsHeldCount = if (sessionsWithAttendance.isEmpty()) 1 else sessionsWithAttendance.size

        return allStudents.map { student ->
            val cleanUid = normalizeUid(student.cardUid)
            val studentRecords = records.filter {
                (cleanUid.isNotBlank() && normalizeUid(it.studentCardUid) == cleanUid) ||
                    it.regNumber.equals(student.regNumber, ignoreCase = true)
            }

            val attendanceBySession = mutableMapOf<Int, Boolean>()
            var presentCount = 0
            for (w in 1..totalSessions) {
                val present = studentRecords.any { it.weekNumber == w }
                attendanceBySession[w] = present
                if (present) presentCount++
            }

            val percentage = if (sessionsHeldCount > 0) {
                (presentCount.toFloat() / sessionsHeldCount.toFloat()) * 100f
            } else 0f

            StudentMatrixRow(
                student = student,
                attendanceBySession = attendanceBySession,
                totalPresent = presentCount,
                totalSessionsHeld = sessionsHeldCount,
                attendancePercentage = percentage
            )
        }
    }
}
