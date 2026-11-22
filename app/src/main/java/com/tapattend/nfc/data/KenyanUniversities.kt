package com.tapattend.nfc.data

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class PresetCourse(
    val code: String,
    val name: String
)

data class PresetDepartment(
    val name: String,
    val courses: List<PresetCourse>
)

data class KenyanUniversity(
    val id: String,
    val name: String,
    val shortName: String,
    val location: String,
    val motto: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val secondaryColorHex: Long = 0xFF1E293B,
    val departments: List<PresetDepartment>
)

object KenyanUniversities {
    const val DEFAULT_UNIVERSITY_ID = "dekut"

    val allKenyanDepartments: List<PresetDepartment> = listOf(
        PresetDepartment(
            name = "Department of Computer Science",
            courses = listOf(
                PresetCourse("CSC", "Bachelor of Science in Computer Science"),
                PresetCourse("SWE", "Bachelor of Science in Software Engineering"),
                PresetCourse("DSC", "Bachelor of Science in Data Science & Analytics"),
                PresetCourse("AI", "Bachelor of Science in Artificial Intelligence & Machine Learning"),
                PresetCourse("CSY", "Bachelor of Science in Cyber Security & Digital Forensics"),
                PresetCourse("MCS", "Bachelor of Science in Mathematics & Computer Science")
            )
        ),
        PresetDepartment(
            name = "Department of Information Technology",
            courses = listOf(
                PresetCourse("BIT", "Bachelor of Science in Information Technology"),
                PresetCourse("BBIT", "Bachelor of Business Information Technology"),
                PresetCourse("BIS", "Bachelor of Science in Business Computing & Information Systems"),
                PresetCourse("DIT", "Diploma in Information Technology")
            )
        ),
        PresetDepartment(
            name = "Department of Electrical & Electronic Engineering",
            courses = listOf(
                PresetCourse("EEE", "Bachelor of Science in Electrical and Electronics Engineering"),
                PresetCourse("TIE", "Bachelor of Science in Telecommunication and Information Engineering"),
                PresetCourse("ECE", "Bachelor of Science in Electrical & Computer Engineering"),
                PresetCourse("BET-EE", "Bachelor of Education Technology (Electrical & Electronics Engineering)"),
                PresetCourse("BTECH-EE", "Bachelor of Technology in Electrical & Electronics Engineering")
            )
        ),
        PresetDepartment(
            name = "Department of Mechatronic Engineering",
            courses = listOf(
                PresetCourse("MCT", "Bachelor of Science in Mechatronic Engineering"),
                PresetCourse("ICE", "Bachelor of Science in Instrumentation & Control Engineering"),
                PresetCourse("RBT", "Bachelor of Science in Robotics & Automation Engineering")
            )
        ),
        PresetDepartment(
            name = "Department of Mechanical & Manufacturing Engineering",
            courses = listOf(
                PresetCourse("MEE", "Bachelor of Science in Mechanical Engineering"),
                PresetCourse("IME", "Bachelor of Science in Industrial & Manufacturing Engineering"),
                PresetCourse("BET-ME", "Bachelor of Education Technology (Mechanical Engineering)"),
                PresetCourse("BTECH-ME", "Bachelor of Technology in Mechanical Engineering"),
                PresetCourse("MAR", "Bachelor of Science in Marine Engineering"),
                PresetCourse("AERO", "Bachelor of Science in Aeronautical Engineering")
            )
        ),
        PresetDepartment(
            name = "Department of Civil & Structural Engineering",
            courses = listOf(
                PresetCourse("CVE", "Bachelor of Science in Civil Engineering"),
                PresetCourse("BET-CE", "Bachelor of Education Technology (Civil Engineering)"),
                PresetCourse("BTECH-CE", "Bachelor of Technology in Civil & Structural Engineering"),
                PresetCourse("WRE", "Bachelor of Science in Water & Environmental Engineering")
            )
        ),
        PresetDepartment(
            name = "Department of Chemical, Process & Mining Engineering",
            courses = listOf(
                PresetCourse("CHE", "Bachelor of Science in Chemical Engineering"),
                PresetCourse("PE", "Bachelor of Science in Petroleum Engineering"),
                PresetCourse("MIN", "Bachelor of Science in Mining & Mineral Processing Engineering"),
                PresetCourse("LTH", "Bachelor of Science in Leather Technology")
            )
        ),
        PresetDepartment(
            name = "Department of Geomatics, GIS & Built Environment",
            courses = listOf(
                PresetCourse("GEGIS", "Bachelor of Science in Geomatic Engineering & Geospatial Information Systems"),
                PresetCourse("GIS", "Bachelor of Science in Geospatial Information Science"),
                PresetCourse("ARC", "Bachelor of Architectural Studies / Architecture"),
                PresetCourse("BQS", "Bachelor of Science in Quantity Surveying"),
                PresetCourse("BCM", "Bachelor of Science in Construction Management"),
                PresetCourse("RES", "Bachelor of Real Estate & Land Economics"),
                PresetCourse("URP", "Bachelor of Urban & Regional Planning")
            )
        ),
        PresetDepartment(
            name = "Department of Pure, Applied & Actuarial Sciences",
            courses = listOf(
                PresetCourse("ACTS", "Bachelor of Science in Actuarial Science"),
                PresetCourse("APST", "Bachelor of Science in Applied Statistics"),
                PresetCourse("ICH", "Bachelor of Science in Industrial Chemistry"),
                PresetCourse("ANA", "Bachelor of Science in Analytical Chemistry"),
                PresetCourse("PHY", "Bachelor of Science in Applied Physics"),
                PresetCourse("MAT", "Bachelor of Science in Mathematics"),
                PresetCourse("BCH", "Bachelor of Science in Biochemistry & Molecular Biology"),
                PresetCourse("MIC", "Bachelor of Science in Microbiology & Biotechnology")
            )
        ),
        PresetDepartment(
            name = "Department of Business, Accounting & Economics",
            courses = listOf(
                PresetCourse("BCOM", "Bachelor of Commerce (B.Com)"),
                PresetCourse("BBA", "Bachelor of Business Administration (BBA)"),
                PresetCourse("ECON", "Bachelor of Economics"),
                PresetCourse("ECST", "Bachelor of Economics & Statistics"),
                PresetCourse("BPSM", "Bachelor of Science in Procurement & Supply Chain Management"),
                PresetCourse("FIN", "Bachelor of Science in Banking & Finance"),
                PresetCourse("HRM", "Bachelor of Science in Human Resource Management"),
                PresetCourse("ENT", "Bachelor of Science in Entrepreneurship")
            )
        ),
        PresetDepartment(
            name = "School of Medicine, Nursing & Health Sciences",
            courses = listOf(
                PresetCourse("MBChB", "Bachelor of Medicine and Bachelor of Surgery"),
                PresetCourse("BSN", "Bachelor of Science in Nursing"),
                PresetCourse("BPHARM", "Bachelor of Pharmacy"),
                PresetCourse("BDS", "Bachelor of Dental Surgery"),
                PresetCourse("CLIN", "Bachelor of Science in Clinical Medicine & Community Health"),
                PresetCourse("MLS", "Bachelor of Science in Medical Laboratory Sciences"),
                PresetCourse("PH", "Bachelor of Science in Public Health"),
                PresetCourse("HRI", "Bachelor of Science in Health Records & Information Management"),
                PresetCourse("NUT", "Bachelor of Science in Food Nutrition & Dietetics")
            )
        ),
        PresetDepartment(
            name = "Department of Agriculture, Biosystems & Food Science",
            courses = listOf(
                PresetCourse("FST", "Bachelor of Science in Food Science & Technology"),
                PresetCourse("AGEN", "Bachelor of Science in Agricultural & Biosystems Engineering"),
                PresetCourse("AGEC", "Bachelor of Science in Agricultural Economics & Agribusiness"),
                PresetCourse("AGR", "Bachelor of Science in Agriculture"),
                PresetCourse("HORT", "Bachelor of Science in Horticulture"),
                PresetCourse("VET", "Bachelor of Veterinary Medicine"),
                PresetCourse("ENV", "Bachelor of Environmental Science & Natural Resources")
            )
        ),
        PresetDepartment(
            name = "School of Education",
            courses = listOf(
                PresetCourse("BEDS", "Bachelor of Education (Science)"),
                PresetCourse("BEDA", "Bachelor of Education (Arts)"),
                PresetCourse("BECE", "Bachelor of Education (Early Childhood Education)"),
                PresetCourse("BSNE", "Bachelor of Education (Special Needs Education)"),
                PresetCourse("BAG", "Bachelor of Education (Agricultural Education)")
            )
        ),
        PresetDepartment(
            name = "School of Law, Security & Social Sciences",
            courses = listOf(
                PresetCourse("LLB", "Bachelor of Laws (LL.B.)"),
                PresetCourse("CRIM", "Bachelor of Science in Criminology & Security Management"),
                PresetCourse("IR", "Bachelor of Arts in International Relations & Diplomacy"),
                PresetCourse("SOC", "Bachelor of Arts in Sociology & Community Development"),
                PresetCourse("PSY", "Bachelor of Arts in Psychology & Counseling"),
                PresetCourse("PUB", "Bachelor of Arts in Public Administration & Governance")
            )
        ),
        PresetDepartment(
            name = "Department of Media, Tourism & Hospitality",
            courses = listOf(
                PresetCourse("BMC", "Bachelor of Journalism & Mass Communication"),
                PresetCourse("FAP", "Bachelor of Film Production & Animation"),
                PresetCourse("THM", "Bachelor of Science in Tourism & Hospitality Management"),
                PresetCourse("HM", "Bachelor of Science in Hotel & Hospitality Management"),
                PresetCourse("INS", "Bachelor of Science in Information Sciences")
            )
        )
    )

