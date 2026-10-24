package com.example.data.export

object CodeExportUtil {

    fun generatePythonCode(
        modelName: String,
        voiceName: String,
        temperature: Float,
        systemInstruction: String,
        scriptText: String
    ): String {
        val sanitizedSys = systemInstruction.replace("\"\"\"", "\\\"\\\"\\\"").trim()
        val sanitizedScript = scriptText.replace("\"\"\"", "\\\"\\\"\\\"").trim()

        return """# Gemini Multimodal Documentary Narration Pipeline
# Required: pip install google-genai

import os
from google import genai
from google.genai import types

# 1. Initialize client with API key from environment variable
client = genai.Client(api_key=os.environ.get("GEMINI_API_KEY"))

# 2. Master Voice Director System Instruction
system_instruction = ${"\"\"\""}
$sanitizedSys
${"\"\"\""}

# 3. Dual-Narrator Script with Dramatic Cadence & Pause Markers
user_script = ${"\"\"\""}
$sanitizedScript
${"\"\"\""}

# 4. Request High-Fidelity Audio Generation
response = client.models.generate_content(
    model="$modelName",
    contents=user_script,
    config=types.GenerateContentConfig(
        system_instruction=system_instruction,
        response_modalities=["AUDIO"],
        speech_config=types.SpeechConfig(
            voice_config=types.VoiceConfig(
                prebuilt_voice_config=types.PrebuiltVoiceConfig(voice_name="$voiceName")
            )
        ),
        temperature=$temperature,
    )
)

# 5. Export and Save Audio File
output_filename = "documentary_narration_$voiceName.wav"
for part in response.candidates[0].content.parts:
    if part.inline_data:
        with open(output_filename, "wb") as f:
            f.write(part.inline_data.data)
        print(f"Audio file saved successfully: {output_filename}")
        break
"""
    }

    fun generateCurlCommand(
        modelName: String,
        voiceName: String,
        temperature: Float,
        systemInstruction: String,
        scriptText: String
    ): String {
        val escapedSys = systemInstruction.replace("\"", "\\\"").replace("\n", "\\n")
        val escapedScript = scriptText.replace("\"", "\\\"").replace("\n", "\\n")

        return """curl "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=${'$'}GEMINI_API_KEY" \
  -H 'Content-Type: application/json' \
  -X POST \
  -d '{
    "contents": [{
      "parts": [{"text": "$escapedScript"}]
    }],
    "systemInstruction": {
      "parts": [{"text": "$escapedSys"}]
    },
    "generationConfig": {
      "responseModalities": ["AUDIO"],
      "speechConfig": {
        "voiceConfig": {
          "prebuiltVoiceConfig": {
            "voiceName": "$voiceName"
          }
        }
      },
      "temperature": $temperature
    }
  }'"""
    }

    fun generateAiStudioPromptJson(
        modelName: String,
        voiceName: String,
        temperature: Float,
        systemInstruction: String,
        scriptText: String
    ): String {
        return """{
  "model": "$modelName",
  "temperature": $temperature,
  "responseModalities": ["AUDIO"],
  "voiceName": "$voiceName",
  "systemInstruction": ${JSONObjectQuote(systemInstruction)},
  "prompt": ${JSONObjectQuote(scriptText)}
}"""
    }

    private fun JSONObjectQuote(text: String): String {
        val escaped = text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
}
