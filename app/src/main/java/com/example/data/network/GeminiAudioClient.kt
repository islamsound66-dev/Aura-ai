package com.example.data.network

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class GeminiAudioResult {
    data class Success(
        val audioBytes: ByteArray,
        val mimeType: String,
        val textSnippet: String? = null,
        val rawResponseLength: Int = 0
    ) : GeminiAudioResult()

    data class Error(
        val message: String,
        val isApiKeyMissing: Boolean = false,
        val statusCode: Int? = null
    ) : GeminiAudioResult()
}

class GeminiAudioClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_MODEL = "gemini-2.5-flash"
        const val TTS_MODEL = "gemini-2.5-flash-preview-tts"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        val AVAILABLE_MODELS = listOf(
            "gemini-2.5-flash",
            "gemini-2.5-flash-preview-tts"
        )
    }

    suspend fun generateSpeech(
        apiKeyOverride: String?,
        model: String = DEFAULT_MODEL,
        scriptText: String,
        systemInstruction: String,
        voiceName: String,
        temperature: Float = 0.40f
    ): GeminiAudioResult = withContext(Dispatchers.IO) {
        val resolvedKey = when {
            !apiKeyOverride.isNullOrBlank() -> apiKeyOverride.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY.trim()
            else -> ""
        }

        if (resolvedKey.isBlank()) {
            return@withContext GeminiAudioResult.Error(
                message = "Gemini API Key is not configured. Please set your GEMINI_API_KEY in the AI Studio Secrets panel or enter it in the Studio settings.",
                isApiKeyMissing = true
            )
        }

        val url = "$BASE_URL/$model:generateContent?key=$resolvedKey"

        val requestJson = try {
            buildRequestJson(
                scriptText = scriptText,
                systemInstruction = systemInstruction,
                voiceName = voiceName,
                temperature = temperature
            )
        } catch (e: Exception) {
            return@withContext GeminiAudioResult.Error("Failed to build request payload: ${e.message}")
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            val response = okHttpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errObj = errJson.optJSONObject("error")
                    errObj?.optString("message") ?: "HTTP ${response.code}: ${response.message}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext GeminiAudioResult.Error(
                    message = errorMsg,
                    statusCode = response.code,
                    isApiKeyMissing = response.code == 400 && errorMsg.contains("API key", ignoreCase = true)
                )
            }

            parseAudioFromResponse(responseBody)
        } catch (e: IOException) {
            Log.e("GeminiAudioClient", "Network error calling Gemini API", e)
            GeminiAudioResult.Error("Network error: ${e.localizedMessage ?: "Failed to reach Gemini servers. Please check internet connection."}")
        } catch (e: Exception) {
            Log.e("GeminiAudioClient", "Unexpected error parsing Gemini response", e)
            GeminiAudioResult.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    private fun buildRequestJson(
        scriptText: String,
        systemInstruction: String,
        voiceName: String,
        temperature: Float
    ): JSONObject {
        val root = JSONObject()

        // contents
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject().apply {
            put("text", scriptText)
        }
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        root.put("contents", contentsArray)

        // systemInstruction
        if (systemInstruction.isNotBlank()) {
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().apply { put("text", systemInstruction) })
            sysObj.put("parts", sysParts)
            root.put("systemInstruction", sysObj)
        }

        // generationConfig with speechConfig
        val genConfig = JSONObject()
        val modalities = JSONArray().apply { put("AUDIO") }
        genConfig.put("responseModalities", modalities)
        genConfig.put("temperature", temperature.toDouble())

        val speechConfig = JSONObject()
        val voiceConfig = JSONObject()
        val prebuiltVoiceConfig = JSONObject().apply {
            put("voiceName", voiceName)
        }
        voiceConfig.put("prebuiltVoiceConfig", prebuiltVoiceConfig)
        speechConfig.put("voiceConfig", voiceConfig)
        genConfig.put("speechConfig", speechConfig)

        root.put("generationConfig", genConfig)

        return root
    }

    private fun parseAudioFromResponse(responseBody: String): GeminiAudioResult {
        try {
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val error = json.optJSONObject("error")?.optString("message") ?: "No candidates returned by model."
                return GeminiAudioResult.Error(error)
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts == null || parts.length() == 0) {
                return GeminiAudioResult.Error("Model returned empty content parts.")
            }

            var audioBytes: ByteArray? = null
            var mimeType = "audio/wav"
            var textSnippet: String? = null

            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    textSnippet = part.getString("text")
                }
                if (part.has("inlineData")) {
                    val inlineData = part.getJSONObject("inlineData")
                    mimeType = inlineData.optString("mimeType", "audio/wav")
                    val base64Data = inlineData.optString("data", "")
                    if (base64Data.isNotBlank()) {
                        audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        break
                    }
                }
            }

            return if (audioBytes != null && audioBytes.isNotEmpty()) {
                GeminiAudioResult.Success(
                    audioBytes = audioBytes,
                    mimeType = mimeType,
                    textSnippet = textSnippet,
                    rawResponseLength = responseBody.length
                )
            } else {
                GeminiAudioResult.Error("Model response did not contain audio data. Response was: ${textSnippet ?: "empty"}")
            }
        } catch (e: Exception) {
            return GeminiAudioResult.Error("Failed to parse audio payload: ${e.message}")
        }
    }
}
