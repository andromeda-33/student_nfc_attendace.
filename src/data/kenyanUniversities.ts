export interface PresetCourse {
  code: string;
  name: string;
}

export interface PresetDepartment {
  name: string;
  courses: PresetCourse[];
}

export interface KenyanUniversity {
  id: string;
  name: string;
  shortName: string;
  location: string;
  motto: string;
  primaryColor: string;
  accentColor: string;
  secondaryColor?: string;
  departments: PresetDepartment[];
}

export const DEFAULT_UNIVERSITY_ID = 'dekut';

export const ALL_KENYAN_DEPARTMENTS: PresetDepartment[] = [
  {
    name: 'Department of Computer Science',
    courses: [
      { code: 'CSC', name: 'Bachelor of Science in Computer Science' },
      { code: 'SWE', name: 'Bachelor of Science in Software Engineering' },
      { code: 'DSC', name: 'Bachelor of Science in Data Science & Analytics' },
      { code: 'AI', name: 'Bachelor of Science in Artificial Intelligence & Machine Learning' },
      { code: 'CSY', name: 'Bachelor of Science in Cyber Security & Digital Forensics' },
      { code: 'MCS', name: 'Bachelor of Science in Mathematics & Computer Science' },
    ],
  },
  {
    name: 'Department of Information Technology',
    courses: [
      { code: 'BIT', name: 'Bachelor of Science in Information Technology' },
      { code: 'BBIT', name: 'Bachelor of Business Information Technology' },
      { code: 'BIS', name: 'Bachelor of Science in Business Computing & Information Systems' },
      { code: 'DIT', name: 'Diploma in Information Technology' },
    ],
  },
  {
    name: 'Department of Electrical & Electronic Engineering',
    courses: [
      { code: 'EEE', name: 'Bachelor of Science in Electrical and Electronics Engineering' },
      { code: 'TIE', name: 'Bachelor of Science in Telecommunication and Information Engineering' },
      { code: 'ECE', name: 'Bachelor of Science in Electrical & Computer Engineering' },
      { code: 'BET-EE', name: 'Bachelor of Education Technology (Electrical & Electronics Engineering)' },
      { code: 'BTECH-EE', name: 'Bachelor of Technology in Electrical & Electronics Engineering' },
    ],
  },
  {
    name: 'Department of Mechatronic Engineering',
    courses: [
      { code: 'MCT', name: 'Bachelor of Science in Mechatronic Engineering' },
      { code: 'ICE', name: 'Bachelor of Science in Instrumentation & Control Engineering' },
      { code: 'RBT', name: 'Bachelor of Science in Robotics & Automation Engineering' },
    ],
  },
  {
    name: 'Department of Mechanical & Manufacturing Engineering',
    courses: [
      { code: 'MEE', name: 'Bachelor of Science in Mechanical Engineering' },
      { code: 'IME', name: 'Bachelor of Science in Industrial & Manufacturing Engineering' },
      { code: 'BET-ME', name: 'Bachelor of Education Technology (Mechanical Engineering)' },
      { code: 'BTECH-ME', name: 'Bachelor of Technology in Mechanical Engineering' },
      { code: 'MAR', name: 'Bachelor of Science in Marine Engineering' },
      { code: 'AERO', name: 'Bachelor of Science in Aeronautical Engineering' },
    ],
  },
  {
    name: 'Department of Civil & Structural Engineering',
    courses: [
      { code: 'CVE', name: 'Bachelor of Science in Civil Engineering' },
      { code: 'BET-CE', name: 'Bachelor of Education Technology (Civil Engineering)' },
      { code: 'BTECH-CE', name: 'Bachelor of Technology in Civil & Structural Engineering' },
      { code: 'WRE', name: 'Bachelor of Science in Water & Environmental Engineering' },
    ],
  },
  {
    name: 'Department of Chemical, Process & Mining Engineering',
    courses: [
      { code: 'CHE', name: 'Bachelor of Science in Chemical Engineering' },
      { code: 'PE', name: 'Bachelor of Science in Petroleum Engineering' },
      { code: 'MIN', name: 'Bachelor of Science in Mining & Mineral Processing Engineering' },
      { code: 'LTH', name: 'Bachelor of Science in Leather Technology' },
    ],
  },
  {
    name: 'Department of Geomatics, GIS & Built Environment',
    courses: [
      { code: 'GEGIS', name: 'Bachelor of Science in Geomatic Engineering & Geospatial Information Systems' },
      { code: 'GIS', name: 'Bachelor of Science in Geospatial Information Science' },
      { code: 'ARC', name: 'Bachelor of Architectural Studies / Architecture' },
      { code: 'BQS', name: 'Bachelor of Science in Quantity Surveying' },
      { code: 'BCM', name: 'Bachelor of Science in Construction Management' },
      { code: 'RES', name: 'Bachelor of Real Estate & Land Economics' },
      { code: 'URP', name: 'Bachelor of Urban & Regional Planning' },
    ],
  },
  {
    name: 'Department of Pure, Applied & Actuarial Sciences',
    courses: [
      { code: 'ACTS', name: 'Bachelor of Science in Actuarial Science' },
      { code: 'APST', name: 'Bachelor of Science in Applied Statistics' },
      { code: 'ICH', name: 'Bachelor of Science in Industrial Chemistry' },
      { code: 'ANA', name: 'Bachelor of Science in Analytical Chemistry' },
      { code: 'PHY', name: 'Bachelor of Science in Applied Physics' },
      { code: 'MAT', name: 'Bachelor of Science in Mathematics' },
      { code: 'BCH', name: 'Bachelor of Science in Biochemistry & Molecular Biology' },
      { code: 'MIC', name: 'Bachelor of Science in Microbiology & Biotechnology' },
    ],
  },
  {
    name: 'Department of Business, Accounting & Economics',
    courses: [
      { code: 'BCOM', name: 'Bachelor of Commerce (B.Com)' },
      { code: 'BBA', name: 'Bachelor of Business Administration (BBA)' },
      { code: 'ECON', name: 'Bachelor of Economics' },
      { code: 'ECST', name: 'Bachelor of Economics & Statistics' },
      { code: 'BPSM', name: 'Bachelor of Science in Procurement & Supply Chain Management' },
      { code: 'FIN', name: 'Bachelor of Science in Banking & Finance' },
      { code: 'HRM', name: 'Bachelor of Science in Human Resource Management' },
      { code: 'ENT', name: 'Bachelor of Science in Entrepreneurship' },
    ],
  },
  {
    name: 'School of Medicine, Nursing & Health Sciences',
    courses: [
      { code: 'MBChB', name: 'Bachelor of Medicine and Bachelor of Surgery' },
      { code: 'BSN', name: 'Bachelor of Science in Nursing' },
      { code: 'BPHARM', name: 'Bachelor of Pharmacy' },
      { code: 'BDS', name: 'Bachelor of Dental Surgery' },
      { code: 'CLIN', name: 'Bachelor of Science in Clinical Medicine & Community Health' },
      { code: 'MLS', name: 'Bachelor of Science in Medical Laboratory Sciences' },
      { code: 'PH', name: 'Bachelor of Science in Public Health' },
      { code: 'HRI', name: 'Bachelor of Science in Health Records & Information Management' },
      { code: 'NUT', name: 'Bachelor of Science in Food Nutrition & Dietetics' },
    ],
  },
  {
    name: 'Department of Agriculture, Biosystems & Food Science',
    courses: [
      { code: 'FST', name: 'Bachelor of Science in Food Science & Technology' },
      { code: 'AGEN', name: 'Bachelor of Science in Agricultural & Biosystems Engineering' },
      { code: 'AGEC', name: 'Bachelor of Science in Agricultural Economics & Agribusiness' },
      { code: 'AGR', name: 'Bachelor of Science in Agriculture' },
      { code: 'HORT', name: 'Bachelor of Science in Horticulture' },
      { code: 'VET', name: 'Bachelor of Veterinary Medicine' },
      { code: 'ENV', name: 'Bachelor of Environmental Science & Natural Resources' },
    ],
  },
  {
    name: 'School of Education',
    courses: [
      { code: 'BEDS', name: 'Bachelor of Education (Science)' },
      { code: 'BEDA', name: 'Bachelor of Education (Arts)' },
      { code: 'BECE', name: 'Bachelor of Education (Early Childhood Education)' },
      { code: 'BSNE', name: 'Bachelor of Education (Special Needs Education)' },
      { code: 'BAG', name: 'Bachelor of Education (Agricultural Education)' },
    ],
  },
  {
    name: 'School of Law, Security & Social Sciences',
    courses: [
      { code: 'LLB', name: 'Bachelor of Laws (LL.B.)' },
      { code: 'CRIM', name: 'Bachelor of Science in Criminology & Security Management' },
      { code: 'IR', name: 'Bachelor of Arts in International Relations & Diplomacy' },
      { code: 'SOC', name: 'Bachelor of Arts in Sociology & Community Development' },
      { code: 'PSY', name: 'Bachelor of Arts in Psychology & Counseling' },
      { code: 'PUB', name: 'Bachelor of Arts in Public Administration & Governance' },
    ],
  },
  {
    name: 'Department of Media, Tourism & Hospitality',
    courses: [
      { code: 'BMC', name: 'Bachelor of Journalism & Mass Communication' },
      { code: 'FAP', name: 'Bachelor of Film Production & Animation' },
      { code: 'THM', name: 'Bachelor of Science in Tourism & Hospitality Management' },
      { code: 'HM', name: 'Bachelor of Science in Hotel & Hospitality Management' },
      { code: 'INS', name: 'Bachelor of Science in Information Sciences' },
    ],
  },
];

