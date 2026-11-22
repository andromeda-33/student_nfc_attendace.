import React, { useMemo, useState } from 'react';
import {
  AlertTriangle,
  ArrowLeft,
  Calendar,
  Check,
  ChevronLeft,
  ChevronRight,
  FileSpreadsheet,
  FileText,
  Grid,
  List,
  Lock,
  Nfc,
  Printer,
  Share2,
  ShieldAlert,
  Smartphone,
  UserPlus,
  X,
} from 'lucide-react';
import {
  buildUniversityCrestSvg,
  getUniversityById,
  inferDepartmentFromCourse,
  isCourseMatch,
} from '../data/kenyanUniversities';
import {
  AttendanceRecord,
  AttendanceSession,
  CustomCourse,
  ExportFormat,
  getColumnName,
  getTotalSessionsCount,
  isMultiSession,
  ParsedCardData,
  Student,
} from '../types/tapattend';
import { getCourseSheetGroups, handleExportFormat } from '../utils/exporter';
import { normalizeUid } from '../utils/storage';

interface LiveScanScreenProps {
  session: AttendanceSession;
  records: AttendanceRecord[];
  students: Student[];
  customCourses: CustomCourse[];
  selectedWeek: number;
  lastScannedResult: {
    status: string;
    record: AttendanceRecord | null;
    wrongCourseStudent?: Student;
  } | null;
  pendingCardToEnroll: ParsedCardData | null;
  webNfcActive: boolean;
  onWeekChanged: (week: number) => void;
  onBack: () => void;
  onOpenMatrix: () => void;
  onManualCheckIn: (student: Student) => void;
  onFastManualCheckIn: (params: {
    regNumber: string;
    fullName: string;
    courseName: string;
    department: string;
    customValues: Record<string, string>;
  }) => void;
  onEnableWebNfc: () => void;
  onDismissEnrollDialog: () => void;
  onSaveAndEnrollTap: (params: {
    cardUid: string;
    existingStudentId: string | null;
    newReg: string | null;
    newName: string | null;
    universityId: string;
    universityName: string;
    department: string;
    courseCode: string;
    courseName: string;
    customValues: Record<string, string>;
  }) => void;
}

