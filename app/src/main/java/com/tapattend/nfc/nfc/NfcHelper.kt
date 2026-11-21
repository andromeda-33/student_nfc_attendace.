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
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tapattend.nfc.data.ParsedCardData
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
            arrayOf(android.nfc.tech.IsoDep::class.java.name),
            arrayOf(android.nfc.tech.NfcA::class.java.name),
            arrayOf(android.nfc.tech.NfcB::class.java.name),
            arrayOf(android.nfc.tech.NfcF::class.java.name),
            arrayOf(android.nfc.tech.NfcV::class.java.name),
            arrayOf(android.nfc.tech.MifareClassic::class.java.name),
            arrayOf(android.nfc.tech.MifareUltralight::class.java.name),
            arrayOf(android.nfc.tech.Ndef::class.java.name)
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

    /**
     * Converts raw tag byte array into uppercase hexadecimal string
     */
    fun extractCardUid(tag: Tag): String {
        val idBytes = tag.id ?: return ""
        return idBytes.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
    }

    /**
     * Reads NDEF payload text if the school card has data encoded on it
     */
    fun readNdefPayload(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage ?: return null
            val records = ndefMessage.records ?: return null
            for (record in records) {
                if (record.tnf == NdefRecord.TNF_WELL_KNOWN) {
                    val payload = record.payload ?: continue
                    if (payload.isEmpty()) continue
                    val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
                    val languageCodeLength = payload[0].toInt() and 51
                    if (payload.size > languageCodeLength + 1) {
                        return String(
                            payload,
                            languageCodeLength + 1,
                            payload.size - languageCodeLength - 1,
                            java.nio.charset.Charset.forName(textEncoding)
                        )
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        } finally {
            try {
                ndef.close()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Parses card UID and any NDEF text for student name and registration heuristics
     */
    fun parseStudentCard(tag: Tag): ParsedCardData {
        val uid = extractCardUid(tag)
        val text = readNdefPayload(tag)

        var suggestedName: String? = null
        var suggestedReg: String? = null

        if (!text.isNullOrBlank()) {
            val trimmed = text.trim()
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val nameRegex = Regex("\"(?:name|fullName)\"\\s*:\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)
                val regRegex = Regex("\"(?:reg|id|regNumber|studentId)\"\\s*:\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)
                suggestedName = nameRegex.find(trimmed)?.groupValues?.get(1)
                suggestedReg = regRegex.find(trimmed)?.groupValues?.get(1)
            } else {
                val parts = trimmed.split(Regex("[,|\\t\\n]")).map { it.trim() }.filter { it.isNotEmpty() }
                if (parts.size >= 2) {
                    val first = parts[0]
                    val second = parts[1]
                    if (first.any { it.isDigit() || it == '/' || it == '-' }) {
                        suggestedReg = first
                        suggestedName = second
                    } else {
                        suggestedName = first
                        suggestedReg = second
                    }
                } else if (parts.size == 1) {
                    if (parts[0].any { it.isDigit() }) {
                        suggestedReg = parts[0]
                    } else {
                        suggestedName = parts[0]
                    }
                }
            }
        }

        return ParsedCardData(
            cardUid = uid,
            ndefText = text,
            suggestedName = suggestedName,
            suggestedRegNumber = suggestedReg
        )
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
                    val fadeSamples = (sampleRate * 8) / 1000 // 8ms smooth envelope
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
