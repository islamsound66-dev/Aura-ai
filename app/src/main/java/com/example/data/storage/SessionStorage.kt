package com.example.data.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.NarrationSession
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

enum class PublicFolderType {
    MUSIC,
    DOCUMENTS
}

data class ExportResult(
    val success: Boolean,
    val savedPathOrUri: String?,
    val destinationName: String,
    val errorMessage: String? = null
)

class SessionStorage(private val context: Context) {
    private val sessionsFile = File(context.filesDir, "saved_narrations.json")
    private val audioDir = File(context.filesDir, "narrations_audio").apply {
        if (!exists()) mkdirs()
    }

    fun loadSessions(): List<NarrationSession> {
        if (!sessionsFile.exists()) return emptyList()
        return try {
            val jsonText = sessionsFile.readText()
            val array = JSONArray(jsonText)
            val list = mutableListOf<NarrationSession>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NarrationSession(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        scriptText = obj.getString("scriptText"),
                        voiceName = obj.getString("voiceName"),
                        characterName = obj.getString("characterName"),
                        temperature = obj.getDouble("temperature").toFloat(),
                        modelName = obj.optString("modelName", "gemini-2.5-flash"),
                        audioFilePath = obj.getString("audioFilePath"),
                        durationSeconds = obj.getDouble("durationSeconds").toFloat(),
                        timestamp = obj.getLong("timestamp"),
                        fileSizeFormatted = obj.optString("fileSizeFormatted", "0 KB")
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e("SessionStorage", "Failed to load sessions", e)
            emptyList()
        }
    }

    fun saveSession(
        title: String,
        scriptText: String,
        voiceName: String,
        characterName: String,
        temperature: Float,
        modelName: String,
        tempAudioFile: File,
        durationSeconds: Float
    ): NarrationSession {
        val id = "session_${System.currentTimeMillis()}"
        val destFile = File(audioDir, "$id.wav")
        tempAudioFile.copyTo(destFile, overwrite = true)

        val sizeKb = destFile.length() / 1024
        val sizeFormatted = if (sizeKb > 1024) String.format("%.1f MB", sizeKb / 1024f) else "$sizeKb KB"

        val session = NarrationSession(
            id = id,
            title = title,
            scriptText = scriptText,
            voiceName = voiceName,
            characterName = characterName,
            temperature = temperature,
            modelName = modelName,
            audioFilePath = destFile.absolutePath,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis(),
            fileSizeFormatted = sizeFormatted
        )

        val current = loadSessions().toMutableList()
        current.add(0, session)
        persistSessions(current)
        return session
    }

    fun deleteSession(sessionId: String) {
        val current = loadSessions().toMutableList()
        val item = current.find { it.id == sessionId }
        if (item != null) {
            val file = File(item.audioFilePath)
            if (file.exists()) file.delete()
            current.remove(item)
            persistSessions(current)
        }
    }

    private fun persistSessions(list: List<NarrationSession>) {
        try {
            val array = JSONArray()
            for (s in list) {
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("title", s.title)
                    put("scriptText", s.scriptText)
                    put("voiceName", s.voiceName)
                    put("characterName", s.characterName)
                    put("temperature", s.temperature.toDouble())
                    put("modelName", s.modelName)
                    put("audioFilePath", s.audioFilePath)
                    put("durationSeconds", s.durationSeconds.toDouble())
                    put("timestamp", s.timestamp)
                    put("fileSizeFormatted", s.fileSizeFormatted)
                }
                array.put(obj)
            }
            sessionsFile.writeText(array.toString(2))
        } catch (e: Exception) {
            Log.e("SessionStorage", "Failed to persist sessions", e)
        }
    }

    /**
     * Exports audio to the device's public Music or Documents directory via MediaStore.
     * Complies with Scoped Storage (Android 10 / API 29 through API 36).
     */
    fun exportToPublicFolder(
        sourceFile: File,
        title: String,
        folderType: PublicFolderType
    ): ExportResult {
        if (!sourceFile.exists() || sourceFile.length() == 0L) {
            return ExportResult(
                success = false,
                savedPathOrUri = null,
                destinationName = folderType.name,
                errorMessage = "Source audio file is empty or missing."
            )
        }

        val sanitizedTitle = title.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(28)
        val fileName = "DocuVoice_${sanitizedTitle}_${System.currentTimeMillis()}.wav"
        val folderName = if (folderType == PublicFolderType.MUSIC) "Music" else "Documents"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relPath = if (folderType == PublicFolderType.MUSIC) {
                    "${Environment.DIRECTORY_MUSIC}/DocuVoice"
                } else {
                    "${Environment.DIRECTORY_DOCUMENTS}/DocuVoice"
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                    put(MediaStore.Audio.Media.TITLE, title)
                    put(MediaStore.Audio.Media.RELATIVE_PATH, relPath)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return ExportResult(
                    success = false,
                    savedPathOrUri = null,
                    destinationName = folderName,
                    errorMessage = "Failed to create MediaStore entry."
                )

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)

                ExportResult(
                    success = true,
                    savedPathOrUri = "$relPath/$fileName",
                    destinationName = "$folderName/DocuVoice/$fileName"
                )
            } else {
                // Legacy Android fallback
                val targetDir = if (folderType == PublicFolderType.MUSIC) {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "DocuVoice")
                } else {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "DocuVoice")
                }
                if (!targetDir.exists()) targetDir.mkdirs()

                val destFile = File(targetDir, fileName)
                sourceFile.copyTo(destFile, overwrite = true)

                ExportResult(
                    success = true,
                    savedPathOrUri = destFile.absolutePath,
                    destinationName = destFile.absolutePath
                )
            }
        } catch (e: Exception) {
            Log.e("SessionStorage", "Failed to export audio file", e)
            ExportResult(
                success = false,
                savedPathOrUri = null,
                destinationName = folderName,
                errorMessage = e.localizedMessage ?: "Unknown storage error."
            )
        }
    }

    /**
     * Exports audio directly to a user-selected SAF Uri (via ActivityResultContracts.CreateDocument).
     */
    fun exportToSafUri(sourceFile: File, destinationUri: Uri): ExportResult {
        return try {
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            ExportResult(
                success = true,
                savedPathOrUri = destinationUri.toString(),
                destinationName = destinationUri.lastPathSegment ?: "Selected Folder"
            )
        } catch (e: Exception) {
            Log.e("SessionStorage", "Failed writing to SAF Uri", e)
            ExportResult(
                success = false,
                savedPathOrUri = null,
                destinationName = "Custom Location",
                errorMessage = e.localizedMessage ?: "Failed writing to selected location."
            )
        }
    }

    fun shareAudio(filePath: String, title: String) {
        val file = File(filePath)
        if (!file.exists()) return

        try {
            val uri: Uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Documentary Voiceover - $title")
                putExtra(Intent.EXTRA_TEXT, "Generated with DocuVoice Studio: $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Narration Audio").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e("SessionStorage", "Failed to share audio", e)
        }
    }
}
