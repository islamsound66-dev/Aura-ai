package com.example.data.audio

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavAudioUtil {

    /**
     * Ensures the given audio bytes are a valid WAV file.
     * If the bytes start with 'RIFF', it's already a valid WAV container.
     * Otherwise, assumes 24,000 Hz, 16-bit mono PCM and generates a standard 44-byte WAV header.
     */
    fun saveAsValidWav(audioBytes: ByteArray, outputFile: File, sampleRate: Int = 24000): File {
        if (isWavHeader(audioBytes) || isMp3Header(audioBytes)) {
            outputFile.writeBytes(audioBytes)
            return outputFile
        }

        val header = createWavHeader(
            pcmDataLength = audioBytes.size,
            sampleRate = sampleRate,
            channels = 1,
            bitsPerSample = 16
        )

        FileOutputStream(outputFile).use { fos ->
            fos.write(header)
            fos.write(audioBytes)
        }
        return outputFile
    }

    private fun isWavHeader(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        return bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
               bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte() &&
               bytes[8] == 'W'.code.toByte() && bytes[9] == 'A'.code.toByte() &&
               bytes[10] == 'V'.code.toByte() && bytes[11] == 'E'.code.toByte()
    }

    private fun isMp3Header(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        if (bytes[0] == 'I'.code.toByte() && bytes[1] == 'D'.code.toByte() && bytes[2] == '3'.code.toByte()) return true
        if (bytes[0] == 0xFF.toByte() && (bytes[1].toInt() and 0xE0) == 0xE0) return true
        return false
    }

    private fun createWavHeader(
        pcmDataLength: Int,
        sampleRate: Int,
        channels: Short,
        bitsPerSample: Short
    ): ByteArray {
        val byteRate = sampleRate * channels * (bitsPerSample / 8)
        val blockAlign = (channels * (bitsPerSample / 8)).toShort()
        val totalDataLen = pcmDataLength + 36

        val buffer = ByteBuffer.allocate(44)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalDataLen)
        buffer.put("WAVE".toByteArray())

        // fmt chunk
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1.toShort()) // AudioFormat 1 = PCM
        buffer.putShort(channels)
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign)
        buffer.putShort(bitsPerSample)

        // data chunk
        buffer.put("data".toByteArray())
        buffer.putInt(pcmDataLength)

        return buffer.array()
    }

    /**
     * Extracts normalized amplitude points (0.0f .. 1.0f) for visualizer rendering.
     */
    fun extractAmplitudes(audioBytes: ByteArray, barCount: Int = 40): List<Float> {
        val pcmOffset = if (isWavHeader(audioBytes)) 44 else 0
        val pcmLength = audioBytes.size - pcmOffset
        if (pcmLength <= 0) {
            return List(barCount) { 0.2f }
        }

        val samplesCount = pcmLength / 2
        if (samplesCount < barCount) {
            return List(barCount) { 0.3f }
        }

        val chunkSize = samplesCount / barCount
        val amplitudes = mutableListOf<Float>()

        val buffer = ByteBuffer.wrap(audioBytes, pcmOffset, pcmLength)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until barCount) {
            var sum = 0L
            val actualChunk = minOf(chunkSize, samplesCount - (i * chunkSize))
            for (j in 0 until actualChunk) {
                if (buffer.remaining() >= 2) {
                    val sample = Math.abs(buffer.short.toInt())
                    sum += sample
                }
            }
            val avg = if (actualChunk > 0) sum.toFloat() / actualChunk else 0f
            val normalized = (avg / 32768f).coerceIn(0.12f, 1.0f)
            amplitudes.add(normalized)
        }

        return amplitudes
    }
}
