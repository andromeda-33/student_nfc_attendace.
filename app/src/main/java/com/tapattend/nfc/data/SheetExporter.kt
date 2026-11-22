package com.tapattend.nfc.data

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    EXCEL("Excel Spreadsheet (.xls)", "xls", "application/vnd.ms-excel"),
    PDF("PDF Document (.pdf)", "pdf", "application/pdf"),
    WORD("Microsoft Word (.doc)", "doc", "application/msword"),
    CSV("Standard CSV (.csv)", "csv", "text/csv")
}

data class CourseSheetGroup(
    val courseName: String,
    val records: List<AttendanceRecord>
)

object SheetExporter {

    /**
     * Strictly splits records into single-course groups so each printed/exported sheet
     * contains students of ONLY ONE course (even for Common Units).
     */
    fun getCourseGroups(
        session: AttendanceSession,
        records: List<AttendanceRecord>
    ): List<CourseSheetGroup> {
        val grouped = linkedMapOf<String, MutableList<AttendanceRecord>>()

        session.allowedCourses.forEach { course ->
            val trimmed = course.trim()
            if (trimmed.isNotEmpty()) {
                grouped.putIfAbsent(trimmed, mutableListOf())
            }
        }

        records.forEach { record ->
            val key = record.studentCourse.trim().ifBlank {
                session.allowedCourses.firstOrNull() ?: "General Programme"
            }
            val existingKey = grouped.keys.find {
                CourseMatcher.isCourseMatch(it, key)
            } ?: key
            grouped.getOrPut(existingKey) { mutableListOf() }.add(record)
        }

        if (grouped.isEmpty()) {
            grouped["General Programme"] = records.toMutableList()
        }

        return grouped.entries.map { (courseName, courseRecords) ->
            CourseSheetGroup(courseName = courseName, records = courseRecords)
        }
    }

    private fun safeSlug(text: String): String {
        return text.replace(Regex("[^a-zA-Z0-9]+"), "_").trim('_').take(36).ifBlank { "Course" }
    }