    private val dekutDepartments: List<PresetDepartment> = listOf(
        PresetDepartment(
            name = "Department of Electrical & Electronic Engineering",
            courses = listOf(
                PresetCourse("E021", "Bachelor of Science in Electrical and Electronics Engineering"),
                PresetCourse("E024", "Bachelor of Science in Telecommunication and Information Engineering"),
                PresetCourse("E042", "Bachelor of Education Technology (Electrical & Electronics Engineering)")
            )
        ),
        PresetDepartment(
            name = "Department of Computer Science",
            courses = listOf(
                PresetCourse("C026", "Bachelor of Science in Computer Science"),
                PresetCourse("C028", "Bachelor of Science in Data Science & Analytics"),
                PresetCourse("C029", "Bachelor of Science in Software Engineering")
            )
        ),
        PresetDepartment(
            name = "Department of Information Technology",
            courses = listOf(
                PresetCourse("C027", "Bachelor of Science in Information Technology"),
                PresetCourse("C025", "Bachelor of Business Information Technology (BBIT)"),
                PresetCourse("C010", "Diploma in Information Technology")
            )
        ),
        PresetDepartment(
            name = "Department of Mechatronic Engineering",
            courses = listOf(
                PresetCourse("E022", "Bachelor of Science in Mechatronic Engineering"),
                PresetCourse("E023", "Bachelor of Science in Industrial Instrumentation")
            )
        ),
        PresetDepartment(
            name = "Department of Mechanical Engineering",
            courses = listOf(
                PresetCourse("E020", "Bachelor of Science in Mechanical Engineering"),
                PresetCourse("E028", "Bachelor of Education Technology (Mechanical Engineering)")
            )
        ),
        PresetDepartment(
            name = "Department of Civil Engineering",
            courses = listOf(
                PresetCourse("E025", "Bachelor of Science in Civil Engineering"),
                PresetCourse("E029", "Bachelor of Education Technology (Civil Engineering)")
            )
        ),
        PresetDepartment(
            name = "Department of Chemical Engineering & Leather Technology",
            courses = listOf(
                PresetCourse("E026", "Bachelor of Science in Chemical Engineering"),
                PresetCourse("S014", "Bachelor of Science in Leather Technology")
            )
        ),
        PresetDepartment(
            name = "Institute of Geomatics, GIS & Remote Sensing (IGGReS)",
            courses = listOf(
                PresetCourse("E031", "Bachelor of Science in Geomatic Engineering & GIS"),
                PresetCourse("E032", "Bachelor of Science in Geospatial Information Science")
            )
        ),
        PresetDepartment(
            name = "School of Business Management & Economics",
            courses = listOf(
                PresetCourse("B010", "Bachelor of Commerce (B.Com)"),
                PresetCourse("B011", "Bachelor of Business Administration (BBA)"),
                PresetCourse("B012", "Bachelor of Science in Actuarial Science"),
                PresetCourse("B014", "Bachelor of Science in Procurement & Supply Chain Management"),
                PresetCourse("B015", "Bachelor of Economics")
            )
        ),
        PresetDepartment(
            name = "School of Science",
            courses = listOf(
                PresetCourse("S011", "Bachelor of Science in Industrial Chemistry"),
                PresetCourse("S012", "Bachelor of Science in Polymer Chemistry"),
                PresetCourse("S013", "Bachelor of Science in Applied Statistics")
            )
        ),
        PresetDepartment(
            name = "School of Nursing & Food Bioresources",
            courses = listOf(
                PresetCourse("H011", "Bachelor of Science in Nursing"),
                PresetCourse("H012", "Bachelor of Science in Food Science & Technology"),
                PresetCourse("H013", "Bachelor of Science in Nutrition & Dietetics")
            )
        ),
        PresetDepartment(
            name = "Institute of Tourism, Criminology & Forensics",
            courses = listOf(
                PresetCourse("T011", "Bachelor of Science in Sustainable Tourism & Hospitality Management"),
                PresetCourse("C041", "Bachelor of Science in Criminology & Security Management")
            )
        )
    ) + allKenyanDepartments.filter { d ->
        listOf(
            "Department of Computer Science",
            "Department of Information Technology",
            "Department of Electrical & Electronic Engineering",
            "Department of Mechatronic Engineering",
            "Department of Mechanical & Manufacturing Engineering",
            "Department of Civil & Structural Engineering"
        ).none { it.equals(d.name, ignoreCase = true) }
    }.distinctBy { it.name.lowercase() }