const DEKUT_SPECIFIC_DEPARTMENTS: PresetDepartment[] = [
  {
    name: 'Department of Electrical & Electronic Engineering',
    courses: [
      { code: 'E021', name: 'Bachelor of Science in Electrical and Electronics Engineering' },
      { code: 'E024', name: 'Bachelor of Science in Telecommunication and Information Engineering' },
      { code: 'E042', name: 'Bachelor of Education Technology (Electrical & Electronics Engineering)' },
    ],
  },
  {
    name: 'Department of Computer Science',
    courses: [
      { code: 'C026', name: 'Bachelor of Science in Computer Science' },
      { code: 'C028', name: 'Bachelor of Science in Data Science & Analytics' },
      { code: 'C029', name: 'Bachelor of Science in Software Engineering' },
    ],
  },
  {
    name: 'Department of Information Technology',
    courses: [
      { code: 'C027', name: 'Bachelor of Science in Information Technology' },
      { code: 'C025', name: 'Bachelor of Business Information Technology (BBIT)' },
      { code: 'C010', name: 'Diploma in Information Technology' },
    ],
  },
  {
    name: 'Department of Mechatronic Engineering',
    courses: [
      { code: 'E022', name: 'Bachelor of Science in Mechatronic Engineering' },
      { code: 'E023', name: 'Bachelor of Science in Industrial Instrumentation' },
    ],
  },
  {
    name: 'Department of Mechanical Engineering',
    courses: [
      { code: 'E020', name: 'Bachelor of Science in Mechanical Engineering' },
      { code: 'E028', name: 'Bachelor of Education Technology (Mechanical Engineering)' },
    ],
  },
  {
    name: 'Department of Civil Engineering',
    courses: [
      { code: 'E025', name: 'Bachelor of Science in Civil Engineering' },
      { code: 'E029', name: 'Bachelor of Education Technology (Civil Engineering)' },
    ],
  },
  {
    name: 'Institute of Geomatics, GIS & Remote Sensing (IGGReS)',
    courses: [
      { code: 'E031', name: 'Bachelor of Science in Geomatic Engineering & GIS' },
      { code: 'E032', name: 'Bachelor of Science in Geospatial Information Science' },
    ],
  },
  {
    name: 'School of Business Management & Economics',
    courses: [
      { code: 'B010', name: 'Bachelor of Commerce (B.Com)' },
      { code: 'B011', name: 'Bachelor of Business Administration (BBA)' },
      { code: 'B012', name: 'Bachelor of Science in Actuarial Science' },
      { code: 'B014', name: 'Bachelor of Science in Procurement & Supply Chain Management' },
      { code: 'B015', name: 'Bachelor of Economics' },
    ],
  },
  {
    name: 'School of Science',
    courses: [
      { code: 'S011', name: 'Bachelor of Science in Industrial Chemistry' },
      { code: 'S012', name: 'Bachelor of Science in Polymer Chemistry' },
      { code: 'S013', name: 'Bachelor of Science in Applied Statistics' },
      { code: 'S014', name: 'Bachelor of Science in Leather Technology' },
    ],
  },
  {
    name: 'School of Nursing & Food Bioresources',
    courses: [
      { code: 'H011', name: 'Bachelor of Science in Nursing' },
      { code: 'H012', name: 'Bachelor of Science in Food Science & Technology' },
      { code: 'H013', name: 'Bachelor of Science in Nutrition & Dietetics' },
    ],
  },
  {
    name: 'Institute of Tourism, Criminology & Forensics',
    courses: [
      { code: 'T011', name: 'Bachelor of Science in Sustainable Tourism & Hospitality Management' },
      { code: 'C041', name: 'Bachelor of Science in Criminology & Security Management' },
    ],
  },
];

