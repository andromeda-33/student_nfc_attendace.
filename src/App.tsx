import { useCallback, useEffect, useState } from 'react';
import {
  onAuthStateChanged,
  signInWithPopup,
  signOut,
  User,
} from 'firebase/auth';
import { Check, Copy, KeyRound, X } from 'lucide-react';
import { auth, firebaseAppConfig, googleProvider } from './firebase';
import { DEFAULT_UNIVERSITY_ID } from './data/kenyanUniversities';
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
  normalizeUid,
  recordTapForSession,
  saveStudents,
} from './utils/storage';
import {
  pushCustomCourseToCloud,
  pushRecordToCloud,
  pushSessionToCloud,
  pushStudentToCloud,
  subscribeUniversityRoster,
  syncDatabaseNow,
} from './utils/cloudSync';
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
import { CardRegistrationModal } from './components/CardRegistrationModal';
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
  const [user, setUser] = useState<User | null>(null);
  const [authReady, setAuthReady] = useState(false);

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
  const [showCardRegisterModal, setShowCardRegisterModal] = useState(false);
  const [pendingRegistrationCard, setPendingRegistrationCard] =
    useState<ParsedCardData | null>(null);
  const [showCardInspectorModal, setShowCardInspectorModal] = useState(false);
  const [showApkLinkModal, setShowApkLinkModal] = useState(false);
  const [copiedToken, setCopiedToken] = useState(false);

  const [lastScannedUid, setLastScannedUid] = useState<string | null>(null);
  const [lastScanResult, setLastScanResult] = useState<{
    status: string;
    record: AttendanceRecord | null;
    wrongCourseStudent?: Student;
  } | null>(null);
  const [pendingCardToEnroll, setPendingCardToEnroll] =
    useState<ParsedCardData | null>(null);
  const [webNfcActive, setWebNfcActive] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [lastSyncedLabel, setLastSyncedLabel] = useState<string | null>(null);
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

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, (u) => {
      setUser(u);
      setAuthReady(true);
    });
    return () => unsub();
  }, []);

  // Attach real-time Firestore listener only when authenticated
  useEffect(() => {
    if (!authReady || !user) return;
    const uniId = activeSession?.universityId || DEFAULT_UNIVERSITY_ID;
    const unsub = subscribeUniversityRoster(uniId, () => {
      refreshData();
    });
    return () => unsub();
  }, [authReady, user, activeSession?.universityId, refreshData]);

  const handleSignIn = async () => {
    try {
      await signInWithPopup(auth, googleProvider);
      showToast('Signed in! Tap "Update Roster" to sync with Cloud Database.');
    } catch (err) {
      showToast(`Sign-in error: ${String(err)}`);
    }
  };

  const handleSignOut = async () => {
    await signOut(auth);
    showToast('Signed out of Cloud Database.');
  };

  const handleSyncDatabase = async () => {
    if (!user) {
      await handleSignIn();
      return;
    }
    setSyncing(true);
    try {
      const uniId = activeSession?.universityId || DEFAULT_UNIVERSITY_ID;
      const res = await syncDatabaseNow(uniId);
      refreshData();
      setLastSyncedLabel(
        new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      );
      showToast(
        `Roster updated! ${res.totalCloud} cloud students synced (${res.added} new).`
      );
    } catch (err) {
      showToast(`Sync failed: ${err instanceof Error ? err.message : String(err)}`);
    } finally {
      setSyncing(false);
    }
  };

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

      if (result.type === 'SUCCESS') {
        playSuccessChime();
        setLastScanResult({
          status: `VERIFIED: ${result.student.fullName}`,
          record: result.record,
        });
        refreshData();
        if (user) {
          void pushRecordToCloud(result.record, user.uid);
        }
      } else if (result.type === 'DUPLICATE') {
        playDuplicateWarningBuzz();
        setLastScanResult({
          status: 'ALREADY SCANNED TODAY',
          record: result.record,
        });
        showToast(`${result.student.fullName} is already marked present!`);
      } else if (result.type === 'WRONG_COURSE') {
        playUnrecognizedSound();
        setLastScanResult({
          status: `WRONG_COURSE: ${result.student.courseName}`,
          record: null,
          wrongCourseStudent: result.student,
        });
        showToast(
          `Blocked: ${result.student.fullName} belongs to ${result.student.courseName}`
        );
      } else if (cardData.hasLocalStudentData) {
        // Offline local card recognition: student Name + Reg + Course read straight from card memory
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

        if (autoRes.type === 'SUCCESS') {
          playSuccessChime();
          setLastScanResult({
            status: `VERIFIED: ${autoRes.student.fullName}`,
            record: autoRes.record,
          });
          refreshData();
          if (user) {
            void pushStudentToCloud(autoRes.student, user.uid);
            void pushRecordToCloud(autoRes.record, user.uid);
          }
          showToast(`Read from card & marked present: ${autoRes.student.fullName}`);
        } else if (autoRes.type === 'WRONG_COURSE') {
          playUnrecognizedSound();
          setLastScanResult({
            status: `WRONG_COURSE: ${autoRes.student.courseName}`,
            record: null,
            wrongCourseStudent: autoRes.student,
          });
          showToast(
            `Blocked: ${autoRes.student.fullName} belongs to ${autoRes.student.courseName}`
          );
        } else if (autoRes.type === 'DUPLICATE') {
          playDuplicateWarningBuzz();
          setLastScanResult({
            status: 'ALREADY SCANNED TODAY',
            record: autoRes.record,
          });
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
    [activeSession, activeWeek, refreshData, screen, showToast, user]
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
        'Web NFC requires Chrome on an NFC-enabled Android device. Use Simulate Tap here.'
      );
      return;
    }

    if (isEmbeddedIframe()) {
      showToast(
        'Web NFC is blocked inside embedded preview frames. Open the Shared App URL in a standalone Android Chrome tab or use the Android APK.'
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
          userEmail={user?.email ?? null}
          syncing={syncing}
          lastSyncedLabel={lastSyncedLabel}
          onSelectSession={(session) => {
            setActiveSessionId(session.id);
            setActiveWeek(1);
            setScreen('LIVE_SCAN');
          }}
          onCreateNewSession={() => setShowCreateDialog(true)}
          onOpenRoster={() => setScreen('STUDENT_ROSTER')}
          onOpenCardRegistration={() => {
            setPendingRegistrationCard(null);
            setShowCardRegisterModal(true);
          }}
          onOpenCardInspector={() => setShowCardInspectorModal(true)}
          onSyncDatabase={() => void handleSyncDatabase()}
          onOpenApkLink={() => setShowApkLinkModal(true)}
          onSignIn={() => void handleSignIn()}
          onSignOut={() => void handleSignOut()}
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
          onCustomCourseAdded={async (course) => {
            refreshData();
            if (user) {
              await pushCustomCourseToCloud(course, user.uid);
            }
          }}
          onCreate={(newSession) => {
            addSession(newSession);
            refreshData();
            if (user) {
              void pushSessionToCloud(newSession, user.uid);
            }
            setShowCreateDialog(false);
            setActiveSessionId(newSession.id);
            setActiveWeek(1);
            setScreen('LIVE_SCAN');
          }}
        />
      )}

      {showCardRegisterModal && (
        <CardRegistrationModal
          initialCardData={pendingRegistrationCard}
          customCourses={customCourses}
          isSignedIn={Boolean(user)}
          onDismiss={() => {
            setShowCardRegisterModal(false);
            setPendingRegistrationCard(null);
          }}
          onStudentSaved={async (savedStudent) => {
            refreshData();
            if (user) {
              await pushStudentToCloud(savedStudent, user.uid);
            }
          }}
          onCustomCourseAdded={async (course) => {
            refreshData();
            if (user) {
              await pushCustomCourseToCloud(course, user.uid);
            }
          }}
        />
      )}

      {showCardInspectorModal && (
        <CardInspectorModal
          onDismiss={() => setShowCardInspectorModal(false)}
          onSendToRegistration={(parsed) => {
            setShowCardInspectorModal(false);
            setPendingRegistrationCard(parsed);
            setShowCardRegisterModal(true);
          }}
        />
      )}

      {showApkLinkModal && user && (() => {
        const apkSyncPayload = JSON.stringify({
          apiKey: firebaseAppConfig.apiKey,
          refreshToken: user.refreshToken,
        });
        return (
          <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl border border-slate-200 shadow-xl w-full max-w-md p-5 space-y-3.5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <KeyRound className="w-5 h-5 text-[#0284C7]" />
                  <h3 className="text-base font-bold text-slate-900">
                    Link Android APK to Cloud DB
                  </h3>
                </div>
                <button
                  onClick={() => setShowApkLinkModal(false)}
                  className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 cursor-pointer"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <p className="text-xs text-slate-600 leading-relaxed">
                Copy this Cloud Sync Key and paste it once inside your Android APK&apos;s{' '}
                <strong>Cloud Sync</strong> menu. Your APK will then push newly tapped student cards and pull updates whenever you press{' '}
                <strong>Update Roster</strong>.
              </p>
              <div className="rounded-xl bg-slate-100 border border-slate-200 p-3 font-mono text-[11px] break-all max-h-28 overflow-y-auto text-slate-800">
                {apkSyncPayload}
              </div>
              <div className="flex justify-end gap-2">
                <button
                  onClick={() => {
                    void navigator.clipboard.writeText(apkSyncPayload);
                    setCopiedToken(true);
                    setTimeout(() => setCopiedToken(false), 2000);
                  }}
                  className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-xs font-semibold cursor-pointer"
                >
                  {copiedToken ? (
                    <>
                      <Check className="w-4 h-4" />
                      <span>Copied!</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-4 h-4" />
                      <span>Copy Sync Key</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        );
      })()}

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
          syncing={syncing}
          onWeekChanged={setActiveWeek}
          onBack={() => setScreen('HOME')}
          onOpenMatrix={() => setScreen('SEMESTER_MATRIX')}
          onSyncRoster={() => void handleSyncDatabase()}
          onManualCheckIn={(student) => {
            void (async () => {
              const weekNum = isMultiSession(activeSession) ? activeWeek : null;
              const result = await manualCheckInStudent(
                activeSession,
                student,
                weekNum
              );
              if (result.type === 'SUCCESS') {
                playSuccessChime();
                setLastScanResult({
                  status: `VERIFIED: ${result.student.fullName}`,
                  record: result.record,
                });
                refreshData();
                if (user) {
                  void pushRecordToCloud(result.record, user.uid);
                }
              } else if (result.type === 'DUPLICATE') {
                playDuplicateWarningBuzz();
                setLastScanResult({
                  status: 'ALREADY SCANNED TODAY',
                  record: result.record,
                });
                showToast(`${result.student.fullName} is already marked present!`);
              } else if (result.type === 'WRONG_COURSE') {
                playUnrecognizedSound();
                setLastScanResult({
                  status: `WRONG_COURSE: ${result.student.courseName}`,
                  record: null,
                  wrongCourseStudent: result.student,
                });
                showToast(
                  `Blocked: ${result.student.fullName} belongs to ${result.student.courseName}`
                );
              }
            })();
          }}
          onSimulateScan={() => {
            const withCard = students.filter((s) => s.cardUid.trim() !== '');
            if (withCard.length > 0) {
              const pick =
                withCard[Math.floor(Math.random() * withCard.length)];
              void processCardData({ cardUid: pick.cardUid });
            } else {
              const fakeUid =
                '86A' +
                Math.floor(Math.random() * 0xffffff)
                  .toString(16)
                  .toUpperCase()
                  .padStart(6, '0');
              void processCardData({ cardUid: fakeUid });
            }
          }}
          onSimulateUnknownCard={() => {
            const fakeUid =
              '86A' +
              Math.floor(1 + Math.random() * 0xfffffe)
                .toString(16)
                .toUpperCase()
                .padStart(6, '0');
            void processCardData({ cardUid: fakeUid });
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
              if (res.type === 'SUCCESS') {
                playSuccessChime();
                setLastScanResult({
                  status: `VERIFIED: ${res.student.fullName}`,
                  record: res.record,
                });
                refreshData();
                if (user) {
                  void pushStudentToCloud(res.student, user.uid);
                  void pushRecordToCloud(res.record, user.uid);
                }
                showToast(`Enrolled & Marked: ${res.student.fullName}`);
              } else if (res.type === 'DUPLICATE') {
                playDuplicateWarningBuzz();
                setLastScanResult({
                  status: 'ALREADY SCANNED TODAY',
                  record: res.record,
                });
                showToast('Already marked present!');
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
          syncing={syncing}
          onBack={() => setScreen('HOME')}
          onSyncRoster={() => void handleSyncDatabase()}
          onOpenCardRegistration={() => setShowCardRegisterModal(true)}
          onAddStudent={(student) => {
            const saved = addOrUpdateStudent(student);
            refreshData();
            if (user) {
              void pushStudentToCloud(saved, user.uid);
            }
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