    val list: List<KenyanUniversity> = listOf(
        KenyanUniversity("dekut", "DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY", "DeKUT", "Nyeri, Kenya", "Better Life Through Technology", 0xFF0B6623, 0xFFD4AF37, 0xFFB91C1C, dekutDepartments),
        KenyanUniversity("uon", "UNIVERSITY OF NAIROBI", "UoN", "Nairobi, Kenya", "Unitate et Labore", 0xFF003366, 0xFFC5A059, 0xFF1E40AF, allKenyanDepartments),
        KenyanUniversity("jkuat", "JOMO KENYATTA UNIVERSITY OF AGRICULTURE AND TECHNOLOGY", "JKUAT", "Juja, Kenya", "Setting Trends in Research & Innovation", 0xFF1B5E20, 0xFFD4AF37, 0xFFC62828, allKenyanDepartments),
        KenyanUniversity("ku", "KENYATTA UNIVERSITY", "KU", "Kahawa, Nairobi, Kenya", "Elimu ni Nguvu", 0xFF004D40, 0xFFD4AF37, 0xFF065F46, allKenyanDepartments),
        KenyanUniversity("tuk", "THE TECHNICAL UNIVERSITY OF KENYA", "TU-K", "Nairobi, Kenya", "Education & Training for the Real World", 0xFF0277BD, 0xFFF59E0B, 0xFF37474F, allKenyanDepartments),
        KenyanUniversity("moi", "MOI UNIVERSITY", "MU", "Eldoret, Kenya", "Foundation of Knowledge", 0xFF1A237E, 0xFFFFB300, 0xFF283593, allKenyanDepartments),
        KenyanUniversity("egerton", "EGERTON UNIVERSITY", "EU", "Njoro, Nakuru, Kenya", "Transforming Lives Through Education", 0xFF2E7D32, 0xFFFBC02D, 0xFF1B5E20, allKenyanDepartments),
        KenyanUniversity("mut", "MURANG'A UNIVERSITY OF TECHNOLOGY", "MUT", "Murang'a, Kenya", "Innovation for Prosperity", 0xFF00695C, 0xFFF9A825, 0xFF004D40, allKenyanDepartments),
        KenyanUniversity("kyu", "KIRINYAGA UNIVERSITY", "KyU", "Kerugoya, Kenya", "Innovation, Excellence & Integrity", 0xFF1B5E20, 0xFF1565C0, 0xFFD4AF37, allKenyanDepartments),
        KenyanUniversity("mmu", "MULTIMEDIA UNIVERSITY OF KENYA", "MMU", "Rongai, Nairobi, Kenya", "Riding on Technology", 0xFF4A148C, 0xFFFF6F00, 0xFF6A1B9A, allKenyanDepartments),
        KenyanUniversity("maseno", "MASENO UNIVERSITY", "MSU", "Maseno, Kisumu, Kenya", "Fountain of Excellence", 0xFF1565C0, 0xFF00897B, 0xFF0D47A1, allKenyanDepartments),
        KenyanUniversity("mmust", "MASINDE MULIRO UNIVERSITY OF SCIENCE AND TECHNOLOGY", "MMUST", "Kakamega, Kenya", "University of Choice", 0xFF0D47A1, 0xFF2E7D32, 0xFFF59E0B, allKenyanDepartments),
        KenyanUniversity("chuka", "CHUKA UNIVERSITY", "CU", "Chuka, Kenya", "Sapientia Divitia Est", 0xFF1E3A8A, 0xFFD4AF37, 0xFF047857, allKenyanDepartments),
        KenyanUniversity("embuni", "UNIVERSITY OF EMBU", "UoEm", "Embu, Kenya", "Knowledge Transforms", 0xFF14532D, 0xFFEAB308, 0xFF991B1B, allKenyanDepartments),
        KenyanUniversity("karatina", "KARATINA UNIVERSITY", "KarU", "Karatina, Kenya", "Service to Humanity", 0xFF166534, 0xFFD97706, 0xFF1E293B, allKenyanDepartments),
        KenyanUniversity("kibabii", "KIBABII UNIVERSITY", "KIBU", "Bungoma, Kenya", "Knowledge for Development", 0xFF0369A1, 0xFFFACC15, 0xFF0F172A, allKenyanDepartments),
        KenyanUniversity("kisii", "KISII UNIVERSITY", "KSU", "Kisii, Kenya", "Knowledge for Transformation", 0xFF7C2D12, 0xFFEAB308, 0xFF1E3A8A, allKenyanDepartments),
        KenyanUniversity("machakos", "MACHAKOS UNIVERSITY", "MksU", "Machakos, Kenya", "Soaring Heights in Transforming Industry", 0xFF065F46, 0xFFF59E0B, 0xFF0284C7, allKenyanDepartments),
        KenyanUniversity("meru", "MERU UNIVERSITY OF SCIENCE AND TECHNOLOGY", "MUST", "Meru, Kenya", "Foundation of Innovations", 0xFF1B5E20, 0xFFD97706, 0xFF1E40AF, allKenyanDepartments),
        KenyanUniversity("seku", "SOUTH EASTERN KENYA UNIVERSITY", "SEKU", "Kitui, Kenya", "Arid to Green", 0xFF15803D, 0xFFCA8A04, 0xFF334155, allKenyanDepartments),
        KenyanUniversity("pwani", "PWANI UNIVERSITY", "PU", "Kilifi, Kenya", "Education for Empowerment", 0xFF0284C7, 0xFF10B981, 0xFFF59E0B, allKenyanDepartments),
        KenyanUniversity("tum", "TECHNICAL UNIVERSITY OF MOMBASA", "TUM", "Mombasa, Kenya", "A Leader in Technological Excellence", 0xFF0C4A6E, 0xFFEAB308, 0xFF0369A1, allKenyanDepartments),
        KenyanUniversity("strathmore", "STRATHMORE UNIVERSITY", "SU", "Nairobi, Kenya", "Ut Omnes Unum Sint", 0xFF002F6C, 0xFFC8102E, 0xFFD4AF37, allKenyanDepartments),
        KenyanUniversity("mku", "MOUNT KENYA UNIVERSITY", "MKU", "Thika, Kenya", "Unlocking Infinite Possibilities", 0xFF1E3A8A, 0xFFD97706, 0xFF047857, allKenyanDepartments),
        KenyanUniversity("kcau", "KCA UNIVERSITY", "KCA", "Nairobi, Kenya", "Advancing Knowledge, Driving Change", 0xFF1E293B, 0xFFD97706, 0xFF0284C7, allKenyanDepartments),
        KenyanUniversity("kabarak", "KABARAK UNIVERSITY", "KABU", "Nakuru, Kenya", "Education in Biblical Perspective", 0xFF6B1D2F, 0xFFD4AF37, 0xFF1E293B, allKenyanDepartments)
    )

