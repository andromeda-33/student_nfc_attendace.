package com.tapattend.nfc.data

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class SessionType(val displayName: String, val description: String) {
    SEMESTER_WEEKLY("Semester Attendance (Weeks)", "Multi-week semester matrix with week columns"),
    EXAM("Exam / Test Attendance", "Single-session exam with seat, time, and anti-proxy attestation"),
    SUBMISSION("Assignment / Lab Submission", "Tracks submission timestamp, order #, and verification"),
    SINGLE_EVENT("Single Occasion / Seminar", "Quick one-time roll call for meetings, workshops or talks"),
    CUSTOM_SHEET("Custom Configured Sheet", "User-defined session columns, custom headers, and custom fields")
}

data class Student(
    val id: String = UUID.randomUUID().toString(),
    val cardUid: String = "",
    val regNumber: String,
    val fullName: String,
    val universityId: String = KenyanUniversities.DEFAULT_UNIVERSITY_ID,
    val universityName: String = KenyanUniversities.getById(KenyanUniversities.DEFAULT_UNIVERSITY_ID).name,
    val department: String = "Department of Computer Science",
    val courseCode: String = "C026",
    val courseName: String = "B.Sc. Computer Science",
    val email: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class CustomCourse(
    val id: String = UUID.randomUUID().toString(),
    val universityId: String,
    val department: String,
    val courseCode: String,
    val courseName: String
)

data class AttendanceSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val courseCode: String,
    val universityId: String = KenyanUniversities.DEFAULT_UNIVERSITY_ID,
    val universityName: String = KenyanUniversities.getById(KenyanUniversities.DEFAULT_UNIVERSITY_ID).name,
    val department: String = "Department of Computer Science",
    val allowedCourses: List<String> = listOf("B.Sc. Computer Science"),
    val sessionType: SessionType,
    val totalWeeks: Int = 14,
    val venue: String = "Lecture Hall 1",
    val instructorName: String = "Class Rep / Lecturer",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val customColumns: List<String> = emptyList(),
    val sessionColumnNames: List<String> = emptyList()
) {
    fun isMultiSession(): Boolean {
        return sessionType == SessionType.SEMESTER_WEEKLY ||
               (sessionType == SessionType.CUSTOM_SHEET && (sessionColumnNames.isNotEmpty() || totalWeeks > 1))
    }

    fun getColumnName(index: Int): String {
        return if (index - 1 in sessionColumnNames.indices) {
            sessionColumnNames[index - 1]
        } else {
            "Week $index"
        }
    }

    fun getTotalSessionsCount(): Int {
        return if (sessionColumnNames.isNotEmpty()) sessionColumnNames.size else totalWeeks
    }
}

data class AttendanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val studentCardUid: String,
    val regNumber: String,
    val studentName: String,
    val studentCourse: String = "B.Sc. Computer Science",
    val studentDepartment: String = "Department of Computer Science",
    val weekNumber: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PRESENT",
    val orderNumber: Int = 1,
    val attestationToken: String = generateAttestation(studentCardUid, sessionId, timestamp),
    val customValues: Map<String, String> = emptyMap()
) {
    companion object {
        fun generateAttestation(cardUid: String, sessionId: String, timestamp: Long): String {
            val raw = "$cardUid|$sessionId|$timestamp|NFC_TAP_ATTEND_SECURE"
            val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
            return digest.take(6).joinToString("") { "%02X".format(it.toInt() and 0xFF) }
        }
    }

    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getFormattedDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

data class StudentMatrixRow(
    val student: Student,
    val attendanceBySession: Map<Int, Boolean>,
    val totalPresent: Int,
    val totalSessionsHeld: Int,
    val attendancePercentage: Float
)

data class ParsedCardData(
    val cardUid: String,
    val ndefText: String? = null,
    val suggestedName: String? = null,
    val suggestedRegNumber: String? = null
)