const DEKUT_COMBINED_DEPTS: PresetDepartment[] = [
  ...DEKUT_SPECIFIC_DEPARTMENTS,
  ...ALL_KENYAN_DEPARTMENTS.filter(
    (d) =>
      ![
        'Department of Computer Science',
        'Department of Information Technology',
        'Department of Electrical & Electronic Engineering',
        'Department of Mechatronic Engineering',
        'Department of Mechanical & Manufacturing Engineering',
        'Department of Civil & Structural Engineering',
      ].some((n) => n.toLowerCase() === d.name.toLowerCase())
  ),
];

export const KENYAN_UNIVERSITIES: KenyanUniversity[] = [
  { id: 'dekut', name: 'DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY', shortName: 'DeKUT', location: 'Nyeri, Kenya', motto: 'Better Life Through Technology', primaryColor: '#0B6623', accentColor: '#D4AF37', secondaryColor: '#B91C1C', departments: DEKUT_COMBINED_DEPTS },
  { id: 'uon', name: 'UNIVERSITY OF NAIROBI', shortName: 'UoN', location: 'Nairobi, Kenya', motto: 'Unitate et Labore', primaryColor: '#003366', accentColor: '#C5A059', secondaryColor: '#1E40AF', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'jkuat', name: 'JOMO KENYATTA UNIVERSITY OF AGRICULTURE AND TECHNOLOGY', shortName: 'JKUAT', location: 'Juja, Kenya', motto: 'Setting Trends in Research & Innovation', primaryColor: '#1B5E20', accentColor: '#D4AF37', secondaryColor: '#C62828', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'ku', name: 'KENYATTA UNIVERSITY', shortName: 'KU', location: 'Kahawa, Nairobi, Kenya', motto: 'Elimu ni Nguvu', primaryColor: '#004D40', accentColor: '#D4AF37', secondaryColor: '#065F46', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'tuk', name: 'THE TECHNICAL UNIVERSITY OF KENYA', shortName: 'TU-K', location: 'Nairobi, Kenya', motto: 'Education & Training for the Real World', primaryColor: '#0277BD', accentColor: '#F59E0B', secondaryColor: '#37474F', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'moi', name: 'MOI UNIVERSITY', shortName: 'MU', location: 'Eldoret, Kenya', motto: 'Foundation of Knowledge', primaryColor: '#1A237E', accentColor: '#FFB300', secondaryColor: '#283593', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'egerton', name: 'EGERTON UNIVERSITY', shortName: 'EU', location: 'Njoro, Nakuru, Kenya', motto: 'Transforming Lives Through Education', primaryColor: '#2E7D32', accentColor: '#FBC02D', secondaryColor: '#1B5E20', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'mut', name: "MURANG'A UNIVERSITY OF TECHNOLOGY", shortName: 'MUT', location: "Murang'a, Kenya", motto: 'Innovation for Prosperity', primaryColor: '#00695C', accentColor: '#F9A825', secondaryColor: '#004D40', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'kyu', name: 'KIRINYAGA UNIVERSITY', shortName: 'KyU', location: 'Kerugoya, Kenya', motto: 'Innovation, Excellence & Integrity', primaryColor: '#1B5E20', accentColor: '#1565C0', secondaryColor: '#D4AF37', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'mmu', name: 'MULTIMEDIA UNIVERSITY OF KENYA', shortName: 'MMU', location: 'Rongai, Nairobi, Kenya', motto: 'Riding on Technology, Inspiring Innovation', primaryColor: '#4A148C', accentColor: '#FF6F00', secondaryColor: '#6A1B9A', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'maseno', name: 'MASENO UNIVERSITY', shortName: 'MSU', location: 'Maseno, Kisumu, Kenya', motto: 'Fountain of Excellence', primaryColor: '#1565C0', accentColor: '#00897B', secondaryColor: '#0D47A1', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'mmust', name: 'MASINDE MULIRO UNIVERSITY OF SCIENCE AND TECHNOLOGY', shortName: 'MMUST', location: 'Kakamega, Kenya', motto: 'University of Choice', primaryColor: '#0D47A1', accentColor: '#2E7D32', secondaryColor: '#F59E0B', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'chuka', name: 'CHUKA UNIVERSITY', shortName: 'CU', location: 'Chuka, Kenya', motto: 'Sapientia Divitia Est', primaryColor: '#1E3A8A', accentColor: '#D4AF37', secondaryColor: '#047857', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'embuni', name: 'UNIVERSITY OF EMBU', shortName: 'UoEm', location: 'Embu, Kenya', motto: 'Knowledge Transforms', primaryColor: '#14532D', accentColor: '#EAB308', secondaryColor: '#991B1B', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'karatina', name: 'KARATINA UNIVERSITY', shortName: 'KarU', location: 'Karatina, Kenya', motto: 'Service to Humanity', primaryColor: '#166534', accentColor: '#D97706', secondaryColor: '#1E293B', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'kibabii', name: 'KIBABII UNIVERSITY', shortName: 'KIBU', location: 'Bungoma, Kenya', motto: 'Knowledge for Development', primaryColor: '#0369A1', accentColor: '#FACC15', secondaryColor: '#0F172A', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'kisii', name: 'KISII UNIVERSITY', shortName: 'KSU', location: 'Kisii, Kenya', motto: 'Knowledge for Transformation', primaryColor: '#7C2D12', accentColor: '#EAB308', secondaryColor: '#1E3A8A', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'machakos', name: 'MACHAKOS UNIVERSITY', shortName: 'MksU', location: 'Machakos, Kenya', motto: 'Soaring Heights in Transforming Industry', primaryColor: '#065F46', accentColor: '#F59E0B', secondaryColor: '#0284C7', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'meru', name: 'MERU UNIVERSITY OF SCIENCE AND TECHNOLOGY', shortName: 'MUST', location: 'Meru, Kenya', motto: 'Foundation of Innovations', primaryColor: '#1B5E20', accentColor: '#D97706', secondaryColor: '#1E40AF', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'seku', name: 'SOUTH EASTERN KENYA UNIVERSITY', shortName: 'SEKU', location: 'Kitui, Kenya', motto: 'Arid to Green', primaryColor: '#15803D', accentColor: '#CA8A04', secondaryColor: '#334155', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'pwani', name: 'PWANI UNIVERSITY', shortName: 'PU', location: 'Kilifi, Kenya', motto: 'Education for Empowerment', primaryColor: '#0284C7', accentColor: '#10B981', secondaryColor: '#F59E0B', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'tum', name: 'TECHNICAL UNIVERSITY OF MOMBASA', shortName: 'TUM', location: 'Mombasa, Kenya', motto: 'A Leader in Technological Excellence', primaryColor: '#0C4A6E', accentColor: '#EAB308', secondaryColor: '#0369A1', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'strathmore', name: 'STRATHMORE UNIVERSITY', shortName: 'SU', location: 'Nairobi, Kenya', motto: 'Ut Omnes Unum Sint', primaryColor: '#002F6C', accentColor: '#C8102E', secondaryColor: '#D4AF37', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'mku', name: 'MOUNT KENYA UNIVERSITY', shortName: 'MKU', location: 'Thika, Kenya', motto: 'Unlocking Infinite Possibilities', primaryColor: '#1E3A8A', accentColor: '#D97706', secondaryColor: '#047857', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'kcau', name: 'KCA UNIVERSITY', shortName: 'KCA', location: 'Nairobi, Kenya', motto: 'Advancing Knowledge, Driving Change', primaryColor: '#1E293B', accentColor: '#D97706', secondaryColor: '#0284C7', departments: ALL_KENYAN_DEPARTMENTS },
  { id: 'kabarak', name: 'KABARAK UNIVERSITY', shortName: 'KABU', location: 'Nakuru, Kenya', motto: 'Education in Biblical Perspective', primaryColor: '#6B1D2F', accentColor: '#D4AF37', secondaryColor: '#1E293B', departments: ALL_KENYAN_DEPARTMENTS },
];

