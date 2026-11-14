package com.chitram.tv.domain

import com.chitram.tv.data.DemoCatalogue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowseTest {
    @Test fun filtersCombineAndClear() {
        val all = browseMovies(DemoCatalogue.movies, BrowseOptions())
        val filtered = browseMovies(DemoCatalogue.movies, BrowseOptions(language = "Tamil", genre = "Drama", year = "2023+"))
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.language == "Tamil" && "Drama" in splitChoices(it.genres) && it.releaseYear >= 2023 })
        assertEquals(DemoCatalogue.movies.size, all.size)
    }

    @Test fun ratingSortIsDescending() {
        val rated = browseMovies(DemoCatalogue.movies, BrowseOptions(sort = "Rating"))
        assertTrue(rated.zipWithNext().all { (a, b) -> a.rating >= b.rating })
    }

    @Test fun newestYearFilterIncludesLaterReleases() {
        val later = DemoCatalogue.movies.first().copy(id = 999L, releaseYear = 2026)
        assertTrue(later in browseMovies(listOf(later), BrowseOptions(year = "2023+")))
    }

    @Test fun recentlyAddedUsesDiscoveryTime() {
        val old = DemoCatalogue.movies.first().copy(id = 901, releaseYear = 2026, firstSeenAtMs = 100)
        val new = DemoCatalogue.movies.first().copy(id = 902, releaseYear = 2020, firstSeenAtMs = 200)
        assertEquals(new.id, browseMovies(listOf(old, new), BrowseOptions(sort = "Recently added")).first().id)
    }
}
