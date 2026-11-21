import React, { useState } from 'react';
import { ArrowLeft, Download, Filter, Plus, X } from 'lucide-react';
import {
  buildUniversityCrestSvg,
  getUniversityById,
} from '../data/kenyanUniversities';
import {
  AttendanceSession,
  getColumnName,
  getTotalSessionsCount,
} from '../types/tapattend';
import { computeSemesterMatrix } from '../utils/storage';

interface SemesterMatrixScreenProps {
  session: AttendanceSession;
  onBack: () => void;
  onExportCsv: () => void;
  onAddSessionColumn: (colName: string) => void;
}

export const SemesterMatrixScreen: React.FC<SemesterMatrixScreenProps> = ({
  session,
  onBack,
  onExportCsv,
  onAddSessionColumn,
}) => {
  const courses =
    session.allowedCourses.length > 0
      ? session.allowedCourses
      : ['B.Sc. Computer Science'];
  const [selectedCourse, setSelectedCourse] = useState(courses[0]);
  const [showOnlyDeficit, setShowOnlyDeficit] = useState(false);
  const [showAddColDialog, setShowAddColDialog] = useState(false);
  const [newColName, setNewColName] = useState('');

  const matrix = computeSemesterMatrix(session, selectedCourse);
  const totalSessions = getTotalSessionsCount(session);
  const deficitCount = matrix.filter((r) => r.attendancePercentage < 75).length;
  const displayedRows = showOnlyDeficit
    ? matrix.filter((r) => r.attendancePercentage < 75)
    : matrix;

  const uni = getUniversityById(session.universityId);
  const uniCrestSvg = buildUniversityCrestSvg(uni, 34);

  const handleAddCol = (e: React.FormEvent) => {
    e.preventDefault();
    const name = newColName.trim() || `Session ${totalSessions + 1}`;
    onAddSessionColumn(name);
    setNewColName('');
    setShowAddColDialog(false);
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] text-[#0F172A] pb-16">
      <header className="sticky top-0 z-20 bg-white border-b border-slate-200">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-3">
          <div className="flex items-center gap-3 min-w-0">
            <button
              onClick={onBack}
              className="p-2 -ml-2 rounded-xl hover:bg-slate-100 text-slate-700 cursor-pointer shrink-0"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div
              className="shrink-0 hidden sm:block"
              dangerouslySetInnerHTML={{ __html: uniCrestSvg }}
            />
            <div className="min-w-0">
              <h1 className="text-base sm:text-lg font-bold truncate">
                {session.courseCode} — {selectedCourse}
              </h1>
              <p className="text-xs text-slate-500 truncate">
                {session.universityName} • {session.department}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={() => setShowAddColDialog(true)}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-xs sm:text-sm font-semibold text-slate-700 cursor-pointer"
            >
              <Plus className="w-4 h-4 text-[#0284C7]" />
              <span className="hidden sm:inline">Add Column</span>
            </button>
            <button
              onClick={onExportCsv}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-xs sm:text-sm font-semibold cursor-pointer"
            >
              <Download className="w-4 h-4" />
              <span>Export {courses.length > 1 ? `${courses.length} CSVs` : 'CSV'}</span>
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 sm:px-6 pt-5 space-y-4">
        {courses.length > 1 && (
          <div className="bg-white rounded-2xl border border-slate-200 p-2 flex flex-wrap gap-1.5">
            {courses.map((course) => (
              <button
                key={course}
                onClick={() => setSelectedCourse(course)}
                className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-colors cursor-pointer ${
                  selectedCourse === course
                    ? 'bg-[#0284C7] text-white'
                    : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
                }`}
              >
                {course}
              </button>
            ))}
          </div>
        )}

        <div className="bg-white rounded-2xl border border-slate-200 px-4 py-3 flex flex-wrap items-center justify-between gap-3">
          <button
            onClick={() => setShowOnlyDeficit(!showOnlyDeficit)}
            className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-bold border transition-colors cursor-pointer ${
              showOnlyDeficit
                ? 'bg-red-50 border-red-300 text-red-700'
                : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100'
            }`}
          >
            <Filter className="w-3.5 h-3.5" />
            <span>Deficit Only (&lt;75%): {deficitCount}</span>
          </button>

          <div className="flex items-center gap-4 text-xs text-slate-600">
            <span className="inline-flex items-center gap-1.5">
              <span className="px-2 py-0.5 rounded bg-[#10B981] text-white font-bold text-[11px]">
                P
              </span>
              Present
            </span>
            <span className="inline-flex items-center gap-1.5">
              <span className="px-2 py-0.5 rounded bg-slate-200 text-slate-600 font-bold text-[11px]">
                -
              </span>
              Absent
            </span>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs sm:text-sm">
              <thead>
                <tr className="bg-[#E0F2FE] text-[#0369A1] border-b border-sky-200">
                  <th className="py-3 px-3 font-bold whitespace-nowrap">S/N</th>
                  <th className="py-3 px-3 font-bold whitespace-nowrap">
                    Registration No.
                  </th>
                  <th className="py-3 px-3 font-bold whitespace-nowrap">
                    Full Name
                  </th>
                  {Array.from({ length: totalSessions }, (_, i) => (
                    <th
                      key={i}
                      className="py-3 px-2.5 font-bold text-center whitespace-nowrap"
                    >
                      {getColumnName(session, i + 1)}
                    </th>
                  ))}
                  <th className="py-3 px-3 font-bold text-center whitespace-nowrap">
                    Attended
                  </th>
                  <th className="py-3 px-3 font-bold text-center whitespace-nowrap">
                    %
                  </th>
                  <th className="py-3 px-3 font-bold text-center whitespace-nowrap">
                    Exam Status
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {displayedRows.map((row, idx) => {
                  const isEligible = row.attendancePercentage >= 75;
                  return (
                    <tr
                      key={row.student.id}
                      className={idx % 2 === 0 ? 'bg-white' : 'bg-slate-50/60'}
                    >
                      <td className="py-2.5 px-3 text-slate-500 tabular-nums">
                        {idx + 1}
                      </td>
                      <td className="py-2.5 px-3 font-semibold text-slate-800 whitespace-nowrap">
                        {row.student.regNumber}
                      </td>
                      <td className="py-2.5 px-3 text-slate-900 whitespace-nowrap">
                        {row.student.fullName}
                      </td>
                      {Array.from({ length: totalSessions }, (_, i) => {
                        const present = row.attendanceBySession[i + 1] === true;
                        return (
                          <td key={i} className="py-2.5 px-2.5 text-center">
                            {present ? (
                              <span className="inline-block px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 font-bold text-[11px]">
                                P
                              </span>
                            ) : (
                              <span className="text-slate-400">-</span>
                            )}
                          </td>
                        );
                      })}
                      <td className="py-2.5 px-3 text-center font-medium tabular-nums whitespace-nowrap">
                        {row.totalPresent}/{row.totalSessionsHeld}
                      </td>
                      <td
                        className={`py-2.5 px-3 text-center font-bold tabular-nums whitespace-nowrap ${
                          isEligible ? 'text-emerald-700' : 'text-red-600'
                        }`}
                      >
                        {Math.round(row.attendancePercentage)}%
                      </td>
                      <td className="py-2.5 px-3 text-center whitespace-nowrap">
                        <span
                          className={`inline-block px-2.5 py-0.5 rounded-md text-[10px] font-bold ${
                            isEligible
                              ? 'bg-emerald-100 text-emerald-800'
                              : 'bg-red-100 text-red-700'
                          }`}
                        >
                          {isEligible ? 'ELIGIBLE' : 'DEFICIT'}
                        </span>
                      </td>
                    </tr>
                  );
                })}
                {displayedRows.length === 0 && (
                  <tr>
                    <td
                      colSpan={totalSessions + 6}
                      className="py-10 text-center text-slate-500"
                    >
                      No students enrolled in {selectedCourse} yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>

      {showAddColDialog && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
          <form
            onSubmit={handleAddCol}
            className="bg-white rounded-2xl border border-slate-200 shadow-xl w-full max-w-sm p-5 space-y-3"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-slate-900">
                Add Session / Column
              </h3>
              <button
                type="button"
                onClick={() => setShowAddColDialog(false)}
                className="p-1 rounded-lg text-slate-400 hover:bg-slate-100 cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <input
              type="text"
              value={newColName}
              onChange={(e) => setNewColName(e.target.value)}
              placeholder={`e.g. Week ${totalSessions + 1} or Lab 3`}
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300"
            />
            <div className="pt-2 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setShowAddColDialog(false)}
                className="px-4 py-2 rounded-xl text-sm font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-sm font-semibold cursor-pointer"
              >
                Add Column
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
