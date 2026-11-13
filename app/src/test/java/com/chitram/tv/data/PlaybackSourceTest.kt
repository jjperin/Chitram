package com.chitram.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackSourceTest {
    private val listing = DemoCatalogue.movies.first().copy(
        id = 90L, isDemo = false, sourceProvider = "einthusan", sourceId = "2473", availability = "listed"
    )

    @Test fun onlyTheExplicitListingGetsItsConfiguredStream() {
        val url = "https://example.org/test.mp4?expires=123"
        assertEquals(url, configuredTestPlaybackUrl(listing, "2473", url))
        assertNull(configuredTestPlaybackUrl(listing.copy(sourceId = "other"), "2473", url))
        assertNull(configuredTestPlaybackUrl(listing.copy(isDemo = true), "2473", url))
        assertNull(configuredTestPlaybackUrl(listing, "2473", "http://example.org/test.mp4"))
    }
}
