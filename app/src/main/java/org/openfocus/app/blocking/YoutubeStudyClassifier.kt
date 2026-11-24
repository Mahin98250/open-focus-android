package org.openfocus.app.blocking

enum class YoutubeContentDecision {
    EDUCATIONAL,
    DISTRACTING,
    UNCERTAIN
}

/**
 * Conservative, fully on-device title classifier.
 * Explicit entertainment signals always override educational-looking words.
 */
object YoutubeStudyClassifier {
    private val distractingTerms = listOf(
        "mrbeast", "mr beast", "beast games", "prank", "pranks", "challenge",
        "24 hours", "last to leave", "funny", "comedy", "memes", "meme",
        "roast", "reaction", "reacts", "gaming", "gameplay", "minecraft",
        "fortnite", "gta 5", "gta v", "free fire", "pubg", "vlog", "daily vlog",
        "dance", "music video", "official video", "lyrics", "song",
        "try not to laugh", "celebrity", "food challenge", "crazy", "prank compilation"
    )

    private val educationalTerms = listOf(
        "study", "studying", "study with me", "lesson", "lecture", "tutorial", "explained",
        "explanation", "chapter", "chapterwise", "class 10", "class 9", "cbse", "icse", "ncert",
        "mathematics", "maths", "algebra", "geometry", "trigonometry",
        "physics", "chemistry", "biology", "science", "history", "geography",
        "economics", "english grammar", "grammar", "exam preparation",
        "revision", "question paper", "important questions", "sample paper", "board exam",
        "entrance exam", "solved", "solutions", "formula", "derivation", "notes", "problem solving",
        "coding", "programming", "python", "javascript", "java programming",
        "computer science", "engineering", "robotics", "artificial intelligence",
        "machine learning", "documentary", "educational", "education",
        "learning", "productivity", "motivation", "motivational", "inspiration",
        "inspirational", "speech", "mindset", "success habits", "habits", "stoicism",
        "mental toughness", "personal growth", "self development", "self improvement",
        "discipline", "career guidance", "ted talk",
        "crash course", "khan academy", "freecodecamp", "3blue1brown",
        "physics wallah", "unacademy", "nptel", "mit open courseware"
    )

    fun classify(title: String?): YoutubeContentDecision {
        val normalized = title
            ?.lowercase()
            ?.replace(Regex("[^a-z0-9+# ]"), " ")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()

        if (normalized.length < 4) return YoutubeContentDecision.UNCERTAIN
        if (distractingTerms.any { normalized.contains(it) }) {
            return YoutubeContentDecision.DISTRACTING
        }
        if (educationalTerms.any { normalized.contains(it) }) {
            return YoutubeContentDecision.EDUCATIONAL
        }
        return YoutubeContentDecision.UNCERTAIN
    }
}
