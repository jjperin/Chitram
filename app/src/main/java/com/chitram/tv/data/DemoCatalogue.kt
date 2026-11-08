package com.chitram.tv.data

object DemoCatalogue {
    val languages = listOf("Tamil", "Hindi", "Telugu", "Malayalam", "Kannada", "Bengali", "Marathi", "Punjabi")
    val genres = listOf("Drama", "Thriller", "Comedy", "Romance", "Action", "Family", "Mystery", "Adventure")

    private val titles = listOf(
        "Monsoon Letters", "The Blue Courtyard", "Night Ferry", "A Thousand Lamps", "Quiet River",
        "City of Jasmine", "The Last Station", "Paper Kites", "Summer at Dawn", "The Long Road",
        "House of Rain", "Between Two Shores", "The Glass Orchard", "Midnight Lantern", "The Golden Hour",
        "A Small Universe", "The Silent Bridge", "After the Festival", "The Fifth Season", "Open Skies",
        "The Banyan Tree", "Letters from Home", "A Song for Winter", "Across the Valley", "The Turning Tide",
        "The Hidden Garden", "The First Light", "A Different Shore", "The Red Notebook", "Days of Wonder",
        "The Old Cinema", "When Clouds Part", "The Painted Door", "Road to Morning", "A Place to Return",
        "The Summer House", "Three Days in June", "The Mountain Path", "An Evening Story", "The Last Page"
    )
    private val palette = listOf(0xFF26495B, 0xFF5B3045, 0xFF665036, 0xFF254A48, 0xFF493D65, 0xFF51442D, 0xFF34526C, 0xFF6A4137)

    val movies: List<MovieEntity> = titles.mapIndexed { index, title ->
        val language = languages[index / 5]
        val genre = genres[index % genres.size]
        MovieEntity(
            id = index + 1L,
            title = title,
            language = language,
            releaseYear = 2015 + (index * 7 % 11),
            genres = if (index % 3 == 0) "$genre|Drama" else genre,
            rating = 6.2 + (index * 3 % 19) / 10.0,
            popularity = (40 - index).toDouble(),
            synopsis = "A demonstration story about family, choices and the places that shape us. This fictional catalogue entry is for testing Chitram's television interface.",
            artColor = palette[index % palette.size],
            isDemo = true
        )
    }
}
