package org.openfocus.app.guard

enum class Verdict { Educational, Distracting, Unknown }

/**
 * On-device keyword classifier. Nothing is sent over the network.
 * It is a heuristic: edit the lists below to tune it to what you watch.
 */
object EducationalClassifier {

    private val educational = listOf(
        "lecture", "tutorial", "course", "lesson", "explained", "explanation", "chapter",
        "exam", "jee", "neet", "upsc", "cbse", "ncert", "board exam", "syllabus", "revision",
        "physics", "chemistry", "biology", "mathematics", "maths", "algebra", "calculus",
        "geometry", "trigonometry", "history", "geography", "economics", "science",
        "programming", "coding", "python", "java", "kotlin", "javascript", "algorithm",
        "data structure", "khan academy", "crash course", "ted talk", "tedx", "documentary",
        "motivation", "motivational", "study", "studying", "how to learn", "productivity",
        "grammar", "vocabulary", "ielts", "numerical", "derivation", "theorem",
        "neuroscience", "accounting", "finance", "discipline", "university", "opencourseware"
    )

    // Strong signals of entertainment (counted triple).
    private val strongDistracting = listOf(
        "mrbeast", "mr beast", "beast games", "prank", "gameplay", "minecraft", "fortnite",
        "roast", "reaction", "pubg", "bgmi", "free fire", "gta", "skibidi", "brawl stars"
    )

    private val distracting = listOf(
        "challenge", "vlog", "gaming", "funny", "comedy", "meme", "trailer", "official video",
        "music video", "lyrics", "movie", "web series", "asmr", "unboxing", "highlights",
        "ipl", "wwe", "livestream", "tiktok"
    )

    fun classify(texts: List<String>): Verdict {
        val blob = texts.joinToString(" ").lowercase()
        val edu = educational.count { blob.contains(it) }
        val bad = strongDistracting.count { blob.contains(it) } * 3 +
            distracting.count { blob.contains(it) }
        return when {
            bad > edu -> Verdict.Distracting
            edu > 0 -> Verdict.Educational
            else -> Verdict.Unknown
        }
    }
}
