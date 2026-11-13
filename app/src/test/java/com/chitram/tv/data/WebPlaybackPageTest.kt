package com.chitram.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebPlaybackPageTest {
    private val listed = DemoCatalogue.movies.first().copy(
        id = 2473L, isDemo = false, sourceProvider = "einthusan", sourceId = "2473",
        sourcePageUrl = "https://einthusan.tv/movie/watch/2473/?lang=malayalam", availability = "listed"
    )

    @Test fun acceptsOnlyItsOfficialCataloguePage() {
        assertEquals(listed.sourcePageUrl, verifiedWebPlaybackPage(listed))
        assertNull(verifiedWebPlaybackPage(listed.copy(isDemo = true)))
        assertNull(verifiedWebPlaybackPage(listed.copy(sourceId = "31c8")))
        assertNull(verifiedWebPlaybackPage(listed.copy(sourcePageUrl = "http://einthusan.tv/movie/watch/2473/")))
        assertNull(verifiedWebPlaybackPage(listed.copy(sourcePageUrl = "https://einthusan.tv.evil.test/movie/watch/2473/")))
        assertNull(verifiedWebPlaybackPage(listed.copy(sourcePageUrl = "https://einthusan.tv/other/2473/")))
    }
}
