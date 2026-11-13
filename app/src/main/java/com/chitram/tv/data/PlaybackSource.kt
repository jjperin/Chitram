package com.chitram.tv.data

import java.net.URI

/** A resolver is invoked when Play is pressed. It must never persist a temporary URL in Room. */
interface VideoUrlResolver {
    suspend fun resolve(movie: MovieEntity): VideoSourceResult
}

sealed interface VideoSourceResult {
    data class Ready(val url: String, val format: VideoFormat) : VideoSourceResult
    data class Unavailable(val message: String) : VideoSourceResult
}

enum class VideoFormat { MP4, HLS }

/** Validates a provider-issued URL without constructing one from a catalogue ID. */
fun validateVideoUrl(url: String, nowEpochSeconds: Long = System.currentTimeMillis() / 1000): VideoSourceResult {
    val uri = try { URI(url) } catch (_: Exception) { return VideoSourceResult.Unavailable("Invalid video link.") }
    if (uri.scheme != "https" || uri.host.isNullOrBlank() || uri.userInfo != null || uri.fragment != null) {
        return VideoSourceResult.Unavailable("Invalid video link.")
    }
    val format = when {
        uri.path.endsWith(".mp4", ignoreCase = true) -> VideoFormat.MP4
        uri.path.endsWith(".m3u8", ignoreCase = true) -> VideoFormat.HLS
        else -> return VideoSourceResult.Unavailable("Unsupported video format.")
    }
    val expiry = uri.rawQuery?.split('&')?.firstOrNull { it.startsWith("e=") }?.substringAfter('=')
    if (expiry != null && (expiry.toLongOrNull() ?: 0L) <= nowEpochSeconds) {
        return VideoSourceResult.Unavailable("The video link has expired.")
    }
    return VideoSourceResult.Ready(url, format)
}

/** Temporary local debug adapter. Replace only after an authorised source mechanism is verified. */
class ConfiguredTestVideoUrlResolver(private val sourceId: String, private val url: String) : VideoUrlResolver {
    override suspend fun resolve(movie: MovieEntity): VideoSourceResult {
        if (movie.isDemo || movie.sourceProvider != "einthusan" || sourceId.isBlank() || movie.sourceId != sourceId) {
            return VideoSourceResult.Unavailable("No playback source is configured for this film.")
        }
        return validateVideoUrl(url)
    }
}

fun configuredTestPlaybackUrl(movie: MovieEntity, sourceId: String, url: String): String? =
    if (!movie.isDemo && movie.sourceProvider == "einthusan" && sourceId.isNotBlank() && movie.sourceId == sourceId) {
        (validateVideoUrl(url) as? VideoSourceResult.Ready)?.url
    } else null
