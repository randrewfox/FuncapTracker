package com.funcap.tracker.backup

import android.content.Context
import android.net.Uri
import com.funcap.tracker.data.AssessmentEntity
import com.funcap.tracker.model.FuncapDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Zero-Setup Backup & Google Drive Integration Manager:
 *
 * 1. Automatic Cloud Backup: Handled automatically by Android Auto Backup via
 *    `backup_rules.xml` and `data_extraction_rules.xml` directly to the user's Google Account.
 * 2. Direct Google Drive File Export & Restore (Zero OAuth Setup):
 *    Uses Android's Storage Access Framework (`CreateDocument` / `OpenDocument`) which exposes
 *    the user's Google Drive directly in the system file picker.
 */
object BackupManager {

    private const val BACKUP_SCHEMA_VERSION = 1

    /**
     * Serializes all logged FUNCAP55 and FUNCAP27 assessments into a lossless JSON backup
     * and writes it to the selected Uri (e.g., inside the user's Google Drive folder).
     */
    suspend fun exportJsonToUri(
        context: Context,
        uri: Uri,
        assessments: List<AssessmentEntity>
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject()
            root.put("schemaVersion", BACKUP_SCHEMA_VERSION)
            root.put("exportedAtEpochMillis", System.currentTimeMillis())
            root.put("app", "FUNCAP Tracker Android")

            val array = JSONArray()
            for (item in assessments) {
                val obj = JSONObject()
                obj.put("timestampEpochMillis", item.timestampEpochMillis)
                obj.put("testType", item.testType)
                obj.put("totalScore", item.totalScore)
                obj.put("equivalentFuncap27Score", item.equivalentFuncap27Score)
                obj.put("domainScores", JSONObject(item.domainScoresJson))
                obj.put("itemResponses", JSONObject(item.itemResponsesJson))
                obj.put("notes", item.notes)
                array.put(obj)
            }
            root.put("assessments", array)

            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                    writer.write(root.toString(2))
                }
            } ?: error("Could not open Google Drive / document output stream.")

            assessments.size
        }
    }

    /**
     * Reads a JSON backup file from Google Drive or local storage and parses all assessments.
     */
    suspend fun importJsonFromUri(
        context: Context,
        uri: Uri
    ): Result<List<AssessmentEntity>> = withContext(Dispatchers.IO) {
        runCatching {
            val rawJson = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
            } ?: error("Could not open selected backup file.")

            val root = JSONObject(rawJson)
            val array = root.getJSONArray("assessments")
            val restored = mutableListOf<AssessmentEntity>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                restored.add(
                    AssessmentEntity(
                        timestampEpochMillis = obj.getLong("timestampEpochMillis"),
                        testType = obj.getString("testType"),
                        totalScore = obj.getDouble("totalScore"),
                        equivalentFuncap27Score = obj.optDouble(
                            "equivalentFuncap27Score",
                            obj.getDouble("totalScore")
                        ),
                        domainScoresJson = obj.getJSONObject("domainScores").toString(),
                        itemResponsesJson = obj.getJSONObject("itemResponses").toString(),
                        notes = obj.optString("notes", "")
                    )
                )
            }
            restored
        }
    }

    /**
     * Exports a clinician- and spreadsheet-friendly CSV log (compatible with Google Sheets on Drive).
     */
    suspend fun exportCsvToUri(
        context: Context,
        uri: Uri,
        assessments: List<AssessmentEntity>
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val sb = StringBuilder()
            sb.append(
                "Date,Test_Type,Total_Score,Equiv_FUNCAP27_Score," +
                "A_Hygiene,B_Walking,C_Upright,D_Home,E_Communication,F_Outside,G_LightSound,H_Concentration,Notes\n"
            )

            for (item in assessments.sortedBy { it.timestampEpochMillis }) {
                val dateStr = dateFormat.format(Date(item.timestampEpochMillis))
                val domains = item.parsedDomainScores()
                val escapedNotes = "\"" + item.notes.replace("\"", "\"\"") + "\""
                sb.append(
                    listOf(
                        dateStr,
                        item.testType,
                        String.format(Locale.US, "%.2f", item.totalScore),
                        String.format(Locale.US, "%.2f", item.equivalentFuncap27Score),
                        String.format(Locale.US, "%.2f", domains[FuncapDomain.A] ?: 0.0),
                        String.format(Locale.US, "%.2f", domains[FuncapDomain.B] ?: 0.0),
                        String.format(Locale.US, "%.2f", domains[FuncapDomain.C] ?: 0.0),
                        String.format(Locale.US, "%.2f", domains[FuncapDomain.D] ?: 0.0),
                        String.format(Locale.US, "%.2f", domains[FuncapDomain.E] ?: 0.0),
                        String.format(Locale.US, "%.2f", domains[FuncapDomain.F] ?: 0.0),
                        String.format(Locale.US, "%.2f", domains[FuncapDomain.G] ?: 0.0),
                        String.format(Locale.US, "%.2f", domains[FuncapDomain.H] ?: 0.0),
                        escapedNotes
                    ).joinToString(",")
                ).append("\n")
            }

            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                    writer.write(sb.toString())
                }
            } ?: error("Could not open CSV output stream.")

            assessments.size
        }
    }
}
