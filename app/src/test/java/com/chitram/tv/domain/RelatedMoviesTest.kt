package com.chitram.tv.domain

import com.chitram.tv.data.DemoCatalogue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RelatedMoviesTest {
    @Test fun recommendsNearbySameLanguageAndGenreWithoutMixingDemosAndListings() {
        val source = DemoCatalogue.movies.first().copy(id = 500)
        val near = source.copy(id = 501, title = "Near")
        val far = source.copy(id = 502, title = "Far", language = "Punjabi", genres = "Comedy", releaseYear = 1990)
        val listing = source.copy(id = 503, isDemo = false)
        val result = relatedMovies(source, listOf(source, far, listing, near))
        assertEquals(listOf(near.id, far.id), result.map { it.id })
        assertTrue(result.none { it.id == source.id || it.id == listing.id })
    }
}
