package com.chitram.tv.data

/** The local test stream is explicit; a catalogue ID never produces a signed media URL. */
fun configuredTestPlaybackUrl(movie: MovieEntity, sourceId: String, url: String): String? =
    url.takeIf {
        !movie.isDemo && movie.sourceProvider == "einthusan" && movie.sourceId == sourceId &&
            sourceId.isNotBlank() && it.startsWith("https://")
    }
