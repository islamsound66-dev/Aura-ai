package com.example.data.audio

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

data class QualitySettings(
    val presetName: String = "Documentary Master",
    val bassBoostDb: Float = 3.0f,     // 0.0 to 8.0 dB
    val trebleBoostDb: Float = 2.5f,   // 0.0 to 8.0 dB
    val normalizeAudio: Boolean = true, // Normalize to -1 dBFS
    val dynamicCompression: Boolean = true, // Evens out whispers and shouts
    val sampleRate: Int = 24000
)

object AudioDspEnhancer {

    val PRESET_DOCUMENTARY_MASTER = QualitySettings(
        presetName = "Documentary Master",
        bassBoostDb = 3.5f,
        trebleBoostDb = 3.0f,
        normalizeAudio = true,
        dynamicCompression = true
    )

    val PRESET_DEEP_CINEMA_BASS = QualitySettings(
        presetName = "Deep Cinema (Victor)",
        bassBoostDb = 6.0f,
        trebleBoostDb = 1.5f,
        normalizeAudio = true,
        dynamicCompression = true
    )

    val PRESET_CRYSTAL_CLARITY = QualitySettings(
        presetName = "Crystal Clarity (Elena)",
        bassBoostDb = 1.0f,
        trebleBoostDb = 5.5f,
        normalizeAudio = true,
        dynamicCompression = true
    )

    val PRESET_RAW_STUDIO = QualitySettings(
        presetName = "Raw Studio",
        bassBoostDb = 0f,
        trebleBoostDb = 0f,
        normalizeAudio = false,
        dynamicCompression = false
    )

    val ALL_PRESETS = listOf(
        PRESET_DOCUMENTARY_MASTER,
        PRESET_DEEP_CINEMA_BASS,
        PRESET_CRYSTAL_CLARITY,
        PRESET_RAW_STUDIO
    )

    /**
     * Applies digital signal processing (DSP) to the audio file:
     * - Bass shelf filter boost
     * - Treble shelf filter boost
     * - Dynamic range compression / leveler
     * - Peak normalization
     */
    fun processAudioFile(
        inputFile: File,
        outputFile: File,
        settings: QualitySettings
    ): File {
        val rawBytes = inputFile.readBytes()
        if (rawBytes.size < 44) return inputFile

        val isWav = rawBytes.size >= 12 &&
                rawBytes[0] == 'R'.code.toByte() && rawBytes[1] == 'I'.code.toByte() &&
                rawBytes[2] == 'F'.code.toByte() && rawBytes[3] == 'F'.code.toByte()

        val pcmOffset = if (isWav) 44 else 0
        val pcmLength = rawBytes.size - pcmOffset
        if (pcmLength <= 0) return inputFile

        val sampleCount = pcmLength / 2
        val samples = FloatArray(sampleCount)

        val buffer = ByteBuffer.wrap(rawBytes, pcmOffset, pcmLength)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until sampleCount) {
            val s = buffer.short.toFloat() / 32768.0f
            samples[i] = s
        }

        // 1. Bass & Treble Equalization Filters
        val sampleRate = settings.sampleRate.toFloat()
        val eqSamples = applyBiQuadShelvingEQ(
            samples = samples,
            sampleRate = sampleRate,
            bassGainDb = settings.bassBoostDb,
            trebleGainDb = settings.trebleBoostDb
        )

        // 2. Dynamic Range Compression (evens out quiet whisper parts and high-energy parts)
        val compressedSamples = if (settings.dynamicCompression) {
            applySoftKneeCompression(eqSamples, threshold = 0.45f, ratio = 2.8f)
        } else {
            eqSamples
        }

        // 3. Peak Normalization to -0.8 dBFS (approx 0.91 full scale)
        val finalSamples = if (settings.normalizeAudio) {
            applyPeakNormalization(compressedSamples, targetPeak = 0.91f)
        } else {
            compressedSamples
        }

        // Convert back to 16-bit PCM
        val outPcmBytes = ByteArray(sampleCount * 2)
        val outBuffer = ByteBuffer.wrap(outPcmBytes)
        outBuffer.order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until sampleCount) {
            val clamped = finalSamples[i].coerceIn(-1.0f, 1.0f)
            val shortVal = (clamped * 32767.0f).toInt().toShort()
            outBuffer.putShort(shortVal)
        }

        return WavAudioUtil.saveAsValidWav(outPcmBytes, outputFile, settings.sampleRate)
    }

    private fun applyPeakNormalization(samples: FloatArray, targetPeak: Float): FloatArray {
        var currentPeak = 0f
        for (s in samples) {
            val a = abs(s)
            if (a > currentPeak) currentPeak = a
        }

        if (currentPeak < 0.001f) return samples
        val gain = targetPeak / currentPeak

        val normalized = FloatArray(samples.size)
        for (i in samples.indices) {
            normalized[i] = (samples[i] * gain).coerceIn(-1.0f, 1.0f)
        }
        return normalized
    }

    private fun applySoftKneeCompression(samples: FloatArray, threshold: Float, ratio: Float): FloatArray {
        val out = FloatArray(samples.size)
        for (i in samples.indices) {
            val x = samples[i]
            val absX = abs(x)
            if (absX <= threshold) {
                out[i] = x
            } else {
                val excess = absX - threshold
                val compressedExcess = excess / ratio
                val sign = if (x >= 0) 1.0f else -1.0f
                out[i] = sign * (threshold + compressedExcess)
            }
        }
        return out
    }

    private fun applyBiQuadShelvingEQ(
        samples: FloatArray,
        sampleRate: Float,
        bassGainDb: Float,
        trebleGainDb: Float
    ): FloatArray {
        if (bassGainDb == 0f && trebleGainDb == 0f) return samples

        var processed = samples

        // Apply Low-Shelf (Bass Boost at 180 Hz)
        if (bassGainDb > 0.1f) {
            val bassLinear = 10.0.pow(bassGainDb / 20.0).toFloat()
            processed = applyFirstOrderLowShelf(processed, sampleRate, cutoffHz = 220f, gain = bassLinear)
        }

        // Apply High-Shelf (Treble / Air Presence at 4.5 kHz)
        if (trebleGainDb > 0.1f) {
            val trebleLinear = 10.0.pow(trebleGainDb / 20.0).toFloat()
            processed = applyFirstOrderHighShelf(processed, sampleRate, cutoffHz = 4200f, gain = trebleLinear)
        }

        return processed
    }

    private fun applyFirstOrderLowShelf(
        samples: FloatArray,
        sampleRate: Float,
        cutoffHz: Float,
        gain: Float
    ): FloatArray {
        val out = FloatArray(samples.size)
        val alpha = (2.0 * Math.PI * cutoffHz / sampleRate).toFloat().coerceIn(0.001f, 0.999f)
        var lowState = 0f

        for (i in samples.indices) {
            val input = samples[i]
            lowState += alpha * (input - lowState)
            out[i] = input + (lowState * (gain - 1.0f))
        }
        return out
    }

    private fun applyFirstOrderHighShelf(
        samples: FloatArray,
        sampleRate: Float,
        cutoffHz: Float,
        gain: Float
    ): FloatArray {
        val out = FloatArray(samples.size)
        val alpha = (2.0 * Math.PI * cutoffHz / sampleRate).toFloat().coerceIn(0.001f, 0.999f)
        var lowState = 0f

        for (i in samples.indices) {
            val input = samples[i]
            lowState += alpha * (input - lowState)
            val highComponent = input - lowState
            out[i] = input + (highComponent * (gain - 1.0f))
        }
        return out
    }
}
