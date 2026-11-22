import React, { useMemo, useState } from 'react';
import {
  ArrowLeft,
  CreditCard,
  Plus,
  Search,
  Trash2,
  Upload,
  UserPlus,
  X,
} from 'lucide-react';
import {
  ALL_KENYAN_DEPARTMENTS,
  DEFAULT_UNIVERSITY_ID,
  getUniversityById,
  KENYAN_UNIVERSITIES,
} from '../data/kenyanUniversities';
import { CustomCourse, Student } from '../types/tapattend';

interface StudentRosterScreenProps {
  students: Student[];
  customCourses: CustomCourse[];
  lastScannedUid: string | null;
  onBack: () => void;
  onAddStudent: (student: Omit<Student, 'id'>) => void;
  onDeleteStudent: (student: Student) => void;
  onImportCsv: (
    csvText: string,
    defaults: {
      universityId: string;
      universityName: string;
      department: string;
      courseCode: string;
      courseName: string;
    }
  ) => number;
}

export const StudentRosterScreen: React.FC<StudentRosterScreenProps> = ({
  students,
  customCourses,
  lastScannedUid,
  onBack,
  onAddStudent,
  onDeleteStudent,
  onImportCsv,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showImportDialog, setShowImportDialog] = useState(false);

  const [universityId, setUniversityId] = useState(DEFAULT_UNIVERSITY_ID);
  const selectedUni = useMemo(() => getUniversityById(universityId), [universityId]);
  const allDepartments = useMemo(() => {
    const merged = [...selectedUni.departments, ...ALL_KENYAN_DEPARTMENTS];
    const seen = new Set<string>();
    return merged.filter((d) => {
      const k = d.name.toLowerCase();
      if (seen.has(k)) return false;
      seen.add(k);
      return true;
    });
  }, [selectedUni]);

  const [department, setDepartment] = useState(
    allDepartments[0]?.name || 'Department of Computer Science'
  );
  const [courseName, setCourseName] = useState(
    allDepartments[0]?.courses[0]?.name || 'Bachelor of Science in Computer Science'
  );

  const [cardUid, setCardUid] = useState(lastScannedUid || '');
  const [regNumber, setRegNumber] = useState('');
  const [fullName, setFullName] = useState('');

  const [csvText, setCsvText] = useState(
    'C026-01-0101/2024, Jane Wanjiru, Department of Computer Science, Bachelor of Science in Computer Science\nE021-01-0102/2024, Kevin Otieno, Department of Electrical & Electronic Engineering, Bachelor of Science in Electrical and Electronics Engineering'
  );
  const [toast, setToast] = useState<string | null>(null);

  const deptCourses = useMemo(() => {
    const preset =
      allDepartments.find((d) => d.name === department)?.courses || [];
    const custom = customCourses
      .filter((c) => c.universityId === universityId && c.department === department)
      .map((c) => ({ code: c.courseCode, name: c.courseName }));
    return [...preset, ...custom];
  }, [allDepartments, department, customCourses, universityId]);

  const filtered = students.filter(
    (s) =>
      s.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.regNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.cardUid.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.courseName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.department.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const openAdd = () => {
    setCardUid(lastScannedUid || '');
    setRegNumber('');
    setFullName('');
    setShowAddDialog(true);
  };

  const handleSaveStudent = (e: React.FormEvent) => {
    e.preventDefault();
    if (!regNumber.trim() || !fullName.trim()) return;
    const matched = deptCourses.find((c) => c.name === courseName);
    onAddStudent({
      cardUid: cardUid.trim().toUpperCase(),
      regNumber: regNumber.trim().toUpperCase(),
      fullName: fullName.trim(),
      universityId: selectedUni.id,
      universityName: selectedUni.name,
      department,
      courseCode: matched?.code || 'GEN',
      courseName,
    });
    setShowAddDialog(false);
    setToast(`Saved: ${fullName.trim()}`);
    setTimeout(() => setToast(null), 2600);
  };

  const handleImport = (e: React.FormEvent) => {
    e.preventDefault();
    if (!csvText.trim()) return;
    const count = onImportCsv(csvText, {
      universityId: selectedUni.id,
      universityName: selectedUni.name,
      department,
      courseCode: deptCourses[0]?.code || 'GEN',
      courseName,
    });
    setShowImportDialog(false);
    setToast(`Imported ${count} student(s) into directory!`);
    setTimeout(() => setToast(null), 3000);
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] text-[#0F172A] pb-20">
      <header className="sticky top-0 z-20 bg-white border-b border-slate-200">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-2">
          <div className="flex items-center gap-3 min-w-0">
            <button
              onClick={onBack}
              className="p-2 -ml-2 rounded-xl hover:bg-slate-100 text-slate-700 cursor-pointer shrink-0"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <h1 className="text-base sm:text-lg font-bold truncate">
              Student Directory
            </h1>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={() => setShowImportDialog(true)}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-xs sm:text-sm font-semibold text-slate-700 cursor-pointer"
            >
              <Upload className="w-4 h-4 text-[#0284C7]" />
              <span className="hidden md:inline">Import CSV</span>
            </button>
            <button
              onClick={openAdd}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-xs sm:text-sm font-semibold cursor-pointer"
            >
              <UserPlus className="w-4 h-4" />
              <span>Add Student</span>
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 sm:px-6 pt-5 space-y-4">
        {toast && (
          <div className="rounded-xl bg-emerald-50 border border-emerald-200 px-4 py-2.5 text-sm font-medium text-emerald-800">
            {toast}
          </div>
        )}

        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by name, reg number, course, department, or card UID..."
            className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-white border border-slate-200 text-sm focus:outline-none focus:border-[#0284C7]"
          />
        </div>

        <div className="flex items-center justify-between text-xs sm:text-sm">
          <span className="font-bold text-slate-700">
            Total Saved: {students.length} students
          </span>
          {lastScannedUid && (
            <span className="font-mono text-xs text-[#0284C7] bg-sky-50 px-2.5 py-1 rounded-md border border-sky-200">
              Last Card: {lastScannedUid}
            </span>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {filtered.map((s) => {
            const hasCard = Boolean(s.cardUid.trim());
            return (
              <div
                key={s.id}
                className="bg-white rounded-xl border border-slate-200 p-3.5 flex items-center gap-3"
              >
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${
                    hasCard
                      ? 'bg-[#E0F2FE] text-[#0284C7]'
                      : 'bg-slate-100 text-slate-400'
                  }`}
                >
                  <CreditCard className="w-5 h-5" />
                </div>

                <div className="flex-1 min-w-0">
                  <div className="text-sm font-bold text-slate-900 truncate">
                    {s.fullName}
                  </div>
                  <div className="text-xs text-slate-600 truncate">
                    {s.regNumber} • {s.courseName}
                  </div>
                  <div className="text-[11px] text-slate-400 truncate">
                    {s.department} • {s.universityName}
                  </div>
                  {hasCard && (
                    <div className="text-[11px] font-mono text-[#0284C7] mt-0.5">
                      UID: {s.cardUid}
                    </div>
                  )}
                </div>

                <button
                  onClick={() => onDeleteStudent(s)}
                  className="p-2 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 cursor-pointer"
                  title="Remove student"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            );
          })}
        </div>
      </main>

      {showAddDialog && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
          <form
            onSubmit={handleSaveStudent}
            className="bg-white rounded-2xl border border-slate-200 shadow-xl w-full max-w-md p-5 space-y-3"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-slate-900">
                Add Student to Directory
              </h3>
              <button
                type="button"
                onClick={() => setShowAddDialog(false)}
                className="p-1.5 rounded-lg text-slate-400 hover:bg-slate-100 cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                University
              </label>
              <select
                value={universityId}
                onChange={(e) => {
                  const id = e.target.value;
                  setUniversityId(id);
                  const uni = getUniversityById(id);
                  const firstDept = uni.departments[0];
                  if (firstDept) {
                    setDepartment(firstDept.name);
                    if (firstDept.courses[0]) setCourseName(firstDept.courses[0].name);
                  }
                }}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white"
              >
                {KENYAN_UNIVERSITIES.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Department (Before Course)
              </label>
              <select
                value={department}
                onChange={(e) => {
                  const d = e.target.value;
                  setDepartment(d);
                  const found = allDepartments.find((x) => x.name === d);
                  if (found?.courses[0]) setCourseName(found.courses[0].name);
                }}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white"
              >
                {allDepartments.map((d) => (
                  <option key={d.name} value={d.name}>
                    {d.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Course / Programme
              </label>
              <select
                value={courseName}
                onChange={(e) => setCourseName(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white"
              >
                {deptCourses.map((c) => (
                  <option key={c.name} value={c.name}>
                    {c.code} — {c.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                NFC Card UID (Hex, optional)
              </label>
              <input
                type="text"
                value={cardUid}
                onChange={(e) => setCardUid(e.target.value.toUpperCase())}
                placeholder="e.g. 86A098BF"
                className="w-full px-3 py-2 text-sm font-mono rounded-xl border border-slate-300"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Registration Number *
              </label>
              <input
                type="text"
                required
                value={regNumber}
                onChange={(e) => setRegNumber(e.target.value.toUpperCase())}
                placeholder="e.g. E021-01-1034/2024"
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Full Name *
              </label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Joseph Karoki Mwangi"
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
              />
            </div>

            <div className="pt-2 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setShowAddDialog(false)}
                className="px-4 py-2 rounded-xl text-sm font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-sm font-semibold cursor-pointer inline-flex items-center gap-1.5"
              >
                <Plus className="w-4 h-4" />
                Save Student
              </button>
            </div>
          </form>
        </div>
      )}

      {showImportDialog && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
          <form
            onSubmit={handleImport}
            className="bg-white rounded-2xl border border-slate-200 shadow-xl w-full max-w-lg p-5 space-y-3.5"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-bold text-slate-900">
                  Bulk Import Class List
                </h3>
                <p className="text-xs text-slate-500">
                  Format: RegNumber, FullName, Department, CourseName, [CardUID]
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowImportDialog(false)}
                className="p-1.5 rounded-lg text-slate-400 hover:bg-slate-100 cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <textarea
              rows={7}
              value={csvText}
              onChange={(e) => setCsvText(e.target.value)}
              className="w-full p-3 text-xs font-mono rounded-xl border border-slate-300"
            />

            <div className="pt-2 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setShowImportDialog(false)}
                className="px-4 py-2 rounded-xl text-sm font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-sm font-semibold cursor-pointer"
              >
                Import Roster
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
