export enum SessionType {
  SEMESTER_WEEKLY = 'SEMESTER_WEEKLY',
  EXAM = 'EXAM',
  SUBMISSION = 'SUBMISSION',
  SINGLE_EVENT = 'SINGLE_EVENT',
  CUSTOM_SHEET = 'CUSTOM_SHEET',
}

export const SESSION_TYPE_META: Record<
  SessionType,
  { displayName: string; description: string; badgeColor: string }
> = {
  [SessionType.SEMESTER_WEEKLY]: {
    displayName: 'Semester Attendance (Weeks)',
    description: 'Multi-week semester matrix with week columns',
    badgeColor: '#0284C7',
  },
  [SessionType.EXAM]: {
    displayName: 'Exam / Test Attendance',
    description: 'Single-session exam with seat, time, and anti-proxy attestation',
    badgeColor: '#DC2626',
  },
  [SessionType.SUBMISSION]: {
    displayName: 'Assignment / Lab Submission',
    description: 'Tracks submission timestamp, order #, and verification',
    badgeColor: '#7C3AED',
  },
  [SessionType.SINGLE_EVENT]: {
    displayName: 'Single Occasion / Seminar',
    description: 'Quick one-time roll call for meetings, workshops or talks',
    badgeColor: '#059669',
  },
  [SessionType.CUSTOM_SHEET]: {
    displayName: 'Custom Configured Sheet',
    description: 'User-defined session columns, custom headers, and custom fields',
    badgeColor: '#D97706',
  },
};

export interface Student {
  id: string;
  cardUid: string;
  regNumber: string;
  fullName: string;
  universityId: string;
  universityName: string;
  department: string;
  courseCode: string;
  courseName: string;
  createdByUid?: string;
  updatedAt?: number;
}

export interface CustomCourse {
  id: string;
  universityId: string;
  department: string;
  courseCode: string;
  courseName: string;
  createdByUid?: string;
}

export interface AttendanceSession {
  id: string;
  title: string;
  courseCode: string;
  universityId: string;
  universityName: string;
  department: string;
  allowedCourses: string[];
  sessionType: SessionType;
  totalWeeks: number;
  venue: string;
  instructorName: string;
  createdTimestamp: number;
  customColumns: string[];
  sessionColumnNames: string[];
  ownerUid?: string;
}

export interface AttendanceRecord {
  id: string;
  sessionId: string;
  studentCardUid: string;
  regNumber: string;
  studentName: string;
  studentCourse: string;
  studentDepartment: string;
  weekNumber: number | null;
  timestamp: number;
  status: string;
  orderNumber: number;
  attestationToken: string;
  customValues: Record<string, string>;
}

export interface StudentMatrixRow {
  student: Student;
  attendanceBySession: Record<number, boolean>;
  totalPresent: number;
  totalSessionsHeld: number;
  attendancePercentage: number;
}

export interface ParsedCardData {
  cardUid: string;
  ndefText?: string;
  suggestedName?: string;
  suggestedRegNumber?: string;
}

export enum ExportFormat {
  EXCEL = 'EXCEL',
  PDF = 'PDF',
  WORD = 'WORD',
  CSV = 'CSV',
}

export function isMultiSession(session: AttendanceSession): boolean {
  return (
    session.sessionType === SessionType.SEMESTER_WEEKLY ||
    (session.sessionType === SessionType.CUSTOM_SHEET &&
      (session.sessionColumnNames.length > 0 || session.totalWeeks > 1))
  );
}

export function getColumnName(session: AttendanceSession, index: number): string {
  if (index - 1 >= 0 && index - 1 < session.sessionColumnNames.length) {
    return session.sessionColumnNames[index - 1];
  }
  return `Week ${index}`;
}

export function getTotalSessionsCount(session: AttendanceSession): number {
  return session.sessionColumnNames.length > 0
    ? session.sessionColumnNames.length
    : session.totalWeeks;
}