    private val uniqueDepartments: List<PresetDepartment> by lazy {
        (dekutDepartments + allKenyanDepartments).distinctBy { it.name.lowercase() }
    }

    private val codeToDepartmentMap: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for (dept in uniqueDepartments) {
            for (course in dept.courses) {
                map.putIfAbsent(course.code.trim().uppercase(), dept.name)
            }
        }
        map
    }

    private val exactCourseToDepartmentMap: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for (dept in uniqueDepartments) {
            for (course in dept.courses) {
                map.putIfAbsent(course.name.trim().lowercase(), dept.name)
            }
        }
        map
    }

    fun getById(id: String?): KenyanUniversity {
        return list.find { it.id == id } ?: list.first()
    }

    fun inferDepartmentFromCourse(courseName: String?, courseCode: String? = null): String? {
        if (!courseCode.isNullOrBlank()) {
            codeToDepartmentMap[courseCode.trim().uppercase()]?.let { return it }
        }
        if (!courseName.isNullOrBlank()) {
            val clean = courseName.trim().lowercase()
            exactCourseToDepartmentMap[clean]?.let { return it }
            for (dept in uniqueDepartments) {
                if (dept.courses.any { CourseMatcher.isCourseMatch(it.name, courseName) }) {
                    return dept.name
                }
            }
        }
        return null
    }

    fun buildCrestSvg(uni: KenyanUniversity, size: Int = 56): String {
        val primary = String.format("#%06X", (0xFFFFFF and uni.primaryColorHex.toInt()))
        val accent = String.format("#%06X", (0xFFFFFF and uni.accentColorHex.toInt()))
        val secondary = String.format("#%06X", (0xFFFFFF and uni.secondaryColorHex.toInt()))
        val abbr = uni.shortName.take(6).uppercase()
        val fontSize = if (abbr.length > 4) "7.8" else "9.5"

        val emblem = if (uni.id == "dekut") {
            """
            <!-- Rising Sunburst over Mt. Kenya -->
            <circle cx="32" cy="18" r="5.2" fill="$accent"/>
            <line x1="32" y1="10" x2="32" y2="12.5" stroke="$accent" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="25" y1="12.5" x2="27" y2="14.2" stroke="$accent" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="39" y1="12.5" x2="37" y2="14.2" stroke="$accent" stroke-width="1.3" stroke-linecap="round"/>
            <!-- Twin Peaks of Mt. Kenya (Batian & Nelion) -->
            <path d="M14 31 L25 18 L31 25 L38 17 L50 31 Z" fill="#FFFFFF" stroke="#E2E8F0" stroke-width="0.6"/>
            <path d="M25 18 L28 22 L23.5 22 Z" fill="#CBD5E1"/>
            <path d="M38 17 L41 21.5 L35.5 21.5 Z" fill="#CBD5E1"/>
            <!-- 8-Tooth Engineering Cogwheel -->
            <g transform="translate(32, 36.5)">
              <rect x="-1.6" y="-8.5" width="3.2" height="17" rx="0.8" fill="$accent"/>
              <rect x="-8.5" y="-1.6" width="17" height="3.2" rx="0.8" fill="$accent"/>
              <rect x="-1.6" y="-8.5" width="3.2" height="17" rx="0.8" fill="$accent" transform="rotate(45)"/>
              <rect x="-1.6" y="-8.5" width="3.2" height="17" rx="0.8" fill="$accent" transform="rotate(-45)"/>
              <circle cx="0" cy="0" r="6" fill="$secondary" stroke="$accent" stroke-width="1.4"/>
              <circle cx="0" cy="0" r="2.3" fill="#FFFFFF"/>
            </g>
            """.trimIndent()
        } else {
            """
            <!-- Heraldic Diagonal Sash, Torch & Open Book -->
            <path d="M13 17 L51 39 L47 45 L11 23 Z" fill="$secondary" opacity="0.45"/>
            <path d="M32 12 L34.5 17 L32 19 L29.5 17 Z" fill="$accent"/>
            <rect x="31" y="18.5" width="2" height="4.5" fill="$accent"/>
            <path d="M20 25 C24.5 22, 29 22, 32 25 C35 22, 39.5 22, 44 25 V35 C39.5 32.5, 35 32.5, 32 35 C29 32.5, 24.5 32.5, 20 35 Z" fill="#FFFFFF" stroke="$accent" stroke-width="1.3"/>
            <line x1="32" y1="25" x2="32" y2="35" stroke="$primary" stroke-width="1.2"/>
            """.trimIndent()
        }

        return """
            <svg xmlns="http://www.w3.org/2000/svg" width="$size" height="$size" viewBox="0 0 64 68">
              <defs>
                <linearGradient id="shield_${uni.id}" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="$primary"/>
                  <stop offset="100%" stop-color="#0B1324"/>
                </linearGradient>
              </defs>
              <!-- Outer Laurel Wreath -->
              <path d="M8 21 C4.5 35, 11 49, 25 58" fill="none" stroke="$accent" stroke-width="2.2" stroke-linecap="round"/>
              <path d="M56 21 C59.5 35, 53 49, 39 58" fill="none" stroke="$accent" stroke-width="2.2" stroke-linecap="round"/>
              <!-- Heraldic Shield -->
              <path d="M32 4 L54 11 V30 C54 45, 43 54, 32 58 C21 54, 10 45, 10 30 V11 Z" fill="url(#shield_${uni.id})" stroke="$accent" stroke-width="2.6"/>
              <path d="M32 7.5 L50.5 13.5 V29.5 C50.5 42, 40 49.5, 32 53.5 C24 49.5, 13.5 42, 13.5 29.5 V13.5 Z" fill="none" stroke="#FFFFFF" stroke-opacity="0.3" stroke-width="1"/>
              $emblem
              <!-- Crest Code Scroll Banner -->
              <path d="M10 46 H54 L51.5 57.5 H12.5 Z" fill="$accent" stroke="#0F172A" stroke-width="0.9"/>
              <text x="32" y="54.5" text-anchor="middle" fill="#0F172A" font-family="Arial, sans-serif" font-size="$fontSize" font-weight="bold">$abbr</text>
            </svg>
        """.trimIndent()
    }
}

