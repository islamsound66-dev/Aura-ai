package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiAudioRequest(
    @Json(name = "contents") val contents: List<ContentItem>,
    @Json(name = "systemInstruction") val systemInstruction: ContentItem? = null,
    @Json(name = "generationConfig") val generationConfig: GenerationConfigItem? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @Json(name = "parts") val parts: List<PartItem>
)

@JsonClass(generateAdapter = true)
data class PartItem(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineDataItem? = null
)

@JsonClass(generateAdapter = true)
data class InlineDataItem(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfigItem(
    @Json(name = "responseModalities") val responseModalities: List<String> = listOf("AUDIO"),
    @Json(name = "speechConfig") val speechConfig: SpeechConfigItem? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class SpeechConfigItem(
    @Json(name = "voiceConfig") val voiceConfig: VoiceConfigItem
)

@JsonClass(generateAdapter = true)
data class VoiceConfigItem(
    @Json(name = "prebuiltVoiceConfig") val prebuiltVoiceConfig: PrebuiltVoiceConfigItem
)

@JsonClass(generateAdapter = true)
data class PrebuiltVoiceConfigItem(
    @Json(name = "voiceName") val voiceName: String
)

@JsonClass(generateAdapter = true)
data class GeminiAudioResponse(
    @Json(name = "candidates") val candidates: List<CandidateItem>? = null,
    @Json(name = "error") val error: GeminiErrorItem? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    @Json(name = "content") val content: ContentItem? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorItem(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: String? = null
)
