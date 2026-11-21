package com.tapattend.nfc.data

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
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val departments: List<PresetDepartment>
)

object KenyanUniversities {
    const val DEFAULT_UNIVERSITY_ID = "dekut"

    private val sharedComputingDepts = listOf(
        PresetDepartment(
            name = "Department of Computer Science",
            courses = listOf(
                PresetCourse("CSC", "B.Sc. Computer Science"),
                PresetCourse("SWE", "B.Sc. Software Engineering"),
                PresetCourse("DSC", "B.Sc. Data Science & Analytics"),
                PresetCourse("CCS", "B.Sc. Computer Security & Forensics")
            )
        ),
        PresetDepartment(
            name = "Department of Information Technology",
            courses = listOf(
                PresetCourse("BIT", "B.Sc. Information Technology"),
                PresetCourse("BBIT", "B.Sc. Business Information Technology"),
                PresetCourse("ICT", "Diploma in Information Communication Technology")
            )
        )
    )

    private val sharedEngineeringDepts = listOf(
        PresetDepartment(
            name = "Department of Electrical & Electronic Engineering",
            courses = listOf(
                PresetCourse("EEE", "B.Sc. Electrical & Electronic Engineering"),
                PresetCourse("TIE", "B.Sc. Telecommunication & Information Engineering")
            )
        ),
        PresetDepartment(
            name = "Department of Mechanical Engineering",
            courses = listOf(
                PresetCourse("MEE", "B.Sc. Mechanical Engineering"),
                PresetCourse("IME", "B.Sc. Industrial & Manufacturing Engineering")
            )
        ),
        PresetDepartment(
            name = "Department of Civil Engineering",
            courses = listOf(
                PresetCourse("CVE", "B.Sc. Civil Engineering"),
                PresetCourse("BCM", "B.Sc. Construction Management")
            )
        )
    )

    private val sharedBusinessDepts = listOf(
        PresetDepartment(
            name = "Department of Business & Commerce",
            courses = listOf(
                PresetCourse("BCOM", "Bachelor of Commerce (B.Com)"),
                PresetCourse("BBA", "Bachelor of Business Administration"),
                PresetCourse("BPSM", "B.Sc. Purchasing & Supplies Management"),
                PresetCourse("BEC", "B.Sc. Economics & Statistics")
            )
        )
    )

