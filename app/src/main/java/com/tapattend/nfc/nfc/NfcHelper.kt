package com.tapattend.nfc.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tapattend.nfc.data.KenyanUniversities
import com.tapattend.nfc.data.ParsedCardData
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.sin

class NfcHelper(private val activity: Activity) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {
        }
    }

    val isNfcSupported: Boolean
        get() = nfcAdapter != null

    val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    /**
     * Enables foreground dispatch so that while the app is active, NFC scans
     * are captured immediately without Android prompting "Open with..." dialogs.
     */
    fun enableForegroundDispatch() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled) return

        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, flags)

        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        )

        val techLists = arrayOf(
            arrayOf(IsoDep::class.java.name),
            arrayOf(android.nfc.tech.NfcA::class.java.name),
            arrayOf(android.nfc.tech.NfcB::class.java.name),
            arrayOf(android.nfc.tech.NfcF::class.java.name),
            arrayOf(android.nfc.tech.NfcV::class.java.name),
            arrayOf(MifareClassic::class.java.name),
            arrayOf(MifareUltralight::class.java.name),
            arrayOf(Ndef::class.java.name)
        )

        try {
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
        } catch (_: Exception) {
        }
    }

    fun disableForegroundDispatch() {
        try {
            nfcAdapter?.disableForegroundDispatch(activity)
        } catch (_: Exception) {
        }
    }

    fun extractCardUid(tag: Tag): String {
        val idBytes = tag.id ?: return ""
        return idBytes.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
    }

    private fun extractPrintableAscii(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            val c = b.toInt() and 0xFF
            when {
                c in 32..126 -> sb.append(c.toChar())
                c == 10 || c == 13 || c == 9 -> sb.append('\n')
                c == 0 -> sb.append(' ')
            }
        }
        return sb.toString()
            .replace(Regex("[ ]{2,}"), " ")
            .lines()
            .map { it.trim() }
            .filter { it.length >= 2 }
            .joinToString("\n")
    }

    private fun decodeNdefRecord(record: NdefRecord): String? {
        val payload = record.payload ?: return null
        if (payload.isEmpty()) return null

        return when (record.tnf) {
            NdefRecord.TNF_WELL_KNOWN -> {
                if (record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                    val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                    val langLen = payload[0].toInt() and 0x3F
                    if (payload.size > langLen + 1) {
                        String(payload, langLen + 1, payload.size - langLen - 1, charset(textEncoding))
                    } else null
                } else if (record.type.contentEquals(NdefRecord.RTD_URI)) {
                    val uriPrefix = when (payload[0].toInt() and 0xFF) {
                        0x01 -> "http://www."
                        0x02 -> "https://www."
                        0x03 -> "http://"
                        0x04 -> "https://"
                        else -> ""
                    }
                    uriPrefix + String(payload, 1, payload.size - 1, Charsets.UTF_8)
                } else {
                    extractPrintableAscii(payload).ifBlank { null }
                }
            }
            NdefRecord.TNF_MIME_MEDIA,
            NdefRecord.TNF_ABSOLUTE_URI,
            NdefRecord.TNF_EXTERNAL_TYPE -> {
                String(payload, Charsets.UTF_8).trim().ifBlank { null }
            }
            else -> extractPrintableAscii(payload).ifBlank { null }
        }
    }

    @Suppress("DEPRECATION")
    private fun readAllNdefText(tag: Tag, intent: Intent?): Pair<String?, String> {
        val messages = mutableListOf<NdefMessage>()
        val log = StringBuilder()

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val msg = ndef.ndefMessage ?: ndef.cachedNdefMessage
                if (msg != null) messages.add(msg)
                log.appendLine("NDEF MaxSize: ${ndef.maxSize} bytes, Writable: ${ndef.isWritable}")
            } catch (e: Exception) {
                log.appendLine("NDEF read error: ${e.message}")
            } finally {
                try { ndef.close() } catch (_: Exception) {}
            }
        }

        if (messages.isEmpty() && intent != null) {
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            rawMsgs?.forEach { parcelable ->
                if (parcelable is NdefMessage) messages.add(parcelable)
            }
        }

        val decodedParts = mutableListOf<String>()
        messages.forEach { msg ->
            msg.records.forEachIndexed { idx, rec ->
                val payload = rec.payload ?: ByteArray(0)
                val typeStr = String(rec.type ?: ByteArray(0), Charsets.UTF_8)
                val text = decodeNdefRecord(rec)
                if (!text.isNullOrBlank()) {
                    decodedParts.add(text)
                    log.appendLine("NDEF[$idx] (TNF=${rec.tnf}, type=$typeStr, ${payload.size}B): $text")
                } else {
                    val hex = if (payload.isNotEmpty()) bytesToHex(payload) else "<empty>"
                    log.appendLine("NDEF[$idx] (TNF=${rec.tnf}, type=$typeStr, ${payload.size}B): HEX=$hex")
                }
            }
        }

        return Pair(
            decodedParts.joinToString("\n").ifBlank { null },
            log.toString().trim()
        )
    }

    private fun readMifareUltralight(tag: Tag): Pair<String?, String> {
        val ul = MifareUltralight.get(tag) ?: return Pair(null, "")
        val dump = StringBuilder()
        val asciiCollected = StringBuilder()
        return try {
            ul.connect()
            val typeLabel = when (ul.type) {
                MifareUltralight.TYPE_ULTRALIGHT_C -> "MIFARE Ultralight C"
                MifareUltralight.TYPE_ULTRALIGHT -> "MIFARE Ultralight"
                else -> "Ultralight / NTAG"
            }
            dump.appendLine("Chip: $typeLabel")

            for (page in 4..36 step 4) {
                try {
                    val data = ul.readPages(page)
                    val ascii = extractPrintableAscii(data)
                    if (ascii.isNotBlank()) {
                        asciiCollected.appendLine(ascii)
                    }
                    dump.appendLine("Pages $page-${page + 3}: ${bytesToHex(data)}")
                } catch (_: Exception) {
                    break
                }
            }
            Pair(asciiCollected.toString().trim().ifBlank { null }, dump.toString().trim())
        } catch (e: Exception) {
            Pair(null, "Ultralight read error: ${e.message}")
        } finally {
            try { ul.close() } catch (_: Exception) {}
        }
    }

    private fun readMifareClassic(tag: Tag): Pair<String?, String> {
        val mfc = MifareClassic.get(tag) ?: return Pair(null, "")
        val dump = StringBuilder()
        val asciiCollected = StringBuilder()
        var unlockedSectors = 0
        var lockedSectors = 0

        val commonKeys = listOf(
            MifareClassic.KEY_DEFAULT,
            MifareClassic.KEY_NFC_FORUM,
            MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY,
            byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        )

        return try {
            mfc.connect()
            dump.appendLine("MIFARE Classic: ${mfc.size} bytes, ${mfc.sectorCount} sectors")

            for (sector in 0 until mfc.sectorCount) {
                var authenticated = false
                for (key in commonKeys) {
                    try {
                        if (mfc.authenticateSectorWithKeyA(sector, key) ||
                            mfc.authenticateSectorWithKeyB(sector, key)
                        ) {
                            authenticated = true
                            break
                        }
                    } catch (_: Exception) {
                    }
                }

                if (!authenticated) {
                    lockedSectors++
                    continue
                }

                unlockedSectors++
                val firstBlock = mfc.sectorToBlock(sector)
                val blockCount = mfc.getBlockCountInSector(sector)
                for (offset in 0 until (blockCount - 1)) {
                    val blockIndex = firstBlock + offset
                    if (sector == 0 && offset == 0) continue // Skip manufacturer UID block
                    try {
                        val blockBytes = mfc.readBlock(blockIndex)
                        val ascii = extractPrintableAscii(blockBytes)
                        if (ascii.isNotBlank()) {
                            asciiCollected.appendLine(ascii)
                            dump.appendLine("S$sector/B$blockIndex: \"$ascii\"")
                        } else {
                            val nonZero = blockBytes.any { it.toInt() != 0 }
                            if (nonZero) {
                                dump.appendLine("S$sector/B$blockIndex: ${bytesToHex(blockBytes)}")
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            dump.appendLine("Sectors readable: $unlockedSectors | Encrypted/locked: $lockedSectors")
            if (unlockedSectors == 0) {
                dump.appendLine("All sectors use private university keys — only card UID is exposed locally.")
            }

            Pair(asciiCollected.toString().trim().ifBlank { null }, dump.toString().trim())
        } catch (e: Exception) {
            Pair(null, "MIFARE Classic error: ${e.message}")
        } finally {
            try { mfc.close() } catch (_: Exception) {}
        }
    }

    private fun readIsoDep(tag: Tag): Pair<String?, String> {
        val iso = IsoDep.get(tag) ?: return Pair(null, "")
        val dump = StringBuilder()
        val asciiCollected = StringBuilder()

        return try {
            iso.connect()
            iso.timeout = 2000
            val hist = iso.historicalBytes ?: iso.hiLayerResponse
            if (hist != null && hist.isNotEmpty()) {
                val histAscii = extractPrintableAscii(hist)
                dump.appendLine("IsoDep Historical Bytes: ${bytesToHex(hist)}")
                if (histAscii.isNotBlank()) {
                    asciiCollected.appendLine(histAscii)
                    dump.appendLine("IsoDep ASCII: $histAscii")
                }
            }

            // Probe standard NFC Forum Type 4 NDEF AID: D2760000850101
            val selectNdefAid = byteArrayOf(
                0x00, 0xA4.toByte(), 0x04, 0x00,
                0x07, 0xD2.toByte(), 0x76, 0x00, 0x00, 0x85.toByte(), 0x01, 0x01,
                0x00
            )
            val resp = iso.transceive(selectNdefAid)
            val swHex = if (resp.size >= 2) {
                "%02X%02X".format(resp[resp.size - 2].toInt() and 0xFF, resp[resp.size - 1].toInt() and 0xFF)
            } else {
                ""
            }
            dump.appendLine("IsoDep NDEF AID status: SW=$swHex")
            if (swHex == "9000") {
                // Select NDEF file E104 and read length + payload
                val selectFile = byteArrayOf(0x00, 0xA4.toByte(), 0x00, 0x0C, 0x02, 0xE1.toByte(), 0x04)
                val selResp = iso.transceive(selectFile)
                if (selResp.size >= 2 && selResp[selResp.size - 2] == 0x90.toByte()) {
                    val readCmd = byteArrayOf(0x00, 0xB0.toByte(), 0x00, 0x00, 0x80.toByte())
                    val fileResp = iso.transceive(readCmd)
                    if (fileResp.size > 4) {
                        val payloadBytes = fileResp.copyOf(fileResp.size - 2)
                        val text = extractPrintableAscii(payloadBytes)
                        if (text.isNotBlank()) {
                            asciiCollected.appendLine(text)
                            dump.appendLine("IsoDep File E104 ASCII:\n$text")
                        }
                    }
                }
            } else {
                dump.appendLine("SmartCard application is proprietary/encrypted (not public Type-4 NDEF).")
            }

            Pair(asciiCollected.toString().trim().ifBlank { null }, dump.toString().trim())
        } catch (e: Exception) {
            Pair(null, "IsoDep probe: ${e.message ?: "Card closed or protected"}")
        } finally {
            try { iso.close() } catch (_: Exception) {}
        }
    }

    /**
     * Reads all accessible local NFC memory (NDEF, MIFARE Ultralight, MIFARE Classic, IsoDep)
     * and parses Name, Registration Number, Course, and Department if present on the card.
     */
    fun parseStudentCard(tag: Tag, intent: Intent? = null): ParsedCardData {
        val uid = extractCardUid(tag)
        val techs = tag.techList.map { it.substringAfterLast('.') }

        val dumpSections = mutableListOf<String>()
        dumpSections.add("Card UID: $uid")
        dumpSections.add("Technologies: ${techs.joinToString(", ")}")

        val textSources = mutableListOf<Pair<String, String>>()

        val (ndefText, ndefDump) = readAllNdefText(tag, intent)
        if (ndefDump.isNotBlank()) dumpSections.add(ndefDump)
        if (!ndefText.isNullOrBlank()) textSources.add("NDEF Payload" to ndefText)

        val (ulText, ulDump) = readMifareUltralight(tag)
        if (ulDump.isNotBlank()) dumpSections.add(ulDump)
        if (!ulText.isNullOrBlank()) textSources.add("MIFARE Ultralight Pages" to ulText)

        val (mfcText, mfcDump) = readMifareClassic(tag)
        if (mfcDump.isNotBlank()) dumpSections.add(mfcDump)
        if (!mfcText.isNullOrBlank()) textSources.add("MIFARE Classic Sectors" to mfcText)

        val (isoText, isoDump) = readIsoDep(tag)
        if (isoDump.isNotBlank()) dumpSections.add(isoDump)
        if (!isoText.isNullOrBlank()) textSources.add("IsoDep SmartCard" to isoText)

        val combinedText = textSources.joinToString("\n") { it.second }.trim()
        val memorySource = if (textSources.isNotEmpty()) {
            textSources.joinToString(" + ") { it.first }
        } else {
            "UID Only (No readable text in card memory)"
        }

        val base = parseTextIntoCardData(
            cardUid = uid,
            rawText = combinedText.ifBlank { null },
            techList = techs,
            memorySource = memorySource,
            rawDump = dumpSections.joinToString("\n\n")
        )
        return base
    }

    /**
     * Parses raw text extracted from an NFC card (or simulated local card payload)
     * into Student Name, Reg Number, Course, and Department.
     */
    fun parseTextIntoCardData(
        cardUid: String,
        rawText: String?,
        techList: List<String> = emptyList(),
        memorySource: String = "NDEF Payload",
        rawDump: String = ""
    ): ParsedCardData {
        var suggestedName: String? = null
        var suggestedReg: String? = null
        var suggestedCourse: String? = null
        var suggestedCourseCode: String? = null
        var suggestedDept: String? = null

        if (!rawText.isNullOrBlank()) {
            val trimmed = rawText.trim()

            // 1. Try JSON object anywhere in the payload
            val jsonStart = trimmed.indexOf('{')
            val jsonEnd = trimmed.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                try {
                    val obj = JSONObject(trimmed.substring(jsonStart, jsonEnd + 1))
                    suggestedName = firstNonBlankJson(obj, "fullName", "name", "studentName")
                    suggestedReg = firstNonBlankJson(obj, "regNumber", "regNo", "reg", "studentId", "id")
                    suggestedCourse = firstNonBlankJson(obj, "courseName", "course", "programme", "program")
                    suggestedCourseCode = firstNonBlankJson(obj, "courseCode", "code")
                    suggestedDept = firstNonBlankJson(obj, "department", "dept", "school", "faculty")
                } catch (_: Exception) {
                }
            }

            // 2. Try Key-Value or vCard lines (e.g. "Name: ...", "Reg: ...", "Course: ...", "Dept: ...")
            val lines = trimmed.lines().map { it.trim() }.filter { it.isNotEmpty() }
            for (line in lines) {
                val lower = line.lowercase()
                val afterColon = line.substringAfter(':', "").substringAfter('=', "").trim()
                if (afterColon.isNotEmpty() && (line.contains(':') || line.contains('='))) {
                    when {
                        (lower.startsWith("fn:") || lower.startsWith("name") || lower.startsWith("full")) && suggestedName == null ->
                            suggestedName = afterColon
                        (lower.startsWith("reg") || lower.startsWith("adm") || lower.startsWith("student id") || lower.startsWith("id")) && suggestedReg == null ->
                            suggestedReg = afterColon.uppercase()
                        (lower.startsWith("course") || lower.startsWith("prog")) && suggestedCourse == null ->
                            suggestedCourse = afterColon
                        (lower.startsWith("dept") || lower.startsWith("department") || lower.startsWith("school") || lower.startsWith("faculty")) && suggestedDept == null ->
                            suggestedDept = afterColon
                    }
                }
            }

            // 3. Fallback delimited format (pipe, comma, semicolon, tab, or multi-line)
            if (suggestedReg == null || suggestedName == null) {
                val tokens = trimmed.split(Regex("[|\\t\\n,;]+"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() && !it.startsWith("{") }

                for (token in tokens) {
                    when {
                        suggestedReg == null && isLikelyRegNumber(token) -> suggestedReg = token.uppercase()
                        suggestedDept == null && isLikelyDepartment(token) -> suggestedDept = token
                        suggestedCourse == null && isLikelyCourse(token) -> suggestedCourse = token
                        suggestedName == null && isLikelyFullName(token) -> suggestedName = token
                    }
                }
            }

            // 4. Regex scan for Kenyan university registration number anywhere in text
            if (suggestedReg == null) {
                val kenyanRegRegex = Regex("""\b[A-Z]{1,4}\d{2,4}[-/]\d{2}[-/]\d{2,6}/\d{2,4}\b""", RegexOption.IGNORE_CASE)
                val slashRegRegex = Regex("""\b[A-Z]{2,5}/\d{4}/\d{2,6}\b""", RegexOption.IGNORE_CASE)
                suggestedReg = (kenyanRegRegex.find(trimmed)?.value ?: slashRegRegex.find(trimmed)?.value)?.uppercase()
            }

            // 5. Auto-infer Kenyan university Department & Course from registration number prefix (e.g. C026 -> B.Sc. Computer Science)
            if (suggestedReg != null && (suggestedCourse == null || suggestedDept == null)) {
                val prefix = Regex("""^[A-Z]+\d+""").find(suggestedReg.trim())?.value
                if (!prefix.isNullOrBlank()) {
                    for (uni in KenyanUniversities.list) {
                        for (dept in uni.departments) {
                            val matchedCourse = dept.courses.find { it.code.equals(prefix, ignoreCase = true) }
                            if (matchedCourse != null) {
                                if (suggestedDept == null) suggestedDept = dept.name
                                if (suggestedCourse == null) suggestedCourse = matchedCourse.name
                                if (suggestedCourseCode == null) suggestedCourseCode = matchedCourse.code
                                break
                            }
                        }
                        if (suggestedCourse != null) break
                    }
                }
            }
        }

        val hasLocalData = !suggestedName.isNullOrBlank() && !suggestedReg.isNullOrBlank()

        return ParsedCardData(
            cardUid = cardUid,
            ndefText = rawText,
            suggestedName = suggestedName,
            suggestedRegNumber = suggestedReg,
            suggestedCourse = suggestedCourse,
            suggestedCourseCode = suggestedCourseCode,
            suggestedDepartment = suggestedDept,
            techList = techList,
            memorySource = memorySource,
            rawDump = rawDump.ifBlank { rawText ?: "UID: $cardUid" },
            hasLocalStudentData = hasLocalData
        )
    }

    private fun firstNonBlankJson(obj: JSONObject, vararg keys: String): String? {
        for (k in keys) {
            val v = obj.optString(k, "").trim()
            if (v.isNotBlank()) return v
        }
        return null
    }

    private fun isLikelyRegNumber(token: String): Boolean {
        if (token.length < 6 || token.length > 32) return false
        val hasDigit = token.any { it.isDigit() }
        val hasSep = token.contains('/') || token.contains('-')
        return hasDigit && hasSep && !token.contains("department", ignoreCase = true)
    }

    private fun isLikelyDepartment(token: String): Boolean {
        val l = token.lowercase()
        return l.contains("department") || l.contains("dept") || l.startsWith("school of") || l.startsWith("faculty of") || l.startsWith("institute of")
    }

    private fun isLikelyCourse(token: String): Boolean {
        val l = token.lowercase()
        return l.startsWith("b.sc") || l.startsWith("bsc") || l.startsWith("bachelor") ||
            l.startsWith("b.tech") || l.startsWith("b.ed") || l.startsWith("diploma") ||
            l.contains("engineering") || l.contains("computer science") || l.contains("information technology")
    }

    private fun isLikelyFullName(token: String): Boolean {
        if (token.length < 4 || token.length > 60) return false
        if (token.any { it.isDigit() }) return false
        if (isLikelyDepartment(token) || isLikelyCourse(token)) return false
        val words = token.split(Regex("\\s+")).filter { it.isNotBlank() }
        return words.size in 2..5 && words.all { it.all { ch -> ch.isLetter() || ch == '\'' || ch == '-' || ch == '.' } }
    }

    /**
     * Crisp ascending chime for successful attendance
     */
    fun playSuccessFeedback() {
        vibrate(longArrayOf(0, 70))
        playToneSequence(
            tones = listOf(880.0 to 90, 1318.5 to 140),
            fallbackTone = ToneGenerator.TONE_PROP_BEEP
        )
    }

    /**
     * Double warning buzz for duplicate scan
     */
    fun playDuplicateWarningFeedback() {
        vibrate(longArrayOf(0, 100, 80, 100))
        playToneSequence(
            tones = listOf(587.3 to 90, 0.0 to 50, 587.3 to 110),
            fallbackTone = ToneGenerator.TONE_PROP_NACK
        )
    }

    /**
     * Distinct descending alert tone for unrecognized student / wrong course
     */
    fun playUnrecognizedStudentFeedback() {
        vibrate(longArrayOf(0, 60, 45, 60, 45, 110))
        playToneSequence(
            tones = listOf(440.0 to 130, 0.0 to 30, 261.6 to 220),
            fallbackTone = ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE
        )
    }

    fun playUnknownCardFeedback() {
        playUnrecognizedStudentFeedback()
    }

    private fun playToneSequence(tones: List<Pair<Double, Int>>, fallbackTone: Int) {
        Thread {
            try {
                val sampleRate = 22050
                val totalMs = tones.sumOf { it.second }
                val totalSamples = (sampleRate * totalMs) / 1000
                val pcm = ShortArray(totalSamples)

                var offset = 0
                for ((freqHz, durationMs) in tones) {
                    val count = (sampleRate * durationMs) / 1000
                    val fadeSamples = (sampleRate * 8) / 1000
                    for (i in 0 until count) {
                        if (offset + i >= totalSamples) break
                        if (freqHz <= 0.0) {
                            pcm[offset + i] = 0
                        } else {
                            val env = when {
                                i < fadeSamples -> i.toDouble() / fadeSamples
                                i > count - fadeSamples -> (count - i).toDouble() / fadeSamples
                                else -> 1.0
                            }.coerceIn(0.0, 1.0)
                            val sample = sin(2.0 * PI * freqHz * i / sampleRate) * env * 0.65
                            pcm[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
                        }
                    }
                    offset += count
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                Thread.sleep((totalMs + 60).toLong())
                track.stop()
                track.release()
            } catch (_: Exception) {
                try {
                    toneGenerator?.startTone(fallbackTone, 200)
                } catch (_: Exception) {
                }
            }
        }.start()
    }

    private fun vibrate(pattern: LongArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            activity.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(pattern, -1)
            }
        }
    }
}
