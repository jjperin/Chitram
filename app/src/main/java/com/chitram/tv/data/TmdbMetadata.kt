package com.chitram.tv.data

import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import org.json.JSONObject

data class TmdbMatch(
    val id: Long, val title: String, val originalTitle: String, val originalLanguage: String, val year: Int,
    val overview: String, val releaseDate: String, val rating: Double,
    val popularity: Double, val posterPath: String?, val backdropPath: String?, val genres: String = ""
)

/** Exact title and film-year match only; ambiguity leaves existing source metadata untouched. */
private val languageSuffix = Regex("\\s*\\((Tamil|Hindi|Telugu|Malayalam|Kannada|Bengali|Marathi|Punjabi)\\)\\s*$", RegexOption.IGNORE_CASE)

internal fun catalogueSearchTitle(title: String): String = title.replace(languageSuffix, "").trim()

fun chooseTmdbMatch(title: String, year: Int, language: String, candidates: List<TmdbMatch>): TmdbMatch? {
    if (year <= 0) return null
    fun normalize(value: String) = value.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "")
    val source = normalize(catalogueSearchTitle(title))
    val exact = candidates.filter { it.year == year && (normalize(it.title) == source || normalize(it.originalTitle) == source) }
    if (exact.size == 1) return exact.single()
    val iso = mapOf("Tamil" to "ta", "Hindi" to "hi", "Telugu" to "te", "Malayalam" to "ml",
        "Kannada" to "kn", "Bengali" to "bn", "Marathi" to "mr", "Punjabi" to "pa")[language]
    return exact.filter { it.originalLanguage == iso }.singleOrNull()
}

class TmdbMetadata(private val token: String) {
    fun search(title: String, year: Int, language: String): TmdbMatch? {
        if (token.isBlank() || year <= 0) return null
        val query = URLEncoder.encode(catalogueSearchTitle(title), "UTF-8")
        val url = URL("https://api.themoviedb.org/3/search/movie?query=$query&year=$year&include_adult=false")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 12_000
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Accept", "application/json")
        return try {
            if (connection.responseCode != 200) error("TMDB HTTP ${connection.responseCode}")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val results = json.getJSONArray("results")
            val candidates = (0 until results.length()).map { index ->
                val item = results.getJSONObject(index)
                TmdbMatch(
                    id = item.getLong("id"), title = item.optString("title"), originalTitle = item.optString("original_title"),
                    originalLanguage = item.optString("original_language"),
                    year = item.optString("release_date").take(4).toIntOrNull() ?: 0,
                    overview = item.optString("overview"), releaseDate = item.optString("release_date"),
                    rating = item.optDouble("vote_average", 0.0), popularity = item.optDouble("popularity", 0.0),
                    posterPath = item.optString("poster_path").takeIf { it.startsWith("/") },
                    backdropPath = item.optString("backdrop_path").takeIf { it.startsWith("/") }
                )
            }
            chooseTmdbMatch(title, year, language, candidates)?.let { match ->
                runCatching { fetchGenres(match.id) }.getOrNull()?.let { match.copy(genres = it) } ?: match
            }
        } finally { connection.disconnect() }
    }

    private fun fetchGenres(id: Long): String {
        val connection = URL("https://api.themoviedb.org/3/movie/$id").openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 12_000
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Accept", "application/json")
        return try {
            if (connection.responseCode != 200) return ""
            val genres = JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).getJSONArray("genres")
            (0 until genres.length()).map { genres.getJSONObject(it).optString("name").trim() }
                .filter { it.isNotBlank() }.distinct().joinToString("|")
        } finally { connection.disconnect() }
    }
}
