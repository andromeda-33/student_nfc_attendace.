import React, { useMemo, useState } from 'react';
import {
  CheckCircle2,
  Database,
  Nfc,
  PlusCircle,
  Smartphone,
  Touchpad,
  X,
} from 'lucide-react';
import {
  buildUniversityCrestSvg,
  DEFAULT_UNIVERSITY_ID,
  getUniversityById,
  KENYAN_UNIVERSITIES,
} from '../data/kenyanUniversities';
import { CustomCourse, Student } from '../types/tapattend';
import { playSuccessChime, playUnrecognizedSound } from '../utils/audio';
import {
  addCustomCourseLocal,
  addOrUpdateStudent,
  findStudentByCardUid,
  normalizeUid,
} from '../utils/storage';

interface CardRegistrationModalProps {
  customCourses: CustomCourse[];
  isSignedIn: boolean;
  onDismiss: () => void;
  onStudentSaved: (student: Student) => Promise<void> | void;
  onCustomCourseAdded: (course: CustomCourse) => Promise<void> | void;
}

export const CardRegistrationModal: React.FC<CardRegistrationModalProps> = ({
  customCourses,
  isSignedIn,
  onDismiss,
  onStudentSaved,
  onCustomCourseAdded,
}) => {
  const [scannedUid, setScannedUid] = useState<string | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [webNfcListening, setWebNfcListening] = useState(false);
  const [statusNote, setStatusNote] = useState<string | null>(null);

  const [universityId, setUniversityId] = useState(DEFAULT_UNIVERSITY_ID);
  const [customUniversityName, setCustomUniversityName] = useState('');
  const selectedUni = useMemo(() => getUniversityById(universityId), [universityId]);

  const departments = useMemo(() => {
    const base = selectedUni.departments.map((d) => d.name);
    const extras = customCourses
      .filter((c) => c.universityId === universityId)
      .map((c) => c.department);
    return Array.from(new Set([...base, ...extras]));
  }, [selectedUni, customCourses, universityId]);

  const [department, setDepartment] = useState(
    selectedUni.departments[0]?.name || 'Department of Computer Science'
  );
  const [isCustomDept, setIsCustomDept] = useState(false);
  const [customDeptInput, setCustomDeptInput] = useState('');

  const effectiveDepartment = isCustomDept
    ? customDeptInput.trim() || 'General Department'
    : department;

  const availableCourses = useMemo(() => {
    const deptObj = selectedUni.departments.find(
      (d) => d.name.toLowerCase() === effectiveDepartment.toLowerCase()
    );
    const preset = deptObj ? deptObj.courses : [];
    const custom = customCourses
      .filter(
        (c) =>
          c.universityId === universityId &&
          c.department.toLowerCase() === effectiveDepartment.toLowerCase()
      )
      .map((c) => ({ code: c.courseCode, name: c.courseName }));

    const merged = [...preset];
    for (const c of custom) {
      if (!merged.some((m) => m.name.toLowerCase() === c.name.toLowerCase())) {
        merged.push(c);
      }
    }
    return merged;
  }, [selectedUni, effectiveDepartment, customCourses, universityId]);

  const [selectedCourseName, setSelectedCourseName] = useState(
    selectedUni.departments[0]?.courses[0]?.name || 'B.Sc. Computer Science'
  );
  const [showAddCustomCourse, setShowAddCustomCourse] = useState(false);
  const [newCourseCode, setNewCourseCode] = useState('');
  const [newCourseName, setNewCourseName] = useState('');

  const [regNumber, setRegNumber] = useState('');
  const [fullName, setFullName] = useState('');
  const [saving, setSaving] = useState(false);

  const handleUniversityChange = (newUniId: string) => {
    setUniversityId(newUniId);
    if (newUniId === 'custom') {
      setIsCustomDept(true);
      return;
    }
    const uni = getUniversityById(newUniId);
    const firstDept = uni.departments[0];
    setIsCustomDept(false);
    if (firstDept) {
      setDepartment(firstDept.name);
      if (firstDept.courses[0]) {
        setSelectedCourseName(firstDept.courses[0].name);
      }
    }
  };

  const handleDepartmentChange = (deptName: string) => {
    if (deptName === '__CUSTOM__') {
      setIsCustomDept(true);
      return;
    }
    setIsCustomDept(false);
    setDepartment(deptName);
    const deptObj = selectedUni.departments.find((d) => d.name === deptName);
    if (deptObj?.courses[0]) {
      setSelectedCourseName(deptObj.courses[0].name);
    }
  };

  const handleCardCaptured = (rawUid: string) => {
    const cleanUid = normalizeUid(rawUid);
    if (!cleanUid) return;
    setScannedUid(cleanUid);
    setFormOpen(true);

    const existing = findStudentByCardUid(cleanUid);
    if (existing) {
      playSuccessChime();
      setFullName(existing.fullName);
      setRegNumber(existing.regNumber);
      setUniversityId(existing.universityId || DEFAULT_UNIVERSITY_ID);
      setDepartment(existing.department);
      setSelectedCourseName(existing.courseName);
      setStatusNote(
        `Card ${cleanUid} is already registered to ${existing.fullName}. Edit below to update.`
      );
    } else {
      playUnrecognizedSound();
      setFullName('');
      setRegNumber('');
      setStatusNote(
        `New card detected (${cleanUid})! Complete the student details below.`
      );
    }
  };

  const handleSimulateCardNear = () => {
    const randomHex =
      '04' +
      Math.floor(0x10000000 + Math.random() * 0xefffffff)
        .toString(16)
        .toUpperCase();
    handleCardCaptured(randomHex);
  };

  const handleStartWebNfc = async () => {
    const win = window as unknown as {
      NDEFReader?: new () => {
        scan: () => Promise<void>;
        onreading: ((event: { serialNumber?: string }) => void) | null;
      };
    };
    if (!win.NDEFReader) {
      setStatusNote(
        'Web NFC requires Chrome on an NFC-enabled Android device. Use "Simulate Card Tap" here.'
      );
      return;
    }
    try {
      const ndef = new win.NDEFReader();
      await ndef.scan();
      setWebNfcListening(true);
      setStatusNote('NFC Listener Active — bring a student card closer to the phone.');
      ndef.onreading = (ev) => {
        const uid = normalizeUid(ev.serialNumber || '');
        if (uid) handleCardCaptured(uid);
      };
    } catch (err) {
      setStatusNote(`NFC error: ${String(err)}`);
    }
  };

  const handleAddCustomCourse = async () => {
    if (!newCourseName.trim()) return;
    const created = addCustomCourseLocal({
      universityId: universityId === 'custom' ? 'custom' : selectedUni.id,
      department: effectiveDepartment,
      courseCode: newCourseCode.trim() || 'CUST',
      courseName: newCourseName.trim(),
    });
    await onCustomCourseAdded(created);
    setSelectedCourseName(created.courseName);
    setNewCourseCode('');
    setNewCourseName('');
    setShowAddCustomCourse(false);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!scannedUid || !regNumber.trim() || !fullName.trim()) return;
    setSaving(true);
    try {
      const matchedCourse = availableCourses.find(
        (c) => c.name === selectedCourseName
      );
      const uniName =
        universityId === 'custom'
          ? customUniversityName.trim().toUpperCase() || 'CUSTOM UNIVERSITY'
          : selectedUni.name;

      const saved = addOrUpdateStudent({
        cardUid: scannedUid,
        regNumber: regNumber.trim().toUpperCase(),
        fullName: fullName.trim(),
        universityId: universityId === 'custom' ? 'custom' : selectedUni.id,
        universityName: uniName,
        department: effectiveDepartment,
        courseCode: matchedCourse?.code || 'GEN',
        courseName: selectedCourseName.trim() || 'General Programme',
      });
      await onStudentSaved(saved);
      playSuccessChime();
      setStatusNote(
        `Saved ${saved.fullName} (${saved.courseName}) to ${
          isSignedIn ? 'Cloud Database & Local Roster' : 'Local Roster'
        }! Ready for next card.`
      );
      setScannedUid(null);
      setFormOpen(false);
      setFullName('');
      setRegNumber('');
    } finally {
      setSaving(false);
    }
  };

  const uniLogoHtml = buildUniversityCrestSvg(selectedUni, 36);

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl w-full max-w-lg max-h-[92vh] flex flex-col overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div
              className="shrink-0"
              dangerouslySetInnerHTML={{ __html: uniLogoHtml }}
            />
            <div>
              <h2 className="text-base font-bold text-slate-900">
                Register NFC Card to Database
              </h2>
              <p className="text-xs text-slate-500">
                Bring card closer to toggle the enrollment form
              </p>
            </div>
          </div>
          <button
            onClick={onDismiss}
            className="p-1.5 rounded-lg text-slate-400 hover:bg-slate-100 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 overflow-y-auto space-y-4">
          {/* Card Listener Zone */}
          <div
            className={`rounded-2xl border-2 border-dashed p-5 text-center transition-colors ${
              formOpen
                ? 'bg-emerald-50/70 border-emerald-300'
                : 'bg-sky-50/70 border-[#0284C7]/40'
            }`}
          >
            <div className="w-14 h-14 mx-auto rounded-full bg-white border border-slate-200 shadow-xs flex items-center justify-center mb-2.5">
              <Nfc
                className={`w-7 h-7 ${
                  formOpen ? 'text-[#059669]' : 'text-[#0284C7] animate-pulse'
                }`}
              />
            </div>
            {formOpen && scannedUid ? (
              <div>
                <div className="text-sm font-bold text-emerald-800">
                  Card Captured: <span className="font-mono">{scannedUid}</span>
                </div>
                <p className="text-xs text-emerald-700 mt-0.5">
                  Fill student details below to save to database
                </p>
              </div>
            ) : (
              <div>
                <div className="text-sm font-bold text-slate-900">
                  Bring Student NFC Card Closer...
                </div>
                <p className="text-xs text-slate-500 mt-0.5">
                  Form will toggle open automatically once the card UID is read
                </p>
              </div>
            )}

            <div className="flex flex-wrap items-center justify-center gap-2 mt-3.5">
              <button
                type="button"
                onClick={handleSimulateCardNear}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-xs font-semibold cursor-pointer"
              >
                <Touchpad className="w-3.5 h-3.5" />
                <span>Simulate Card Tap</span>
              </button>
              <button
                type="button"
                onClick={handleStartWebNfc}
                className={`inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl border text-xs font-semibold cursor-pointer ${
                  webNfcListening
                    ? 'bg-emerald-100 border-emerald-300 text-emerald-800'
                    : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                }`}
              >
                <Smartphone className="w-3.5 h-3.5" />
                <span>{webNfcListening ? 'Web NFC Listening' : 'Turn On Web NFC'}</span>
              </button>
            </div>
          </div>

          {statusNote && (
            <div className="rounded-xl bg-slate-100 border border-slate-200 px-3.5 py-2.5 text-xs text-slate-700 flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-[#0284C7] shrink-0" />
              <span>{statusNote}</span>
            </div>
          )}

          {/* Toggled Student Registration Form */}
          {formOpen && (
            <form onSubmit={handleSave} className="space-y-3.5 pt-1">
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">
                  1. University
                </label>
                <select
                  value={universityId}
                  onChange={(e) => handleUniversityChange(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white focus:outline-none focus:border-[#0284C7]"
                >
                  {KENYAN_UNIVERSITIES.map((u) => (
                    <option key={u.id} value={u.id}>
                      {u.name} ({u.shortName})
                    </option>
                  ))}
                  <option value="custom">+ Other / Custom University</option>
                </select>
                {universityId === 'custom' && (
                  <input
                    type="text"
                    value={customUniversityName}
                    onChange={(e) => setCustomUniversityName(e.target.value)}
                    placeholder="Enter full university name"
                    className="mt-2 w-full px-3 py-2 text-sm rounded-xl border border-slate-300 focus:outline-none focus:border-[#0284C7]"
                  />
                )}
              </div>

              {/* Department comes BEFORE Course */}
              <div>
                <label className="block text-xs font-semibold text-slate-600 mb-1">
                  2. Department (Select before Course)
                </label>
                <select
                  value={isCustomDept ? '__CUSTOM__' : department}
                  onChange={(e) => handleDepartmentChange(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white focus:outline-none focus:border-[#0284C7]"
                >
                  {departments.map((d) => (
                    <option key={d} value={d}>
                      {d}
                    </option>
                  ))}
                  <option value="__CUSTOM__">+ Custom Department</option>
                </select>
                {isCustomDept && (
                  <input
                    type="text"
                    value={customDeptInput}
                    onChange={(e) => setCustomDeptInput(e.target.value)}
                    placeholder="e.g. Department of Actuarial Science"
                    className="mt-2 w-full px-3 py-2 text-sm rounded-xl border border-slate-300 focus:outline-none focus:border-[#0284C7]"
                  />
                )}
              </div>

              {/* Course / Programme */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <label className="text-xs font-semibold text-slate-600">
                    3. Course / Programme
                  </label>
                  <button
                    type="button"
                    onClick={() => setShowAddCustomCourse(!showAddCustomCourse)}
                    className="text-xs font-semibold text-[#0284C7] hover:underline inline-flex items-center gap-1 cursor-pointer"
                  >
                    <PlusCircle className="w-3.5 h-3.5" />
                    <span>{showAddCustomCourse ? 'Cancel' : 'Add Custom Course'}</span>
                  </button>
                </div>

                {showAddCustomCourse ? (
                  <div className="rounded-xl bg-sky-50 border border-sky-200 p-3 space-y-2">
                    <div className="grid grid-cols-3 gap-2">
                      <input
                        type="text"
                        value={newCourseCode}
                        onChange={(e) => setNewCourseCode(e.target.value.toUpperCase())}
                        placeholder="Code (e.g. C030)"
                        className="px-2.5 py-1.5 text-xs rounded-lg border border-slate-300 bg-white"
                      />
                      <input
                        type="text"
                        value={newCourseName}
                        onChange={(e) => setNewCourseName(e.target.value)}
                        placeholder="Course Name (e.g. B.Sc. Cyber Security)"
                        className="col-span-2 px-2.5 py-1.5 text-xs rounded-lg border border-slate-300 bg-white"
                      />
                    </div>
                    <button
                      type="button"
                      onClick={() => void handleAddCustomCourse()}
                      className="px-3 py-1.5 rounded-lg bg-[#0284C7] text-white text-xs font-semibold cursor-pointer"
                    >
                      Save Custom Course
                    </button>
                  </div>
                ) : (
                  <select
                    value={selectedCourseName}
                    onChange={(e) => setSelectedCourseName(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white focus:outline-none focus:border-[#0284C7]"
                  >
                    {availableCourses.map((c) => (
                      <option key={`${c.code}-${c.name}`} value={c.name}>
                        {c.code} — {c.name}
                      </option>
                    ))}
                    {availableCourses.length === 0 && (
                      <option value="General Programme">General Programme</option>
                    )}
                  </select>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 mb-1">
                    4. Registration Number *
                  </label>
                  <input
                    type="text"
                    required
                    value={regNumber}
                    onChange={(e) => setRegNumber(e.target.value.toUpperCase())}
                    placeholder="e.g. C026-01-0123/2023"
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 focus:outline-none focus:border-[#0284C7]"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600 mb-1">
                    5. Full Name *
                  </label>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="e.g. Jane Wanjiru"
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 focus:outline-none focus:border-[#0284C7]"
                  />
                </div>
              </div>

              <div className="pt-2 flex items-center justify-between gap-2 border-t border-slate-100">
                <span className="text-[11px] text-slate-500 inline-flex items-center gap-1">
                  <Database className="w-3.5 h-3.5 text-[#059669]" />
                  {isSignedIn
                    ? 'Saves directly to Cloud Firestore'
                    : 'Saves locally (Sign in to sync)'}
                </span>
                <button
                  type="submit"
                  disabled={saving || !regNumber.trim() || !fullName.trim()}
                  className="px-5 py-2.5 rounded-xl bg-[#059669] hover:bg-emerald-700 disabled:opacity-40 text-white text-sm font-semibold cursor-pointer"
                >
                  {saving ? 'Saving...' : 'Save Student to Database'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