/**
 * Intelligent, spell-tolerant & format-agnostic Kenyan degree matcher.
 * Handles differences such as:
 * "Bachelor of Science Electrical and Electronics Engineering" == "B.Sc. Electrical & Electronic Engineering"
 */
object CourseMatcher {
    private val STOP_WORDS = setOf(
        "in", "of", "and", "the", "with", "for", "to", "option", "hons", "honours",
        "bachelor", "science", "arts", "commerce", "technology", "education",
        "bsc", "btech", "beng", "bed", "bcom", "bba", "ba", "llb", "diploma", "certificate", "degree"
    )

    private val REGEX_PUNCT = Regex("""[.,()/\\:\-_]+""")
    private val REGEX_BSC = Regex("""\bbachelor\s+of\s+science\b""")
    private val REGEX_BTECH = Regex("""\bbachelor\s+of\s+technology\b""")
    private val REGEX_BENG = Regex("""\bbachelor\s+of\s+engineering\b""")
    private val REGEX_BED = Regex("""\bbachelor\s+of\s+education\b""")
    private val REGEX_BCOM = Regex("""\bbachelor\s+of\s+commerce\b""")
    private val REGEX_BBA = Regex("""\bbachelor\s+of\s+business\s+administration\b""")
    private val REGEX_BA = Regex("""\bbachelor\s+of\s+arts\b""")
    private val REGEX_LLB = Regex("""\bbachelor\s+of\s+laws\b""")
    private val REGEX_B_SC = Regex("""\bb\s+sc\b""")
    private val REGEX_B_TECH = Regex("""\bb\s+tech\b""")
    private val REGEX_B_ENG = Regex("""\bb\s+eng\b""")
    private val REGEX_B_ED = Regex("""\bb\s+ed\b""")
    private val REGEX_B_COM = Regex("""\bb\s+com\b""")
    private val REGEX_B_A = Regex("""\bb\s+a\b""")
    private val REGEX_ELEC = Regex("""\belectronics\b""")
    private val REGEX_TELE = Regex("""\btelecommunications\b""")
    private val REGEX_MECH = Regex("""\bmechatronics\b""")
    private val REGEX_SCI = Regex("""\bsciences\b""")
    private val REGEX_SYS = Regex("""\bsystems\b""")
    private val REGEX_TECHS = Regex("""\btechnologies\b""")
    private val REGEX_COMP = Regex("""\bcomputers\b""")
    private val REGEX_TYPO_ENG = Regex("""\b(?:enginering|engeneering)\b""")
    private val REGEX_TYPO_TECH = Regex("""\b(?:techonology|tecnology)\b""")
    private val REGEX_SPACES = Regex("""\s+""")

