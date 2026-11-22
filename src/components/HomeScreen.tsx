import React, { useMemo } from 'react';
import {
  BadgeCheck,
  CheckCircle2,
  ChevronRight,
  Cpu,
  FileSpreadsheet,
  Plus,
  Trash2,
  Users,
} from 'lucide-react';
import {
  buildUniversityCrestSvg,
  DEFAULT_UNIVERSITY_ID,
  getUniversityById,
} from '../data/kenyanUniversities';
import {
  AttendanceRecord,
  AttendanceSession,
  isMultiSession,
  SESSION_TYPE_META,
} from '../types/tapattend';

interface HomeScreenProps {
  sessions: AttendanceSession[];
  records: AttendanceRecord[];
  studentCount: number;
  onSelectSession: (session: AttendanceSession) => void;
  onCreateNewSession: () => void;
  onOpenRoster: () => void;
  onOpenCardInspector: () => void;
  onDeleteSession: (session: AttendanceSession) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  sessions,
  records,
  studentCount,
  onSelectSession,
  onCreateNewSession,
  onOpenRoster,
  onOpenCardInspector,
  onDeleteSession,
}) => {
  const defaultCrestSvg = useMemo(
    () => buildUniversityCrestSvg(getUniversityById(DEFAULT_UNIVERSITY_ID), 40),
    []
  );

  return (
    <div className="min-h-screen bg-[#F8FAFC] text-[#0F172A] pb-24">
      <header className="sticky top-0 z-20 bg-white/95 backdrop-blur border-b border-slate-200">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-3">
          <div className="flex items-center gap-3 min-w-0">
            <div
              className="w-10 h-10 flex items-center justify-center shrink-0"
              dangerouslySetInnerHTML={{ __html: defaultCrestSvg }}
            />
            <div className="min-w-0">
              <h1 className="text-base sm:text-lg font-bold tracking-tight text-[#0F172A] leading-tight truncate">
                TapAttend NFC
              </h1>
              <p className="text-[11px] sm:text-xs text-slate-500 truncate">
                Offline NFC Roll Call • Kenyan Universities
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={onOpenCardInspector}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl border border-violet-200 bg-violet-50 hover:bg-violet-100 text-violet-800 text-xs sm:text-sm font-semibold transition-colors cursor-pointer"
            >
              <Cpu className="w-4 h-4" />
              <span className="hidden sm:inline">Inspect NFC Card</span>
            </button>

            <button
              onClick={onOpenRoster}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 text-xs sm:text-sm font-semibold cursor-pointer"
            >
              <Users className="w-4 h-4 text-[#0284C7]" />
              <span className="hidden sm:inline">Roster</span>
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 sm:px-6 pt-5 space-y-5">
        {/* Quick Stats Banner */}
        <section className="grid grid-cols-2 sm:grid-cols-3 gap-3 sm:gap-4">
          <button
            onClick={onOpenRoster}
            className="text-left rounded-2xl p-4 bg-[#0284C7]/10 border border-[#0284C7]/20 hover:border-[#0284C7]/40 transition-colors cursor-pointer"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-[#0284C7] text-white flex items-center justify-center shrink-0">
                <BadgeCheck className="w-5 h-5" />
              </div>
              <div>
                <div className="text-xl font-bold text-[#0284C7] tabular-nums">
                  {studentCount}
                </div>
                <div className="text-xs text-slate-600 font-medium">
                  Saved Students
                </div>
              </div>
            </div>
          </button>

          <div className="rounded-2xl p-4 bg-[#059669]/10 border border-[#059669]/20">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-[#059669] text-white flex items-center justify-center shrink-0">
                <FileSpreadsheet className="w-5 h-5" />
              </div>
              <div>
                <div className="text-xl font-bold text-[#059669] tabular-nums">
                  {sessions.length}
                </div>
                <div className="text-xs text-slate-600 font-medium">
                  Active Sheets
                </div>
              </div>
            </div>
          </div>

          <button
            onClick={onCreateNewSession}
            className="col-span-2 sm:col-span-1 rounded-2xl p-4 bg-[#0284C7] hover:bg-[#0369A1] text-white flex items-center justify-center gap-2 font-semibold text-sm shadow-xs transition-colors cursor-pointer"
          >
            <Plus className="w-5 h-5" />
            <span>New Sheet / Session</span>
          </button>
        </section>

        {/* Sessions List */}
        <section>
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-base font-bold text-slate-900">
              Attendance Sessions &amp; Sheets
            </h2>
            <span className="text-xs text-slate-500">
              {records.length} total verified taps
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
            {sessions.map((session) => {
              const meta = SESSION_TYPE_META[session.sessionType];
              const sessionRecords = records.filter(
                (r) => r.sessionId === session.id
              );
              const multi = isMultiSession(session);
              const crestSvg = buildUniversityCrestSvg(
                getUniversityById(session.universityId),
                32
              );

              return (
                <div
                  key={session.id}
                  onClick={() => onSelectSession(session)}
                  className="group bg-white rounded-2xl border border-slate-200 hover:border-[#0284C7]/60 hover:shadow-md transition-all p-4 sm:p-5 cursor-pointer flex flex-col justify-between"
                >
                  <div>
                    <div className="flex items-center justify-between gap-2">
                      <div className="flex items-center gap-2">
                        <div
                          className="shrink-0"
                          dangerouslySetInnerHTML={{ __html: crestSvg }}
                        />
                        <span
                          className="inline-flex items-center px-2.5 py-1 rounded-md text-[11px] font-bold"
                          style={{
                            backgroundColor: `${meta.badgeColor}18`,
                            color: meta.badgeColor,
                          }}
                        >
                          {meta.displayName}
                        </span>
                      </div>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onDeleteSession(session);
                        }}
                        title="Delete session"
                        className="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors cursor-pointer"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>

                    <div className="text-[11px] font-bold text-emerald-700 mt-2.5 truncate">
                      {session.universityName}
                    </div>
                    <h3 className="text-base font-bold text-slate-900 group-hover:text-[#0284C7] transition-colors mt-0.5">
                      {session.title}
                    </h3>
                    <p className="text-xs text-slate-500 mt-0.5">
                      {session.courseCode} • {session.department}
                    </p>
                    <p className="text-xs text-slate-600 mt-1">
                      Courses ({session.allowedCourses.length}):{' '}
                      <span className="font-semibold">
                        {session.allowedCourses.join(', ')}
                      </span>
                    </p>
                  </div>

                  <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                    <div className="flex items-center gap-1.5 text-xs font-medium text-slate-700">
                      <CheckCircle2 className="w-4 h-4 text-[#059669]" />
                      <span>
                        {multi
                          ? `${sessionRecords.length} total taps (${session.totalWeeks} cols)`
                          : `${sessionRecords.length} students signed in`}
                      </span>
                    </div>

                    <div className="flex items-center gap-1 text-xs font-bold text-[#0284C7]">
                      <span>Open Sheet</span>
                      <ChevronRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      </main>
    </div>
  );
};
