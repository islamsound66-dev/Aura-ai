package com.example.data.model

data class NarrationSession(
    val id: String,
    val title: String,
    val scriptText: String,
    val voiceName: String,
    val characterName: String,
    val temperature: Float,
    val modelName: String,
    val audioFilePath: String,
    val durationSeconds: Float,
    val timestamp: Long,
    val fileSizeFormatted: String
)
