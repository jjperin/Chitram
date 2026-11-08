package com.chitram.tv.domain

import com.chitram.tv.data.DemoCatalogue
import com.chitram.tv.data.ProfileEntity
import com.chitram.tv.data.ProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationsTest {
    private val tamilProfile = ProfileEntity(
        id = 1, name = "Amma", avatar = "Aurora", primaryLanguage = "Tamil",
        secondaryLanguages = "Hindi", favouriteGenres = "Drama"
    )

    @Test fun languageRowsFollowProfilePreferences() {
        val titles = homeRows(tamilProfile, DemoCatalogue.movies, emptyList()).map { it.title }
        assertTrue("Tamil Films" in titles)
        assertTrue("Hindi Films" in titles)
        assertFalse("Malayalam Films" in titles)
        assertFalse("Continue Watching" in titles)
    }

    @Test fun completedFilmsAreNotRecommended() {
        val first = recommend(tamilProfile, DemoCatalogue.movies).first()
        val progress = ProgressEntity(1, first.id, 1_000, 1_000, true, 1)
        assertFalse(first.id in recommend(tamilProfile, DemoCatalogue.movies, listOf(progress)).map { it.id })
    }

    @Test fun recommendationOrderIsDeterministic() {
        val first = recommend(tamilProfile, DemoCatalogue.movies).map { it.id }
        val second = recommend(tamilProfile, DemoCatalogue.movies).map { it.id }
        assertEquals(first, second)
    }

    @Test fun completedFilmsLeaveContinueWatching() {
        val movieId = DemoCatalogue.movies.first().id
        val active = ProgressEntity(1, movieId, 50_000, 100_000, false, 1)
        assertTrue(homeRows(tamilProfile, DemoCatalogue.movies, listOf(active)).first().title == "Continue Watching")
        assertFalse(homeRows(tamilProfile, DemoCatalogue.movies, listOf(active.copy(completed = true))).any { it.title == "Continue Watching" })
    }

    @Test fun primaryLanguageHasStrongBoost() {
        val result = recommend(tamilProfile.copy(favouriteGenres = ""), DemoCatalogue.movies)
        assertEquals("Tamil", result.first().language)
    }
}
