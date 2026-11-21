package com.tapattend.nfc.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CloudDatabaseSync(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tapattend_cloud_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PROJECT_ID = "tera-inscriber-r01dl"
        private const val DATABASE_ID =
            "ai-studio-tapattendnfc-97fb216d-8b5e-46f7-9722-155ba8a9c1bf"
        private const val API_KEY = "AIzaSyBIrTS8bcV7qD20CwuMVEKtpiXMUZ7yD5Q"
        private const val KEY_REFRESH_TOKEN = "firebase_refresh_token"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }

    fun getRefreshToken(): String = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token.trim()).apply()
    }

    fun isConfigured(): Boolean = getRefreshToken().isNotBlank()

    fun getLastSyncTimeFormatted(): String? {
        val ts = prefs.getLong(KEY_LAST_SYNC, 0L)
        if (ts == 0L) return null
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
    }

    data class AuthSession(val idToken: String, val userId: String)

    data class SyncSummary(
        val success: Boolean,
        val message: String,
        val addedCount: Int = 0,
        val totalCloudCount: Int = 0
    )

    private fun exchangeRefreshToken(): AuthSession {
        val refreshToken = getRefreshToken()
        if (refreshToken.isBlank()) {
            throw IllegalStateException("Cloud Sync Key not configured. Open Cloud Sync to paste your key.")
        }

        val url = URL("https://securetoken.googleapis.com/v1/token?key=$API_KEY")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            doOutput = true
            connectTimeout = 10000
            readTimeout = 10000
        }

        OutputStreamWriter(conn.outputStream).use {
            it.write("grant_type=refresh_token&refresh_token=$refreshToken")
        }

        val code = conn.responseCode
        val body = readStream(if (code in 200..299) conn.inputStream else conn.errorStream)
        if (code !in 200..299) {
            throw IllegalStateException("Auth token refresh failed ($code): $body")
        }

        val json = JSONObject(body)
        return AuthSession(
            idToken = json.getString("id_token"),
            userId = json.getString("user_id")
        )
    }

    private fun isoNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun sanitizeId(raw: String): String {
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "")
        return cleaned.ifBlank { "id_${System.currentTimeMillis()}" }.take(128)
    }

    suspend fun pushStudent(student: Student): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isConfigured()) return@withContext Result.success(Unit)
            val cleanUid = student.cardUid.replace(":", "").replace(" ", "").trim().uppercase()
            if (cleanUid.length < 4) return@withContext Result.success(Unit)

            val session = exchangeRefreshToken()
            val docId = sanitizeId(student.id)
            val base =
                "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/$DATABASE_ID/documents"
            val url = URL("$base/students/$docId")

            val nowIso = isoNow()
            val fields = JSONObject().apply {
                put("id", strVal(docId))
                put("cardUid", strVal(cleanUid.take(32)))
                put("regNumber", strVal(student.regNumber.take(40)))
                put("fullName", strVal(student.fullName.take(100)))
                put("universityId", strVal(student.universityId.take(64)))
                put("universityName", strVal(student.universityName.take(140)))
                put("department", strVal(student.department.take(120)))
                put("courseCode", strVal(student.courseCode.take(32)))
                put("courseName", strVal(student.courseName.take(140)))
                put("createdByUid", strVal(session.userId))
                put("createdAt", JSONObject().put("timestampValue", nowIso))
                put("updatedAt", JSONObject().put("timestampValue", nowIso))
            }

            val payload = JSONObject().put("fields", fields).toString()
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("Authorization", "Bearer ${session.idToken}")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = readStream(conn.errorStream)
                return@withContext Result.failure(IllegalStateException("Cloud save failed ($code): $err"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushCustomCourse(course: CustomCourse): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isConfigured()) return@withContext Result.success(Unit)
            val session = exchangeRefreshToken()
            val docId = sanitizeId(course.id)
            val base =
                "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/$DATABASE_ID/documents"
            val url = URL("$base/customCourses/$docId")

            val fields = JSONObject().apply {
                put("id", strVal(docId))
                put("universityId", strVal(course.universityId.take(64)))
                put("department", strVal(course.department.take(120)))
                put("courseCode", strVal(course.courseCode.take(32)))
                put("courseName", strVal(course.courseName.take(140)))
                put("createdByUid", strVal(session.userId))
                put("createdAt", JSONObject().put("timestampValue", isoNow()))
            }

            val payload = JSONObject().put("fields", fields).toString()
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("Authorization", "Bearer ${session.idToken}")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) {
                return@withContext Result.failure(IllegalStateException("Cloud course save failed ($code)"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncRoster(
        universityId: String,
        storage: AttendanceStorage
    ): SyncSummary = withContext(Dispatchers.IO) {
        try {
            if (!isConfigured()) {
                return@withContext SyncSummary(
                    success = false,
                    message = "Paste your Cloud Sync Key in Settings to enable multi-device updates."
                )
            }

            val session = exchangeRefreshToken()

            // 1. Push local students with valid card UIDs
            storage.getStudents().forEach { student ->
                val uid = student.cardUid.replace(":", "").replace(" ", "").trim()
                if (uid.length >= 4) {
                    pushStudent(student)
                }
            }

            // 2. Query students for this university from Firestore
            val base =
                "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/$DATABASE_ID/documents"
            val queryUrl = URL("$base:runQuery")
            val structuredQuery = JSONObject().apply {
                put("from", JSONArray().put(JSONObject().put("collectionId", "students")))
                put(
                    "where",
                    JSONObject().put(
                        "fieldFilter",
                        JSONObject().apply {
                            put("field", JSONObject().put("fieldPath", "universityId"))
                            put("op", "EQUAL")
                            put("value", strVal(universityId))
                        }
                    )
                )
            }

            val conn = (queryUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer ${session.idToken}")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 12000
                readTimeout = 12000
            }
            OutputStreamWriter(conn.outputStream).use {
                it.write(JSONObject().put("structuredQuery", structuredQuery).toString())
            }

            val code = conn.responseCode
            val body = readStream(if (code in 200..299) conn.inputStream else conn.errorStream)
            if (code !in 200..299) {
                return@withContext SyncSummary(false, "Sync error ($code): $body")
            }

            val arr = JSONArray(body)
            val cloudStudents = mutableListOf<Student>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                if (!item.has("document")) continue
                val doc = item.getJSONObject("document")
                val f = doc.optJSONObject("fields") ?: continue
                cloudStudents.add(
                    Student(
                        id = readStr(f, "id").ifBlank { doc.getString("name").substringAfterLast("/") },
                        cardUid = readStr(f, "cardUid"),
                        regNumber = readStr(f, "regNumber"),
                        fullName = readStr(f, "fullName"),
                        universityId = readStr(f, "universityId", universityId),
                        universityName = readStr(f, "universityName"),
                        department = readStr(f, "department"),
                        courseCode = readStr(f, "courseCode"),
                        courseName = readStr(f, "courseName"),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            val added = storage.mergeCloudStudents(cloudStudents)
            prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()

            SyncSummary(
                success = true,
                message = "Updated! Synced ${cloudStudents.size} students ($added new).",
                addedCount = added,
                totalCloudCount = cloudStudents.size
            )
        } catch (e: Exception) {
            SyncSummary(false, "Sync failed: ${e.message ?: "Network error"}")
        }
    }

    private fun strVal(v: String): JSONObject = JSONObject().put("stringValue", v)

    private fun readStr(fields: JSONObject, key: String, fallback: String = ""): String {
        val obj = fields.optJSONObject(key) ?: return fallback
        return obj.optString("stringValue", fallback)
    }

    private fun readStream(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        BufferedReader(InputStreamReader(stream)).use { reader ->
            return reader.readText()
        }
    }
}
