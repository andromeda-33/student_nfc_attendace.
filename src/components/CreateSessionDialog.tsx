import React, { useMemo, useState } from 'react';
import {
  CalendarDays,
  CheckSquare,
  FileUp,
  History,
  Minus,
  Plus,
  PlusCircle,
  SlidersHorizontal,
  Trash2,
  X,
} from 'lucide-react';
import {
  ALL_KENYAN_DEPARTMENTS,
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

interface CourseSlot {
  department: string;
  isCustomDept: boolean;
  customDeptInput: string;
  courseName: string;
  showCustomCourseBox: boolean;
  customCourseCode: string;
  customCourseName: string;
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

  const [universityId, setUniversityId] = useState(DEFAULT_UNIVERSITY_ID);
  const [customUniversityName, setCustomUniversityName] = useState('');
  const selectedUni = useMemo(() => getUniversityById(universityId), [universityId]);

  const allDepartments = useMemo(() => {
    return universityId === 'custom'
      ? ALL_KENYAN_DEPARTMENTS
      : selectedUni.departments;
  }, [universityId, selectedUni]);

  const [courseSlots, setCourseSlots] = useState<CourseSlot[]>(() => [
    {
      department:
        allDepartments[0]?.name || 'Department of Electrical & Electronic Engineering',
      isCustomDept: false,
      customDeptInput: '',
      courseName:
        allDepartments[0]?.courses[0]?.name ||
        'Bachelor of Science in Electrical and Electronics Engineering',
      showCustomCourseBox: false,
      customCourseCode: '',
      customCourseName: '',
    },
  ]);

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
    const depts =
      newUniId === 'custom'
        ? ALL_KENYAN_DEPARTMENTS
        : getUniversityById(newUniId).departments;
    const d0 = depts[0];
    setCourseSlots([
      {
        department: d0?.name || 'Department of Computer Science',
        isCustomDept: false,
        customDeptInput: '',
        courseName:
          d0?.courses[0]?.name || 'Bachelor of Science in Computer Science',
        showCustomCourseBox: false,
        customCourseCode: '',
        customCourseName: '',
      },
    ]);
  };

  const setCourseCount = (targetCount: number) => {
    const clamped = Math.max(1, Math.min(12, targetCount));
    setCourseSlots((prev) => {
      if (clamped === prev.length) return prev;
      if (clamped < prev.length) return prev.slice(0, clamped);
      const next = [...prev];
      while (next.length < clamped) {
        const last = next[next.length - 1];
        const deptObj = allDepartments.find(
          (d) => d.name.toLowerCase() === last.department.toLowerCase()
        );
        const unusedCourse =
          deptObj?.courses.find(
            (c) =>
              !next.some(
                (s) => s.courseName.toLowerCase() === c.name.toLowerCase()
              )
          )?.name || last.courseName;
        next.push({
          department: last.department,
          isCustomDept: last.isCustomDept,
          customDeptInput: last.customDeptInput,
          courseName: unusedCourse,
          showCustomCourseBox: false,
          customCourseCode: '',
          customCourseName: '',
        });
      }
      return next;
    });
  };

  const addDifferentDeptSlot = () => {
    setCourseSlots((prev) => {
      if (prev.length >= 12) return prev;
      const used = new Set(prev.map((s) => s.department.toLowerCase()));
      const nextDept =
        allDepartments.find((d) => !used.has(d.name.toLowerCase())) ||
        allDepartments[0];
      return [
        ...prev,
        {
          department: nextDept.name,
          isCustomDept: false,
          customDeptInput: '',
          courseName:
            nextDept.courses[0]?.name || 'Bachelor of Science in Computer Science',
          showCustomCourseBox: false,
          customCourseCode: '',
          customCourseName: '',
        },
      ];
    });
  };

  const updateSlot = (index: number, patch: Partial<CourseSlot>) => {
    setCourseSlots((prev) =>
      prev.map((slot, i) => (i === index ? { ...slot, ...patch } : slot))
    );
  };

  const getCoursesForDepartment = (deptName: string) => {
    const presets =
      allDepartments.find((d) => d.name.toLowerCase() === deptName.toLowerCase())
        ?.courses || [];
    const custom = customCourses
      .filter(
        (c) =>
          c.universityId === (universityId === 'custom' ? 'custom' : universityId) &&
          c.department.toLowerCase() === deptName.toLowerCase()
      )
      .map((c) => ({ code: c.courseCode, name: c.courseName }));
    const merged = [...presets, ...custom];
    const seen = new Set<string>();
    return merged.filter((c) => {
      const k = c.name.toLowerCase();
      if (seen.has(k)) return false;
      seen.add(k);
      return true;
    });
  };

  const handleSaveCustomCourseForSlot = async (index: number) => {
    const slot = courseSlots[index];
    if (!slot || !slot.customCourseName.trim()) return;
    const deptName = slot.isCustomDept
      ? slot.customDeptInput.trim() || 'General Department'
      : slot.department;
    const created = addCustomCourseLocal({
      universityId: universityId === 'custom' ? 'custom' : selectedUni.id,
      department: deptName,
      courseCode: slot.customCourseCode.trim() || 'CUST',
      courseName: slot.customCourseName.trim(),
    });
    await onCustomCourseAdded(created);
    updateSlot(index, {
      courseName: created.courseName,
      showCustomCourseBox: false,
      customCourseCode: '',
      customCourseName: '',
    });
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

    const uniqueDepts = Array.from(
      new Set(
        courseSlots.map((s) =>
          s.isCustomDept
            ? s.customDeptInput.trim() || 'General Department'
            : s.department
        )
      )
    );
    const uniqueCourses = Array.from(
      new Set(courseSlots.map((s) => s.courseName.trim()).filter(Boolean))
    );

    const newSession: AttendanceSession = {
      id:
        typeof crypto !== 'undefined' && crypto.randomUUID
          ? crypto.randomUUID()
          : `sess-${Date.now()}`,
      title: finalTitle,
      courseCode: finalCode,
      universityId: universityId === 'custom' ? 'custom' : selectedUni.id,
      universityName: uniName,
      department: uniqueDepts.join(' / '),
      allowedCourses:
        uniqueCourses.length > 0 ? uniqueCourses : ['General Programme'],
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

  const uniLogoHtml = buildUniversityCrestSvg(selectedUni, 42);

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
                Handles single courses or common units across same/different Kenyan departments
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
          {/* 1. University */}
          <div className="rounded-xl bg-slate-50 border border-slate-200 p-3.5 space-y-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                1. University / School (Crest Logo &amp; Sheet Header)
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

            {/* 2. Ask How Many Different Courses */}
            <div className="rounded-xl bg-sky-50 border border-sky-200 p-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <div className="text-xs font-bold text-slate-800">
                    2. How many different courses are in this session?
                  </div>
                  <div className="text-[11px] text-slate-600">
                    Pick 1 for a single class, or 2+ for common units
                  </div>
                </div>
                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    disabled={courseSlots.length <= 1}
                    onClick={() => setCourseCount(courseSlots.length - 1)}
                    className="w-8 h-8 rounded-lg border border-slate-300 bg-white flex items-center justify-center disabled:opacity-40 cursor-pointer"
                  >
                    <Minus className="w-4 h-4" />
                  </button>
                  <span className="px-3 py-1 rounded-lg bg-white border border-slate-200 text-xs font-bold text-[#0369A1]">
                    {courseSlots.length} Course{courseSlots.length > 1 ? 's' : ''}
                  </span>
                  <button
                    type="button"
                    disabled={courseSlots.length >= 12}
                    onClick={() => setCourseCount(courseSlots.length + 1)}
                    className="w-8 h-8 rounded-lg border border-slate-300 bg-white flex items-center justify-center disabled:opacity-40 cursor-pointer"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>

            {/* 3. Department + Course per Slot */}
            <div className="space-y-2.5">
              {courseSlots.map((slot, index) => {
                const effectiveSlotDept = slot.isCustomDept
                  ? slot.customDeptInput.trim() || 'General Department'
                  : slot.department;
                const deptCourses = getCoursesForDepartment(effectiveSlotDept);

                return (
                  <div
                    key={index}
                    className="rounded-xl bg-white border border-slate-200 p-3 space-y-2.5"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-xs font-bold text-[#0284C7]">
                        Course #{index + 1}
                      </span>
                      <div className="flex items-center gap-2">
                        {index > 0 && (
                          <button
                            type="button"
                            onClick={() => {
                              const s0 = courseSlots[0];
                              const s0Courses = getCoursesForDepartment(s0.department);
                              const nextCourse =
                                s0Courses.find(
                                  (c) =>
                                    c.name.toLowerCase() !==
                                    s0.courseName.toLowerCase()
                                )?.name || slot.courseName;
                              updateSlot(index, {
                                department: s0.department,
                                isCustomDept: s0.isCustomDept,
                                customDeptInput: s0.customDeptInput,
                                courseName: nextCourse,
                              });
                            }}
                            className="text-[11px] font-semibold text-[#0369A1] hover:underline cursor-pointer"
                          >
                            Same Dept as #1
                          </button>
                        )}
                        {index > 0 && (
                          <button
                            type="button"
                            onClick={() =>
                              setCourseSlots((prev) =>
                                prev.filter((_, idx) => idx !== index)
                              )
                            }
                            className="p-1 text-slate-400 hover:text-red-600 cursor-pointer"
                            title="Remove course"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    </div>

                    <div>
                      <label className="block text-[11px] font-semibold text-slate-600 mb-1">
                        Department
                      </label>
                      <select
                        value={slot.isCustomDept ? '__CUSTOM__' : slot.department}
                        onChange={(e) => {
                          const val = e.target.value;
                          if (val === '__CUSTOM__') {
                            updateSlot(index, { isCustomDept: true });
                          } else {
                            const found = allDepartments.find((d) => d.name === val);
                            updateSlot(index, {
                              isCustomDept: false,
                              department: val,
                              courseName:
                                found?.courses[0]?.name || slot.courseName,
                            });
                          }
                        }}
                        className="w-full px-3 py-2 text-xs sm:text-sm rounded-xl border border-slate-300 bg-white"
                      >
                        {allDepartments.map((d) => (
                          <option key={d.name} value={d.name}>
                            {d.name}
                          </option>
                        ))}
                        <option value="__CUSTOM__">+ Custom Department</option>
                      </select>
                      {slot.isCustomDept && (
                        <input
                          type="text"
                          value={slot.customDeptInput}
                          onChange={(e) =>
                            updateSlot(index, { customDeptInput: e.target.value })
                          }
                          placeholder="Enter Department Name"
                          className="mt-1.5 w-full px-3 py-1.5 text-xs rounded-lg border border-slate-300"
                        />
                      )}
                    </div>

                    <div>
                      <div className="flex items-center justify-between mb-1">
                        <label className="text-[11px] font-semibold text-slate-600">
                          Course / Programme
                        </label>
                        <button
                          type="button"
                          onClick={() =>
                            updateSlot(index, {
                              showCustomCourseBox: !slot.showCustomCourseBox,
                            })
                          }
                          className="text-[11px] font-semibold text-[#0284C7] hover:underline cursor-pointer"
                        >
                          {slot.showCustomCourseBox ? 'Hide' : '+ Custom Course'}
                        </button>
                      </div>
                      <select
                        value={slot.courseName}
                        onChange={(e) =>
                          updateSlot(index, { courseName: e.target.value })
                        }
                        className="w-full px-3 py-2 text-xs sm:text-sm rounded-xl border border-slate-300 bg-white"
                      >
                        {deptCourses.map((c) => (
                          <option key={`${c.code}-${c.name}`} value={c.name}>
                            {c.code} — {c.name}
                          </option>
                        ))}
                        {deptCourses.every(
                          (c) =>
                            c.name.toLowerCase() !== slot.courseName.toLowerCase()
                        ) && (
                          <option value={slot.courseName}>
                            {slot.courseName}
                          </option>
                        )}
                      </select>

                      {slot.showCustomCourseBox && (
                        <div className="flex gap-1.5 mt-2">
                          <input
                            type="text"
                            value={slot.customCourseCode}
                            onChange={(e) =>
                              updateSlot(index, {
                                customCourseCode: e.target.value.toUpperCase(),
                              })
                            }
                            placeholder="Code"
                            className="w-20 px-2 py-1.5 text-xs rounded-lg border border-slate-300"
                          />
                          <input
                            type="text"
                            value={slot.customCourseName}
                            onChange={(e) =>
                              updateSlot(index, { customCourseName: e.target.value })
                            }
                            placeholder="Bachelor of Science in ..."
                            className="flex-1 px-2 py-1.5 text-xs rounded-lg border border-slate-300"
                          />
                          <button
                            type="button"
                            onClick={() => void handleSaveCustomCourseForSlot(index)}
                            className="px-2.5 py-1.5 rounded-lg bg-[#0284C7] text-white text-xs font-semibold inline-flex items-center gap-1 cursor-pointer"
                          >
                            <PlusCircle className="w-3.5 h-3.5" />
                            Use
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}

              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setCourseCount(courseSlots.length + 1)}
                  className="px-3 py-2 rounded-xl bg-sky-50 hover:bg-sky-100 border border-sky-200 text-[#0369A1] text-xs font-semibold cursor-pointer"
                >
                  + Add Course (Same Dept)
                </button>
                <button
                  type="button"
                  onClick={addDifferentDeptSlot}
                  className="px-3 py-2 rounded-xl bg-white hover:bg-slate-50 border border-slate-300 text-slate-700 text-xs font-semibold cursor-pointer"
                >
                  + Add Course (Different Dept)
                </button>
              </div>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold uppercase tracking-wide text-slate-500 mb-2">
              3. Sheet Occasion Type
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
                placeholder="e.g. Engineering Mathematics III"
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
