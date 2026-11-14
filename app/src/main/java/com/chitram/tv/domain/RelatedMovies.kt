package com.chitram.tv.domain

import com.chitram.tv.data.MovieEntity
import kotlin.math.abs

fun relatedMovies(movie: MovieEntity, catalogue: List<MovieEntity>, limit: Int = 12): List<MovieEntity> {
    val genres = splitChoices(movie.genres).toSet()
    return catalogue.asSequence()
        .filter { it.id != movie.id && it.isDemo == movie.isDemo }
        .map { candidate ->
            val score = (if (candidate.language == movie.language) 10 else 0) +
                splitChoices(candidate.genres).count { it in genres } * 4 +
                (5 - abs(candidate.releaseYear - movie.releaseYear)).coerceAtLeast(0)
            candidate to score
        }
        .sortedWith(compareByDescending<Pair<MovieEntity, Int>> { it.second }.thenByDescending { it.first.releaseYear }.thenBy { it.first.title })
        .take(limit)
        .map { it.first }
        .toList()
}
