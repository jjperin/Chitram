package com.chitram.tv.updates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GitHubUpdatesTest {
    private val digest = "a".repeat(64)
    private fun release(tag: String, url: String = "https://github.com/jjperin/Chitram/releases/download/$tag/chitram-tv.apk") =
        """{"tag_name":"$tag","assets":[{"name":"chitram-tv.apk","browser_download_url":"$url","digest":"sha256:$digest","size":12345}]}"""

    @Test fun newerReleaseIsOffered() {
        assertEquals(2, parseRelease(release("v2"), 1)?.versionCode)
    }

    @Test fun releaseNotesAreShownWithoutChangingAssetValidation() {
        val json = release("v2").replace("\"tag_name\":\"v2\"", "\"tag_name\":\"v2\",\"body\":\"Navigation and artwork polish\"")
        assertEquals("Navigation and artwork polish", parseRelease(json, 1)?.notes)
    }

    @Test fun installedAndOlderReleasesAreIgnored() {
        assertNull(parseRelease(release("v2"), 2))
        assertNull(parseRelease(release("v2"), 3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun foreignDownloadIsRejected() {
        parseRelease(release("v2", "https://example.com/chitram-tv.apk"), 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingDigestIsRejected() {
        parseRelease("""{"tag_name":"v2","assets":[{"name":"chitram-tv.apk","browser_download_url":"https://github.com/jjperin/Chitram/releases/download/v2/chitram-tv.apk","size":12345}]}""", 1)
    }
}
