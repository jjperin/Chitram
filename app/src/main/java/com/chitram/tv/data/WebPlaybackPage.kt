package com.chitram.tv.data

import java.net.URI

/** Only the catalogue's official watch page for this exact Einthusan ID may open in the test WebView. */
fun verifiedWebPlaybackPage(movie: MovieEntity): String? {
    if (movie.isDemo || movie.sourceProvider != "einthusan") return null
    val sourceId = movie.sourceId?.takeIf { it.matches(Regex("[A-Za-z0-9]+")) } ?: return null
    val address = movie.sourcePageUrl ?: return null
    val uri = try { URI(address) } catch (_: Exception) { return null }
    if (uri.scheme != "https" || uri.host != "einthusan.tv" || uri.userInfo != null ||
        uri.fragment != null || uri.port !in listOf(-1, 443)) return null
    val pageId = Regex("^/movie/watch/([A-Za-z0-9]+)/?$").matchEntire(uri.path)?.groupValues?.get(1)
    return address.takeIf { pageId == sourceId }
}
