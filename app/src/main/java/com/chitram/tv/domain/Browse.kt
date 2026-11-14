package com.chitram.tv.domain

import com.chitram.tv.data.MovieEntity

data class BrowseOptions(
    val language: String = "All",
    val genre: String = "All",
    val year: String = "All",
    val sort: String = "Newest"
)

fun browseMovies(movies: List<MovieEntity>, options: BrowseOptions): List<MovieEntity> {
    val matching = movies.asSequence()
        .filter { options.language == "All" || it.language == options.language }
        .filter { options.genre == "All" || options.genre in splitChoices(it.genres) }
        .filter {
            when (options.year) {
                "2023+" -> it.releaseYear >= 2023
                "2020–2022" -> it.releaseYear in 2020..2022
                "2015–2019" -> it.releaseYear in 2015..2019
                else -> true
            }
        }
    return when (options.sort) {
        "Recently added" -> matching.sortedWith(compareByDescending<MovieEntity> { it.firstSeenAtMs }.thenByDescending { it.releaseYear }.thenBy { it.title })
        "Rating" -> matching.sortedWith(compareByDescending<MovieEntity> { it.rating }.thenBy { it.title })
        "Popularity" -> matching.sortedWith(compareByDescending<MovieEntity> { it.popularity }.thenBy { it.title })
        "A–Z" -> matching.sortedBy { it.title }
        else -> matching.sortedWith(compareByDescending<MovieEntity> { it.releaseYear }.thenBy { it.title })
    }.toList()
}
