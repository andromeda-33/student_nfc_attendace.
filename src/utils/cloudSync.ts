import {
  collection,
  doc,
  getDocsFromServer,
  onSnapshot,
  query,
  serverTimestamp,
  setDoc,
  where,
} from 'firebase/firestore';
import { auth, db, handleFirestoreError, OperationType } from '../firebase';
import {
  AttendanceRecord,
  AttendanceSession,
  CustomCourse,
  Student,
} from '../types/tapattend';
import {
  getCustomCourses,
  getStudents,
  mergeCloudCustomCourses,
  mergeCloudStudents,
  normalizeUid,
} from './storage';

function clamp(val: string, max: number, fallback = 'N/A'): string {
  const s = (val || '').trim();
  if (!s) return fallback;
  return s.slice(0, max);
}

function sanitizeId(id: string): string {
  const cleaned = (id || '').replace(/[^a-zA-Z0-9_-]/g, '');
  return (cleaned || `id_${Date.now()}`).slice(0, 128);
}

export async function pushStudentToCloud(
  student: Student,
  uid: string
): Promise<void> {
  const id = sanitizeId(student.id);
  const path = `students/${id}`;
  const cardUid = normalizeUid(student.cardUid).replace(/[^A-Z0-9]/g, '');
  if (cardUid.length < 4) return;

  try {
    await setDoc(doc(db, 'students', id), {
      id,
      cardUid: cardUid.slice(0, 32),
      regNumber: clamp(student.regNumber, 40),
      fullName: clamp(student.fullName, 100),
      universityId: clamp(student.universityId, 64, 'dekut'),
      universityName: clamp(
        student.universityName,
        140,
        'DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY'
      ),
      department: clamp(student.department, 120),
      courseCode: clamp(student.courseCode, 32, 'GEN'),
      courseName: clamp(student.courseName, 140),
      createdByUid: student.createdByUid || uid,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
  } catch (err) {
    handleFirestoreError(err, OperationType.WRITE, path);
  }
}

export async function pushCustomCourseToCloud(
  course: CustomCourse,
  uid: string
): Promise<void> {
  const id = sanitizeId(course.id);
  const path = `customCourses/${id}`;
  try {
    await setDoc(doc(db, 'customCourses', id), {
      id,
      universityId: clamp(course.universityId, 64, 'dekut'),
      department: clamp(course.department, 120),
      courseCode: clamp(course.courseCode, 32, 'CUST'),
      courseName: clamp(course.courseName, 140),
      createdByUid: course.createdByUid || uid,
      createdAt: serverTimestamp(),
    });
  } catch (err) {
    handleFirestoreError(err, OperationType.WRITE, path);
  }
}

export async function pushSessionToCloud(
  session: AttendanceSession,
  uid: string
): Promise<void> {
  const id = sanitizeId(session.id);
  const path = `sessions/${id}`;
  const allowed = (
    session.allowedCourses.length > 0
      ? session.allowedCourses
      : ['General Programme']
  )
    .slice(0, 15)
    .map((c) => clamp(c, 140));

  try {
    await setDoc(doc(db, 'sessions', id), {
      id,
      title: clamp(session.title, 140),
      courseCode: clamp(session.courseCode, 32),
      universityId: clamp(session.universityId, 64, 'dekut'),
      universityName: clamp(
        session.universityName,
        140,
        'DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY'
      ),
      department: clamp(session.department, 120),
      allowedCourses: allowed,
      sessionType: session.sessionType,
      totalWeeks: Math.min(20, Math.max(1, session.totalWeeks)),
      venue: clamp(session.venue, 100),
      instructorName: clamp(session.instructorName, 100),
      customColumns: session.customColumns.slice(0, 10).map((c) => clamp(c, 60)),
      sessionColumnNames: session.sessionColumnNames
        .slice(0, 20)
        .map((c) => clamp(c, 60)),
      ownerUid: session.ownerUid || uid,
      createdAt: serverTimestamp(),
    });
  } catch (err) {
    handleFirestoreError(err, OperationType.WRITE, path);
  }
}

export async function pushRecordToCloud(
  record: AttendanceRecord,
  uid: string
): Promise<void> {
  const sessId = sanitizeId(record.sessionId);
  const recId = sanitizeId(record.id);
  const path = `sessions/${sessId}/records/${recId}`;
  try {
    await setDoc(doc(db, 'sessions', sessId, 'records', recId), {
      id: recId,
      sessionId: sessId,
      studentCardUid: clamp(record.studentCardUid, 32),
      regNumber: clamp(record.regNumber, 40),
      studentName: clamp(record.studentName, 100),
      studentCourse: clamp(record.studentCourse, 140),
      studentDepartment: clamp(record.studentDepartment, 120),
      weekNumber: record.weekNumber,
      timestamp: record.timestamp,
      status: record.status,
      orderNumber: Math.max(1, record.orderNumber),
      attestationToken: clamp(record.attestationToken, 32),
      customValues: record.customValues || {},
      recordedByUid: uid,
      createdAt: serverTimestamp(),
    });
  } catch (err) {
    handleFirestoreError(err, OperationType.WRITE, path);
  }
}

export async function syncDatabaseNow(
  universityId: string
): Promise<{ added: number; updated: number; totalCloud: number }> {
  const user = auth.currentUser;
  if (!user) {
    throw new Error('Sign in with Google first to sync with the cloud database.');
  }

  // Push local students that belong to this user or have valid UIDs
  const localStudents = getStudents();
  for (const s of localStudents) {
    if (normalizeUid(s.cardUid).length >= 4 && (!s.createdByUid || s.createdByUid === user.uid)) {
      await pushStudentToCloud(s, user.uid);
    }
  }

  const localCourses = getCustomCourses();
  for (const c of localCourses) {
    if (!c.createdByUid || c.createdByUid === user.uid) {
      await pushCustomCourseToCloud(c, user.uid);
    }
  }

  // Pull latest students for the selected university from Firestore server
  const studentsPath = 'students';
  const cloudStudents: Student[] = [];
  try {
    const q = query(
      collection(db, studentsPath),
      where('universityId', '==', universityId)
    );
    const snap = await getDocsFromServer(q);
    snap.forEach((docSnap) => {
      const d = docSnap.data();
      cloudStudents.push({
        id: String(d.id || docSnap.id),
        cardUid: String(d.cardUid || ''),
        regNumber: String(d.regNumber || ''),
        fullName: String(d.fullName || ''),
        universityId: String(d.universityId || universityId),
        universityName: String(d.universityName || ''),
        department: String(d.department || ''),
        courseCode: String(d.courseCode || ''),
        courseName: String(d.courseName || ''),
        createdByUid: d.createdByUid ? String(d.createdByUid) : undefined,
        updatedAt:
          d.updatedAt && typeof d.updatedAt.toMillis === 'function'
            ? d.updatedAt.toMillis()
            : Date.now(),
      });
    });
  } catch (err) {
    handleFirestoreError(err, OperationType.LIST, studentsPath);
  }

  const coursesPath = 'customCourses';
  const cloudCourses: CustomCourse[] = [];
  try {
    const cq = query(
      collection(db, coursesPath),
      where('universityId', '==', universityId)
    );
    const cSnap = await getDocsFromServer(cq);
    cSnap.forEach((docSnap) => {
      const d = docSnap.data();
      cloudCourses.push({
        id: String(d.id || docSnap.id),
        universityId: String(d.universityId || universityId),
        department: String(d.department || ''),
        courseCode: String(d.courseCode || ''),
        courseName: String(d.courseName || ''),
        createdByUid: d.createdByUid ? String(d.createdByUid) : undefined,
      });
    });
  } catch (err) {
    handleFirestoreError(err, OperationType.LIST, coursesPath);
  }

  const mergeRes = mergeCloudStudents(cloudStudents);
  mergeCloudCustomCourses(cloudCourses);
  return { ...mergeRes, totalCloud: cloudStudents.length };
}

export function subscribeUniversityRoster(
  universityId: string,
  onSynced: () => void
): () => void {
  const path = 'students';
  const q = query(
    collection(db, path),
    where('universityId', '==', universityId)
  );
  return onSnapshot(
    q,
    (snapshot) => {
      const cloudStudents: Student[] = [];
      snapshot.forEach((docSnap) => {
        const d = docSnap.data();
        cloudStudents.push({
          id: String(d.id || docSnap.id),
          cardUid: String(d.cardUid || ''),
          regNumber: String(d.regNumber || ''),
          fullName: String(d.fullName || ''),
          universityId: String(d.universityId || universityId),
          universityName: String(d.universityName || ''),
          department: String(d.department || ''),
          courseCode: String(d.courseCode || ''),
          courseName: String(d.courseName || ''),
          createdByUid: d.createdByUid ? String(d.createdByUid) : undefined,
          updatedAt:
            d.updatedAt && typeof d.updatedAt.toMillis === 'function'
              ? d.updatedAt.toMillis()
              : Date.now(),
        });
      });
      if (cloudStudents.length > 0) {
        mergeCloudStudents(cloudStudents);
        onSynced();
      }
    },
    (error) => {
      handleFirestoreError(error, OperationType.LIST, path);
    }
  );
}