    private val normCache = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val tokenCache = java.util.concurrent.ConcurrentHashMap<String, List<String>>()

    private fun normalizeBase(raw: String): String {
        val key = raw.trim().lowercase()
        normCache[key]?.let { return it }

        val normalized = REGEX_SPACES.replace(
            key.replace("&", " and ")
                .replace(REGEX_PUNCT, " ")
                .replace(REGEX_BSC, "bsc")
                .replace(REGEX_BTECH, "btech")
                .replace(REGEX_BENG, "beng")
                .replace(REGEX_BED, "bed")
                .replace(REGEX_BCOM, "bcom")
                .replace(REGEX_BBA, "bba")
                .replace(REGEX_BA, "ba")
                .replace(REGEX_LLB, "llb")
                .replace(REGEX_B_SC, "bsc")
                .replace(REGEX_B_TECH, "btech")
                .replace(REGEX_B_ENG, "beng")
                .replace(REGEX_B_ED, "bed")
                .replace(REGEX_B_COM, "bcom")
                .replace(REGEX_B_A, "ba")
                .replace(REGEX_ELEC, "electronic")
                .replace(REGEX_TELE, "telecommunication")
                .replace(REGEX_MECH, "mechatronic")
                .replace(REGEX_SCI, "science")
                .replace(REGEX_SYS, "system")
                .replace(REGEX_TECHS, "technology")
                .replace(REGEX_COMP, "computer")
                .replace(REGEX_TYPO_ENG, "engineering")
                .replace(REGEX_TYPO_TECH, "technology"),
            " "
        ).trim()
        if (normCache.size < 512) normCache[key] = normalized
        return normalized
    }