export function getUniversityById(id?: string): KenyanUniversity {
  return KENYAN_UNIVERSITIES.find((u) => u.id === id) || KENYAN_UNIVERSITIES[0];
}

const COURSE_STOP_WORDS = new Set([
  'in', 'of', 'and', 'the', 'with', 'for', 'to', 'option', 'hons', 'honours',
  'bachelor', 'science', 'arts', 'commerce', 'technology', 'education',
  'bsc', 'btech', 'beng', 'bed', 'bcom', 'bba', 'ba', 'llb', 'diploma', 'certificate', 'degree',
]);

function normalizeCourseBase(raw: string): string {
  return raw
    .toLowerCase()
    .replace(/&/g, ' and ')
    .replace(/[.,()/\\:\-_]+/g, ' ')
    .replace(/\bbachelor\s+of\s+science\b/g, 'bsc')
    .replace(/\bbachelor\s+of\s+technology\b/g, 'btech')
    .replace(/\bbachelor\s+of\s+engineering\b/g, 'beng')
    .replace(/\bbachelor\s+of\s+education\b/g, 'bed')
    .replace(/\bbachelor\s+of\s+commerce\b/g, 'bcom')
    .replace(/\bbachelor\s+of\s+business\s+administration\b/g, 'bba')
    .replace(/\bbachelor\s+of\s+arts\b/g, 'ba')
    .replace(/\bbachelor\s+of\s+laws\b/g, 'llb')
    .replace(/\bb\s+sc\b/g, 'bsc')
    .replace(/\bb\s+tech\b/g, 'btech')
    .replace(/\bb\s+eng\b/g, 'beng')
    .replace(/\bb\s+ed\b/g, 'bed')
    .replace(/\bb\s+com\b/g, 'bcom')
    .replace(/\bb\s+a\b/g, 'ba')
    .replace(/\belectronics\b/g, 'electronic')
    .replace(/\btelecommunications\b/g, 'telecommunication')
    .replace(/\bmechatronics\b/g, 'mechatronic')
    .replace(/\bsciences\b/g, 'science')
    .replace(/\bsystems\b/g, 'system')
    .replace(/\btechnologies\b/g, 'technology')
    .replace(/\bcomputers\b/g, 'computer')
    .replace(/\b(enginering|engeneering)\b/g, 'engineering')
    .replace(/\b(techonology|tecnology)\b/g, 'technology')
    .replace(/\s+/g, ' ')
    .trim();
}

