import React, { useMemo, useState } from 'react';
import {
  CalendarDays,
  Check,
  CheckSquare,
  FileUp,
  History,
  PlusCircle,
  SlidersHorizontal,
  X,
} from 'lucide-react';
import {
  buildUniversityCrestSvg,
  DEFAULT_UNIVERSITY_ID,
  getUniversityById,
  KENYAN_UNIVERSITIES,
} from '../data/kenyanUniversities';
import {
  AttendanceSession,
  CustomCourse,
  SessionType,
} from '../types/tapattend';
import { addCustomCourseLocal } from '../utils/storage';

interface CreateSessionDialogProps {
  customCourses: CustomCourse[];
  onDismiss: () => void;
  onCreate: (session: AttendanceSession) => void;
  onCustomCourseAdded: (course: CustomCourse) => Promise<void> | void;
}

const TYPE_OPTIONS: {
  type: SessionType;
  title: string;
  description: string;
  icon: React.FC<{ className?: string }>;
}[] = [
  {
    type: SessionType.SEMESTER_WEEKLY,
    title: 'Whole Semester Attendance',
    description: 'Multi-week semester matrix (Week 1..N) with 75% rule',
    icon: CalendarDays,
  },
  {
    type: SessionType.EXAM,
    title: 'Single Exam / Test Attendance',
    description: 'Records desk/time with digital signature attestation',
    icon: CheckSquare,
  },
  {
    type: SessionType.SUBMISSION,
    title: 'Assignment / Lab Submission',
    description: 'Timestamps drop-offs, logs submission order #1, #2...',
    icon: FileUp,
  },
  {
    type: SessionType.SINGLE_EVENT,
    title: 'Single Occasion / Seminar',
    description: 'One-time roll call for workshops, talks or meetings',
    icon: History,
  },
  {
    type: SessionType.CUSTOM_SHEET,
    title: 'Custom Configured Sheet',
    description: 'Define custom session names, custom tracking fields & columns',
    icon: SlidersHorizontal,
  },
];