export const LiveScanScreen: React.FC<LiveScanScreenProps> = ({
  session,
  records,
  students,
  customCourses,
  selectedWeek,
  lastScannedResult,
  pendingCardToEnroll,
  webNfcActive,
  onWeekChanged,
  onBack,
  onOpenMatrix,
  onManualCheckIn,
  onFastManualCheckIn,
  onEnableWebNfc,
  onDismissEnrollDialog,
  onSaveAndEnrollTap,
}) => {
  const [showManualDialog, setShowManualDialog] = useState(false);
  const [manualTab, setManualTab] = useState<'fast' | 'roster'>('fast');
  const [fastFullName, setFastFullName] = useState('');
  const [fastRegNumber, setFastRegNumber] = useState('');
  const [fastCourseName, setFastCourseName] = useState(
    session.allowedCourses[0] || 'General Programme'
  );
  const [fastCustomValues, setFastCustomValues] = useState<Record<string, string>>({});

  const [showExportDialog, setShowExportDialog] = useState(false);
  const [showPdfPreview, setShowPdfPreview] = useState(false);
  const [manualQuery, setManualQuery] = useState('');

  const totalSessions = getTotalSessionsCount(session);
  const multi = isMultiSession(session);

  const isSuccess = lastScannedResult?.status.startsWith('VERIFIED') ?? false;
  const isDuplicate = lastScannedResult?.status.startsWith('ALREADY') ?? false;
  const isWrongCourse = lastScannedResult?.status.startsWith('WRONG_COURSE') ?? false;

  const alreadyPresentUids = new Set(
    records.map((r) => normalizeUid(r.studentCardUid)).filter(Boolean)
  );
  const alreadyPresentRegs = new Set(
    records.map((r) => r.regNumber.trim().toLowerCase())
  );

  const filteredManualStudents = students.filter(
    (s) =>
      s.fullName.toLowerCase().includes(manualQuery.toLowerCase()) ||
      s.regNumber.toLowerCase().includes(manualQuery.toLowerCase()) ||
      s.courseName.toLowerCase().includes(manualQuery.toLowerCase())
  );

  const resolvedFastDept =
    inferDepartmentFromCourse(fastCourseName) || session.department;

  const formatTime = (ts: number) =>
    new Date(ts).toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    });

  const uni = useMemo(
    () => getUniversityById(session.universityId),
    [session.universityId]
  );
  const headerCrestSvg = useMemo(() => buildUniversityCrestSvg(uni, 34), [uni]);
  const uniCrestSvg = useMemo(() => buildUniversityCrestSvg(uni, 54), [uni]);
  const courseSheetGroups = useMemo(
    () => getCourseSheetGroups(session, records),
    [session, records]
  );

  const openManualModal = () => {
    setFastFullName('');
    setFastRegNumber('');
    setFastCourseName(session.allowedCourses[0] || 'General Programme');
    setFastCustomValues({});
    setManualTab('fast');
    setShowManualDialog(true);
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] text-[#0F172A] pb-12">
      <header className="sticky top-0 z-20 bg-white border-b border-slate-200 print:hidden">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-3">
          <div className="flex items-center gap-3 min-w-0">
            <button
              onClick={onBack}
              className="p-2 -ml-2 rounded-xl hover:bg-slate-100 text-slate-700 cursor-pointer shrink-0"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div
              className="shrink-0"
              dangerouslySetInnerHTML={{ __html: headerCrestSvg }}
            />
            <div className="min-w-0">
              <h1 className="text-base sm:text-lg font-bold truncate">
                {session.courseCode}
              </h1>
              <p className="text-xs text-slate-500 truncate">
                {session.universityName}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            {multi && (
              <button
                onClick={onOpenMatrix}
                className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-xs sm:text-sm font-semibold text-slate-700 cursor-pointer"
              >
                <Grid className="w-4 h-4 text-[#0284C7]" />
                <span>Matrix</span>
              </button>
            )}
            <button
              onClick={() => setShowExportDialog(true)}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-xs sm:text-sm font-semibold cursor-pointer"
            >
              <Share2 className="w-4 h-4" />
              <span>Export</span>
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 sm:px-6 pt-5 grid grid-cols-1 lg:grid-cols-12 gap-5 print:hidden">
        {/* Left: NFC Tap Radar & Controls */}
        <section className="lg:col-span-5 space-y-4">
          <div className="bg-white rounded-2xl border border-slate-200 p-5 space-y-4 shadow-xs">
            <div className="rounded-xl bg-slate-50 border border-slate-200 p-3 text-xs space-y-1">
              <div className="font-bold text-slate-800">{session.department}</div>
              <div className="text-slate-600">
                Allowed Course(s):{' '}
                <span className="font-semibold text-[#0284C7]">
                  {session.allowedCourses.join(', ')}
                </span>
              </div>
              {session.allowedCourses.length > 1 && (
                <div className="text-[11px] text-emerald-700 font-medium">
                  Common class mode: generates {session.allowedCourses.length} separate
                  course sheets on export/print.
                </div>
              )}
            </div>

            {multi && (
              <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                <span className="text-xs font-semibold text-slate-500">
                  Active Column:
                </span>
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => selectedWeek > 1 && onWeekChanged(selectedWeek - 1)}
                    disabled={selectedWeek <= 1}
                    className="p-1.5 rounded-lg hover:bg-slate-100 disabled:opacity-30 cursor-pointer"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <span className="px-3 py-1 rounded-lg bg-[#E0F2FE] text-[#0369A1] text-xs font-bold">
                    {getColumnName(session, selectedWeek)}
                  </span>
                  <button
                    onClick={() =>
                      selectedWeek < totalSessions && onWeekChanged(selectedWeek + 1)
                    }
                    disabled={selectedWeek >= totalSessions}
                    className="p-1.5 rounded-lg hover:bg-slate-100 disabled:opacity-30 cursor-pointer"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}

            {/* Pulsing NFC Tap Zone */}
            <div className="flex flex-col items-center text-center pt-1">
              <button
                type="button"
                onClick={onEnableWebNfc}
                className={`relative w-36 h-36 rounded-full flex flex-col items-center justify-center border-4 transition-all cursor-pointer ${
                  isSuccess
                    ? 'bg-emerald-50 border-[#10B981] text-[#10B981]'
                    : isDuplicate
                    ? 'bg-amber-50 border-[#F59E0B] text-[#F59E0B]'
                    : isWrongCourse
                    ? 'bg-red-50 border-[#DC2626] text-[#DC2626]'
                    : 'bg-sky-50 border-[#0284C7] text-[#0284C7] hover:scale-[1.02]'
                }`}
              >
                {!isSuccess && !isDuplicate && !isWrongCourse && (
                  <span className="absolute inset-0 rounded-full border-2 border-[#0284C7]/40 animate-ping" />
                )}
                {isSuccess ? (
                  <Check className="w-12 h-12" />
                ) : isDuplicate ? (
                  <AlertTriangle className="w-12 h-12" />
                ) : isWrongCourse ? (
                  <ShieldAlert className="w-12 h-12" />
                ) : (
                  <Nfc className="w-12 h-12" />
                )}
                <span className="text-xs font-extrabold tracking-wide mt-1">
                  {isSuccess
                    ? 'TAP OK'
                    : isDuplicate
                    ? 'DUPLICATE'
                    : isWrongCourse
                    ? 'WRONG CLASS'
                    : webNfcActive
                    ? 'LISTENING'
                    : 'TAP TO SCAN'}
                </span>
              </button>

              <p className="text-sm text-slate-600 mt-3 font-medium">
                {webNfcActive
                  ? 'Hold student card to back of phone'
                  : 'Tap radar to start Web NFC, or use Fast Manual Check-In'}
              </p>

              <div className="mt-2 inline-flex items-center px-3.5 py-1 rounded-full bg-slate-100 text-xs font-bold text-slate-700">
                Present: {records.length} Students
              </div>
            </div>

            {/* Wrong Course Rejection Banner */}
            {isWrongCourse && lastScannedResult?.wrongCourseStudent && (
              <div className="rounded-xl border border-red-200 bg-red-50 p-3.5 text-xs text-red-900 space-y-1">
                <div className="font-bold flex items-center gap-1.5 text-red-700">
                  <ShieldAlert className="w-4 h-4 shrink-0" />
                  <span>Blocked for this Class (Wrong Course)</span>
                </div>
                <p>
                  <strong>{lastScannedResult.wrongCourseStudent.fullName}</strong> (
                  {lastScannedResult.wrongCourseStudent.regNumber}) belongs to{' '}
                  <strong>{lastScannedResult.wrongCourseStudent.courseName}</strong>,
                  which is not enrolled in this sheet ({session.allowedCourses.join(', ')}).
                </p>
              </div>
            )}

            {/* Last Scanned Result Card */}
            {lastScannedResult?.record && (
              <div
                className={`rounded-xl border p-3.5 flex items-center gap-3 ${
                  isDuplicate
                    ? 'bg-amber-50 border-amber-200'
                    : 'bg-emerald-50 border-emerald-200'
                }`}
              >
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-bold shrink-0 ${
                    isDuplicate ? 'bg-[#F59E0B]' : 'bg-[#10B981]'
                  }`}
                >
                  {lastScannedResult.record.studentName.slice(0, 1)}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="text-sm font-bold text-slate-900 truncate">
                    {lastScannedResult.record.studentName}
                  </div>
                  <div className="text-xs text-slate-600">
                    {lastScannedResult.record.regNumber} •{' '}
                    {lastScannedResult.record.studentCourse}
                  </div>
                  <div className="text-[11px] font-mono text-slate-500 flex items-center gap-1 mt-0.5">
                    <Lock className="w-3 h-3 shrink-0" />
                    <span>
                      SHA-256: {lastScannedResult.record.attestationToken} •{' '}
                      {formatTime(lastScannedResult.record.timestamp)}
                    </span>
                  </div>
                </div>
              </div>
            )}

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-1">
              <button
                type="button"
                onClick={openManualModal}
                className="inline-flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-xs sm:text-sm font-semibold cursor-pointer"
              >
                <UserPlus className="w-4 h-4" />
                <span>Fast Manual Check-In</span>
              </button>
              <button
                type="button"
                onClick={onEnableWebNfc}
                className={`inline-flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl border text-xs sm:text-sm font-semibold cursor-pointer ${
                  webNfcActive
                    ? 'bg-emerald-50 border-emerald-300 text-emerald-700'
                    : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                }`}
              >
                <Smartphone className="w-4 h-4" />
                <span>{webNfcActive ? 'Web NFC Active' : 'Start Web NFC'}</span>
              </button>
            </div>
          </div>
        </section>

        {/* Right: Live Roll Call List */}
        <section className="lg:col-span-7">
          <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h2 className="text-base font-bold text-slate-900">
                  Live Roll Call ({records.length})
                </h2>
                <p className="text-xs text-slate-500">
                  {session.venue} • {session.instructorName}
                </p>
              </div>
              {multi && (
                <span className="text-xs font-semibold px-2.5 py-1 rounded-lg bg-slate-100 text-slate-700">
                  {getColumnName(session, selectedWeek)}
                </span>
              )}
            </div>

            {records.length === 0 ? (
              <div className="py-12 text-center border border-dashed border-slate-200 rounded-xl">
                <Nfc className="w-8 h-8 text-slate-300 mx-auto mb-2" />
                <p className="text-sm font-medium text-slate-600">
                  Waiting for first student card tap or manual entry...
                </p>
                <p className="text-xs text-slate-400 mt-1">
                  Accepting: {session.allowedCourses.join(' or ')}
                </p>
              </div>
            ) : (
              <div className="space-y-2 max-h-[540px] overflow-y-auto pr-1">
                {[...records].reverse().map((r) => (
                  <div
                    key={r.id}
                    className="rounded-xl border border-slate-200 p-3 flex items-center justify-between gap-3 hover:bg-slate-50/70"
                  >
                    <div className="min-w-0">
                      <div className="text-sm font-semibold text-slate-900 truncate">
                        {r.studentName}
                      </div>
                      <div className="text-xs text-slate-500">
                        {r.regNumber} • {r.studentCourse} • {formatTime(r.timestamp)}
                      </div>
                      <div className="text-[11px] font-mono text-slate-400">
                        {r.attestationToken}
                      </div>
                    </div>
                    <span className="text-xs font-bold text-[#059669] bg-emerald-50 px-2.5 py-1 rounded-md shrink-0">
                      ✓ {r.status}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      </main>

      {/* Fast Manual Check-In Modal (Pre-filled Department & Course) */}
      {showManualDialog && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xl w-full max-w-md overflow-hidden">
            <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between">
              <h3 className="text-base font-bold">Fast Manual Check-In</h3>
              <button
                onClick={() => setShowManualDialog(false)}
                className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-4 space-y-3">
              <div className="grid grid-cols-2 rounded-xl bg-slate-100 p-1 text-xs font-semibold">
                <button
                  type="button"
                  onClick={() => setManualTab('fast')}
                  className={`py-2 rounded-lg cursor-pointer ${
                    manualTab === 'fast'
                      ? 'bg-white shadow-xs text-[#0284C7]'
                      : 'text-slate-600'
                  }`}
                >
                  Quick Entry (Name &amp; Reg)
                </button>
                <button
                  type="button"
                  onClick={() => setManualTab('roster')}
                  className={`py-2 rounded-lg cursor-pointer ${
                    manualTab === 'roster'
                      ? 'bg-white shadow-xs text-[#0284C7]'
                      : 'text-slate-600'
                  }`}
                >
                  Saved Roster
                </button>
              </div>

              {manualTab === 'fast' ? (
                <form
                  onSubmit={(e) => {
                    e.preventDefault();
                    if (!fastFullName.trim() || !fastRegNumber.trim()) return;
                    onFastManualCheckIn({
                      regNumber: fastRegNumber.trim().toUpperCase(),
                      fullName: fastFullName.trim(),
                      courseName: fastCourseName,
                      department: resolvedFastDept,
                      customValues: fastCustomValues,
                    });
                    setShowManualDialog(false);
                  }}
                  className="space-y-3"
                >
                  <div className="rounded-xl bg-sky-50 border border-sky-200 px-3 py-2.5 text-xs space-y-0.5">
                    <div className="text-[10px] font-bold uppercase tracking-wider text-[#0369A1]">
                      Pre-filled from Sheet
                    </div>
                    <div className="font-semibold text-slate-900">
                      {session.universityName}
                    </div>
                    <div className="text-slate-700">
                      Department: <span className="font-medium">{resolvedFastDept}</span>
                    </div>
                    {session.allowedCourses.length <= 1 && (
                      <div className="text-slate-900 font-bold">
                        Course: {fastCourseName}
                      </div>
                    )}
                  </div>

                  {session.allowedCourses.length > 1 && (
                    <div>
                      <label className="block text-xs font-semibold text-slate-600 mb-1">
                        Course (From Sheet)
                      </label>
                      <select
                        value={fastCourseName}
                        onChange={(e) => setFastCourseName(e.target.value)}
                        className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white"
                      >
                        {session.allowedCourses.map((c) => (
                          <option key={c} value={c}>
                            {c}
                          </option>
                        ))}
                      </select>
                    </div>
                  )}

                  <div>
                    <label className="block text-xs font-semibold text-slate-600 mb-1">
                      Student Full Name *
                    </label>
                    <input
                      type="text"
                      required
                      autoFocus
                      value={fastFullName}
                      onChange={(e) => setFastFullName(e.target.value)}
                      placeholder="e.g. Joseph Karoki Mwangi"
                      className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 focus:outline-none focus:border-[#0284C7]"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-600 mb-1">
                      Registration Number *
                    </label>
                    <input
                      type="text"
                      required
                      value={fastRegNumber}
                      onChange={(e) => setFastRegNumber(e.target.value.toUpperCase())}
                      placeholder="e.g. E021-01-1034/2024"
                      className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 focus:outline-none focus:border-[#0284C7]"
                    />
                  </div>

                  {session.customColumns.length > 0 && (
                    <div className="space-y-2 pt-1 border-t border-slate-100">
                      {session.customColumns.map((col) => (
                        <input
                          key={col}
                          type="text"
                          placeholder={col}
                          value={fastCustomValues[col] || ''}
                          onChange={(e) =>
                            setFastCustomValues({
                              ...fastCustomValues,
                              [col]: e.target.value,
                            })
                          }
                          className="w-full px-3 py-1.5 text-sm rounded-lg border border-slate-300"
                        />
                      ))}
                    </div>
                  )}

                  <div className="flex justify-end gap-2 pt-1">
                    <button
                      type="button"
                      onClick={() => setShowManualDialog(false)}
                      className="px-4 py-2 rounded-xl text-sm font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={!fastFullName.trim() || !fastRegNumber.trim()}
                      className="px-4 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] disabled:opacity-40 text-white text-sm font-semibold cursor-pointer inline-flex items-center gap-1.5"
                    >
                      <Check className="w-4 h-4" />
                      Mark Present
                    </button>
                  </div>
                </form>
              ) : (
                <>
                  <input
                    type="text"
                    value={manualQuery}
                    onChange={(e) => setManualQuery(e.target.value)}
                    placeholder="Search Name, Reg No, or Course..."
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 focus:outline-none focus:border-[#0284C7]"
                  />
                  <div className="max-h-72 overflow-y-auto divide-y divide-slate-100">
                    {filteredManualStudents.map((s) => {
                      const cleanUid = normalizeUid(s.cardUid);
                      const isPresent =
                        (cleanUid !== '' && alreadyPresentUids.has(cleanUid)) ||
                        alreadyPresentRegs.has(s.regNumber.trim().toLowerCase());
                      return (
                        <div
                          key={s.id}
                          className="py-2.5 flex items-center justify-between gap-3"
                        >
                          <div className="min-w-0">
                            <div className="text-sm font-bold text-slate-900 truncate">
                              {s.fullName}
                            </div>
                            <div className="text-xs text-slate-500 truncate">
                              {s.regNumber} • {s.courseName}
                            </div>
                          </div>
                          {isPresent ? (
                            <span className="text-xs font-bold text-[#059669]">
                              Present
                            </span>
                          ) : (
                            <button
                              onClick={() => {
                                onManualCheckIn(s);
                                setShowManualDialog(false);
                              }}
                              className="px-3 py-1.5 rounded-lg bg-[#0284C7] hover:bg-[#0369A1] text-white text-xs font-semibold cursor-pointer shrink-0"
                            >
                              Mark Present
                            </button>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Quick On-Tap Card Enrollment Dialog (for UID-only / locked cards) */}
      {pendingCardToEnroll && (
        <QuickEnrollDialog
          cardData={pendingCardToEnroll}
          session={session}
          students={students}
          customCourses={customCourses}
          onDismiss={onDismissEnrollDialog}
          onSaveAndCheckIn={(params) => {
            onSaveAndEnrollTap(params);
            onDismissEnrollDialog();
          }}
        />
      )}

      {/* Export Format Chooser Modal */}
      {showExportDialog && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xl w-full max-w-md p-5 space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-slate-900">
                Export Attendance Sheet(s)
              </h3>
              <button
                onClick={() => setShowExportDialog(false)}
                className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <p className="text-xs text-slate-500">
              Generates{' '}
              <strong>
                {courseSheetGroups.length} separate sheet
                {courseSheetGroups.length > 1 ? 's' : ''}
              </strong>{' '}
              (1 per course) with {session.universityName} crest &amp; header.
            </p>

            <div className="space-y-2">
              {[
                {
                  fmt: ExportFormat.PDF,
                  title: `PDF / Print Sheets (${courseSheetGroups.length} Page${
                    courseSheetGroups.length > 1 ? 's' : ''
                  })`,
                  sub: 'Official A4 sheets with University crest & signature lines',
                  color: '#DC2626',
                  icon: Printer,
                },
                {
                  fmt: ExportFormat.EXCEL,
                  title: 'Excel Spreadsheet (.xls)',
                  sub: 'One formatted workbook per course with University banner',
                  color: '#107C41',
                  icon: Calendar,
                },
                {
                  fmt: ExportFormat.WORD,
                  title: 'Microsoft Word (.doc)',
                  sub: 'One editable document per course with crest & approvals',
                  color: '#185ABD',
                  icon: FileText,
                },
                {
                  fmt: ExportFormat.CSV,
                  title: 'Standard CSV (.csv)',
                  sub: 'One comma-separated file per course',
                  color: '#0284C7',
                  icon: List,
                },
              ].map((item) => {
                const Icon = item.icon;
                return (
                  <button
                    key={item.fmt}
                    onClick={() => {
                      setShowExportDialog(false);
                      handleExportFormat(item.fmt, session, records, () =>
                        setShowPdfPreview(true)
                      );
                    }}
                    className="w-full text-left flex items-center gap-3 p-3 rounded-xl transition-colors cursor-pointer"
                    style={{ backgroundColor: `${item.color}12` }}
                  >
                    <div
                      className="w-9 h-9 rounded-lg flex items-center justify-center text-white shrink-0"
                      style={{ backgroundColor: item.color }}
                    >
                      <Icon className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-sm font-bold text-slate-900">
                        {item.title}
                      </div>
                      <div className="text-xs text-slate-500">{item.sub}</div>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* Multi-Course Printable A4 PDF Preview Modal (1 Course per Sheet) */}
      {showPdfPreview && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 flex items-center justify-center p-4 print:static print:bg-white print:p-0">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-4xl max-h-[92vh] flex flex-col overflow-hidden print:shadow-none print:max-h-none print:rounded-none">
            <div className="px-5 py-3 border-b border-slate-200 flex items-center justify-between print:hidden">
              <div className="flex items-center gap-2 font-bold text-slate-800 text-sm">
                <FileSpreadsheet className="w-4 h-4 text-[#0284C7]" />
                <span>
                  A4 Print Preview — {courseSheetGroups.length} Course Sheet
                  {courseSheetGroups.length > 1 ? 's' : ''}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => window.print()}
                  className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-[#0284C7] text-white text-xs font-semibold cursor-pointer"
                >
                  <Printer className="w-4 h-4" />
                  Print / Save PDF ({courseSheetGroups.length} Sheet
                  {courseSheetGroups.length > 1 ? 's' : ''})
                </button>
                <button
                  onClick={() => setShowPdfPreview(false)}
                  className="p-1.5 rounded-lg text-slate-500 hover:bg-slate-100 cursor-pointer"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="p-6 sm:p-8 overflow-y-auto space-y-10 text-slate-900 print:p-0 print:space-y-0">
              {courseSheetGroups.map((group, sheetIdx) => (
                <div
                  key={group.courseName}
                  className={`border border-slate-200 rounded-xl p-6 sm:p-8 print:border-0 print:rounded-none print:p-6 ${
                    sheetIdx < courseSheetGroups.length - 1 ? 'print:break-after-page' : ''
                  }`}
                >
                  {/* University Name & Crest Logo Header */}
                  <div
                    className="flex items-center gap-4 pb-4 mb-4 border-b-2"
                    style={{ borderColor: uni.primaryColor }}
                  >
                    <div
                      className="shrink-0"
                      dangerouslySetInnerHTML={{ __html: uniCrestSvg }}
                    />
                    <div className="flex-1 min-w-0">
                      <h2
                        className="text-base sm:text-lg font-extrabold tracking-tight"
                        style={{ color: uni.primaryColor }}
                      >
                        {session.universityName}
                      </h2>
                      <p className="text-xs font-semibold text-slate-700">
                        {inferDepartmentFromCourse(group.courseName) || session.department}
                      </p>
                      <p className="text-xs text-slate-500 mt-0.5">
                        Course / Programme:{' '}
                        <strong className="text-slate-900">{group.courseName}</strong>
                      </p>
                    </div>
                    <div className="text-right text-[11px] text-slate-500 shrink-0">
                      Sheet {sheetIdx + 1} of {courseSheetGroups.length}
                    </div>
                  </div>

                  <div className="mb-4 flex flex-wrap justify-between gap-2 text-xs">
                    <div>
                      <div className="font-bold text-sm text-[#0284C7]">
                        {session.courseCode}: {session.title}
                      </div>
                      <div className="text-slate-600 mt-0.5">
                        Venue: {session.venue} &nbsp;|&nbsp; Instructor:{' '}
                        {session.instructorName}
                      </div>
                    </div>
                    <div className="text-right text-slate-600">
                      <div>
                        Present: <strong>{group.records.length}</strong>
                      </div>
                      <div>{new Date().toLocaleDateString()}</div>
                    </div>
                  </div>

                  <table className="w-full border-collapse text-xs">
                    <thead>
                      <tr className="bg-slate-100 border-y border-slate-300">
                        <th className="py-2 px-2 text-left">S/N</th>
                        <th className="py-2 px-2 text-left">Reg Number</th>
                        <th className="py-2 px-2 text-left">Student Name</th>
                        <th className="py-2 px-2 text-left">Time In</th>
                        <th className="py-2 px-2 text-left">Attestation Token</th>
                        <th className="py-2 px-2 text-left">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200">
                      {group.records.length === 0 ? (
                        <tr>
                          <td
                            colSpan={6}
                            className="py-6 text-center text-slate-400 italic"
                          >
                            No students from {group.courseName} recorded in this session yet.
                          </td>
                        </tr>
                      ) : (
                        group.records.map((r, idx) => (
                          <tr key={r.id}>
                            <td className="py-2 px-2">{idx + 1}</td>
                            <td className="py-2 px-2 font-bold">{r.regNumber}</td>
                            <td className="py-2 px-2">{r.studentName}</td>
                            <td className="py-2 px-2">{formatTime(r.timestamp)}</td>
                            <td className="py-2 px-2 font-mono">
                              {r.attestationToken}
                            </td>
                            <td className="py-2 px-2 font-bold text-emerald-700">
                              {r.status}
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>

                  <div className="pt-10 grid grid-cols-2 gap-12 text-xs">
                    <div className="border-t border-slate-800 pt-2">
                      Class Rep / Invigilator Signature
                    </div>
                    <div className="border-t border-slate-800 pt-2">
                      Lecturer / COD Approval
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

interface QuickEnrollDialogProps {
  cardData: ParsedCardData;
  session: AttendanceSession;
  students: Student[];
  customCourses: CustomCourse[];
  onDismiss: () => void;
  onSaveAndCheckIn: (params: {
    cardUid: string;
    existingStudentId: string | null;
    newReg: string | null;
    newName: string | null;
    universityId: string;
    universityName: string;
    department: string;
    courseCode: string;
    courseName: string;
    customValues: Record<string, string>;
  }) => void;
}

const QuickEnrollDialog: React.FC<QuickEnrollDialogProps> = ({
  cardData,
  session,
  students,
  onDismiss,
  onSaveAndCheckIn,
}) => {
  const [mode, setMode] = useState<'new' | 'link'>('new');
  const [fullName, setFullName] = useState(cardData.suggestedName || '');
  const [regNumber, setRegNumber] = useState(cardData.suggestedRegNumber || '');
  const matchedSessionCourse = cardData.suggestedCourse
    ? session.allowedCourses.find((c) =>
        isCourseMatch(c, cardData.suggestedCourse || '')
      )
    : undefined;
  const [courseName, setCourseName] = useState(
    matchedSessionCourse || session.allowedCourses[0] || 'General Programme'
  );
  const department =
    cardData.suggestedDepartment ||
    inferDepartmentFromCourse(courseName) ||
    session.department;
  const [selectedStudentId, setSelectedStudentId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [customValues, setCustomValues] = useState<Record<string, string>>({});

  const matchingStudents = students.filter(
    (s) =>
      s.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.regNumber.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const canSubmit =
    mode === 'new'
      ? fullName.trim() !== '' && regNumber.trim() !== ''
      : selectedStudentId !== null;

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xl w-full max-w-md p-5 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Nfc className="w-6 h-6 text-[#0284C7]" />
            <div>
              <h3 className="text-base font-bold text-slate-900">
                Card UID Not Mapped Yet
              </h3>
              <p className="text-xs font-mono text-[#0284C7]">
                UID: {cardData.cardUid}
              </p>
            </div>
          </div>
          <button
            onClick={onDismiss}
            className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="grid grid-cols-2 rounded-xl bg-slate-100 p-1 text-xs font-semibold">
          <button
            type="button"
            onClick={() => setMode('new')}
            className={`py-2 rounded-lg cursor-pointer ${
              mode === 'new' ? 'bg-white shadow-xs text-[#0284C7]' : 'text-slate-600'
            }`}
          >
            Enter Name &amp; Reg
          </button>
          <button
            type="button"
            onClick={() => setMode('link')}
            className={`py-2 rounded-lg cursor-pointer ${
              mode === 'link' ? 'bg-white shadow-xs text-[#0284C7]' : 'text-slate-600'
            }`}
          >
            Link from Roster
          </button>
        </div>

        {mode === 'new' ? (
          <div className="space-y-3">
            <div className="rounded-lg bg-sky-50 border border-sky-200 px-3 py-2 text-[11px] text-[#0369A1]">
              Pre-filled: <strong>{session.universityName}</strong> • {department}
            </div>
            {session.allowedCourses.length > 1 ? (
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">
                  Course / Programme
                </label>
                <select
                  value={courseName}
                  onChange={(e) => setCourseName(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white"
                >
                  {session.allowedCourses.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>
            ) : (
              <div className="text-xs font-semibold text-slate-700 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2">
                Course: {courseName}
              </div>
            )}
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Student Full Name *
              </label>
              <input
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Joseph Karoki Mwangi"
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Registration Number *
              </label>
              <input
                type="text"
                value={regNumber}
                onChange={(e) => setRegNumber(e.target.value.toUpperCase())}
                placeholder="e.g. E021-01-1034/2024"
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
              />
            </div>
          </div>
        ) : (
          <div className="space-y-2">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search roster name or reg..."
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
            />
            <div className="max-h-36 overflow-y-auto space-y-1">
              {matchingStudents.map((s) => {
                const selected = selectedStudentId === s.id;
                return (
                  <button
                    key={s.id}
                    type="button"
                    onClick={() => setSelectedStudentId(s.id)}
                    className={`w-full text-left px-3 py-2 rounded-lg flex items-center justify-between cursor-pointer ${
                      selected ? 'bg-[#E0F2FE] text-[#0369A1]' : 'hover:bg-slate-50'
                    }`}
                  >
                    <div>
                      <div className="text-xs font-bold">{s.fullName}</div>
                      <div className="text-[11px] text-slate-500">
                        {s.regNumber} • {s.courseName}
                      </div>
                    </div>
                    {selected && <Check className="w-4 h-4 text-[#0284C7]" />}
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {session.customColumns.length > 0 && (
          <div className="space-y-2 pt-1 border-t border-slate-100">
            <div className="text-xs font-bold text-slate-700">Session Fields:</div>
            {session.customColumns.map((col) => (
              <input
                key={col}
                type="text"
                placeholder={col}
                value={customValues[col] || ''}
                onChange={(e) =>
                  setCustomValues({ ...customValues, [col]: e.target.value })
                }
                className="w-full px-3 py-1.5 text-sm rounded-lg border border-slate-300"
              />
            ))}
          </div>
        )}

        <div className="flex justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={onDismiss}
            className="px-4 py-2 rounded-xl text-sm font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="button"
            disabled={!canSubmit}
            onClick={() =>
              mode === 'new'
                ? onSaveAndCheckIn({
                    cardUid: cardData.cardUid,
                    existingStudentId: null,
                    newReg: regNumber.trim(),
                    newName: fullName.trim(),
                    universityId: session.universityId,
                    universityName: session.universityName,
                    department: department.trim(),
                    courseCode: session.courseCode,
                    courseName,
                    customValues,
                  })
                : onSaveAndCheckIn({
                    cardUid: cardData.cardUid,
                    existingStudentId: selectedStudentId,
                    newReg: null,
                    newName: null,
                    universityId: session.universityId,
                    universityName: session.universityName,
                    department,
                    courseCode: session.courseCode,
                    courseName,
                    customValues,
                  })
            }
            className="px-4 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] disabled:opacity-40 text-white text-sm font-semibold cursor-pointer"
          >
            Save &amp; Mark Present
          </button>
        </div>
      </div>
    </div>
  );
};