const UNIQUE_DEPARTMENTS: PresetDepartment[] = (() => {
  const seen = new Set<string>();
  const out: PresetDepartment[] = [];
  for (const d of [...DEKUT_COMBINED_DEPTS, ...ALL_KENYAN_DEPARTMENTS]) {
    const k = d.name.toLowerCase();
    if (!seen.has(k)) {
      seen.add(k);
      out.push(d);
    }
  }
  return out;
})();

const CODE_TO_DEPT_MAP = new Map<string, string>();
const EXACT_COURSE_TO_DEPT_MAP = new Map<string, string>();
for (const dept of UNIQUE_DEPARTMENTS) {
  for (const course of dept.courses) {
    const codeKey = course.code.trim().toUpperCase();
    if (!CODE_TO_DEPT_MAP.has(codeKey)) CODE_TO_DEPT_MAP.set(codeKey, dept.name);
    const courseKey = course.name.trim().toLowerCase();
    if (!EXACT_COURSE_TO_DEPT_MAP.has(courseKey)) EXACT_COURSE_TO_DEPT_MAP.set(courseKey, dept.name);
  }
}

const TOKEN_CACHE = new Map<string, string[]>();

function extractCoreTokens(raw: string): string[] {
  const key = raw.trim().toLowerCase();
  const cached = TOKEN_CACHE.get(key);
  if (cached) return cached;

  const baseWords = Array.from(
    new Set(
      normalizeCourseBase(raw)
        .split(' ')
        .map((t) => t.trim())
        .filter((t) => t.length >= 2)
    )
  );
  const nonStop = baseWords.filter((t) => !COURSE_STOP_WORDS.has(t));
  const result = nonStop.length > 0 ? nonStop : baseWords;
  if (TOKEN_CACHE.size < 512) TOKEN_CACHE.set(key, result);
  return result;
}