    /**
     * Generates one CSV file per course in the session
     */
    fun exportMultiCourseCsv(
        context: Context,
        session: AttendanceSession,
        records: List<AttendanceRecord>
    ): List<File> {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val groups = getCourseGroups(session, records)
        val uni = KenyanUniversities.getById(session.universityId)

        return groups.map { group ->
            val filename = "${safeSlug(session.courseCode)}_${safeSlug(group.courseName)}_Attendance.csv"
            val file = File(exportDir, filename)

            FileWriter(file).use { writer ->
                writer.append("${escapeCsv(session.universityName.ifBlank { uni.name })}\n")
                writer.append("${escapeCsv(session.department)}\n")
                writer.append("Course / Programme,${escapeCsv(group.courseName)}\n")
                writer.append("Session Title,${escapeCsv(session.title)}\n")
                writer.append("Course Code,${escapeCsv(session.courseCode)}\n")
                writer.append("Session Type,${escapeCsv(session.sessionType.displayName)}\n")
                writer.append("Venue,${escapeCsv(session.venue)}\n")
                writer.append("Instructor / Class Rep,${escapeCsv(session.instructorName)}\n")
                writer.append("Total Present (${escapeCsv(group.courseName)}),${group.records.size}\n")
                writer.append("Generated On,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

                writer.append("S/N,Registration Number,Full Name,Course,NFC Card UID,Time In,Status")
                session.customColumns.forEach { col -> writer.append(",${escapeCsv(col)}") }
                writer.append(",Digital Attestation (Signature Substitute)\n")

                group.records.forEachIndexed { index, record ->
                    writer.append("${index + 1},")
                    writer.append("${escapeCsv(record.regNumber)},")
                    writer.append("${escapeCsv(record.studentName)},")
                    writer.append("${escapeCsv(group.courseName)},")
                    writer.append("${record.studentCardUid},")
                    writer.append("${record.getFormattedTime()},")
                    writer.append("${record.status}")
                    session.customColumns.forEach { col ->
                        val customVal = record.customValues[col] ?: ""
                        writer.append(",${escapeCsv(customVal)}")
                    }
                    writer.append(",${record.attestationToken}\n")
                }
            }
            file
        }
    }

    /**
     * Generates a Multi-Session / Semester Attendance Matrix CSV (filtered to single course if given)
     */
    fun exportSemesterMatrixCsv(
        context: Context,
        session: AttendanceSession,
        matrix: List<StudentMatrixRow>,
        courseFilter: String? = null
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val courseLabel = courseFilter ?: session.allowedCourses.firstOrNull() ?: session.courseCode
        val filename = "${safeSlug(session.courseCode)}_${safeSlug(courseLabel)}_Matrix.csv"
        val file = File(exportDir, filename)
        val totalSessions = session.getTotalSessionsCount()

        FileWriter(file).use { writer ->
            writer.append("${escapeCsv(session.universityName)}\n")
            writer.append("${escapeCsv(session.department)}\n")
            writer.append("Course / Programme,${escapeCsv(courseLabel)}\n")
            writer.append("Unit,${escapeCsv(session.courseCode)} - ${escapeCsv(session.title)}\n")
            writer.append("Total Sessions / Columns,$totalSessions\n")
            writer.append("Report Date,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

            writer.append("S/N,Registration Number,Full Name")
            for (w in 1..totalSessions) {
                writer.append(",${escapeCsv(session.getColumnName(w))}")
            }
            writer.append(",Total Present,Total Held,Attendance %,Eligibility (>=75%)\n")

            matrix.forEachIndexed { index, row ->
                writer.append("${index + 1},")
                writer.append("${escapeCsv(row.student.regNumber)},")
                writer.append("${escapeCsv(row.student.fullName)}")

                for (w in 1..totalSessions) {
                    val present = row.attendanceBySession[w] ?: false
                    writer.append(if (present) ",P" else ",A")
                }

                val percentageStr = "%.1f%%".format(row.attendancePercentage)
                val eligible = if (row.attendancePercentage >= 75f) "ELIGIBLE" else "DEFICIT"
                writer.append(",${row.totalPresent},${row.totalSessionsHeld},$percentageStr,$eligible\n")
            }
        }
        return file
    }

    /**
     * Generates one Excel (.xls) file per course with University Header & Crest
     */
    fun exportMultiCourseExcel(
        context: Context,
        session: AttendanceSession,
        records: List<AttendanceRecord>
    ): List<File> {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val groups = getCourseGroups(session, records)
        val uni = KenyanUniversities.getById(session.universityId)
        val primaryHex = String.format("#%06X", (0xFFFFFF and uni.primaryColorHex.toInt()))

        return groups.map { group ->
            val filename = "${safeSlug(session.courseCode)}_${safeSlug(group.courseName)}_Attendance.xls"
            val file = File(exportDir, filename)

            val content = buildString {
                append(
                    """
                    <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
                    <head>
                        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                        <style>
                            td, th { font-family: Calibri, Arial; font-size: 11pt; }
                            .uni { background-color: $primaryHex; color: #FFFFFF; font-weight: bold; font-size: 13pt; text-align: center; }
                            .hdr { background-color: #0284C7; color: #FFFFFF; font-weight: bold; text-align: center; }
                            .subhdr { background-color: #F1F5F9; font-weight: bold; }
                            .meta { font-weight: bold; background-color: #E0F2FE; }
                        </style>
                    </head>
                    <body>
                        <table border="1" cellspacing="0" cellpadding="4">
                            <tr><td colspan="7" class="uni">${session.universityName.ifBlank { uni.name }}</td></tr>
                            <tr><td colspan="7" class="hdr">${session.department} — ${group.courseName}</td></tr>
                            <tr><td colspan="2" class="meta">Unit / Session:</td><td colspan="5">${session.courseCode}: ${session.title}</td></tr>
                            <tr><td colspan="2" class="meta">Venue &amp; Instructor:</td><td colspan="5">${session.venue} &bull; ${session.instructorName}</td></tr>
                            <tr><td colspan="2" class="meta">Total Present:</td><td colspan="5">${group.records.size} Students (${group.courseName})</td></tr>
                            <tr><td colspan="2" class="meta">Date:</td><td colspan="5">${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}</td></tr>
                            <tr><td colspan="7"></td></tr>
                            <tr class="subhdr">
                                <th>S/N</th>
                                <th>Registration No.</th>
                                <th>Student Name</th>
                                <th>NFC Card UID</th>
                                <th>Time In</th>
                                <th>Status</th>
                                <th>Digital Attestation</th>
                            </tr>
                    """.trimIndent()
                )

                group.records.forEachIndexed { idx, r ->
                    append(
                        """
                        <tr>
                            <td align="center">${idx + 1}</td>
                            <td><b>${r.regNumber}</b></td>
                            <td>${r.studentName}</td>
                            <td align="center">${r.studentCardUid}</td>
                            <td align="center">${r.getFormattedTime()}</td>
                            <td align="center" style="color:#059669; font-weight:bold;">${r.status}</td>
                            <td align="center" style="font-family:Courier New;">${r.attestationToken}</td>
                        </tr>
                        """.trimIndent()
                    )
                }

                append("</table></body></html>")
            }

            FileWriter(file).use { it.write(content) }
            file
        }
    }

    /**
     * Generates one Microsoft Word (.doc) file per course with University crest logo & formal approvals
     */
    fun exportMultiCourseWord(
        context: Context,
        session: AttendanceSession,
        records: List<AttendanceRecord>
    ): List<File> {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val groups = getCourseGroups(session, records)
        val uni = KenyanUniversities.getById(session.universityId)
        val primaryHex = String.format("#%06X", (0xFFFFFF and uni.primaryColorHex.toInt()))
        val crestSvg = KenyanUniversities.buildCrestSvg(uni, 56)

        return groups.map { group ->
            val filename = "${safeSlug(session.courseCode)}_${safeSlug(group.courseName)}_Attendance.doc"
            val file = File(exportDir, filename)

            val content = buildString {
                append(
                    """
                    <html xmlns:w="urn:schemas-microsoft-com:office:word" xmlns="http://www.w3.org/TR/REC-html40">
                    <head>
                        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                        <title>${session.courseCode} - ${group.courseName}</title>
                        <style>
                            body { font-family: 'Calibri', 'Times New Roman', serif; font-size: 11pt; margin: 0.8in; }
                            table { width: 100%; border-collapse: collapse; margin-top: 12pt; }
                            th, td { border: 1pt solid #CBD5E1; padding: 6pt; font-size: 10pt; }
                            th { background-color: #F8FAFC; color: #0F172A; text-align: left; }
                        </style>
                    </head>
                    <body>
                        <table style="border:none; border-bottom: 2pt solid $primaryHex; margin-top:0;">
                            <tr style="border:none;">
                                <td style="border:none; width:64pt; vertical-align:middle;">$crestSvg</td>
                                <td style="border:none; vertical-align:middle;">
                                    <div style="font-size:15pt; font-weight:bold; color:$primaryHex;">${session.universityName.ifBlank { uni.name }}</div>
                                    <div style="font-size:11pt; font-weight:bold; color:#334155;">${session.department}</div>
                                    <div style="font-size:10pt; color:#475569;">Programme / Course: <strong>${group.courseName}</strong></div>
                                </td>
                            </tr>
                        </table>

                        <p style="margin-top:12pt;">
                            <strong style="color:#0284C7; font-size:13pt;">${session.courseCode}: ${session.title}</strong><br>
                            Venue: ${session.venue} &nbsp;|&nbsp; Lecturer / Rep: ${session.instructorName}<br>
                            <strong>Students Present:</strong> ${group.records.size} &nbsp;|&nbsp;
                            <strong>Date:</strong> ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())}
                        </p>

                        <table>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Registration No.</th>
                                    <th>Student Name</th>
                                    <th>Time In</th>
                                    <th>Status</th>
                                    <th>Digital Attestation</th>
                                </tr>
                            </thead>
                            <tbody>
                    """.trimIndent()
                )

                group.records.forEachIndexed { idx, r ->
                    append(
                        """
                        <tr>
                            <td>${idx + 1}</td>
                            <td><b>${r.regNumber}</b></td>
                            <td>${r.studentName}</td>
                            <td>${r.getFormattedTime()}</td>
                            <td style="color:#15803D; font-weight:bold;">${r.status}</td>
                            <td style="font-family:Courier New; font-size:9pt;">SHA256:${r.attestationToken}</td>
                        </tr>
                        """.trimIndent()
                    )
                }

                append(
                    """
                            </tbody>
                        </table>

                        <div style="margin-top: 40pt;">
                            <table style="border:none;">
                                <tr style="border:none;">
                                    <td style="border:none; width:50%; padding-right:20pt;">
                                        <div style="border-top: 1pt solid #000; padding-top: 4pt;">
                                            <strong>Class Representative / Invigilator:</strong><br>
                                            Signature: __________________________<br>
                                            Date: ______________________________
                                        </div>
                                    </td>
                                    <td style="border:none; width:50%; padding-left:20pt;">
                                        <div style="border-top: 1pt solid #000; padding-top: 4pt;">
                                            <strong>Course Lecturer / Department Head:</strong><br>
                                            Signature: __________________________<br>
                                            Date: ______________________________
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </body>
                    </html>
                    """.trimIndent()
                )
            }

            FileWriter(file).use { it.write(content) }
            file
        }
    }

    /**
     * Generates a multi-page A4 PDF where EACH course gets its own dedicated sheet(s)
     * complete with the University Crest Logo, University Name, Department, and Signature blocks.
     */
    fun exportPdf(
        context: Context,
        session: AttendanceSession,
        records: List<AttendanceRecord>
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val filename = "${safeSlug(session.courseCode)}_Attendance_Sheets.pdf"
        val file = File(exportDir, filename)

        val groups = getCourseGroups(session, records)
        val uni = KenyanUniversities.getById(session.universityId)
        val uniPrimaryColor = (0xFF000000L or (uni.primaryColorHex and 0xFFFFFFL)).toInt()
        val uniAccentColor = (0xFF000000L or (uni.accentColorHex and 0xFFFFFFL)).toInt()

        val document = PdfDocument()
        val rowsPerPage = 24
        var pageNumber = 1

        val uniPaint = Paint().apply {
            color = uniPrimaryColor
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 12.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val deptPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.8f
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.8f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }
        val accentLinePaint = Paint().apply {
            color = uniPrimaryColor
            strokeWidth = 2f
        }
        val headerBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
        }
        val crestFillPaint = Paint().apply {
            color = uniPrimaryColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val crestStrokePaint = Paint().apply {
            color = uniAccentColor
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        val crestAccentPaint = Paint().apply {
            color = uniAccentColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val crestSecondaryPaint = Paint().apply {
            color = (0xFF000000L or (uni.secondaryColorHex and 0xFFFFFFL)).toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val crestWhitePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val crestBannerTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = if (uni.shortName.length > 4) 6.5f else 7.8f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        groups.forEachIndexed { groupIdx, group ->
            val chunks = if (group.records.isEmpty()) listOf(emptyList()) else group.records.chunked(rowsPerPage)

            chunks.forEachIndexed { chunkIdx, chunkRecords ->
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber++).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Draw Detailed Vector University Crest Emblem on top-left (center x=62, y=26..78)
                val shieldPath = Path().apply {
                    moveTo(62f, 26f)
                    lineTo(84f, 33f)
                    lineTo(84f, 52f)
                    quadTo(84f, 67f, 62f, 76f)
                    quadTo(40f, 67f, 40f, 52f)
                    lineTo(40f, 33f)
                    close()
                }
                canvas.drawPath(shieldPath, crestFillPaint)
                canvas.drawPath(shieldPath, crestStrokePaint)

                if (uni.id == "dekut") {
                    // Rising Sun + Twin Mt. Kenya Peaks
                    canvas.drawCircle(62f, 39f, 4.5f, crestAccentPaint)
                    val peaksPath = Path().apply {
                        moveTo(45f, 51f)
                        lineTo(55f, 39f)
                        lineTo(61f, 45f)
                        lineTo(68f, 38f)
                        lineTo(79f, 51f)
                        close()
                    }
                    canvas.drawPath(peaksPath, crestWhitePaint)

                    // 8-tooth Engineering Cogwheel
                    canvas.save()
                    canvas.translate(62f, 55.5f)
                    for (angle in listOf(0f, 45f, 90f, 135f)) {
                        canvas.save()
                        canvas.rotate(angle)
                        canvas.drawRect(-1.5f, -7.2f, 1.5f, 7.2f, crestAccentPaint)
                        canvas.restore()
                    }
                    canvas.drawCircle(0f, 0f, 5.2f, crestSecondaryPaint)
                    canvas.drawCircle(0f, 0f, 2.1f, crestWhitePaint)
                    canvas.restore()
                } else {
                    // Open Book + Torch Crest for Other Kenyan Universities
                    canvas.drawCircle(62f, 36f, 2.5f, crestAccentPaint)
                    val bookPath = Path().apply {
                        moveTo(50f, 44f)
                        quadTo(56f, 41f, 62f, 44f)
                        quadTo(68f, 41f, 74f, 44f)
                        lineTo(74f, 53f)
                        quadTo(68f, 50f, 62f, 53f)
                        quadTo(56f, 50f, 50f, 53f)
                        close()
                    }
                    canvas.drawPath(bookPath, crestWhitePaint)
                    canvas.drawLine(62f, 44f, 62f, 53f, crestFillPaint)
                }

                // Bottom Gold Scroll Banner with University Short Code
                canvas.drawRect(44f, 63.5f, 80f, 73f, crestAccentPaint)
                canvas.drawText(uni.shortName.take(6).uppercase(), 62f, 70.8f, crestBannerTextPaint)

                // Draw University Name, Department & Single Course Name
                val displayUniName = session.universityName.ifBlank { uni.name }
                canvas.drawText(displayUniName.take(54), 96f, 42f, uniPaint)
                canvas.drawText(session.department.take(64), 96f, 56f, deptPaint)
                canvas.drawText("Course Sheet: ${group.courseName}", 96f, 70f, boldPaint)
                canvas.drawText("Sheet ${groupIdx + 1}/${groups.size}", 510f, 42f, textPaint)

                canvas.drawLine(40f, 84f, 555f, 84f, accentLinePaint)

                var y = 104f
                canvas.drawText("${session.courseCode}: ${session.title}".take(68), 40f, y, titlePaint)
                y += 15f
                canvas.drawText(
                    "Venue: ${session.venue}  |  Lecturer/Rep: ${session.instructorName}  |  Present: ${group.records.size}",
                    40f,
                    y,
                    textPaint
                )
                y += 13f
                canvas.drawText("Generated: $dateStr", 40f, y, textPaint)
                y += 16f

                // Table Header
                canvas.drawRect(40f, y, 555f, y + 20f, headerBgPaint)
                canvas.drawText("S/N", 46f, y + 13f, boldPaint)
                canvas.drawText("Reg Number", 76f, y + 13f, boldPaint)
                canvas.drawText("Student Name", 190f, y + 13f, boldPaint)
                canvas.drawText("Time In", 355f, y + 13f, boldPaint)
                canvas.drawText("Attestation", 415f, y + 13f, boldPaint)
                canvas.drawText("Status", 505f, y + 13f, boldPaint)
                y += 24f

                if (chunkRecords.isEmpty()) {
                    canvas.drawText("No students recorded for ${group.courseName} in this session.", 46f, y + 12f, textPaint)
                } else {
                    val baseOffset = chunkIdx * rowsPerPage
                    chunkRecords.forEachIndexed { idx, r ->
                        canvas.drawText("${baseOffset + idx + 1}", 46f, y + 10f, textPaint)
                        canvas.drawText(r.regNumber.take(19), 76f, y + 10f, boldPaint)
                        canvas.drawText(r.studentName.take(25), 190f, y + 10f, textPaint)
                        canvas.drawText(r.getFormattedTime(), 355f, y + 10f, textPaint)
                        canvas.drawText(r.attestationToken.take(12), 415f, y + 10f, textPaint)
                        canvas.drawText(r.status, 505f, y + 10f, boldPaint)
                        canvas.drawLine(40f, y + 15f, 555f, y + 15f, linePaint)
                        y += 20f
                    }
                }

                // Signatures at bottom
                val sigY = 765f
                canvas.drawLine(40f, sigY, 225f, sigY, linePaint)
                canvas.drawText("Class Rep / Invigilator Signature", 40f, sigY + 12f, textPaint)

                canvas.drawLine(340f, sigY, 530f, sigY, linePaint)
                canvas.drawText("Lecturer / HOD Approval", 340f, sigY + 12f, textPaint)

                document.finishPage(page)
            }
        }

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    /**
     * Shares one or multiple exported course files via Android Share Sheet
     */
    fun shareFiles(context: Context, files: List<File>, mimeType: String = "text/csv") {
        if (files.isEmpty()) return
        if (files.size == 1) {
            shareFile(context, files.first(), mimeType)
            return
        }

        val uris = ArrayList<Uri>(
            files.map { file ->
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        )
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            putExtra(Intent.EXTRA_SUBJECT, "Attendance Sheets (${files.size} Courses)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share ${files.size} Course Sheets via"))
    }

    fun shareFile(context: Context, file: File, mimeType: String = "text/csv") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Attendance Sheet: ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Attendance Sheet via"))
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