    private fun extractCoreTokens(raw: String): List<String> {
        val key = raw.trim().lowercase()
        tokenCache[key]?.let { return it }

        val baseWords = normalizeBase(raw)
            .split(" ")
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinct()
        val nonStop = baseWords.filter { it !in STOP_WORDS }
        val result = if (nonStop.isNotEmpty()) nonStop else baseWords
        if (tokenCache.size < 512) tokenCache[key] = result
        return result
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }
        return dp[b.length]
    }

    private fun tokensFuzzyEqual(t1: String, t2: String): Boolean {
        if (t1 == t2) return true
        val pair = setOf(t1, t2)
        if (pair == setOf("it", "information technology") || pair == setOf("cs", "computer")) return true
        if (abs(t1.length - t2.length) > 2) return false
        val maxLen = max(t1.length, t2.length)
        val dist = levenshtein(t1, t2)
        return when {
            maxLen >= 7 -> dist <= 2
            maxLen >= 4 -> dist <= 1
            else -> false
        }
    }

    fun isCourseMatch(
        courseA: String,
        courseB: String,
        codeA: String? = null,
        codeB: String? = null
    ): Boolean {
        val aTrim = courseA.trim()
        val bTrim = courseB.trim()
        if (aTrim.isBlank() || bTrim.isBlank()) return false
        if (aTrim.equals(bTrim, ignoreCase = true)) return true

        val cleanCodeA = codeA?.trim()?.uppercase()?.substringBefore('-')?.substringBefore('/')
        val cleanCodeB = codeB?.trim()?.uppercase()?.substringBefore('-')?.substringBefore('/')
        if (!cleanCodeA.isNullOrBlank() && cleanCodeA.length >= 3 && cleanCodeA == cleanCodeB) {
            return true
        }

        val normA = normalizeBase(aTrim)
        val normB = normalizeBase(bTrim)
        if (normA == normB) return true

        val tokensA = extractCoreTokens(aTrim)
        val tokensB = extractCoreTokens(bTrim)
        if (tokensA.isEmpty() || tokensB.isEmpty()) return normA == normB

        val matchedFromA = tokensA.count { ta -> tokensB.any { tb -> tokensFuzzyEqual(ta, tb) } }
        val matchedFromB = tokensB.count { tb -> tokensA.any { ta -> tokensFuzzyEqual(ta, tb) } }

        val shorterCount = min(tokensA.size, tokensB.size)
        val longerCount = max(tokensA.size, tokensB.size)
        val overlapOnShorter = matchedFromA.coerceAtMost(matchedFromB).toDouble() / shorterCount.toDouble()
        val overlapOnLonger = ((matchedFromA + matchedFromB) / 2.0) / longerCount.toDouble()

        return overlapOnShorter >= 0.85 && overlapOnLonger >= 0.72
    }
}