function levenshtein(a: string, b: string): number {
  if (a === b) return 0;
  if (!a.length) return b.length;
  if (!b.length) return a.length;
  const dp = Array.from({ length: b.length + 1 }, (_, i) => i);
  for (let i = 1; i <= a.length; i++) {
    let prev = dp[0];
    dp[0] = i;
    for (let j = 1; j <= b.length; j++) {
      const temp = dp[j];
      const cost = a[i - 1] === b[j - 1] ? 0 : 1;
      dp[j] = Math.min(dp[j] + 1, dp[j - 1] + 1, prev + cost);
      prev = temp;
    }
  }
  return dp[b.length];
}

function tokensFuzzyEqual(t1: string, t2: string): boolean {
  if (t1 === t2) return true;
  if (Math.abs(t1.length - t2.length) > 2) return false;
  const maxLen = Math.max(t1.length, t2.length);
  const dist = levenshtein(t1, t2);
  if (maxLen >= 7) return dist <= 2;
  if (maxLen >= 4) return dist <= 1;
  return false;
}

export function isCourseMatch(
  courseA: string,
  courseB: string,
  codeA?: string,
  codeB?: string
): boolean {
  const aTrim = courseA.trim();
  const bTrim = courseB.trim();
  if (!aTrim || !bTrim) return false;
  if (aTrim.toLowerCase() === bTrim.toLowerCase()) return true;

  const cleanCodeA = codeA?.trim().toUpperCase().split(/[-/]/)[0];
  const cleanCodeB = codeB?.trim().toUpperCase().split(/[-/]/)[0];
  if (cleanCodeA && cleanCodeB && cleanCodeA.length >= 3 && cleanCodeA === cleanCodeB) {
    return true;
  }

  const normA = normalizeCourseBase(aTrim);
  const normB = normalizeCourseBase(bTrim);
  if (normA === normB) return true;

  const tokensA = extractCoreTokens(aTrim);
  const tokensB = extractCoreTokens(bTrim);
  if (tokensA.length === 0 || tokensB.length === 0) return normA === normB;

  const matchedFromA = tokensA.filter((ta) =>
    tokensB.some((tb) => tokensFuzzyEqual(ta, tb))
  ).length;
  const matchedFromB = tokensB.filter((tb) =>
    tokensA.some((ta) => tokensFuzzyEqual(ta, tb))
  ).length;

  const shorter = Math.min(tokensA.length, tokensB.length);
  const longer = Math.max(tokensA.length, tokensB.length);
  const overlapShorter = Math.min(matchedFromA, matchedFromB) / shorter;
  const overlapLonger = (matchedFromA + matchedFromB) / 2 / longer;

  return overlapShorter >= 0.85 && overlapLonger >= 0.72;
}