    val list: List<KenyanUniversity> = listOf(
        KenyanUniversity(
            id = "dekut",
            name = "DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY",
            shortName = "DeKUT",
            location = "Nyeri, Kenya",
            primaryColorHex = 0xFF0B6623,
            accentColorHex = 0xFFD4AF37,
            departments = listOf(
                PresetDepartment(
                    name = "Department of Computer Science",
                    courses = listOf(
                        PresetCourse("C026", "B.Sc. Computer Science"),
                        PresetCourse("C028", "B.Sc. Data Science & Analytics"),
                        PresetCourse("C029", "B.Sc. Software Engineering")
                    )
                ),
                PresetDepartment(
                    name = "Department of Information Technology",
                    courses = listOf(
                        PresetCourse("C027", "B.Sc. Information Technology"),
                        PresetCourse("C025", "B.Sc. Business Information Technology (BBIT)"),
                        PresetCourse("C010", "Diploma in Information Technology")
                    )
                ),
                PresetDepartment(
                    name = "Department of Mechatronic Engineering",
                    courses = listOf(
                        PresetCourse("E022", "B.Sc. Mechatronic Engineering"),
                        PresetCourse("E023", "B.Sc. Industrial Instrumentation")
                    )
                ),
                PresetDepartment(
                    name = "Department of Electrical & Electronic Engineering",
                    courses = listOf(
                        PresetCourse("E021", "B.Sc. Electrical & Electronic Engineering"),
                        PresetCourse("E024", "B.Sc. Telecommunication & Information Engineering")
                    )
                ),
                PresetDepartment(
                    name = "Department of Mechanical Engineering",
                    courses = listOf(
                        PresetCourse("E020", "B.Sc. Mechanical Engineering"),
                        PresetCourse("E028", "B.Tech. Mechanical Engineering Technology")
                    )
                ),
                PresetDepartment(
                    name = "Department of Civil Engineering",
                    courses = listOf(
                        PresetCourse("E025", "B.Sc. Civil Engineering"),
                        PresetCourse("E029", "B.Tech. Building Construction")
                    )
                ),
                PresetDepartment(
                    name = "Institute of Geomatics, GIS & Remote Sensing (IGGReS)",
                    courses = listOf(
                        PresetCourse("E031", "B.Sc. Geomatic Engineering & GIS"),
                        PresetCourse("E032", "B.Sc. Geospatial Information Science")
                    )
                ),
                PresetDepartment(
                    name = "School of Business Management & Economics",
                    courses = listOf(
                        PresetCourse("B010", "Bachelor of Commerce (B.Com)"),
                        PresetCourse("B011", "Bachelor of Business Administration (BBA)"),
                        PresetCourse("B012", "B.Sc. Actuarial Science"),
                        PresetCourse("B014", "B.Sc. Procurement & Contract Management")
                    )
                ),
                PresetDepartment(
                    name = "School of Health Sciences",
                    courses = listOf(
                        PresetCourse("H011", "B.Sc. Nursing"),
                        PresetCourse("H012", "B.Sc. Food Science & Technology"),
                        PresetCourse("H013", "B.Sc. Nutrition & Dietetics")
                    )
                ),
                PresetDepartment(
                    name = "Institute of Tourism & Hospitality Management",
                    courses = listOf(
                        PresetCourse("T011", "B.Sc. Sustainable Tourism & Hospitality Management")
                    )
                )
            )
        ),
        KenyanUniversity(
            id = "uon",
            name = "UNIVERSITY OF NAIROBI",
            shortName = "UoN",
            location = "Nairobi, Kenya",
            primaryColorHex = 0xFF003366,
            accentColorHex = 0xFFC5A059,
            departments = sharedComputingDepts + sharedEngineeringDepts + sharedBusinessDepts + listOf(
                PresetDepartment(
                    name = "School of Law",
                    courses = listOf(PresetCourse("LLB", "Bachelor of Laws (LL.B.)"))
                ),
                PresetDepartment(
                    name = "School of Medicine & Health Sciences",
                    courses = listOf(
                        PresetCourse("MBChB", "Bachelor of Medicine & Surgery"),
                        PresetCourse("BPHARM", "Bachelor of Pharmacy"),
                        PresetCourse("BSN", "B.Sc. Nursing")
                    )
                )
            )
        ),
        KenyanUniversity(
            id = "jkuat",
            name = "JOMO KENYATTA UNIVERSITY OF AGRICULTURE AND TECHNOLOGY",
            shortName = "JKUAT",
            location = "Juja, Kenya",
            primaryColorHex = 0xFF1B5E20,
            accentColorHex = 0xFFC62828,
            departments = sharedComputingDepts + sharedEngineeringDepts + listOf(
                PresetDepartment(
                    name = "Department of Mechatronic Engineering",
                    courses = listOf(PresetCourse("MCT", "B.Sc. Mechatronic Engineering"))
                ),
                PresetDepartment(
                    name = "School of Architecture & Building Sciences",
                    courses = listOf(
                        PresetCourse("BAR", "Bachelor of Architecture"),
                        PresetCourse("BQS", "B.Sc. Quantity Surveying")
                    )
                )
            ) + sharedBusinessDepts
        ),
        KenyanUniversity(
            id = "ku",
            name = "KENYATTA UNIVERSITY",
            shortName = "KU",
            location = "Kahawa, Nairobi, Kenya",
            primaryColorHex = 0xFF004D40,
            accentColorHex = 0xFFD4AF37,
            departments = sharedComputingDepts + listOf(
                PresetDepartment(
                    name = "School of Education",
                    courses = listOf(
                        PresetCourse("BEDS", "B.Ed. (Science)"),
                        PresetCourse("BEDA", "B.Ed. (Arts)")
                    )
                )
            ) + sharedEngineeringDepts + sharedBusinessDepts
        ),
        KenyanUniversity(
            id = "tuk",
            name = "THE TECHNICAL UNIVERSITY OF KENYA",
            shortName = "TU-K",
            location = "Nairobi, Kenya",
            primaryColorHex = 0xFF0277BD,
            accentColorHex = 0xFF37474F,
            departments = sharedComputingDepts + sharedEngineeringDepts + listOf(
                PresetDepartment(
                    name = "School of Creative Arts & Technologies",
                    courses = listOf(
                        PresetCourse("BTECH-IT", "B.Tech. Information Technology"),
                        PresetCourse("BTECH-EE", "B.Tech. Electrical & Electronic Engineering")
                    )
                )
            )
        ),
        KenyanUniversity(
            id = "moi",
            name = "MOI UNIVERSITY",
            shortName = "MU",
            location = "Eldoret, Kenya",
            primaryColorHex = 0xFF1A237E,
            accentColorHex = 0xFFFFB300,
            departments = sharedComputingDepts + sharedEngineeringDepts + sharedBusinessDepts + listOf(
                PresetDepartment(
                    name = "School of Information Sciences",
                    courses = listOf(
                        PresetCourse("INS", "B.Sc. Information Sciences"),
                        PresetCourse("COM", "B.Sc. Communication & Public Relations")
                    )
                )
            )
        ),
        KenyanUniversity(
            id = "egerton",
            name = "EGERTON UNIVERSITY",
            shortName = "EU",
            location = "Njoro, Nakuru, Kenya",
            primaryColorHex = 0xFF2E7D32,
            accentColorHex = 0xFFFBC02D,
            departments = sharedComputingDepts + listOf(
                PresetDepartment(
                    name = "Faculty of Agriculture",
                    courses = listOf(
                        PresetCourse("AGEC", "B.Sc. Agricultural Economics"),
                        PresetCourse("AGEN", "B.Sc. Agricultural Engineering")
                    )
                )
            ) + sharedBusinessDepts
        ),
        KenyanUniversity(
            id = "mut",
            name = "MURANG'A UNIVERSITY OF TECHNOLOGY",
            shortName = "MUT",
            location = "Murang'a, Kenya",
            primaryColorHex = 0xFF00695C,
            accentColorHex = 0xFFF9A825,
            departments = sharedComputingDepts + sharedEngineeringDepts + sharedBusinessDepts
        ),
        KenyanUniversity(
            id = "kyu",
            name = "KIRINYAGA UNIVERSITY",
            shortName = "KyU",
            location = "Kerugoya, Kenya",
            primaryColorHex = 0xFF1B5E20,
            accentColorHex = 0xFF1565C0,
            departments = sharedComputingDepts + sharedBusinessDepts + listOf(
                PresetDepartment(
                    name = "School of Health Sciences",
                    courses = listOf(
                        PresetCourse("HRI", "B.Sc. Health Records & Information Management"),
                        PresetCourse("PH", "B.Sc. Public Health")
                    )
                )
            )
        ),
        KenyanUniversity(
            id = "mmu",
            name = "MULTIMEDIA UNIVERSITY OF KENYA",
            shortName = "MMU",
            location = "Rongai, Nairobi, Kenya",
            primaryColorHex = 0xFF4A148C,
            accentColorHex = 0xFFFF6F00,
            departments = sharedComputingDepts + sharedEngineeringDepts + listOf(
                PresetDepartment(
                    name = "Faculty of Media & Communication",
                    courses = listOf(
                        PresetCourse("BMC", "Bachelor of Mass Communication"),
                        PresetCourse("FAP", "Bachelor of Film Production & Animation")
                    )
                )
            )
        ),
        KenyanUniversity(
            id = "maseno",
            name = "MASENO UNIVERSITY",
            shortName = "MSU",
            location = "Maseno, Kisumu, Kenya",
            primaryColorHex = 0xFF1565C0,
            accentColorHex = 0xFF00897B,
            departments = sharedComputingDepts + sharedBusinessDepts + listOf(
                PresetDepartment(
                    name = "School of Mathematics, Statistics & Actuarial Science",
                    courses = listOf(
                        PresetCourse("ACTS", "B.Sc. Actuarial Science with IT"),
                        PresetCourse("APST", "B.Sc. Applied Statistics with IT")
                    )
                )
            )
        ),
        KenyanUniversity(
            id = "mmust",
            name = "MASINDE MULIRO UNIVERSITY OF SCIENCE AND TECHNOLOGY",
            shortName = "MMUST",
            location = "Kakamega, Kenya",
            primaryColorHex = 0xFF0D47A1,
            accentColorHex = 0xFF2E7D32,
            departments = sharedComputingDepts + sharedEngineeringDepts + sharedBusinessDepts
        ),
        KenyanUniversity(
            id = "strathmore",
            name = "STRATHMORE UNIVERSITY",
            shortName = "SU",
            location = "Nairobi, Kenya",
            primaryColorHex = 0xFF002F6C,
            accentColorHex = 0xFFC8102E,
            departments = sharedComputingDepts + sharedBusinessDepts + listOf(
                PresetDepartment(
                    name = "Strathmore Law School",
                    courses = listOf(PresetCourse("LLB", "Bachelor of Laws (LL.B.)"))
                )
            )
        ),
        KenyanUniversity(
            id = "mku",
            name = "MOUNT KENYA UNIVERSITY",
            shortName = "MKU",
            location = "Thika, Kenya",
            primaryColorHex = 0xFF1E3A8A,
            accentColorHex = 0xFFD97706,
            departments = sharedComputingDepts + sharedBusinessDepts + listOf(
                PresetDepartment(
                    name = "School of Health Sciences",
                    courses = listOf(
                        PresetCourse("BSN", "B.Sc. Nursing"),
                        PresetCourse("BPHARM", "Bachelor of Pharmacy"),
                        PresetCourse("CLIN", "B.Sc. Clinical Medicine")
                    )
                )
            )
        )
    )

    fun getById(id: String?): KenyanUniversity {
        return list.find { it.id == id } ?: list.first()
    }

    fun buildCrestSvg(uni: KenyanUniversity, size: Int = 52): String {
        val primary = String.format("#%06X", (0xFFFFFF and uni.primaryColorHex.toInt()))
        val accent = String.format("#%06X", (0xFFFFFF and uni.accentColorHex.toInt()))
        val abbr = uni.shortName.take(6).uppercase()
        val fontSize = if (abbr.length > 4) "9" else "11"
        return """
            <svg xmlns="http://www.w3.org/2000/svg" width="$size" height="$size" viewBox="0 0 64 64">
              <path d="M32 4 L56 12 V30 C56 46 44 56 32 60 C20 56 8 46 8 30 V12 Z" fill="$primary" stroke="$accent" stroke-width="3"/>
              <path d="M22 22 L32 17 L42 22 L32 27 Z" fill="$accent"/>
              <text x="32" y="42" text-anchor="middle" fill="#FFFFFF" font-family="Arial, sans-serif" font-size="$fontSize" font-weight="bold">$abbr</text>
            </svg>
        """.trimIndent()
    }
}
