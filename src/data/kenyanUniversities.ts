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
  departments: PresetDepartment[];
}

export const DEFAULT_UNIVERSITY_ID = 'dekut';

const SHARED_ENGINEERING_DEPTS: PresetDepartment[] = [
  {
    name: 'Department of Electrical & Electronic Engineering',
    courses: [
      { code: 'EEE', name: 'B.Sc. Electrical & Electronic Engineering' },
      { code: 'TIE', name: 'B.Sc. Telecommunication & Information Engineering' },
    ],
  },
  {
    name: 'Department of Mechanical Engineering',
    courses: [
      { code: 'MEE', name: 'B.Sc. Mechanical Engineering' },
      { code: 'TIE-M', name: 'B.Sc. Industrial & Manufacturing Engineering' },
    ],
  },
  {
    name: 'Department of Civil Engineering',
    courses: [
      { code: 'CVE', name: 'B.Sc. Civil Engineering' },
      { code: 'BCM', name: 'B.Sc. Construction Management' },
    ],
  },
];

const SHARED_COMPUTING_DEPTS: PresetDepartment[] = [
  {
    name: 'Department of Computer Science',
    courses: [
      { code: 'CSC', name: 'B.Sc. Computer Science' },
      { code: 'SWE', name: 'B.Sc. Software Engineering' },
      { code: 'DSC', name: 'B.Sc. Data Science & Analytics' },
      { code: 'CCS', name: 'B.Sc. Computer Security & Forensics' },
    ],
  },
  {
    name: 'Department of Information Technology',
    courses: [
      { code: 'BIT', name: 'B.Sc. Information Technology' },
      { code: 'BBIT', name: 'B.Sc. Business Information Technology' },
      { code: 'ICT', name: 'Diploma in Information Communication Technology' },
    ],
  },
];

const SHARED_BUSINESS_DEPTS: PresetDepartment[] = [
  {
    name: 'Department of Business & Commerce',
    courses: [
      { code: 'BCOM', name: 'Bachelor of Commerce (B.Com)' },
      { code: 'BBA', name: 'Bachelor of Business Administration' },
      { code: 'BPSM', name: 'B.Sc. Purchasing & Supplies Management' },
      { code: 'BEC', name: 'B.Sc. Economics & Statistics' },
    ],
  },
];

