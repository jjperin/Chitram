package com.chitram.tv.domain

import com.chitram.tv.data.MovieEntity
import com.chitram.tv.data.ProfileEntity
import com.chitram.tv.data.ProgressEntity

fun splitChoices(value: String): List<String> = value.split('|').filter { it.isNotBlank() }
fun joinChoices(values: Collection<String>): String = values.distinct().joinToString("|")

/** Stable, deliberately simple ranking. Weights favour language before genre and rating. */
fun recommend(
    profile: ProfileEntity,
    movies: List<MovieEntity>,
    progress: List<ProgressEntity> = emptyList(),
    limit: Int = 12
): List<MovieEntity> {
    val secondary = splitChoices(profile.secondaryLanguages).toSet()
    val genres = splitChoices(profile.favouriteGenres).toSet()
    val completed = progress.filter { it.completed }.map { it.movieId }.toSet()
    return movies.asSequence()
        .filter { it.id !in completed }
        .map { movie ->
            val languagePoints = when (movie.language) {
                profile.primaryLanguage -> 60.0
                in secondary -> 30.0
                else -> 0.0
            }
            val genrePoints = splitChoices(movie.genres).count { it in genres } * 12.0
            val ratingPoints = movie.rating.coerceIn(0.0, 10.0) * 2.0
            val recencyPoints = (movie.releaseYear - 2015).coerceIn(0, 12) * 0.4
            movie to (languagePoints + genrePoints + ratingPoints + recencyPoints)
        }
        .sortedWith(compareByDescending<Pair<MovieEntity, Double>> { it.second }.thenBy { it.first.id })
        .take(limit)
        .map { it.first }
        .toList()
}

data class HomeRow(val title: String, val movies: List<MovieEntity>, val showProgress: Boolean = false)

fun homeRows(
    profile: ProfileEntity,
    movies: List<MovieEntity>,
    progress: List<ProgressEntity>
): List<HomeRow> {
    val recommendable = movies.filter { it.isDemo || it.availability == "confirmed" }
    val byId = recommendable.associateBy { it.id }
    val inProgress = progress.filter { !it.completed && it.positionMs > 0 && it.durationMs > 0 }
        .sortedByDescending { it.updatedAtMs }.mapNotNull { byId[it.movieId] }
    val selectedLanguages = listOf(profile.primaryLanguage) + splitChoices(profile.secondaryLanguages)
    val selected = selectedLanguages.distinct()
    return buildList {
        if (inProgress.isNotEmpty()) add(HomeRow("Continue Watching", inProgress, true))
        val recommended = recommend(profile, recommendable, progress)
        if (recommended.isNotEmpty()) add(HomeRow("Recommended for You", recommended))
        val listed = movies.filter { !it.isDemo && it.language in selected }
            .sortedWith(compareByDescending<MovieEntity> { it.releaseYear }.thenBy { it.title }).take(12)
        if (listed.isNotEmpty()) add(HomeRow("Einthusan Listings · Playback Unverified", listed))
        val recent = movies.filter { it.language in selected }.sortedWith(compareByDescending<MovieEntity> { it.releaseYear }.thenBy { it.title }).take(12)
        if (recent.isNotEmpty()) add(HomeRow("Latest by Year", recent))
        selected.forEach { language ->
            val items = movies.filter { it.language == language }.take(12)
            if (items.isNotEmpty()) add(HomeRow("$language Films", items))
        }
    }
}
