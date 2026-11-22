import React, { useState } from 'react';
import {
  Check,
  Copy,
  Cpu,
  ExternalLink,
  Nfc,
  Smartphone,
  X,
} from 'lucide-react';
import { ParsedCardData } from '../types/tapattend';
import { playDuplicateWarningBuzz, playSuccessChime } from '../utils/audio';
import {
  decodeWebNdefReading,
  formatWebNfcError,
  isEmbeddedIframe,
  WebNdefReadingEventLike,
} from '../utils/nfcParser';

interface CardInspectorModalProps {
  onDismiss: () => void;
}

export const CardInspectorModal: React.FC<CardInspectorModalProps> = ({
  onDismiss,
}) => {
  const [inspectedCard, setInspectedCard] = useState<ParsedCardData | null>(null);
  const [webNfcListening, setWebNfcListening] = useState(false);
  const [nfcNote, setNfcNote] = useState<string | null>(null);
  const [showStandaloneBanner, setShowStandaloneBanner] = useState(() =>
    isEmbeddedIframe()
  );
  const [copied, setCopied] = useState(false);
  const [copiedUrl, setCopiedUrl] = useState(false);

  const currentUrl =
    typeof window !== 'undefined' ? window.location.href : '';

  const handleStartWebNfc = async () => {
    const win = window as unknown as {
      NDEFReader?: new () => {
        scan: () => Promise<void>;
        onreading: ((event: WebNdefReadingEventLike) => void) | null;
      };
    };

    if (!win.NDEFReader) {
      setNfcNote(
        'Web NFC is only available in Android Chrome. For full MIFARE Classic sector reading (like DeKUT student IDs), use the Android APK.'
      );
      return;
    }

    if (isEmbeddedIframe()) {
      setShowStandaloneBanner(true);
      setNfcNote(
        'Chrome disables Web NFC inside embedded frames. Use "Open Standalone Tab" below on Android Chrome, or use the Android APK.'
      );
      return;
    }

    try {
      const ndef = new win.NDEFReader();
      await ndef.scan();
      setWebNfcListening(true);
      setShowStandaloneBanner(false);
      setNfcNote(
        'Web NFC active — tap a physical student ID card to read NDEF memory.'
      );
      ndef.onreading = (ev) => {
        const parsed = decodeWebNdefReading(ev);
        setInspectedCard(parsed);
        if (parsed.hasLocalStudentData || parsed.ndefText) {
          playSuccessChime();
          setNfcNote(`Local data read via ${parsed.memorySource}`);
        } else {
          playDuplicateWarningBuzz();
          setNfcNote(
            `UID read (${parsed.cardUid}) — no public NDEF text exposed by card.`
          );
        }
      };
    } catch (err) {
      const formatted = formatWebNfcError(err);
      if (formatted.isIframeBlocked) {
        setShowStandaloneBanner(true);
      }
      setNfcNote(formatted.message);
    }
  };

  const statusStyle = !inspectedCard
    ? ''
    : inspectedCard.hasLocalStudentData
      ? 'bg-emerald-50 border-emerald-400 text-emerald-950'
      : inspectedCard.ndefText
        ? 'bg-amber-50 border-amber-400 text-amber-950'
        : 'bg-rose-50 border-rose-400 text-rose-950';

  const statusHeading = !inspectedCard
    ? ''
    : inspectedCard.hasLocalStudentData
      ? 'Student Details Read Locally!'
      : inspectedCard.ndefText
        ? 'Readable Text Found (Partial Parse)'
        : 'UID Only — Card Memory Locked / Empty';

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl w-full max-w-lg max-h-[92vh] flex flex-col overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-violet-100 text-violet-700 flex items-center justify-center shrink-0">
              <Cpu className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-900">
                Local NFC Card Inspector
              </h2>
              <p className="text-xs text-slate-500">
                Reads Name, Reg No, Dept &amp; Course directly off the card
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
          {showStandaloneBanner && (
            <div className="rounded-xl bg-amber-50 border border-amber-300 p-3.5 text-xs text-amber-950 space-y-2">
              <div className="font-bold">
                Embedded Preview Detected — Web NFC Requires Top-Level Tab
              </div>
              <p className="leading-relaxed text-amber-900">
                Chrome restricts <span className="font-mono">NDEFReader</span> inside
                embedded iframes. Open this app directly in a standalone Android Chrome
                tab, or install the Android APK (which reads raw MIFARE Classic sectors).
              </p>
              <div className="flex flex-wrap items-center gap-2">
                <a
                  href={currentUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-900 hover:bg-amber-950 text-white font-semibold"
                >
                  <ExternalLink className="w-3.5 h-3.5" />
                  <span>Open Standalone Tab</span>
                </a>
                <button
                  type="button"
                  onClick={() => {
                    void navigator.clipboard.writeText(currentUrl);
                    setCopiedUrl(true);
                    setTimeout(() => setCopiedUrl(false), 1800);
                  }}
                  className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-amber-400 bg-white hover:bg-amber-100 text-amber-950 font-semibold cursor-pointer"
                >
                  {copiedUrl ? (
                    <>
                      <Check className="w-3.5 h-3.5" />
                      <span>URL Copied</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" />
                      <span>Copy Phone URL</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          )}

          {!inspectedCard ? (
            <div className="rounded-2xl border-2 border-dashed border-violet-300 bg-violet-50/70 p-5 text-center space-y-3">
              <div className="w-12 h-12 mx-auto rounded-full bg-white border border-violet-200 shadow-xs flex items-center justify-center">
                <Nfc className="w-6 h-6 text-violet-600 animate-pulse" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-violet-950">
                  Tap Student ID Card to Read Chip Data
                </h3>
                <p className="text-xs text-violet-800/80 mt-1 leading-relaxed">
                  Decodes student fields directly from NFC card memory with zero database
                  lookup. (Android Chrome supports NDEF; the Android APK also reads raw
                  MIFARE Classic blocks.)
                </p>
              </div>
              <div className="flex flex-wrap items-center justify-center gap-2 pt-1">
                <button
                  type="button"
                  onClick={() => void handleStartWebNfc()}
                  className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold cursor-pointer ${
                    webNfcListening
                      ? 'bg-emerald-600 text-white'
                      : 'bg-violet-600 hover:bg-violet-700 text-white'
                  }`}
                >
                  <Smartphone className="w-3.5 h-3.5" />
                  <span>
                    {webNfcListening ? 'Web NFC Listening...' : 'Scan Physical Card (Web NFC)'}
                  </span>
                </button>
              </div>
            </div>
          ) : (
            <>
              <div className={`rounded-xl border p-3.5 ${statusStyle}`}>
                <div className="flex items-center justify-between gap-2">
                  <span className="text-sm font-bold">{statusHeading}</span>
                  <button
                    type="button"
                    onClick={() => setInspectedCard(null)}
                    className="text-xs font-semibold underline cursor-pointer"
                  >
                    Scan Again
                  </button>
                </div>
                <div className="text-xs font-mono font-bold mt-1">
                  UID: {inspectedCard.cardUid}
                </div>
                {inspectedCard.techList && inspectedCard.techList.length > 0 && (
                  <div className="text-xs opacity-80 mt-0.5">
                    Tech: {inspectedCard.techList.join(', ')}
                  </div>
                )}
                <div className="text-xs opacity-80">
                  Source: {inspectedCard.memorySource}
                </div>
              </div>

              <div className="rounded-xl border border-slate-200 p-3.5 space-y-1.5">
                <div className="text-xs font-bold text-slate-900 mb-1">
                  Extracted Student Fields
                </div>
                <FieldRow label="Full Name" value={inspectedCard.suggestedName} />
                <FieldRow label="Reg Number" value={inspectedCard.suggestedRegNumber} />
                <FieldRow label="Department" value={inspectedCard.suggestedDepartment} />
                <FieldRow label="Course" value={inspectedCard.suggestedCourse} />
              </div>

              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-700">
                    Raw Card Memory Dump
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      void navigator.clipboard.writeText(inspectedCard.rawDump || '');
                      setCopied(true);
                      setTimeout(() => setCopied(false), 1800);
                    }}
                    className="inline-flex items-center gap-1 text-xs font-semibold text-[#0284C7] hover:underline cursor-pointer"
                  >
                    {copied ? (
                      <>
                        <Check className="w-3.5 h-3.5" />
                        <span>Copied!</span>
                      </>
                    ) : (
                      <>
                        <Copy className="w-3.5 h-3.5" />
                        <span>Copy Dump</span>
                      </>
                    )}
                  </button>
                </div>
                <pre className="rounded-xl bg-slate-900 text-slate-100 p-3 text-[11px] font-mono overflow-x-auto max-h-44 whitespace-pre-wrap">
                  {inspectedCard.rawDump || 'No raw bytes read.'}
                </pre>
              </div>
            </>
          )}

          {nfcNote && (
            <div className="rounded-xl bg-slate-100 border border-slate-200 px-3 py-2 text-xs text-slate-700">
              {nfcNote}
            </div>
          )}
        </div>

        <div className="px-5 py-3.5 border-t border-slate-200 flex items-center justify-end gap-2 bg-slate-50">
          {inspectedCard && (
            <button
              type="button"
              onClick={() => setInspectedCard(null)}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-violet-700 hover:bg-violet-50 cursor-pointer"
            >
              Scan Another
            </button>
          )}
          <button
            type="button"
            onClick={onDismiss}
            className="px-4 py-2 rounded-xl bg-[#0284C7] hover:bg-[#0369A1] text-white text-xs font-semibold cursor-pointer"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};

const FieldRow: React.FC<{ label: string; value?: string }> = ({ label, value }) => (
  <div className="flex items-start justify-between gap-3 text-xs">
    <span className="text-slate-500 w-24 shrink-0">{label}:</span>
    <span
      className={`flex-1 font-semibold ${value ? 'text-slate-900' : 'text-slate-400 italic'}`}
    >
      {value || 'Not found on card'}
    </span>
  </div>
);
