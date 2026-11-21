import {
  buildUniversityCrestSvg,
  getUniversityById,
} from '../data/kenyanUniversities';
import {
  AttendanceRecord,
  AttendanceSession,
  ExportFormat,
  getColumnName,
  getTotalSessionsCount,
  SESSION_TYPE_META,
  StudentMatrixRow,
} from '../types/tapattend';
import { computeSemesterMatrix } from './storage';

export interface CourseSheetGroup {
  courseName: string;
  records: AttendanceRecord[];
}

function escapeCsv(value: string): string {
  if (value.includes(',') || value.includes('"') || value.includes('\n')) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function formatTime(ts: number): string {
  return new Date(ts).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

function slug(text: string): string {
  return text.replace(/[^a-zA-Z0-9]+/g, '_').replace(/^_|_$/g, '');
}

function downloadBlob(content: string, filename: string, mimeType: string): void {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

/**
 * Splits attendance records strictly by single course so every generated/printed
 * sheet only contains students of ONE course. If multiple courses exist in the
 * session (common unit), returns 1 sheet group per registered course.
 */
export function getCourseSheetGroups(
  session: AttendanceSession,
  records: AttendanceRecord[]
): CourseSheetGroup[] {
  const courseList: string[] = [];
  const seen = new Set<string>();

  for (const c of session.allowedCourses) {
    const trimmed = c.trim();
    if (trimmed && !seen.has(trimmed.toLowerCase())) {
      seen.add(trimmed.toLowerCase());
      courseList.push(trimmed);
    }
  }

  for (const r of records) {
    const rc = (r.studentCourse || '').trim();
    if (rc && !seen.has(rc.toLowerCase())) {
      seen.add(rc.toLowerCase());
      courseList.push(rc);
    }
  }

  if (courseList.length === 0) {
    courseList.push('General Programme');
  }

  return courseList.map((courseName) => ({
    courseName,
    records: records.filter(
      (r) =>
        (r.studentCourse || '').trim().toLowerCase() ===
        courseName.trim().toLowerCase()
    ),
  }));
}

export function exportSingleSessionCsv(
  session: AttendanceSession,
  records: AttendanceRecord[]
): number {
  const groups = getCourseSheetGroups(session, records);
  const safeCode = slug(session.courseCode);

  groups.forEach((group) => {
    const lines: string[] = [
      `${escapeCsv(session.universityName)}`,
      'OFFICIAL ATTENDANCE REPORT',
      `Course / Programme,${escapeCsv(group.courseName)}`,
      `Department,${escapeCsv(session.department)}`,
      `Unit / Sheet Title,${escapeCsv(session.courseCode)} - ${escapeCsv(session.title)}`,
      `Session Type,${escapeCsv(SESSION_TYPE_META[session.sessionType].displayName)}`,
      `Venue,${escapeCsv(session.venue)}`,
      `Instructor / Class Rep,${escapeCsv(session.instructorName)}`,
      `Total Present,${group.records.length}`,
      `Generated On,${new Date().toLocaleString()}`,
      '',
    ];

    const customHeaders = session.customColumns.map(escapeCsv).join(',');
    lines.push(
      `S/N,Registration Number,Full Name,Course,NFC Card UID,Time In,Status${
        customHeaders ? `,${customHeaders}` : ''
      },Digital Attestation (Signature Substitute)`
    );

    group.records.forEach((r, idx) => {
      const customVals = session.customColumns
        .map((c) => escapeCsv(r.customValues[c] || ''))
        .join(',');
      lines.push(
        `${idx + 1},${escapeCsv(r.regNumber)},${escapeCsv(r.studentName)},${escapeCsv(
          group.courseName
        )},${r.studentCardUid},${formatTime(r.timestamp)},${r.status}${
          customVals ? `,${customVals}` : ''
        },${r.attestationToken}`
      );
    });

    downloadBlob(
      lines.join('\n'),
      `${safeCode}_${slug(group.courseName)}_Attendance.csv`,
      'text/csv;charset=utf-8;'
    );
  });

  return groups.length;
}

export function exportSemesterMatrixCsv(
  session: AttendanceSession
): number {
  const courses =
    session.allowedCourses.length > 0
      ? session.allowedCourses
      : ['General Programme'];
  const safeCode = slug(session.courseCode);
  const totalSessions = getTotalSessionsCount(session);

  courses.forEach((courseName) => {
    const matrix: StudentMatrixRow[] = computeSemesterMatrix(session, courseName);
    const lines: string[] = [
      `${escapeCsv(session.universityName)}`,
      'SEMESTER ATTENDANCE MATRIX REPORT',
      `Course / Programme,${escapeCsv(courseName)}`,
      `Department,${escapeCsv(session.department)}`,
      `Unit,${escapeCsv(session.courseCode)} - ${escapeCsv(session.title)}`,
      `Total Sessions / Columns,${totalSessions}`,
      `Report Date,${new Date().toLocaleString()}`,
      '',
    ];

    const colHeaders = Array.from({ length: totalSessions }, (_, i) =>
      escapeCsv(getColumnName(session, i + 1))
    ).join(',');

    lines.push(
      `S/N,Registration Number,Full Name,${colHeaders},Total Present,Total Held,Attendance %,Eligibility (>=75%)`
    );

    matrix.forEach((row, idx) => {
      const cells = Array.from({ length: totalSessions }, (_, i) =>
        row.attendanceBySession[i + 1] ? 'P' : 'A'
      ).join(',');
      const pct = `${row.attendancePercentage.toFixed(1)}%`;
      const eligible = row.attendancePercentage >= 75 ? 'ELIGIBLE' : 'DEFICIT';
      lines.push(
        `${idx + 1},${escapeCsv(row.student.regNumber)},${escapeCsv(
          row.student.fullName
        )},${cells},${row.totalPresent},${row.totalSessionsHeld},${pct},${eligible}`
      );
    });

    downloadBlob(
      lines.join('\n'),
      `${safeCode}_${slug(courseName)}_Matrix.csv`,
      'text/csv;charset=utf-8;'
    );
  });

  return courses.length;
}

export function exportExcelXls(
  session: AttendanceSession,
  records: AttendanceRecord[]
): number {
  const groups = getCourseSheetGroups(session, records);
  const safeCode = slug(session.courseCode);
  const uni = getUniversityById(session.universityId);

  groups.forEach((group) => {
    const rowsHtml = group.records
      .map(
        (r, idx) => `
      <tr>
        <td align="center">${idx + 1}</td>
        <td><b>${escapeHtml(r.regNumber)}</b></td>
        <td>${escapeHtml(r.studentName)}</td>
        <td align="center">${escapeHtml(r.studentCardUid)}</td>
        <td align="center">${formatTime(r.timestamp)}</td>
        <td align="center" style="color:#059669;font-weight:bold;">${escapeHtml(r.status)}</td>
        <td align="center" style="font-family:Courier New;">${escapeHtml(r.attestationToken)}</td>
      </tr>`
      )
      .join('');

    const html = `
    <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
      <style>
        td, th { font-family: Calibri, Arial; font-size: 11pt; }
        .uni { background-color: ${uni.primaryColor}; color: #FFFFFF; font-weight: bold; text-align: center; font-size: 14pt; }
        .hdr { background-color: #0284C7; color: #FFFFFF; font-weight: bold; text-align: center; }
        .subhdr { background-color: #F1F5F9; font-weight: bold; }
        .meta { font-weight: bold; background-color: #E0F2FE; }
      </style>
    </head>
    <body>
      <table border="1" cellspacing="0" cellpadding="4">
        <tr><td colspan="7" class="uni">${escapeHtml(session.universityName)}</td></tr>
        <tr><td colspan="7" class="hdr" style="font-size:12pt;">${escapeHtml(group.courseName)} — ${escapeHtml(session.courseCode)}: ${escapeHtml(session.title)}</td></tr>
        <tr><td colspan="2" class="meta">Department:</td><td colspan="5">${escapeHtml(session.department)}</td></tr>
        <tr><td colspan="2" class="meta">Venue:</td><td colspan="5">${escapeHtml(session.venue)}</td></tr>
        <tr><td colspan="2" class="meta">Instructor / Rep:</td><td colspan="5">${escapeHtml(session.instructorName)}</td></tr>
        <tr><td colspan="2" class="meta">Total Present:</td><td colspan="5">${group.records.length} Students</td></tr>
        <tr><td colspan="2" class="meta">Date:</td><td colspan="5">${new Date().toLocaleString()}</td></tr>
        <tr><td colspan="7"></td></tr>
        <tr class="subhdr">
          <th>S/N</th><th>Registration No.</th><th>Student Name</th><th>NFC Card UID</th><th>Time In</th><th>Status</th><th>Digital Attestation</th>
        </tr>
        ${rowsHtml}
      </table>
    </body>
    </html>`;

    downloadBlob(
      html,
      `${safeCode}_${slug(group.courseName)}_Attendance.xls`,
      'application/vnd.ms-excel'
    );
  });

  return groups.length;
}

export function exportWordDoc(
  session: AttendanceSession,
  records: AttendanceRecord[]
): number {
  const groups = getCourseSheetGroups(session, records);
  const safeCode = slug(session.courseCode);
  const uni = getUniversityById(session.universityId);
  const crestSvg = buildUniversityCrestSvg(uni, 54);

  groups.forEach((group) => {
    const rowsHtml = group.records
      .map(
        (r, idx) => `
      <tr>
        <td>${idx + 1}</td>
        <td><b>${escapeHtml(r.regNumber)}</b></td>
        <td>${escapeHtml(r.studentName)}</td>
        <td>${formatTime(r.timestamp)}</td>
        <td style="color:#15803D;font-weight:bold;">${escapeHtml(r.status)}</td>
        <td style="font-family:Courier New;font-size:9pt;">SHA256:${escapeHtml(r.attestationToken)}</td>
      </tr>`
      )
      .join('');

    const html = `
    <html xmlns:w="urn:schemas-microsoft-com:office:word" xmlns="http://www.w3.org/TR/REC-html40">
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
      <title>${escapeHtml(session.courseCode)} - ${escapeHtml(group.courseName)}</title>
      <style>
        body { font-family: Calibri, serif; font-size: 11pt; margin: 0.8in; }
        .header-row { display: flex; align-items: center; gap: 12pt; border-bottom: 2pt solid ${uni.primaryColor}; padding-bottom: 8pt; margin-bottom: 10pt; }
        h1 { color: ${uni.primaryColor}; font-size: 15pt; margin: 0; }
        h2 { color: #0284C7; font-size: 13pt; margin: 4pt 0; }
        p.subtitle { color: #475569; font-size: 10pt; margin: 2pt 0; }
        table { width: 100%; border-collapse: collapse; margin-top: 12pt; }
        th, td { border: 1pt solid #CBD5E1; padding: 6pt; font-size: 10pt; }
        th { background-color: #F8FAFC; color: #0F172A; text-align: left; }
      </style>
    </head>
    <body>
      <div class="header-row">
        <div>${crestSvg}</div>
        <div>
          <h1>${escapeHtml(session.universityName)}</h1>
          <p class="subtitle"><strong>Department:</strong> ${escapeHtml(session.department)}</p>
          <h2>${escapeHtml(group.courseName)} — ${escapeHtml(session.courseCode)}: ${escapeHtml(session.title)}</h2>
        </div>
      </div>
      <p><strong>Venue:</strong> ${escapeHtml(session.venue)} &nbsp;|&nbsp; <strong>Instructor:</strong> ${escapeHtml(session.instructorName)} &nbsp;|&nbsp; <strong>Present:</strong> ${group.records.length}</p>
      <p><strong>Date:</strong> ${new Date().toLocaleString()}</p>
      <table>
        <thead>
          <tr><th>#</th><th>Registration No.</th><th>Student Name</th><th>Time In</th><th>Status</th><th>Digital Attestation</th></tr>
        </thead>
        <tbody>${rowsHtml}</tbody>
      </table>
      <div style="margin-top: 44pt; display: flex; justify-content: space-between;">
        <div style="border-top: 1pt solid #000; width: 42%; padding-top: 6pt;"><strong>Class Representative / Invigilator</strong><br>Signature: ____________________</div>
        <div style="border-top: 1pt solid #000; width: 42%; padding-top: 6pt;"><strong>Course Lecturer / COD</strong><br>Signature: ____________________</div>
      </div>
    </body>
    </html>`;

    downloadBlob(
      html,
      `${safeCode}_${slug(group.courseName)}_Attendance.doc`,
      'application/msword'
    );
  });

  return groups.length;
}

export function handleExportFormat(
  format: ExportFormat,
  session: AttendanceSession,
  records: AttendanceRecord[],
  onRequestPdfPreview: () => void
): number {
  switch (format) {
    case ExportFormat.EXCEL:
      return exportExcelXls(session, records);
    case ExportFormat.WORD:
      return exportWordDoc(session, records);
    case ExportFormat.CSV:
      return exportSingleSessionCsv(session, records);
    case ExportFormat.PDF:
      onRequestPdfPreview();
      return getCourseSheetGroups(session, records).length;
  }
}