export const KENYAN_UNIVERSITIES: KenyanUniversity[] = [
  {
    id: 'dekut',
    name: 'DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY',
    shortName: 'DeKUT',
    location: 'Nyeri, Kenya',
    motto: 'Better Life Through Technology',
    primaryColor: '#0B6623',
    accentColor: '#D4AF37',
    departments: [
      {
        name: 'Department of Computer Science',
        courses: [
          { code: 'C026', name: 'B.Sc. Computer Science' },
          { code: 'C028', name: 'B.Sc. Data Science & Analytics' },
          { code: 'C029', name: 'B.Sc. Software Engineering' },
        ],
      },
      {
        name: 'Department of Information Technology',
        courses: [
          { code: 'C027', name: 'B.Sc. Information Technology' },
          { code: 'C025', name: 'B.Sc. Business Information Technology (BBIT)' },
          { code: 'C010', name: 'Diploma in Information Technology' },
        ],
      },
      {
        name: 'Department of Mechatronic Engineering',
        courses: [
          { code: 'E022', name: 'B.Sc. Mechatronic Engineering' },
          { code: 'E023', name: 'B.Sc. Industrial Instrumentation' },
        ],
      },
      {
        name: 'Department of Electrical & Electronic Engineering',
        courses: [
          { code: 'E021', name: 'B.Sc. Electrical & Electronic Engineering' },
          { code: 'E024', name: 'B.Sc. Telecommunication & Information Engineering' },
        ],
      },
      {
        name: 'Department of Mechanical Engineering',
        courses: [
          { code: 'E020', name: 'B.Sc. Mechanical Engineering' },
          { code: 'E028', name: 'B.Tech. Mechanical Engineering Technology' },
        ],
      },
      {
        name: 'Department of Civil Engineering',
        courses: [
          { code: 'E025', name: 'B.Sc. Civil Engineering' },
          { code: 'E029', name: 'B.Tech. Building Construction' },
        ],
      },
      {
        name: 'Institute of Geomatics, GIS & Remote Sensing (IGGReS)',
        courses: [
          { code: 'E031', name: 'B.Sc. Geomatic Engineering & GIS' },
          { code: 'E032', name: 'B.Sc. Geospatial Information Science' },
        ],
      },
      {
        name: 'School of Business Management & Economics',
        courses: [
          { code: 'B010', name: 'Bachelor of Commerce (B.Com)' },
          { code: 'B011', name: 'Bachelor of Business Administration (BBA)' },
          { code: 'B012', name: 'B.Sc. Actuarial Science' },
          { code: 'B014', name: 'B.Sc. Procurement & Contract Management' },
        ],
      },
      {
        name: 'School of Health Sciences',
        courses: [
          { code: 'H011', name: 'B.Sc. Nursing' },
          { code: 'H012', name: 'B.Sc. Food Science & Technology' },
          { code: 'H013', name: 'B.Sc. Nutrition & Dietetics' },
        ],
      },
      {
        name: 'Institute of Tourism & Hospitality Management',
        courses: [
          { code: 'T011', name: 'B.Sc. Sustainable Tourism & Hospitality Management' },
        ],
      },
    ],
  },
  {
    id: 'uon',
    name: 'UNIVERSITY OF NAIROBI',
    shortName: 'UoN',
    location: 'Nairobi, Kenya',
    motto: 'Unitate et Labore',
    primaryColor: '#003366',
    accentColor: '#C5A059',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_ENGINEERING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
      {
        name: 'School of Law',
        courses: [{ code: 'LLB', name: 'Bachelor of Laws (LL.B.)' }],
      },
      {
        name: 'School of Medicine & Health Sciences',
        courses: [
          { code: 'MBChB', name: 'Bachelor of Medicine & Surgery' },
          { code: 'BPHARM', name: 'Bachelor of Pharmacy' },
          { code: 'BSN', name: 'B.Sc. Nursing' },
        ],
      },
    ],
  },
  {
    id: 'jkuat',
    name: 'JOMO KENYATTA UNIVERSITY OF AGRICULTURE AND TECHNOLOGY',
    shortName: 'JKUAT',
    location: 'Juja, Kenya',
    motto: 'Setting Trends in Higher Education, Research & Innovation',
    primaryColor: '#1B5E20',
    accentColor: '#C62828',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_ENGINEERING_DEPTS,
      {
        name: 'Department of Mechatronic Engineering',
        courses: [{ code: 'MCT', name: 'B.Sc. Mechatronic Engineering' }],
      },
      {
        name: 'School of Architecture & Building Sciences',
        courses: [
          { code: 'BAR', name: 'Bachelor of Architecture' },
          { code: 'BQS', name: 'B.Sc. Quantity Surveying' },
        ],
      },
      ...SHARED_BUSINESS_DEPTS,
    ],
  },
  {
    id: 'ku',
    name: 'KENYATTA UNIVERSITY',
    shortName: 'KU',
    location: 'Kahawa, Nairobi, Kenya',
    motto: 'Elimu ni Nguvu',
    primaryColor: '#004D40',
    accentColor: '#D4AF37',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      {
        name: 'School of Education',
        courses: [
          { code: 'BEDS', name: 'B.Ed. (Science)' },
          { code: 'BEDA', name: 'B.Ed. (Arts)' },
        ],
      },
      ...SHARED_ENGINEERING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
    ],
  },
  {
    id: 'tuk',
    name: 'THE TECHNICAL UNIVERSITY OF KENYA',
    shortName: 'TU-K',
    location: 'Nairobi, Kenya',
    motto: 'Education and Training for the Real World',
    primaryColor: '#0277BD',
    accentColor: '#37474F',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_ENGINEERING_DEPTS,
      {
        name: 'School of Creative Arts & Technologies',
        courses: [
          { code: 'BTECH-IT', name: 'B.Tech. Information Technology' },
          { code: 'BTECH-EE', name: 'B.Tech. Electrical & Electronic Engineering' },
        ],
      },
    ],
  },
  {
    id: 'moi',
    name: 'MOI UNIVERSITY',
    shortName: 'MU',
    location: 'Eldoret, Kenya',
    motto: 'Foundation of Knowledge',
    primaryColor: '#1A237E',
    accentColor: '#FFB300',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_ENGINEERING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
      {
        name: 'School of Information Sciences',
        courses: [
          { code: 'INS', name: 'B.Sc. Information Sciences' },
          { code: 'COM-MEDIA', name: 'B.Sc. Communication & Public Relations' },
        ],
      },
    ],
  },
  {
    id: 'egerton',
    name: 'EGERTON UNIVERSITY',
    shortName: 'EU',
    location: 'Njoro, Nakuru, Kenya',
    motto: 'Transforming Lives Through Quality Education',
    primaryColor: '#2E7D32',
    accentColor: '#FBC02D',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      {
        name: 'Faculty of Agriculture',
        courses: [
          { code: 'AGEC', name: 'B.Sc. Agricultural Economics' },
          { code: 'AGEN', name: 'B.Sc. Agricultural Engineering' },
        ],
      },
      ...SHARED_BUSINESS_DEPTS,
    ],
  },
  {
    id: 'mut',
    name: "MURANG'A UNIVERSITY OF TECHNOLOGY",
    shortName: 'MUT',
    location: "Murang'a, Kenya",
    motto: 'Innovation for Prosperity',
    primaryColor: '#00695C',
    accentColor: '#F9A825',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_ENGINEERING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
    ],
  },
  {
    id: 'kyu',
    name: 'KIRINYAGA UNIVERSITY',
    shortName: 'KyU',
    location: 'Kerugoya, Kenya',
    motto: 'Innovation, Excellence & Integrity',
    primaryColor: '#1B5E20',
    accentColor: '#1565C0',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
      {
        name: 'School of Health Sciences',
        courses: [
          { code: 'HRI', name: 'B.Sc. Health Records & Information Management' },
          { code: 'PH', name: 'B.Sc. Public Health' },
        ],
      },
    ],
  },
  {
    id: 'mmu',
    name: 'MULTIMEDIA UNIVERSITY OF KENYA',
    shortName: 'MMU',
    location: 'Rongai, Nairobi, Kenya',
    motto: 'Riding on Technology, Inspiring Innovation',
    primaryColor: '#4A148C',
    accentColor: '#FF6F00',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_ENGINEERING_DEPTS,
      {
        name: 'Faculty of Media & Communication',
        courses: [
          { code: 'BMC', name: 'Bachelor of Mass Communication' },
          { code: 'FAP', name: 'Bachelor of Film Production & Animation' },
        ],
      },
    ],
  },
  {
    id: 'maseno',
    name: 'MASENO UNIVERSITY',
    shortName: 'MSU',
    location: 'Maseno, Kisumu, Kenya',
    motto: 'Fountain of Excellence',
    primaryColor: '#1565C0',
    accentColor: '#00897B',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
      {
        name: 'School of Mathematics, Statistics & Actuarial Science',
        courses: [
          { code: 'ACTS', name: 'B.Sc. Actuarial Science with IT' },
          { code: 'APST', name: 'B.Sc. Applied Statistics with IT' },
        ],
      },
    ],
  },
  {
    id: 'mmust',
    name: 'MASINDE MULIRO UNIVERSITY OF SCIENCE AND TECHNOLOGY',
    shortName: 'MMUST',
    location: 'Kakamega, Kenya',
    motto: 'University of Choice',
    primaryColor: '#0D47A1',
    accentColor: '#2E7D32',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_ENGINEERING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
    ],
  },
  {
    id: 'strathmore',
    name: 'STRATHMORE UNIVERSITY',
    shortName: 'SU',
    location: 'Nairobi, Kenya',
    motto: 'Ut Omnes Unum Sint',
    primaryColor: '#002F6C',
    accentColor: '#C8102E',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
      {
        name: 'Strathmore Law School',
        courses: [{ code: 'LLB', name: 'Bachelor of Laws (LL.B.)' }],
      },
    ],
  },
  {
    id: 'mku',
    name: 'MOUNT KENYA UNIVERSITY',
    shortName: 'MKU',
    location: 'Thika, Kenya',
    motto: 'Unlocking Infinite Possibilities',
    primaryColor: '#1E3A8A',
    accentColor: '#D97706',
    departments: [
      ...SHARED_COMPUTING_DEPTS,
      ...SHARED_BUSINESS_DEPTS,
      {
        name: 'School of Health Sciences',
        courses: [
          { code: 'BSN', name: 'B.Sc. Nursing' },
          { code: 'BPHARM', name: 'Bachelor of Pharmacy' },
          { code: 'CLIN', name: 'B.Sc. Clinical Medicine' },
        ],
      },
    ],
  },
];

