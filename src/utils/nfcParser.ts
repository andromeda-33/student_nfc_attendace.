import {
  inferDepartmentFromCourse,
  KENYAN_UNIVERSITIES,
} from '../data/kenyanUniversities';
import { ParsedCardData } from '../types/tapattend';
import { normalizeUid } from './storage';

export interface WebNdefRecordLike {
  recordType: string;
  mediaType?: string;
  id?: string;
  encoding?: string;
  lang?: string;
  data?: DataView | null;
  toRecords?: () => WebNdefRecordLike[];
}

export interface WebNdefReadingEventLike {
  serialNumber?: string;
  message?: {
    records?: WebNdefRecordLike[];
  };
}

function isLikelyRegNumber(token: string): boolean {
  if (token.length < 6 || token.length > 32) return false;
  const hasDigit = /\d/.test(token);
  const hasSep = token.includes('/') || token.includes('-');
  return hasDigit && hasSep && !token.toLowerCase().includes('department');
}

function isLikelyDepartment(token: string): boolean {
  const l = token.toLowerCase();
  return (
    l.includes('department') ||
    l.includes('dept') ||
    l.startsWith('school of') ||
    l.startsWith('faculty of') ||
    l.startsWith('institute of')
  );
}

function isLikelyCourse(token: string): boolean {
  const l = token.toLowerCase();
  return (
    l.startsWith('b.sc') ||
    l.startsWith('bsc') ||
    l.startsWith('bachelor') ||
    l.startsWith('b.tech') ||
    l.startsWith('b.ed') ||
    l.startsWith('b.com') ||
    l.startsWith('b.a') ||
    l.startsWith('diploma') ||
    l.startsWith('certificate') ||
    l.includes('engineering') ||
    l.includes('computer science') ||
    l.includes('information technology') ||
    l.includes('actuarial') ||
    l.includes('commerce') ||
    l.includes('economics') ||
    l.includes('nursing') ||
    l.includes('medicine') ||
    l.includes('education')
  );
}

