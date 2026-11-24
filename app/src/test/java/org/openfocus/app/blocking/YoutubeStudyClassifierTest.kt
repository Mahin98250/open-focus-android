package org.openfocus.app.blocking

import org.junit.Assert.assertEquals
import org.junit.Test

class YoutubeStudyClassifierTest {
    @Test
    fun educationalTitlesAreAllowed() {
        assertEquals(
            YoutubeContentDecision.EDUCATIONAL,
            YoutubeStudyClassifier.classify("Class 10 CBSE Physics - Electricity explained")
        )
        assertEquals(
            YoutubeContentDecision.EDUCATIONAL,
            YoutubeStudyClassifier.classify("Python programming tutorial for beginners")
        )
        assertEquals(
            YoutubeContentDecision.EDUCATIONAL,
            YoutubeStudyClassifier.classify("How to build discipline and stay motivated")
        )
        assertEquals(
            YoutubeContentDecision.EDUCATIONAL,
            YoutubeStudyClassifier.classify("Never Give Up - An Inspirational Speech")
        )
        assertEquals(
            YoutubeContentDecision.EDUCATIONAL,
            YoutubeStudyClassifier.classify("CBSE Class 10 NCERT important questions")
        )
    }

    @Test
    fun entertainmentSignalsOverrideEducationalWords() {
        assertEquals(
            YoutubeContentDecision.DISTRACTING,
            YoutubeStudyClassifier.classify("MrBeast explains his crazy 24 hour challenge")
        )
        assertEquals(
            YoutubeContentDecision.DISTRACTING,
            YoutubeStudyClassifier.classify("Funny gaming reaction")
        )
    }

    @Test
    fun ambiguousTitlesAreNotAllowedByStrictMode() {
        assertEquals(YoutubeContentDecision.UNCERTAIN, YoutubeStudyClassifier.classify("A day in my life"))
        assertEquals(YoutubeContentDecision.UNCERTAIN, YoutubeStudyClassifier.classify(null))
    }
}