export const CreateSessionDialog: React.FC<CreateSessionDialogProps> = ({
  customCourses,
  onDismiss,
  onCreate,
  onCustomCourseAdded,
}) => {
  const [selectedType, setSelectedType] = useState<SessionType>(
    SessionType.SEMESTER_WEEKLY
  );

  // University -> Department -> Course(s) hierarchy
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

  // Courses under university (showing selected department courses first, plus all uni courses for common units)
  const allUniCourses = useMemo(() => {
    const deptMatches =
      selectedUni.departments.find(
        (d) => d.name.toLowerCase() === effectiveDepartment.toLowerCase()
      )?.courses || [];
    const otherDepts = selectedUni.departments
      .filter((d) => d.name.toLowerCase() !== effectiveDepartment.toLowerCase())
      .flatMap((d) => d.courses);
    const custom = customCourses
      .filter((c) => c.universityId === universityId)
      .map((c) => ({ code: c.courseCode, name: c.courseName }));

    const merged = [...deptMatches, ...custom, ...otherDepts];
    const seen = new Set<string>();
    return merged.filter((c) => {
      const k = c.name.toLowerCase();
      if (seen.has(k)) return false;
      seen.add(k);
      return true;
    });
  }, [selectedUni, effectiveDepartment, customCourses, universityId]);

  const [allowedCourses, setAllowedCourses] = useState<string[]>([
    selectedUni.departments[0]?.courses[0]?.name || 'B.Sc. Computer Science',
  ]);
  const [newCustomCourseCode, setNewCustomCourseCode] = useState('');
  const [newCustomCourseName, setNewCustomCourseName] = useState('');

  const [title, setTitle] = useState('');
  const [courseCode, setCourseCode] = useState('');
  const [venue, setVenue] = useState('Resource Centre Hall 1');
  const [instructor, setInstructor] = useState('Class Rep / Lecturer');
  const [totalWeeks, setTotalWeeks] = useState(14);

  const [isMultiSessionCustom, setIsMultiSessionCustom] = useState(true);
  const [customSessionNamesText, setCustomSessionNamesText] = useState(
    'Session 1, Session 2, Midterm Lab, Session 4, Final Demo'
  );
  const [customColumnInput, setCustomColumnInput] = useState('');
  const [customColumnsList, setCustomColumnsList] = useState<string[]>([]);

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
        setAllowedCourses([firstDept.courses[0].name]);
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
      setAllowedCourses([deptObj.courses[0].name]);
    }
  };

  const toggleCourse = (courseName: string) => {
    setAllowedCourses((prev) => {
      if (prev.includes(courseName)) {
        return prev.length > 1 ? prev.filter((c) => c !== courseName) : prev;
      }
      return [...prev, courseName];
    });
  };

  const handleAddCustomCourse = async () => {
    if (!newCustomCourseName.trim()) return;
    const created = addCustomCourseLocal({
      universityId: universityId === 'custom' ? 'custom' : selectedUni.id,
      department: effectiveDepartment,
      courseCode: newCustomCourseCode.trim() || 'CUST',
      courseName: newCustomCourseName.trim(),
    });
    await onCustomCourseAdded(created);
    setAllowedCourses((prev) =>
      prev.includes(created.courseName) ? prev : [...prev, created.courseName]
    );
    setNewCustomCourseCode('');
    setNewCustomCourseName('');
  };

  const handleAddCustomCol = () => {
    const trimmed = customColumnInput.trim();
    if (trimmed && !customColumnsList.includes(trimmed)) {
      setCustomColumnsList([...customColumnsList, trimmed]);
      setCustomColumnInput('');
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const finalCode = courseCode.trim().toUpperCase() || 'CCS 2101';
    const finalTitle = title.trim() || `${finalCode} Attendance`;
    const uniName =
      universityId === 'custom'
        ? customUniversityName.trim().toUpperCase() || 'CUSTOM UNIVERSITY'
        : selectedUni.name;

    let sessionColNames: string[] = [];
    if (selectedType === SessionType.CUSTOM_SHEET && isMultiSessionCustom) {
      sessionColNames = customSessionNamesText
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);
    } else if (selectedType === SessionType.SEMESTER_WEEKLY) {
      sessionColNames = Array.from({ length: totalWeeks }, (_, i) => `Week ${i + 1}`);
    }

    const calculatedWeeks =
      sessionColNames.length > 0
        ? sessionColNames.length
        : selectedType === SessionType.SEMESTER_WEEKLY
        ? totalWeeks
        : 1;

    const newSession: AttendanceSession = {
      id:
        typeof crypto !== 'undefined' && crypto.randomUUID
          ? crypto.randomUUID()
          : `sess-${Date.now()}`,
      title: finalTitle,
      courseCode: finalCode,
      universityId: universityId === 'custom' ? 'custom' : selectedUni.id,
      universityName: uniName,
      department: effectiveDepartment,
      allowedCourses:
        allowedCourses.length > 0 ? allowedCourses : ['B.Sc. Computer Science'],
      sessionType: selectedType,
      totalWeeks: calculatedWeeks,
      venue: venue.trim() || 'Lecture Hall 1',
      instructorName: instructor.trim() || 'Class Rep / Lecturer',
      createdTimestamp: Date.now(),
      customColumns: customColumnsList,
      sessionColumnNames: sessionColNames,
    };

    onCreate(newSession);
  };

  const uniLogoHtml = buildUniversityCrestSvg(selectedUni, 38);

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-xl max-h-[92vh] flex flex-col overflow-hidden border border-slate-200">
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div
              className="shrink-0"
              dangerouslySetInnerHTML={{ __html: uniLogoHtml }}
            />
            <div>
              <h2 className="text-lg font-bold text-slate-900">
                Create &amp; Customize Sheet
              </h2>
              <p className="text-xs text-slate-500">
                Bind University, Department &amp; Course(s) to prevent wrong-class scans
              </p>
            </div>
          </div>
          <button
            onClick={onDismiss}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 overflow-y-auto space-y-4">
          {/* University & Department & Courses */}
          <div className="rounded-xl bg-slate-50 border border-slate-200 p-3.5 space-y-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                University (Printed on Sheet Header with Logo)
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
                <option value="custom">+ Custom University</option>
              </select>
              {universityId === 'custom' && (
                <input
                  type="text"
                  value={customUniversityName}
                  onChange={(e) => setCustomUniversityName(e.target.value)}
                  placeholder="Enter University Name"
                  className="mt-2 w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white"
                />
              )}
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                Department (Comes before Course)
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
                  placeholder="Enter Department Name"
                  className="mt-2 w-full px-3 py-2 text-sm rounded-xl border border-slate-300 bg-white"
                />
              )}
            </div>

            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="text-xs font-bold text-slate-700">
                  Target Course(s) — Select multiple for Common Units
                </label>
                <span className="text-[11px] font-semibold text-[#0284C7]">
                  {allowedCourses.length} sheet{allowedCourses.length > 1 ? 's' : ''} on print
                </span>
              </div>
              <div className="max-h-36 overflow-y-auto bg-white rounded-xl border border-slate-200 p-2 space-y-1">
                {allUniCourses.map((c) => {
                  const checked = allowedCourses.includes(c.name);
                  return (
                    <button
                      key={`${c.code}-${c.name}`}
                      type="button"
                      onClick={() => toggleCourse(c.name)}
                      className={`w-full text-left px-2.5 py-1.5 rounded-lg text-xs flex items-center justify-between gap-2 cursor-pointer ${
                        checked
                          ? 'bg-[#E0F2FE] text-[#0369A1] font-semibold'
                          : 'hover:bg-slate-50 text-slate-700'
                      }`}
                    >
                      <span className="truncate">
                        <span className="font-mono text-[11px] opacity-75">
                          {c.code}
                        </span>{' '}
                        • {c.name}
                      </span>
                      {checked && <Check className="w-3.5 h-3.5 shrink-0" />}
                    </button>
                  );
                })}
              </div>

              <div className="flex gap-1.5 mt-2">
                <input
                  type="text"
                  value={newCustomCourseCode}
                  onChange={(e) => setNewCustomCourseCode(e.target.value.toUpperCase())}
                  placeholder="Code"
                  className="w-24 px-2.5 py-1.5 text-xs rounded-lg border border-slate-300 bg-white"
                />
                <input
                  type="text"
                  value={newCustomCourseName}
                  onChange={(e) => setNewCustomCourseName(e.target.value)}
                  placeholder="Add custom course name..."
                  className="flex-1 px-2.5 py-1.5 text-xs rounded-lg border border-slate-300 bg-white"
                />
                <button
                  type="button"
                  onClick={() => void handleAddCustomCourse()}
                  className="px-2.5 py-1.5 rounded-lg bg-[#0284C7] text-white text-xs font-semibold inline-flex items-center gap-1 cursor-pointer"
                >
                  <PlusCircle className="w-3.5 h-3.5" />
                  Add
                </button>
              </div>
              <p className="text-[11px] text-slate-500 mt-1.5">
                Multi-course common units automatically generate 1 separate attendance sheet per course when printed/exported.
              </p>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold uppercase tracking-wide text-slate-500 mb-2">
              Sheet Occasion Type
            </label>
            <div className="space-y-1.5">
              {TYPE_OPTIONS.map((opt) => {
                const Icon = opt.icon;
                const active = selectedType === opt.type;
                return (
                  <button
                    key={opt.type}
                    type="button"
                    onClick={() => setSelectedType(opt.type)}
                    className={`w-full text-left flex items-center gap-3 p-2.5 rounded-xl border transition-all cursor-pointer ${
                      active
                        ? 'bg-[#E0F2FE] border-[#0284C7] text-[#0369A1]'
                        : 'bg-slate-50 border-slate-200 hover:bg-slate-100 text-slate-700'
                    }`}
                  >
                    <Icon
                      className={`w-4 h-4 shrink-0 ${
                        active ? 'text-[#0284C7]' : 'text-slate-400'
                      }`}
                    />
                    <div>
                      <div className={`text-xs sm:text-sm ${active ? 'font-bold' : 'font-medium'}`}>
                        {opt.title}
                      </div>
                      <div className="text-[11px] text-slate-500">{opt.description}</div>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {selectedType === SessionType.CUSTOM_SHEET && (
            <div className="rounded-xl bg-sky-50/70 border border-sky-200 p-3.5 space-y-3">
              <div className="text-xs font-bold text-[#0369A1]">
                Custom Matrix Configuration
              </div>
              <label className="flex items-center gap-2 text-xs font-medium text-slate-700 cursor-pointer">
                <input
                  type="checkbox"
                  checked={isMultiSessionCustom}
                  onChange={(e) => setIsMultiSessionCustom(e.target.checked)}
                  className="rounded accent-[#0284C7]"
                />
                <span>Multi-Session / Multi-Column Matrix</span>
              </label>

              {isMultiSessionCustom && (
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">
                    Column Names (comma separated)
                  </label>
                  <input
                    type="text"
                    value={customSessionNamesText}
                    onChange={(e) => setCustomSessionNamesText(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 bg-white"
                  />
                </div>
              )}

              <div className="flex gap-2">
                <input
                  type="text"
                  value={customColumnInput}
                  onChange={(e) => setCustomColumnInput(e.target.value)}
                  placeholder="Custom tracking field (e.g. Seat #)"
                  className="flex-1 px-3 py-1.5 text-sm rounded-lg border border-slate-300 bg-white"
                />
                <button
                  type="button"
                  onClick={handleAddCustomCol}
                  className="px-3 py-1.5 rounded-lg bg-[#0284C7] text-white text-xs font-semibold cursor-pointer"
                >
                  Add Field
                </button>
              </div>
              {customColumnsList.length > 0 && (
                <div className="flex flex-wrap gap-1.5">
                  {customColumnsList.map((col) => (
                    <button
                      key={col}
                      type="button"
                      onClick={() =>
                        setCustomColumnsList(customColumnsList.filter((c) => c !== col))
                      }
                      className="inline-flex items-center gap-1 px-2 py-1 rounded-md bg-white border border-sky-200 text-xs cursor-pointer"
                    >
                      <span>{col}</span>
                      <X className="w-3 h-3" />
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {selectedType === SessionType.SEMESTER_WEEKLY && (
            <div className="bg-slate-50 rounded-xl p-3 border border-slate-200">
              <div className="flex items-center justify-between text-xs font-medium text-slate-800 mb-1">
                <span>Number of Weeks</span>
                <span className="font-bold text-[#0284C7]">{totalWeeks} Weeks</span>
              </div>
              <input
                type="range"
                min={1}
                max={16}
                value={totalWeeks}
                onChange={(e) => setTotalWeeks(Number(e.target.value))}
                className="w-full accent-[#0284C7]"
              />
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Unit / Course Code
              </label>
              <input
                type="text"
                value={courseCode}
                onChange={(e) => setCourseCode(e.target.value.toUpperCase())}
                placeholder="e.g. CCS 2101"
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Venue / Room
              </label>
              <input
                type="text"
                value={venue}
                onChange={(e) => setVenue(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Unit / Sheet Title
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Data Structures"
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">
                Lecturer / Class Rep
              </label>
              <input
                type="text"
                value={instructor}
                onChange={(e) => setInstructor(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
              />
            </div>
          </div>

          <div className="pt-2 flex items-center justify-end gap-2 border-t border-slate-100">
            <button
              type="button"
              onClick={onDismiss}
              className="px-4 py-2 rounded-xl text-sm font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-sm font-semibold shadow-sm cursor-pointer"
            >
              Create Sheet
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
