package com.example.data.model

enum class MarkerCategory {
    SPEAKER,
    PAUSE,
    PACING,
    EMOTION
}

data class AudioMarker(
    val label: String,
    val tagText: String,
    val category: MarkerCategory,
    val description: String
)

object AudioMarkerCatalog {
    val markers = listOf(
        // Speaker Markers
        AudioMarker(
            label = "Victor (Male)",
            tagText = "[MALE NARRATOR - VICTOR]\n",
            category = MarkerCategory.SPEAKER,
            description = "Switches to deep baritone authoritative narrator"
        ),
        AudioMarker(
            label = "Elena (Female)",
            tagText = "[FEMALE NARRATOR - ELENA]\n",
            category = MarkerCategory.SPEAKER,
            description = "Switches to crystal clear energetic female narrator"
        ),

        // Pause Markers
        AudioMarker(
            label = "Pause 0.8s",
            tagText = "[pause=0.8s] ",
            category = MarkerCategory.PAUSE,
            description = "Brief beat before revelation or transition"
        ),
        AudioMarker(
            label = "Pause 1.2s",
            tagText = "[pause=1.2s] ",
            category = MarkerCategory.PAUSE,
            description = "Dramatic suspense pause before key revelation"
        ),
        AudioMarker(
            label = "Pause 1.5s",
            tagText = "[pause=1.5s] ",
            category = MarkerCategory.PAUSE,
            description = "Extended cliffhanger pause"
        ),
        AudioMarker(
            label = "Lingering ...",
            tagText = "... ",
            category = MarkerCategory.PAUSE,
            description = "Lingering, reflective thought pause"
        ),

        // Pacing Cues
        AudioMarker(
            label = "[SLOW, DEEP]",
            tagText = "[SLOW, DEEP] ",
            category = MarkerCategory.PACING,
            description = "Directs measured, resonant, slow delivery"
        ),
        AudioMarker(
            label = "[BUILDING TENSION]",
            tagText = "[BUILDING TENSION] ",
            category = MarkerCategory.PACING,
            description = "Gradually increases intensity and suspense"
        ),
        AudioMarker(
            label = "[ENERGETIC, EXPOSING TRUTH]",
            tagText = "[ENERGETIC, EXPOSING TRUTH] ",
            category = MarkerCategory.PACING,
            description = "Fast, passionate revelation pace"
        ),
        AudioMarker(
            label = "[WHISPER]",
            tagText = "[WHISPER] ",
            category = MarkerCategory.PACING,
            description = "Intimate, secret, low-volume delivery"
        ),

        // Emotion & Power
        AudioMarker(
            label = "[EMPHASIS]",
            tagText = "[EMPHASIS] ",
            category = MarkerCategory.EMOTION,
            description = "Punches key words with vocal weight"
        ),
        AudioMarker(
            label = "[EMOTIONAL, PASSIONATE]",
            tagText = "[EMOTIONAL, PASSIONATE] ",
            category = MarkerCategory.EMOTION,
            description = "Vivid emotional resonance and pathos"
        ),
        AudioMarker(
            label = "[REVELATION]",
            tagText = "[REVELATION] ",
            category = MarkerCategory.EMOTION,
            description = "Awe-inspiring discovery tone"
        ),
        AudioMarker(
            label = "[CLIMAX]",
            tagText = "[CLIMAX] ",
            category = MarkerCategory.EMOTION,
            description = "Peak documentary crescendo"
        )
    )
}
