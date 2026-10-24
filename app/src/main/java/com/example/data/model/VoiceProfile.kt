package com.example.data.model

data class VoiceProfile(
    val id: String,
    val name: String,
    val characterName: String,
    val gender: String, // "Male" or "Female"
    val tone: String,
    val description: String,
    val pacing: String,
    val energy: String,
    val isPrimaryFeatured: Boolean = false,
    val accentTag: String = "Cinematic"
)

object VoiceCatalog {
    val victor = VoiceProfile(
        id = "Zephyr",
        name = "Zephyr",
        characterName = "Victor (Male Narrator)",
        gender = "Male",
        tone = "Deep baritone, cinematic, rich, authoritative with emotional depth",
        description = "Ideal for history, nature mysteries, true crime, deep ocean, and military chronicles.",
        pacing = "Unhurried, measured, building suspense with resonant pauses",
        energy = "Intense, grounded, commanding, and dramatic",
        isPrimaryFeatured = true,
        accentTag = "Deep Baritone"
    )

    val elena = VoiceProfile(
        id = "Aoede",
        name = "Aoede",
        characterName = "Elena (Female Narrator)",
        gender = "Female",
        tone = "Crystal clear, warm, expressive, high-clarity articulate treble",
        description = "Ideal for scientific breakthroughs, cosmic discoveries, energetic revelations, and human drama.",
        pacing = "Dynamic, fluid, and engaging",
        energy = "Energetic, passionate, emotionally vivid, and captivating",
        isPrimaryFeatured = true,
        accentTag = "Crystal Treble"
    )

    val fenrir = VoiceProfile(
        id = "Fenrir",
        name = "Fenrir",
        characterName = "Marcus (Gravel & Tension)",
        gender = "Male",
        tone = "Raspy, gritty, resonant, dark atmospheric timbre",
        description = "Suited for horror chronicles, espionage thrillers, and cataclysmic documentaries.",
        pacing = "Deliberate, heavy, ominous",
        energy = "Dark, menacing, hyper-focused",
        isPrimaryFeatured = false,
        accentTag = "Gritty Raspy"
    )

    val kore = VoiceProfile(
        id = "Kore",
        name = "Kore",
        characterName = "Clara (Warm Authority)",
        gender = "Female",
        tone = "Serene, sophisticated, velvet warmth, pristine clarity",
        description = "Suited for wildlife epics, ancient civilizational histories, and philosophical essays.",
        pacing = "Graceful, balanced, soothing yet commanding",
        energy = "Steadfast, reflective, elegant",
        isPrimaryFeatured = false,
        accentTag = "Velvet Warm"
    )

    val charon = VoiceProfile(
        id = "Charon",
        name = "Charon",
        characterName = "Vincent (Cryptic & Solemn)",
        gender = "Male",
        tone = "Whispered gravitas, low frequency, solemn resonance",
        description = "Suited for ancient curses, deep space existential questions, and unsolved disappearances.",
        pacing = "Slow, lingering, profound pauses",
        energy = "Mysterious, haunting, solemn",
        isPrimaryFeatured = false,
        accentTag = "Solemn Gravitas"
    )

    val puck = VoiceProfile(
        id = "Puck",
        name = "Puck",
        characterName = "Leo (Dynamic Storyteller)",
        gender = "Male",
        tone = "Crisp, inquisitive, versatile, storytelling cadence",
        description = "Suited for tech explainers, archaeological adventure expeditions, and rapid-fire revelations.",
        pacing = "Brisk, punctuated, animated",
        energy = "Curious, agile, persuasive",
        isPrimaryFeatured = false,
        accentTag = "Animated"
    )

    val allVoices: List<VoiceProfile> = listOf(victor, elena, fenrir, kore, charon, puck)

    fun findById(id: String): VoiceProfile =
        allVoices.find { it.id.equals(id, ignoreCase = true) } ?: victor
}
