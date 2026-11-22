import { useCallback, useEffect, useState } from 'react';
import {
  AttendanceRecord,
  AttendanceSession,
  CustomCourse,
  isMultiSession,
  ParsedCardData,
  Student,
} from './types/tapattend';
import {
  addOrUpdateStudent,
  addSession,
  addSessionColumn,
  deleteSession,
  enrollAndRecordTap,
  getCustomCourses,
  getRecords,
  getSessions,
  getStudents,
  importStudentsFromCsv,
  manualCheckInStudent,
  manualFastCheckIn,
  normalizeUid,
  recordTapForSession,
  RecordTapResult,
  saveStudents,
} from './utils/storage';
import {
  playDuplicateWarningBuzz,
  playSuccessChime,
  playUnrecognizedSound,
} from './utils/audio';
import { exportSemesterMatrixCsv } from './utils/exporter';
import { HomeScreen } from './components/HomeScreen';
import { CreateSessionDialog } from './components/CreateSessionDialog';
import { LiveScanScreen } from './components/LiveScanScreen';
import { SemesterMatrixScreen } from './components/SemesterMatrixScreen';
import { StudentRosterScreen } from './components/StudentRosterScreen';
import { CardInspectorModal } from './components/CardInspectorModal';
import {
  decodeWebNdefReading,
  formatWebNfcError,
  isEmbeddedIframe,
  WebNdefReadingEventLike,
} from './utils/nfcParser';

type AppScreen = 'HOME' | 'LIVE_SCAN' | 'SEMESTER_MATRIX' | 'STUDENT_ROSTER';