function isLikelyFullName(token: string): boolean {
  if (token.length < 4 || token.length > 64) return false;
  if (/\d/.test(token)) return false;
  if (isLikelyDepartment(token) || isLikelyCourse(token)) return false;
  const words = token.split(/\s+/).filter(Boolean);
  return (
    words.length >= 2 &&
    words.length <= 6 &&
    words.every((w) => /^[A-Za-z'.-]+$/.test(w))
  );
}

function firstNonBlank(obj: Record<string, unknown>, keys: string[]): string | undefined {
  for (const key of keys) {
    const val = obj[key];
    if (typeof val === 'string' && val.trim().length > 0) {
      return val.trim();
    }
  }
  return undefined;
}

export function parseTextIntoCardData(
  cardUid: string,
  rawText?: string | null,
  options?: {
    techList?: string[];
    memorySource?: string;
    rawDump?: string;
  }
): ParsedCardData {
  const cleanUid = normalizeUid(cardUid);
  let suggestedName: string | undefined;
  let suggestedRegNumber: string | undefined;
  let suggestedCourse: string | undefined;
  let suggestedCourseCode: string | undefined;
  let suggestedDepartment: string | undefined;

  if (rawText && rawText.trim().length > 0) {
    const trimmed = rawText.trim();

    // 1. Try JSON object anywhere in text
    const jsonStart = trimmed.indexOf('{');
    const jsonEnd = trimmed.lastIndexOf('}');
    if (jsonStart >= 0 && jsonEnd > jsonStart) {
      try {
        const obj = JSON.parse(trimmed.slice(jsonStart, jsonEnd + 1)) as Record<
          string,
          unknown
        >;
        suggestedName = firstNonBlank(obj, ['fullName', 'name', 'studentName']);
        suggestedRegNumber = firstNonBlank(obj, [
          'regNumber',
          'regNo',
          'reg',
          'studentId',
          'id',
        ])?.toUpperCase();
        suggestedCourse = firstNonBlank(obj, [
          'courseName',
          'course',
          'programme',
          'program',
        ]);
        suggestedCourseCode = firstNonBlank(obj, ['courseCode', 'code']);
        suggestedDepartment = firstNonBlank(obj, [
          'department',
          'dept',
          'school',
          'faculty',
        ]);
      } catch {
        // Not valid JSON, continue to key-value / regex parsing
      }
    }

    // 2. Try Key-Value or vCard lines ("Name: ...", "Reg: ...", "Department: ...", "Course: ...")
    const lines = trimmed
      .split(/\r?\n/)
      .map((l) => l.trim())
      .filter(Boolean);
    for (const line of lines) {
      const lower = line.toLowerCase();
      const sepIndex = line.search(/[:=]/);
      if (sepIndex <= 0) continue;
      const value = line.slice(sepIndex + 1).trim();
      if (!value) continue;

      if (
        (lower.startsWith('fn:') || lower.startsWith('name') || lower.startsWith('full')) &&
        !suggestedName
      ) {
        suggestedName = value;
      } else if (
        (lower.startsWith('reg') ||
          lower.startsWith('adm') ||
          lower.startsWith('student id') ||
          lower.startsWith('id')) &&
        !suggestedRegNumber
      ) {
        suggestedRegNumber = value.toUpperCase();
      } else if (
        (lower.startsWith('course') || lower.startsWith('prog')) &&
        !suggestedCourse
      ) {
        suggestedCourse = value;
      } else if (
        (lower.startsWith('dept') ||
          lower.startsWith('department') ||
          lower.startsWith('school') ||
          lower.startsWith('faculty')) &&
        !suggestedDepartment
      ) {
        suggestedDepartment = value;
      }
    }

    // 3. Fallback delimited tokens (pipe, tab, newline, comma, semicolon)
    if (!suggestedRegNumber || !suggestedName || !suggestedCourse) {
      const tokens = trimmed
        .split(/[|\t\n,;]+/)
        .map((t) => t.trim())
        .filter((t) => t.length > 0 && !t.startsWith('{'));

      for (const token of tokens) {
        if (!suggestedRegNumber && isLikelyRegNumber(token)) {
          suggestedRegNumber = token.toUpperCase();
        } else if (!suggestedDepartment && isLikelyDepartment(token)) {
          suggestedDepartment = token;
        } else if (!suggestedCourse && isLikelyCourse(token)) {
          suggestedCourse = token;
        } else if (!suggestedName && isLikelyFullName(token)) {
          suggestedName = token;
        }
      }
    }

    // 4. Kenyan University Reg No Regex
    if (!suggestedRegNumber) {
      const match =
        trimmed.match(/\b[A-Z]{1,4}\d{2,4}[-/]\d{2,6}(?:[-/]\d{2,6})?\/\d{2,4}\b/i) ||
        trimmed.match(/\b[A-Z]{2,5}\/\d{4}\/\d{2,6}\b/i);
      if (match) {
        suggestedRegNumber = match[0].toUpperCase();
      }
    }

    // 5. Extract prefix code & infer Department & Course from Kenyan catalog
    if (!suggestedCourseCode && suggestedRegNumber) {
      suggestedCourseCode = suggestedRegNumber.trim().match(/^[A-Z]+\d+/)?.[0];
    }

    if (!suggestedDepartment) {
      suggestedDepartment = inferDepartmentFromCourse(
        suggestedCourse,
        suggestedCourseCode
      );
    }

    if (!suggestedCourse && suggestedCourseCode) {
      for (const uni of KENYAN_UNIVERSITIES) {
        let found = false;
        for (const dept of uni.departments) {
          const matchedCourse = dept.courses.find(
            (c) => c.code.toUpperCase() === suggestedCourseCode?.toUpperCase()
          );
          if (matchedCourse) {
            suggestedCourse = matchedCourse.name;
            if (!suggestedDepartment) suggestedDepartment = dept.name;
            found = true;
            break;
          }
        }
        if (found) break;
      }
    }
  }

  const hasLocalStudentData = Boolean(suggestedName && suggestedRegNumber);

  return {
    cardUid: cleanUid,
    ndefText: rawText?.trim() || undefined,
    suggestedName,
    suggestedRegNumber,
    suggestedCourse,
    suggestedCourseCode,
    suggestedDepartment,
    techList: options?.techList ?? [],
    memorySource:
      options?.memorySource ||
      (rawText && rawText.trim().length > 0
        ? 'Web NFC NDEF Payload'
        : 'UID Only (No readable Web NFC NDEF text)'),
    rawDump: options?.rawDump || rawText?.trim() || `Card UID: ${cleanUid}\nNo public NDEF text found on card.`,
    hasLocalStudentData,
  };
}

function bytesToHex(bytes: Uint8Array): string {
  return Array.from(bytes)
    .map((b) => b.toString(16).padStart(2, '0').toUpperCase())
    .join(' ');
}

function extractPrintableAscii(bytes: Uint8Array): string {
  let out = '';
  for (const b of bytes) {
    if (b >= 32 && b <= 126) {
      out += String.fromCharCode(b);
    } else if (b === 10 || b === 13 || b === 9) {
      out += '\n';
    } else if (b === 0) {
      out += ' ';
    }
  }
  return out
    .replace(/[ ]{2,}/g, ' ')
    .split('\n')
    .map((l) => l.trim())
    .filter((l) => l.length >= 2)
    .join('\n');
}

export function decodeWebNdefReading(event: WebNdefReadingEventLike): ParsedCardData {
  const uid = normalizeUid(event.serialNumber || '');
  const records = event.message?.records ?? [];

  const decodedParts: string[] = [];
  const dumpLines: string[] = [
    `Card UID: ${uid || '(Unavailable)'}`,
    `NDEF Records Found: ${records.length}`,
  ];

  const inspectRecord = (rec: WebNdefRecordLike, label: string) => {
    const meta = [
      `type=${rec.recordType}`,
      rec.mediaType ? `mime=${rec.mediaType}` : null,
      rec.id ? `id=${rec.id}` : null,
      rec.encoding ? `enc=${rec.encoding}` : null,
      rec.lang ? `lang=${rec.lang}` : null,
    ]
      .filter(Boolean)
      .join(', ');

    if (!rec.data || rec.data.byteLength === 0) {
      dumpLines.push(`${label} (${meta}): <empty payload / 0 bytes>`);
    } else {
      const bytes = new Uint8Array(
        rec.data.buffer,
        rec.data.byteOffset,
        rec.data.byteLength
      );
      const encoding = rec.encoding || 'utf-8';
      let decoded = '';
      try {
        decoded = new TextDecoder(encoding, { fatal: false })
          .decode(bytes)
          .replace(/\0+/g, ' ')
          .trim();
      } catch {
        decoded = new TextDecoder('utf-8', { fatal: false })
          .decode(bytes)
          .replace(/\0+/g, ' ')
          .trim();
      }

      const ascii = extractPrintableAscii(bytes);
      const bestText = decoded || ascii;
      const hexPreview = bytesToHex(bytes);

      dumpLines.push(
        `${label} (${meta}, ${bytes.byteLength} bytes):\n  HEX: ${hexPreview}${
          bestText ? `\n  TEXT: ${bestText}` : '\n  TEXT: <non-printable binary data>'
        }`
      );

      if (bestText) {
        decodedParts.push(bestText);
      }
    }

    // Some cards (e.g., Smart Poster / nested NDEF) store sub-records via toRecords()
    if (typeof rec.toRecords === 'function') {
      try {
        const nested = rec.toRecords();
        nested.forEach((subRec, subIdx) => {
          inspectRecord(subRec, `${label}.${subIdx}`);
        });
      } catch {
        // Ignore if record type does not expose nested records
      }
    }
  };

  records.forEach((rec, idx) => inspectRecord(rec, `Record[${idx}]`));

  if (records.length === 0) {
    dumpLines.push(
      'No NDEF records exposed over Web NFC. Note: Encrypted or MIFARE Classic / IsoDep SmartCard sectors require the native Android APK to probe chip technologies.'
    );
  } else if (decodedParts.length === 0) {
    dumpLines.push(
      'Note: Card contains an empty/binary NDEF container. Student details may be stored in raw MIFARE/IsoDep sectors (use the native Android APK Inspector to probe those).'
    );
  }

  const combinedText = decodedParts.join('\n').trim();
  return parseTextIntoCardData(uid, combinedText || null, {
    techList: ['Web NFC', ...(records.length > 0 ? ['NDEF'] : ['UID'])],
    memorySource:
      decodedParts.length > 0
        ? 'Web NFC NDEF Records'
        : records.length > 0
          ? 'NDEF Container (Empty / Binary Record Only)'
          : 'UID Only (No NDEF records on card)',
    rawDump: dumpLines.join('\n'),
  });
}

export function isEmbeddedIframe(): boolean {
  if (typeof window === 'undefined') return false;
  try {
    return window.self !== window.top;
  } catch {
    return true;
  }
}

export function formatWebNfcError(err: unknown): {
  isIframeBlocked: boolean;
  message: string;
} {
  const raw = err instanceof Error ? `${err.name}: ${err.message}` : String(err);
  const isTopLevelIssue =
    raw.toLowerCase().includes('top-level browsing context') ||
    isEmbeddedIframe();

  if (isTopLevelIssue) {
    return {
      isIframeBlocked: true,
      message:
        'Chrome blocks Web NFC inside embedded preview frames. Open TapAttend in a standalone browser tab on Android Chrome (or use the native Android APK).',
    };
  }

  return {
    isIframeBlocked: false,
    message: `Web NFC error: ${raw}`,
  };
}

