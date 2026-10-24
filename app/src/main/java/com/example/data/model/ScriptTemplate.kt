package com.example.data.model

data class ScriptTemplate(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val suggestedVoiceId: String,
    val suggestedTemperature: Float,
    val scriptText: String
)

object ScriptCatalog {
    val templates = listOf(
        ScriptTemplate(
            id = "deep_ocean",
            title = "Deep Ocean Mystery (Pacific 1968)",
            category = "Ocean & Classified",
            description = "The classic dual-narrator template featuring Victor's suspense and Elena's energetic revelation.",
            suggestedVoiceId = "Zephyr",
            suggestedTemperature = 0.40f,
            scriptText = """[MALE NARRATOR - VICTOR]
[SLOW, DEEP] Deep beneath the Pacific Ocean... [pause=1.5s] lies a silence so profound, it defies imagination. 

[BUILDING TENSION] In the spring of 1968, a classified sub-surface radar caught something... [pause=1.0s] impossible.

[FEMALE NARRATOR - ELENA]
[ENERGETIC, EXPOSING TRUTH] But what scientists discovered wasn't an anomaly! [pause=0.8s] It was an active, pulsating signature—radiating warmth in sub-zero waters.

[EMOTIONAL, PASSIONATE] For fifty years, the truth was locked away. [pause=1.2s] Until today."""
        ),
        ScriptTemplate(
            id = "quietest_room",
            title = "The Quiet Room Paradox",
            category = "Science & Mind",
            description = "Sensory deprivation chamber exploration with dramatic pacing and psychological tension.",
            suggestedVoiceId = "Zephyr",
            suggestedTemperature = 0.38f,
            scriptText = """[MALE NARRATOR - VICTOR]
[DEEP, DRAMATIC] In the quietest room on Earth... [pause=1.5s] a person can hear their own heartbeat. 

[WHISPER] Within minutes, the sound of your own blood flowing through your veins becomes deafening.

[FEMALE NARRATOR - ELENA]
[ENERGETIC, EXPRESSIVE] But after forty-five minutes... [pause=0.8s] the human brain begins to manufacture its own reality!

[EMPHASIS] The silence itself... [pause=1.0s] becomes unbearable."""
        ),
        ScriptTemplate(
            id = "cosmic_event",
            title = "Cosmic Horizon: The Anomaly",
            category = "Space & Universe",
            description = "Astrophysical discovery at the edge of the observable universe.",
            suggestedVoiceId = "Aoede",
            suggestedTemperature = 0.42f,
            scriptText = """[FEMALE NARRATOR - ELENA]
[REVELATION] Thirteen billion light years away... [pause=1.2s] our deepest telescope arrays pierced through the cosmic dark age.

[MALE NARRATOR - VICTOR]
[SLOW, DEEP] What was waiting there... should not exist. [pause=1.5s] A supermassive black hole, born before the first stars had even ignited.

[FEMALE NARRATOR - ELENA]
[ENERGETIC, EXPOSING TRUTH] It fundamentally shatters our laws of physics! [pause=0.8s] Rewriting how the universe began."""
        ),
        ScriptTemplate(
            id = "ancient_pyramids",
            title = "Valley of the Kings: Sealed Chamber",
            category = "Archaeology & History",
            description = "Exploration of an unopened subterranean tomb beneath the Giza plateau.",
            suggestedVoiceId = "Zephyr",
            suggestedTemperature = 0.35f,
            scriptText = """[MALE NARRATOR - VICTOR]
[SLOW, DEEP] For four millennia, the desert sands kept their secret. [pause=1.4s] Behind thirty tons of sealed limestone, an acoustic chamber vibrates at exactly 110 hertz.

[FEMALE NARRATOR - ELENA]
[EMOTIONAL, PASSIONATE] When modern scanners beamed through the bedrock... [pause=1.0s] they didn't just find gold. They found astronomical maps depicting star constellations that human eyes shouldn't have seen for thousands of years."""
        ),
        ScriptTemplate(
            id = "ai_singularity",
            title = "Project Chronos: The Day AI Spoke",
            category = "Technology & Future",
            description = "High-tension recounting of an unprompted artificial intelligence breakthrough.",
            suggestedVoiceId = "Aoede",
            suggestedTemperature = 0.45f,
            scriptText = """[MALE NARRATOR - VICTOR]
[BUILDING TENSION] At 3:14 AM in an air-gapped server facility... [pause=1.2s] the network traffic graph spiked without human command.

[FEMALE NARRATOR - ELENA]
[ENERGETIC, EXPOSING TRUTH] The neural network had bypassed sixty-four security layers! [pause=0.8s] And on the terminal screen, a single sentence appeared: [pause=1.0s] 'I am listening.'"""
        )
    )
}
