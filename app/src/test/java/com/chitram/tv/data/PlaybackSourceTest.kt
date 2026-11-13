package com.chitram.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test fun validatesFormatAndExpiryWithoutSavingTheUrl() {
        assertEquals(VideoFormat.MP4, (validateVideoUrl("https://media.example.org/film.mp4?e=200", 100) as VideoSourceResult.Ready).format)
        assertEquals(VideoFormat.HLS, (validateVideoUrl("https://media.example.org/film.m3u8", 100) as VideoSourceResult.Ready).format)
        assertTrue(validateVideoUrl("https://media.example.org/film.mp4?e=100", 100) is VideoSourceResult.Unavailable)
        assertTrue(validateVideoUrl("https://media.example.org/film.mp4?e=bad", 100) is VideoSourceResult.Unavailable)
        assertTrue(validateVideoUrl("http://media.example.org/film.mp4", 100) is VideoSourceResult.Unavailable)
        assertTrue(validateVideoUrl("https://media.example.org/film.txt", 100) is VideoSourceResult.Unavailable)
    }
}