export default function App() {
  const [screen, setScreen] = useState<AppScreen>('HOME');

  const [students, setStudents] = useState<Student[]>(() => getStudents());
  const [sessions, setSessions] = useState<AttendanceSession[]>(() =>
    getSessions()
  );
  const [records, setRecords] = useState<AttendanceRecord[]>(() => getRecords());
  const [customCourses, setCustomCourses] = useState<CustomCourse[]>(() =>
    getCustomCourses()
  );

  const [activeSessionId, setActiveSessionId] = useState<string | null>(null);
  const [activeWeek, setActiveWeek] = useState<number>(1);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showCardInspectorModal, setShowCardInspectorModal] = useState(false);

  const [lastScannedUid, setLastScannedUid] = useState<string | null>(null);
  const [lastScanResult, setLastScanResult] = useState<{
    status: string;
    record: AttendanceRecord | null;
    wrongCourseStudent?: Student;
  } | null>(null);
  const [pendingCardToEnroll, setPendingCardToEnroll] =
    useState<ParsedCardData | null>(null);
  const [webNfcActive, setWebNfcActive] = useState(false);
  const [bannerMessage, setBannerMessage] = useState<string | null>(null);

  const activeSession =
    sessions.find((s) => s.id === activeSessionId) ?? null;

  const refreshData = useCallback(() => {
    setStudents(getStudents());
    setSessions(getSessions());
    setRecords(getRecords());
    setCustomCourses(getCustomCourses());
  }, []);

  const showToast = useCallback((msg: string) => {
    setBannerMessage(msg);
    setTimeout(() => {
      setBannerMessage((prev) => (prev === msg ? null : prev));
    }, 3500);
  }, []);

  const applyTapResult = useCallback(
    (res: RecordTapResult, successToast?: string) => {
      if (res.type === 'SUCCESS') {
        playSuccessChime();
        setLastScanResult({
          status: `VERIFIED: ${res.student.fullName}`,
          record: res.record,
        });
        refreshData();
        if (successToast) {
          showToast(successToast);
        }
      } else if (res.type === 'DUPLICATE') {
        playDuplicateWarningBuzz();
        setLastScanResult({
          status: 'ALREADY SCANNED TODAY',
          record: res.record,
        });
        showToast(`${res.student.fullName} is already marked present!`);
      } else if (res.type === 'WRONG_COURSE') {
        playUnrecognizedSound();
        setLastScanResult({
          status: `WRONG_COURSE: ${res.student.courseName}`,
          record: null,
          wrongCourseStudent: res.student,
        });
        showToast(
          `Blocked: ${res.student.fullName} belongs to ${res.student.courseName}`
        );
      }
    },
    [refreshData, showToast]
  );

  const processCardData = useCallback(
    async (cardData: ParsedCardData) => {
      const cleanUid = normalizeUid(cardData.cardUid);
      setLastScannedUid(cleanUid);

      if (!activeSession || screen !== 'LIVE_SCAN') {
        if (cardData.hasLocalStudentData) {
          showToast(
            `Read locally: ${cardData.suggestedName} (${cardData.suggestedRegNumber})`
          );
        } else {
          showToast(`NFC Card Scanned: ${cleanUid}`);
        }
        return;
      }

      const weekNum = isMultiSession(activeSession) ? activeWeek : null;
      const result = await recordTapForSession(activeSession, cleanUid, weekNum);

      if (result.type !== 'UNKNOWN_CARD') {
        applyTapResult(result);
      } else if (cardData.hasLocalStudentData) {
        const autoRes = await enrollAndRecordTap({
          session: activeSession,
          cardUid: cleanUid,
          weekNumber: weekNum,
          existingStudentIdToLink: null,
          newRegNumber: cardData.suggestedRegNumber,
          newFullName: cardData.suggestedName,
          universityId: activeSession.universityId,
          universityName: activeSession.universityName,
          department: cardData.suggestedDepartment || activeSession.department,
          courseCode: cardData.suggestedCourseCode || activeSession.courseCode,
          courseName:
            cardData.suggestedCourse ||
            activeSession.allowedCourses[0] ||
            'General Programme',
        });
        if (autoRes.type !== 'UNKNOWN_CARD') {
          applyTapResult(
            autoRes,
            autoRes.type === 'SUCCESS'
              ? `Read from card & marked present: ${autoRes.student.fullName}`
              : undefined
          );
        }
      } else {
        playUnrecognizedSound();
        setLastScanResult({
          status: `UNKNOWN CARD: ${cleanUid}`,
          record: null,
        });
        setPendingCardToEnroll({ ...cardData, cardUid: cleanUid });
      }
    },
    [activeSession, activeWeek, applyTapResult, screen, showToast]
  );

  const handleEnableWebNfc = async () => {
    const win = window as unknown as {
      NDEFReader?: new () => {
        scan: () => Promise<void>;
        onreading: ((event: WebNdefReadingEventLike) => void) | null;
      };
    };

    if (!win.NDEFReader) {
      showToast(
        'Web NFC requires Chrome on an NFC-enabled Android device. Use Fast Manual Check-In here.'
      );
      return;
    }

    if (isEmbeddedIframe()) {
      showToast(
        'Web NFC is blocked inside embedded preview frames. Open in a standalone Android Chrome tab or use the Android APK.'
      );
      return;
    }

    try {
      const ndef = new win.NDEFReader();
      await ndef.scan();
      setWebNfcActive(true);
      showToast('Web NFC active! Hold a student card to the back of the device.');
      ndef.onreading = (event) => {
        const parsed = decodeWebNdefReading(event);
        if (parsed.cardUid) {
          void processCardData(parsed);
        }
      };
    } catch (err) {
      showToast(formatWebNfcError(err).message);
    }
  };

  useEffect(() => {
    setLastScanResult(null);
  }, [activeSessionId, activeWeek]);

  return (
    <div className="min-h-screen bg-[#F8FAFC]">
      {bannerMessage && (
        <div className="fixed bottom-5 left-1/2 -translate-x-1/2 z-50 px-4 py-2.5 rounded-xl bg-slate-900 text-white text-xs sm:text-sm font-medium shadow-lg max-w-md text-center">
          {bannerMessage}
        </div>
      )}

      {screen === 'HOME' && (
        <HomeScreen
          sessions={sessions}
          records={records}
          studentCount={students.length}
          onSelectSession={(session) => {
            setActiveSessionId(session.id);
            setActiveWeek(1);
            setScreen('LIVE_SCAN');
          }}
          onCreateNewSession={() => setShowCreateDialog(true)}
          onOpenRoster={() => setScreen('STUDENT_ROSTER')}
          onOpenCardInspector={() => setShowCardInspectorModal(true)}
          onDeleteSession={(session) => {
            deleteSession(session.id);
            refreshData();
          }}
        />
      )}

      {showCreateDialog && (
        <CreateSessionDialog
          customCourses={customCourses}
          onDismiss={() => setShowCreateDialog(false)}
          onCustomCourseAdded={() => {
            refreshData();
          }}
          onCreate={(newSession) => {
            addSession(newSession);
            refreshData();
            setShowCreateDialog(false);
            setActiveSessionId(newSession.id);
            setActiveWeek(1);
            setScreen('LIVE_SCAN');
          }}
        />
      )}

      {showCardInspectorModal && (
        <CardInspectorModal
          onDismiss={() => setShowCardInspectorModal(false)}
        />
      )}

      {screen === 'LIVE_SCAN' && activeSession && (
        <LiveScanScreen
          session={activeSession}
          records={records.filter(
            (r) =>
              r.sessionId === activeSession.id &&
              (!isMultiSession(activeSession) || r.weekNumber === activeWeek)
          )}
          students={students}
          customCourses={customCourses}
          selectedWeek={activeWeek}
          lastScannedResult={lastScanResult}
          pendingCardToEnroll={pendingCardToEnroll}
          webNfcActive={webNfcActive}
          onWeekChanged={setActiveWeek}
          onBack={() => setScreen('HOME')}
          onOpenMatrix={() => setScreen('SEMESTER_MATRIX')}
          onManualCheckIn={(student) => {
            void (async () => {
              const weekNum = isMultiSession(activeSession) ? activeWeek : null;
              const result = await manualCheckInStudent(
                activeSession,
                student,
                weekNum
              );
              applyTapResult(result);
            })();
          }}
          onFastManualCheckIn={(params) => {
            void (async () => {
              const weekNum = isMultiSession(activeSession) ? activeWeek : null;
              const result = await manualFastCheckIn({
                session: activeSession,
                regNumber: params.regNumber,
                fullName: params.fullName,
                courseName: params.courseName,
                department: params.department,
                weekNumber: weekNum,
                customValues: params.customValues,
              });
              applyTapResult(
                result,
                result.type === 'SUCCESS'
                  ? `Marked present: ${result.student.fullName}`
                  : undefined
              );
            })();
          }}
          onEnableWebNfc={() => {
            void handleEnableWebNfc();
          }}
          onDismissEnrollDialog={() => setPendingCardToEnroll(null)}
          onSaveAndEnrollTap={(params) => {
            void (async () => {
              const res = await enrollAndRecordTap({
                session: activeSession,
                cardUid: params.cardUid,
                weekNumber: isMultiSession(activeSession) ? activeWeek : null,
                existingStudentIdToLink: params.existingStudentId,
                newRegNumber: params.newReg || undefined,
                newFullName: params.newName || undefined,
                universityId: params.universityId,
                universityName: params.universityName,
                department: params.department,
                courseCode: params.courseCode,
                courseName: params.courseName,
                customValues: params.customValues,
              });
              applyTapResult(
                res,
                res.type === 'SUCCESS'
                  ? `Marked present: ${res.student.fullName}`
                  : undefined
              );
            })();
          }}
        />
      )}

      {screen === 'SEMESTER_MATRIX' && activeSession && (
        <SemesterMatrixScreen
          session={activeSession}
          onBack={() => setScreen('LIVE_SCAN')}
          onExportCsv={() => exportSemesterMatrixCsv(activeSession)}
          onAddSessionColumn={(colName) => {
            addSessionColumn(activeSession.id, colName);
            refreshData();
          }}
        />
      )}

      {screen === 'STUDENT_ROSTER' && (
        <StudentRosterScreen
          students={students}
          customCourses={customCourses}
          lastScannedUid={lastScannedUid}
          onBack={() => setScreen('HOME')}
          onAddStudent={(student) => {
            addOrUpdateStudent(student);
            refreshData();
          }}
          onDeleteStudent={(student) => {
            saveStudents(students.filter((s) => s.id !== student.id));
            refreshData();
          }}
          onImportCsv={(csvText, defaults) => {
            const count = importStudentsFromCsv(csvText, defaults);
            refreshData();
            return count;
          }}
        />
      )}
    </div>
  );
}
