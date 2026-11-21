import { DEFAULT_UNIVERSITY_ID, getUniversityById } from '../data/kenyanUniversities';
import {
  AttendanceRecord,
  AttendanceSession,
  CustomCourse,
  getTotalSessionsCount,
  SessionType,
  Student,
  StudentMatrixRow,
} from '../types/tapattend';

const KEY_STUDENTS = 'tapattend_students_v2';
const KEY_SESSIONS = 'tapattend_sessions_v2';
const KEY_RECORDS = 'tapattend_records_v2';
const KEY_CUSTOM_COURSES = 'tapattend_custom_courses_v2';

export function createSafeId(prefix = 'id'): string {
  const raw =
    typeof crypto !== 'undefined' && crypto.randomUUID
      ? crypto.randomUUID()
      : `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
  return `${prefix}-${raw}`.replace(/[^a-zA-Z0-9_-]/g, '').slice(0, 64);
}

export function normalizeUid(cardUid: string): string {
  return cardUid.replace(/:/g, '').replace(/\s+/g, '').trim().toUpperCase();
}

export async function generateAttestationToken(
  cardUid: string,
  sessionId: string,
  timestamp: number
): Promise<string> {
  const raw = `${cardUid}|${sessionId}|${timestamp}|NFC_TAP_ATTEND_SECURE`;
  if (typeof crypto !== 'undefined' && crypto.subtle) {
    const encoded = new TextEncoder().encode(raw);
    const hashBuf = await crypto.subtle.digest('SHA-256', encoded);
    const bytes = new Uint8Array(hashBuf).slice(0, 6);
    return Array.from(bytes)
      .map((b) => b.toString(16).padStart(2, '0').toUpperCase())
      .join('');
  }
  let h = 0;
  for (let i = 0; i < raw.length; i++) {
    h = (h << 5) - h + raw.charCodeAt(i);
    h |= 0;
  }
  return Math.abs(h).toString(16).toUpperCase().padStart(12, '0').slice(0, 12);
}

const DEKUT = getUniversityById(DEFAULT_UNIVERSITY_ID);

const DEFAULT_STUDENTS: Student[] = [
  {
    id: 'stu-dekut-1',
    cardUid: '04A23B5C89',
    regNumber: 'C026-01-0901/2023',
    fullName: 'Alexander Kamau',
    universityId: DEKUT.id,
    universityName: DEKUT.name,
    department: 'Department of Computer Science',
    courseCode: 'C026',
    courseName: 'B.Sc. Computer Science',
    updatedAt: Date.now(),
  },
  {
    id: 'stu-dekut-2',
    cardUid: '04F18290EA',
    regNumber: 'C026-01-0914/2023',
    fullName: 'Beatrice Mwangi',
    universityId: DEKUT.id,
    universityName: DEKUT.name,
    department: 'Department of Computer Science',
    courseCode: 'C026',
    courseName: 'B.Sc. Computer Science',
    updatedAt: Date.now(),
  },
  {
    id: 'stu-dekut-3',
    cardUid: '04C398110D',
    regNumber: 'C027-01-0427/2023',
    fullName: 'Brian Kipchoge',
    universityId: DEKUT.id,
    universityName: DEKUT.name,
    department: 'Department of Information Technology',
    courseCode: 'C027',
    courseName: 'B.Sc. Information Technology',
    updatedAt: Date.now(),
  },
  {
    id: 'stu-dekut-4',
    cardUid: '04561234AB',
    regNumber: 'C027-01-0433/2023',
    fullName: 'Faith Wanjiku',
    universityId: DEKUT.id,
    universityName: DEKUT.name,
    department: 'Department of Information Technology',
    courseCode: 'C027',
    courseName: 'B.Sc. Information Technology',
    updatedAt: Date.now(),
  },
  {
    id: 'stu-dekut-5',
    cardUid: '047890CDEF',
    regNumber: 'E022-01-0148/2023',
    fullName: 'Daniel Omondi',
    universityId: DEKUT.id,
    universityName: DEKUT.name,
    department: 'Department of Mechatronic Engineering',
    courseCode: 'E022',
    courseName: 'B.Sc. Mechatronic Engineering',
    updatedAt: Date.now(),
  },
  {
    id: 'stu-dekut-6',
    cardUid: '0499AA1234',
    regNumber: 'C029-01-0255/2023',
    fullName: 'Grace Achieng',
    universityId: DEKUT.id,
    universityName: DEKUT.name,
    department: 'Department of Computer Science',
    courseCode: 'C029',
    courseName: 'B.Sc. Software Engineering',
    updatedAt: Date.now(),
  },
];

const DEFAULT_SESSIONS: AttendanceSession[] = [
  {
    id: 'sess-ccs2101',
    title: 'CCS 2101: Data Structures & Algorithms (Common Unit)',
    courseCode: 'CCS 2101',
    universityId: DEKUT.id,
    universityName: DEKUT.name,
    department: 'Department of Computer Science',
    allowedCourses: [
      'B.Sc. Computer Science',
      'B.Sc. Information Technology',
    ],
    sessionType: SessionType.SEMESTER_WEEKLY,
    totalWeeks: 14,
    venue: 'Resource Centre Hall 2',
    instructorName: 'Dr. Mwangi / Class Rep',
    createdTimestamp: Date.now() - 86400000 * 5,
    customColumns: [],
    sessionColumnNames: Array.from({ length: 14 }, (_, i) => `Week ${i + 1}`),
  },
  {
    id: 'sess-igs1102',
    title: 'IGS 1102: Communication Skills Exam',
    courseCode: 'IGS 1102',
    universityId: DEKUT.id,
    universityName: DEKUT.name,
    department: 'Department of Computer Science',
    allowedCourses: ['B.Sc. Computer Science'],
    sessionType: SessionType.EXAM,
    totalWeeks: 1,
    venue: 'Freedom Hall',
    instructorName: 'Prof. Njoroge',
    createdTimestamp: Date.now() - 86400000 * 2,
    customColumns: ['Seat #'],
    sessionColumnNames: [],
  },
];

export function initStorage(): void {
  if (!localStorage.getItem(KEY_STUDENTS)) {
    localStorage.setItem(KEY_STUDENTS, JSON.stringify(DEFAULT_STUDENTS));
  }
  if (!localStorage.getItem(KEY_SESSIONS)) {
    localStorage.setItem(KEY_SESSIONS, JSON.stringify(DEFAULT_SESSIONS));
  }
  if (!localStorage.getItem(KEY_RECORDS)) {
    localStorage.setItem(KEY_RECORDS, JSON.stringify([]));
  }
  if (!localStorage.getItem(KEY_CUSTOM_COURSES)) {
    localStorage.setItem(KEY_CUSTOM_COURSES, JSON.stringify([]));
  }
}

export function getStudents(): Student[] {
  initStorage();
  try {
    return JSON.parse(localStorage.getItem(KEY_STUDENTS) || '[]');
  } catch {
    return [];
  }
}

export function saveStudents(students: Student[]): void {
  localStorage.setItem(KEY_STUDENTS, JSON.stringify(students));
}

export function findStudentByCardUid(cardUid: string): Student | undefined {
  const normalized = normalizeUid(cardUid);
  if (!normalized) return undefined;
  return getStudents().find((s) => normalizeUid(s.cardUid) === normalized);
}

export function addOrUpdateStudent(
  student: Omit<Student, 'id'> & { id?: string }
): Student {
  const students = getStudents();
  const cleanUid = normalizeUid(student.cardUid);
  const idx = students.findIndex(
    (s) =>
      (student.id && s.id === student.id) ||
      (cleanUid !== '' && normalizeUid(s.cardUid) === cleanUid) ||
      s.regNumber.toLowerCase() === student.regNumber.trim().toLowerCase()
  );

  const cleanStudent: Student = {
    id: idx >= 0 ? students[idx].id : student.id || createSafeId('stu'),
    cardUid: cleanUid,
    regNumber: student.regNumber.trim().toUpperCase(),
    fullName: student.fullName.trim(),
    universityId: student.universityId || DEFAULT_UNIVERSITY_ID,
    universityName: student.universityName || DEKUT.name,
    department: student.department.trim() || 'Department of Computer Science',
    courseCode: student.courseCode.trim().toUpperCase() || 'GEN',
    courseName: student.courseName.trim() || 'General Programme',
    createdByUid: student.createdByUid || (idx >= 0 ? students[idx].createdByUid : undefined),
    updatedAt: Date.now(),
  };

  if (idx >= 0) {
    students[idx] = cleanStudent;
  } else {
    students.push(cleanStudent);
  }
  saveStudents(students);
  return cleanStudent;
}

export function mergeCloudStudents(cloudStudents: Student[]): {
  added: number;
  updated: number;
} {
  const local = getStudents();
  let added = 0;
  let updated = 0;
  const byId = new Map(local.map((s) => [s.id, s]));

  for (const cs of cloudStudents) {
    const cleanUid = normalizeUid(cs.cardUid);
    const existingByUid = local.find(
      (l) =>
        (cleanUid && normalizeUid(l.cardUid) === cleanUid) ||
        l.regNumber.toLowerCase() === cs.regNumber.toLowerCase()
    );
    const targetId = existingByUid ? existingByUid.id : cs.id;
    const prev = byId.get(targetId);

    if (!prev) {
      byId.set(targetId, { ...cs, id: targetId, cardUid: cleanUid });
      added++;
    } else if ((cs.updatedAt || 0) >= (prev.updatedAt || 0)) {
      byId.set(targetId, { ...cs, id: targetId, cardUid: cleanUid });
      updated++;
    }
  }

  saveStudents(Array.from(byId.values()));
  return { added, updated };
}

export function importStudentsFromCsv(
  csvText: string,
  defaults: {
    universityId: string;
    universityName: string;
    department: string;
    courseCode: string;
    courseName: string;
  }
): number {
  let count = 0;
  const lines = csvText.split(/\r?\n/);
  const currentList = getStudents();

  for (const line of lines) {
    const parts = line.split(',').map((p) => p.trim().replace(/^"|"$/g, ''));
    if (parts.length >= 2) {
      const reg = parts[0];
      const name = parts[1];
      const dept = parts.length >= 3 && parts[2] ? parts[2] : defaults.department;
      const courseName =
        parts.length >= 4 && parts[3] ? parts[3] : defaults.courseName;
      const uid = parts.length >= 5 ? normalizeUid(parts[4]) : '';

      if (reg && name && reg.toLowerCase() !== 'registration number') {
        const existingIdx = currentList.findIndex(
          (s) => s.regNumber.toLowerCase() === reg.toLowerCase()
        );
        const newStudent: Student = {
          id: existingIdx >= 0 ? currentList[existingIdx].id : createSafeId('stu'),
          cardUid:
            uid || (existingIdx >= 0 ? currentList[existingIdx].cardUid : ''),
          regNumber: reg.toUpperCase(),
          fullName: name,
          universityId: defaults.universityId,
          universityName: defaults.universityName,
          department: dept,
          courseCode: defaults.courseCode,
          courseName,
          updatedAt: Date.now(),
        };
        if (existingIdx >= 0) {
          currentList[existingIdx] = newStudent;
        } else {
          currentList.push(newStudent);
        }
        count++;
      }
    }
  }
  saveStudents(currentList);
  return count;
}

export function getCustomCourses(): CustomCourse[] {
  initStorage();
  try {
    return JSON.parse(localStorage.getItem(KEY_CUSTOM_COURSES) || '[]');
  } catch {
    return [];
  }
}

export function saveCustomCourses(courses: CustomCourse[]): void {
  localStorage.setItem(KEY_CUSTOM_COURSES, JSON.stringify(courses));
}

export function addCustomCourseLocal(
  course: Omit<CustomCourse, 'id'> & { id?: string }
): CustomCourse {
  const existing = getCustomCourses();
  const duplicate = existing.find(
    (c) =>
      c.universityId === course.universityId &&
      c.department.toLowerCase() === course.department.toLowerCase() &&
      c.courseName.toLowerCase() === course.courseName.trim().toLowerCase()
  );
  if (duplicate) return duplicate;

  const created: CustomCourse = {
    id: course.id || createSafeId('crs'),
    universityId: course.universityId,
    department: course.department.trim(),
    courseCode: course.courseCode.trim().toUpperCase() || 'CUST',
    courseName: course.courseName.trim(),
    createdByUid: course.createdByUid,
  };
  existing.push(created);
  saveCustomCourses(existing);
  return created;
}

export function mergeCloudCustomCourses(cloudCourses: CustomCourse[]): void {
  const local = getCustomCourses();
  const map = new Map(local.map((c) => [c.id, c]));
  for (const c of cloudCourses) {
    map.set(c.id, c);
  }
  saveCustomCourses(Array.from(map.values()));
}

export function getSessions(): AttendanceSession[] {
  initStorage();
  try {
    return JSON.parse(localStorage.getItem(KEY_SESSIONS) || '[]');
  } catch {
    return [];
  }
}

export function saveSessions(sessions: AttendanceSession[]): void {
  localStorage.setItem(KEY_SESSIONS, JSON.stringify(sessions));
}

export function addSession(session: AttendanceSession): void {
  const sessions = getSessions();
  sessions.unshift(session);
  saveSessions(sessions);
}

export function addSessionColumn(
  sessionId: string,
  columnName: string
): AttendanceSession | null {
  const sessions = getSessions();
  const idx = sessions.findIndex((s) => s.id === sessionId);
  if (idx < 0) return null;

  const s = sessions[idx];
  const newCols =
    s.sessionColumnNames.length > 0
      ? [...s.sessionColumnNames]
      : Array.from({ length: s.totalWeeks }, (_, i) => `Week ${i + 1}`);
  newCols.push(columnName);

  const updated: AttendanceSession = {
    ...s,
    sessionColumnNames: newCols,
    totalWeeks: newCols.length,
  };
  sessions[idx] = updated;
  saveSessions(sessions);
  return updated;
}

export function deleteSession(sessionId: string): void {
  saveSessions(getSessions().filter((s) => s.id !== sessionId));
  saveRecords(getRecords().filter((r) => r.sessionId !== sessionId));
}

export function getRecords(): AttendanceRecord[] {
  initStorage();
  try {
    return JSON.parse(localStorage.getItem(KEY_RECORDS) || '[]');
  } catch {
    return [];
  }
}

export function saveRecords(records: AttendanceRecord[]): void {
  localStorage.setItem(KEY_RECORDS, JSON.stringify(records));
}

export function getRecordsForSession(
  sessionId: string,
  weekNumber: number | null = null
): AttendanceRecord[] {
  return getRecords().filter(
    (r) => r.sessionId === sessionId && (weekNumber === null || r.weekNumber === weekNumber)
  );
}

export type RecordTapResult =
  | { type: 'SUCCESS'; record: AttendanceRecord; student: Student }
  | { type: 'DUPLICATE'; record: AttendanceRecord; student: Student }
  | { type: 'WRONG_COURSE'; student: Student; allowedCourses: string[] }
  | { type: 'UNKNOWN_CARD'; cardUid: string };

export async function recordTapForSession(
  session: AttendanceSession,
  cardUid: string,
  weekNumber: number | null = null,
  customValues: Record<string, string> = {},
  allowCourseOverride = false
): Promise<RecordTapResult> {
  const cleanUid = normalizeUid(cardUid);
  const student = findStudentByCardUid(cleanUid);
  if (!student) {
    return { type: 'UNKNOWN_CARD', cardUid: cleanUid };
  }
  return markStudentPresent(
    session,
    student,
    cleanUid,
    weekNumber,
    customValues,
    allowCourseOverride
  );
}

export async function manualCheckInStudent(
  session: AttendanceSession,
  student: Student,
  weekNumber: number | null = null,
  customValues: Record<string, string> = {}
): Promise<RecordTapResult> {
  const effectiveUid =
    normalizeUid(student.cardUid) || `MANUAL-${student.regNumber.toUpperCase()}`;
  return markStudentPresent(
    session,
    student,
    effectiveUid,
    weekNumber,
    customValues,
    false
  );
}

async function markStudentPresent(
  session: AttendanceSession,
  student: Student,
  effectiveUid: string,
  weekNumber: number | null,
  customValues: Record<string, string>,
  allowCourseOverride: boolean
): Promise<RecordTapResult> {
  // Enforce course restriction so students cannot accidentally register in wrong class
  if (!allowCourseOverride && session.allowedCourses.length > 0) {
    const allowedSet = new Set(
      session.allowedCourses.map((c) => c.trim().toLowerCase())
    );
    if (!allowedSet.has(student.courseName.trim().toLowerCase())) {
      return {
        type: 'WRONG_COURSE',
        student,
        allowedCourses: session.allowedCourses,
      };
    }
  }

  const existingRecords = getRecordsForSession(session.id, weekNumber);
  const duplicate = existingRecords.find(
    (r) =>
      normalizeUid(r.studentCardUid) === effectiveUid ||
      r.regNumber.toLowerCase() === student.regNumber.toLowerCase()
  );
  if (duplicate) {
    return { type: 'DUPLICATE', record: duplicate, student };
  }

  const allSessionRecords = getRecordsForSession(session.id);
  const now = Date.now();
  const attestationToken = await generateAttestationToken(
    effectiveUid,
    session.id,
    now
  );

  const newRecord: AttendanceRecord = {
    id: createSafeId('rec'),
    sessionId: session.id,
    studentCardUid: effectiveUid,
    regNumber: student.regNumber,
    studentName: student.fullName,
    studentCourse: student.courseName,
    studentDepartment: student.department,
    weekNumber,
    timestamp: now,
    status: 'PRESENT',
    orderNumber: allSessionRecords.length + 1,
    attestationToken,
    customValues,
  };

  const allRecords = getRecords();
  allRecords.push(newRecord);
  saveRecords(allRecords);

  return { type: 'SUCCESS', record: newRecord, student };
}

export async function enrollAndRecordTap(params: {
  session: AttendanceSession;
  cardUid: string;
  weekNumber: number | null;
  existingStudentIdToLink?: string | null;
  newRegNumber?: string;
  newFullName?: string;
  universityId?: string;
  universityName?: string;
  department?: string;
  courseCode?: string;
  courseName?: string;
  customValues?: Record<string, string>;
}): Promise<RecordTapResult> {
  const cleanUid = normalizeUid(params.cardUid);

  if (params.existingStudentIdToLink) {
    const students = getStudents();
    const idx = students.findIndex((s) => s.id === params.existingStudentIdToLink);
    if (idx >= 0) {
      students[idx] = { ...students[idx], cardUid: cleanUid, updatedAt: Date.now() };
      saveStudents(students);
    }
  } else if (params.newRegNumber?.trim() && params.newFullName?.trim()) {
    addOrUpdateStudent({
      cardUid: cleanUid,
      regNumber: params.newRegNumber.trim(),
      fullName: params.newFullName.trim(),
      universityId: params.universityId || params.session.universityId,
      universityName: params.universityName || params.session.universityName,
      department: params.department?.trim() || params.session.department,
      courseCode: params.courseCode?.trim() || params.session.courseCode,
      courseName:
        params.courseName?.trim() ||
        params.session.allowedCourses[0] ||
        'General Programme',
    });
  }

  return recordTapForSession(
    params.session,
    cleanUid,
    params.weekNumber,
    params.customValues || {}
  );
}

export function computeSemesterMatrix(
  session: AttendanceSession,
  filterCourse?: string
): StudentMatrixRow[] {
  const allStudents = getStudents().filter((s) => {
    if (filterCourse) {
      return s.courseName.trim().toLowerCase() === filterCourse.trim().toLowerCase();
    }
    if (session.allowedCourses.length > 0) {
      return session.allowedCourses.some(
        (c) => c.trim().toLowerCase() === s.courseName.trim().toLowerCase()
      );
    }
    return true;
  });

  const records = getRecordsForSession(session.id);
  const totalSessions = getTotalSessionsCount(session);

  const sessionsWithAttendance = new Set(
    records.map((r) => r.weekNumber).filter((w): w is number => w !== null)
  );
  const sessionsHeldCount = Math.max(1, sessionsWithAttendance.size);

  return allStudents.map((student) => {
    const cleanUid = normalizeUid(student.cardUid);
    const studentRecords = records.filter(
      (r) => cleanUid !== '' && normalizeUid(r.studentCardUid) === cleanUid
    );

    const attendanceBySession: Record<number, boolean> = {};
    let presentCount = 0;
    for (let w = 1; w <= totalSessions; w++) {
      const present = studentRecords.some((r) => r.weekNumber === w);
      attendanceBySession[w] = present;
      if (present) presentCount++;
    }

    const attendancePercentage = (presentCount / sessionsHeldCount) * 100;

    return {
      student,
      attendanceBySession,
      totalPresent: presentCount,
      totalSessionsHeld: sessionsHeldCount,
      attendancePercentage,
    };
  });
}