export function getUniversityById(id?: string): KenyanUniversity {
  return (
    KENYAN_UNIVERSITIES.find((u) => u.id === id) ||
    KENYAN_UNIVERSITIES[0]
  );
}

export function buildUniversityCrestSvg(
  uni: Pick<KenyanUniversity, 'shortName' | 'primaryColor' | 'accentColor'>,
  size = 56
): string {
  const abbr = (uni.shortName || 'UNI').slice(0, 6).toUpperCase();
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 64 64">
    <defs>
      <linearGradient id="crestGrad" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="${uni.primaryColor}"/>
        <stop offset="100%" stop-color="#0F172A"/>
      </linearGradient>
    </defs>
    <path d="M32 4 L56 12 V30 C56 46 44 56 32 60 C20 56 8 46 8 30 V12 Z" fill="url(#crestGrad)" stroke="${uni.accentColor}" stroke-width="3"/>
    <path d="M32 10 L50 16 V30 C50 42 40 50 32 53 C24 50 14 42 14 30 V16 Z" fill="none" stroke="rgba(255,255,255,0.28)" stroke-width="1.2"/>
    <path d="M22 22 L32 17 L42 22 L32 27 Z" fill="${uni.accentColor}"/>
    <text x="32" y="42" text-anchor="middle" fill="#FFFFFF" font-family="Arial, sans-serif" font-size="${abbr.length > 4 ? '9' : '11'}" font-weight="bold">${abbr}</text>
  </svg>`;
}