export function inferDepartmentFromCourse(
  courseName?: string,
  courseCode?: string
): string | undefined {
  if (courseCode) {
    const byCode = CODE_TO_DEPT_MAP.get(courseCode.trim().toUpperCase());
    if (byCode) return byCode;
  }
  if (courseName) {
    const byExact = EXACT_COURSE_TO_DEPT_MAP.get(courseName.trim().toLowerCase());
    if (byExact) return byExact;
    for (const dept of UNIQUE_DEPARTMENTS) {
      if (dept.courses.some((c) => isCourseMatch(c.name, courseName))) {
        return dept.name;
      }
    }
  }
  return undefined;
}

export function buildUniversityCrestSvg(
  uni: Pick<KenyanUniversity, 'shortName' | 'primaryColor' | 'accentColor'> & {
    id?: string;
    secondaryColor?: string;
  },
  size = 56
): string {
  const abbr = (uni.shortName || 'UNI').slice(0, 6).toUpperCase();
  const secondary = uni.secondaryColor || '#1E293B';
  const gradId = `shield_${(uni.id || abbr).replace(/[^a-zA-Z0-9]/g, '')}`;
  const isDekut = uni.id === 'dekut' || abbr === 'DEKUT';

  const emblem = isDekut
    ? `
      <circle cx="32" cy="18" r="5.2" fill="${uni.accentColor}"/>
      <line x1="32" y1="10" x2="32" y2="12.5" stroke="${uni.accentColor}" stroke-width="1.3" stroke-linecap="round"/>
      <line x1="25" y1="12.5" x2="27" y2="14.2" stroke="${uni.accentColor}" stroke-width="1.3" stroke-linecap="round"/>
      <line x1="39" y1="12.5" x2="37" y2="14.2" stroke="${uni.accentColor}" stroke-width="1.3" stroke-linecap="round"/>
      <path d="M14 31 L25 18 L31 25 L38 17 L50 31 Z" fill="#FFFFFF" stroke="#E2E8F0" stroke-width="0.6"/>
      <path d="M25 18 L28 22 L23.5 22 Z" fill="#CBD5E1"/>
      <path d="M38 17 L41 21.5 L35.5 21.5 Z" fill="#CBD5E1"/>
      <g transform="translate(32, 36.5)">
        <rect x="-1.6" y="-8.5" width="3.2" height="17" rx="0.8" fill="${uni.accentColor}"/>
        <rect x="-8.5" y="-1.6" width="17" height="3.2" rx="0.8" fill="${uni.accentColor}"/>
        <rect x="-1.6" y="-8.5" width="3.2" height="17" rx="0.8" fill="${uni.accentColor}" transform="rotate(45)"/>
        <rect x="-1.6" y="-8.5" width="3.2" height="17" rx="0.8" fill="${uni.accentColor}" transform="rotate(-45)"/>
        <circle cx="0" cy="0" r="6" fill="${secondary}" stroke="${uni.accentColor}" stroke-width="1.4"/>
        <circle cx="0" cy="0" r="2.3" fill="#FFFFFF"/>
      </g>
    `
    : `
      <path d="M13 17 L51 39 L47 45 L11 23 Z" fill="${secondary}" opacity="0.45"/>
      <path d="M32 12 L34.5 17 L32 19 L29.5 17 Z" fill="${uni.accentColor}"/>
      <rect x="31" y="18.5" width="2" height="4.5" fill="${uni.accentColor}"/>
      <path d="M20 25 C24.5 22, 29 22, 32 25 C35 22, 39.5 22, 44 25 V35 C39.5 32.5, 35 32.5, 32 35 C29 32.5, 24.5 32.5, 20 35 Z" fill="#FFFFFF" stroke="${uni.accentColor}" stroke-width="1.3"/>
      <line x1="32" y1="25" x2="32" y2="35" stroke="${uni.primaryColor}" stroke-width="1.2"/>
    `;

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 64 68">
    <defs>
      <linearGradient id="${gradId}" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="${uni.primaryColor}"/>
        <stop offset="100%" stop-color="#0B1324"/>
      </linearGradient>
    </defs>
    <path d="M8 21 C4.5 35, 11 49, 25 58" fill="none" stroke="${uni.accentColor}" stroke-width="2.2" stroke-linecap="round"/>
    <path d="M56 21 C59.5 35, 53 49, 39 58" fill="none" stroke="${uni.accentColor}" stroke-width="2.2" stroke-linecap="round"/>
    <path d="M32 4 L54 11 V30 C54 45, 43 54, 32 58 C21 54, 10 45, 10 30 V11 Z" fill="url(#${gradId})" stroke="${uni.accentColor}" stroke-width="2.6"/>
    <path d="M32 7.5 L50.5 13.5 V29.5 C50.5 42, 40 49.5, 32 53.5 C24 49.5, 13.5 42, 13.5 29.5 V13.5 Z" fill="none" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
    ${emblem}
    <path d="M10 46 H54 L51.5 57.5 H12.5 Z" fill="${uni.accentColor}" stroke="#0F172A" stroke-width="0.9"/>
    <text x="32" y="54.5" text-anchor="middle" fill="#0F172A" font-family="Arial, sans-serif" font-size="${abbr.length > 4 ? '7.8' : '9.5'}" font-weight="bold">${abbr}</text>
  </svg>`;
}
