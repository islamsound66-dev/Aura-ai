package com.example

import com.example.data.audio.WavAudioUtil
import com.example.data.export.CodeExportUtil
import com.example.data.model.ScriptCatalog
import com.example.data.model.VoiceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DocuVoiceUnitTest {

    @Test
    fun testVoiceCatalogContainsVictorAndElena() {
        val victor = VoiceCatalog.findById("Zephyr")
        assertEquals("Zephyr", victor.id)
        assertEquals("Male", victor.gender)

        val elena = VoiceCatalog.findById("Aoede")
        assertEquals("Aoede", elena.id)
        assertEquals("Female", elena.gender)

        assertTrue("Voice catalog should have multiple narrator voices", VoiceCatalog.allVoices.size >= 6)
    }

    @Test
    fun testScriptCatalogHasTemplates() {
        val ocean = ScriptCatalog.templates.find { it.id == "deep_ocean" }
        assertNotNull("Ocean mystery template must exist", ocean)
        assertTrue(ocean!!.scriptText.contains("[MALE NARRATOR - VICTOR]"))
        assertTrue(ocean.scriptText.contains("[FEMALE NARRATOR - ELENA]"))
        assertTrue(ocean.scriptText.contains("[pause=1.5s]"))
    }

    @Test
    fun testCodeExportUtilGeneratesValidPython() {
        val pythonCode = CodeExportUtil.generatePythonCode(
            modelName = "gemini-2.5-flash",
            voiceName = "Zephyr",
            temperature = 0.40f,
            systemInstruction = "You are a voice director.",
            scriptText = "[MALE NARRATOR] Hello world"
        )

        assertTrue(pythonCode.contains("import os"))
        assertTrue(pythonCode.contains("from google import genai"))
        assertTrue(pythonCode.contains("response_modalities=[\"AUDIO\"]"))
        assertTrue(pythonCode.contains("voice_name=\"Zephyr\""))
        assertTrue(pythonCode.contains("temperature=0.4"))
    }

    @Test
    fun testWavAudioUtilHeaderGeneration() {
        val tempFile = File.createTempFile("test_audio", ".wav")
        tempFile.deleteOnExit()

        val dummyPcm = ByteArray(4800) { 0 }
        val wavFile = WavAudioUtil.saveAsValidWav(dummyPcm, tempFile, 24000)

        val bytes = wavFile.readBytes()
        assertTrue("WAV file must be at least 44 bytes", bytes.size >= 44)
        val riff = String(bytes.sliceArray(0..3))
        val wave = String(bytes.sliceArray(8..11))
        assertEquals("RIFF", riff)
        assertEquals("WAVE", wave)

        val amps = WavAudioUtil.extractAmplitudes(bytes, 20)
        assertEquals(20, amps.size)
    }

    @Test
    fun testAudioDspEnhancerProcessing() {
        val inFile = File.createTempFile("dsp_in", ".wav")
        val outFile = File.createTempFile("dsp_out", ".wav")
        inFile.deleteOnExit()
        outFile.deleteOnExit()

        val dummyPcm = ByteArray(9600) { 100 }
        val wavIn = WavAudioUtil.saveAsValidWav(dummyPcm, inFile, 24000)

        val processed = com.example.data.audio.AudioDspEnhancer.processAudioFile(
            inputFile = wavIn,
            outputFile = outFile,
            settings = com.example.data.audio.AudioDspEnhancer.PRESET_DOCUMENTARY_MASTER
        )

        assertTrue(processed.exists())
        assertTrue("Processed audio file should have valid size", processed.length() >= 44)
        val bytes = processed.readBytes()
        assertEquals("RIFF", String(bytes.sliceArray(0..3)))
        assertEquals("WAVE", String(bytes.sliceArray(8..11)))
    }

    @Test
    fun testExportResultModel() {
        val res = com.example.data.storage.ExportResult(
            success = true,
            savedPathOrUri = "Music/DocuVoice/test.wav",
            destinationName = "Music/DocuVoice/test.wav"
        )
        assertTrue(res.success)
        assertEquals("Music/DocuVoice/test.wav", res.savedPathOrUri)
    }
}
