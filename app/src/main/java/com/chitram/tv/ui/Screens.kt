package com.chitram.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chitram.tv.data.DemoCatalogue
import com.chitram.tv.data.MovieEntity
import com.chitram.tv.data.ProfileEntity
import com.chitram.tv.data.ProgressEntity
import com.chitram.tv.domain.homeRows
import com.chitram.tv.domain.BrowseOptions
import com.chitram.tv.domain.browseMovies
import com.chitram.tv.domain.splitChoices

@Composable
fun Sidebar(
    current: String, profile: ProfileEntity?, onNavigate: (String) -> Unit,
    onSwitchProfile: () -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxHeight().background(Panel).padding(horizontal = 19.dp, vertical = 32.dp)) {
        Text("CHITRAM", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text("FILMS, AT HOME", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(58.dp))
        listOf("home" to "Home", "search" to "Search", "browse" to "Browse", "mylist" to "My List").forEach { (route, label) ->
            FocusAction(label, { onNavigate(route) }, modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp), selected = current == route, quiet = true)
        }
        Spacer(Modifier.weight(1f))
        Text("DEMO CATALOGUE", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(13.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(profile, Modifier.size(41.dp))
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(profile?.name ?: "Profile", color = Cream, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(profile?.primaryLanguage ?: "", color = Muted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        FocusAction("Switch profile", onSwitchProfile, modifier = Modifier.fillMaxWidth(), quiet = true)
    }
}

@Composable
fun ProfilePickerScreen(
    profiles: List<ProfileEntity>, onSelect: (Long) -> Unit,
    onCreate: () -> Unit, onEdit: (Long) -> Unit, error: String?
) {
    Column(Modifier.fillMaxSize().background(Ink).padding(horizontal = 64.dp, vertical = 44.dp)) {
        Text("CHITRAM", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(Modifier.height(45.dp))
        ScreenHeading("Welcome", "Who's watching?", "Choose a profile to make this screen yours.")
        if (profiles.isEmpty()) {
            EmptyState("Make it yours", "Create a family profile to start exploring the demonstration catalogue.")
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                profiles.forEach { profile ->
                    Column(Modifier.width(162.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.clickable { onSelect(profile.id) }.padding(12.dp)) { Avatar(profile, Modifier.size(106.dp)) }
                        Spacer(Modifier.height(9.dp))
                        FocusAction(profile.name, { onSelect(profile.id) }, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        FocusAction("Edit", { onEdit(profile.id) }, Modifier.fillMaxWidth(), quiet = true)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        FocusAction("+  Create profile", onCreate)
        if (error != null) Text(error, color = Color(0xFFFF9C93), modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun ProfileEditorScreen(
    existing: ProfileEntity?,
    onSave: (String, String, String, Set<String>, Set<String>) -> Unit,
    onBack: () -> Unit, error: String?
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var avatar by remember(existing?.id) { mutableStateOf(existing?.avatar ?: "Aurora") }
    var primary by remember(existing?.id) { mutableStateOf(existing?.primaryLanguage ?: "Tamil") }
    var secondary by remember(existing?.id) { mutableStateOf(splitChoices(existing?.secondaryLanguages ?: "").toSet()) }
    var genres by remember(existing?.id) { mutableStateOf(splitChoices(existing?.favouriteGenres ?: "").toSet()) }

    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(horizontal = 64.dp, vertical = 36.dp)) {
        ScreenHeading("Profiles", if (existing == null) "Create a profile" else "Edit ${existing.name}", "Set the languages and stories this viewer enjoys.")
        Text("NAME", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(value = name, onValueChange = { name = it.take(24) }, label = { Text("Profile name") }, singleLine = true, modifier = Modifier.width(360.dp))
        Spacer(Modifier.height(20.dp))
        Text("AVATAR", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(9.dp))
        ChoiceRows(listOf("Aurora", "Cedar", "Ocean", "Amber"), setOf(avatar)) { avatar = it }
        Spacer(Modifier.height(20.dp))
        Text("PRIMARY LANGUAGE", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(9.dp))
        ChoiceRows(DemoCatalogue.languages, setOf(primary)) { choice -> primary = choice; secondary = secondary - choice }
        Spacer(Modifier.height(20.dp))
        Text("OTHER LANGUAGES  ·  OPTIONAL", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(9.dp))
        ChoiceRows(DemoCatalogue.languages.filter { it != primary }, secondary) { choice ->
            secondary = if (choice in secondary) secondary - choice else secondary + choice
        }
        Spacer(Modifier.height(20.dp))
        Text("FAVOURITE GENRES  ·  OPTIONAL", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(9.dp))
        ChoiceRows(DemoCatalogue.genres, genres) { choice -> genres = if (choice in genres) genres - choice else genres + choice }
        Spacer(Modifier.height(26.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FocusAction("Save profile", { onSave(name, avatar, primary, secondary, genres) }, selected = true)
            FocusAction("Back", onBack, quiet = true)
        }
        if (name.isBlank()) Text("Enter a name to save this profile.", color = Muted, modifier = Modifier.padding(top = 10.dp))
        if (error != null) Text(error, color = Color(0xFFFF9C93), modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun ChoiceRows(choices: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    choices.chunked(4).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.padding(bottom = 9.dp)) {
            row.forEach { choice -> FocusAction(choice, { onToggle(choice) }, selected = choice in selected, quiet = true) }
        }
    }
}

@Composable
fun HomeScreen(profile: ProfileEntity?, movies: List<MovieEntity>, progress: List<ProgressEntity>, onMovie: (Long) -> Unit) {
    if (profile == null) { EmptyState("Choose a profile", "Switch to a profile to see your home screen."); return }
    val rows = remember(profile, movies, progress) { homeRows(profile, movies, progress) }
    LazyColumn(Modifier.fillMaxSize().background(Ink), verticalArrangement = Arrangement.spacedBy(32.dp)) {
        item {
            Column(Modifier.padding(start = 34.dp, end = 34.dp, top = 26.dp)) {
                Text("GOOD EVENING, ${profile.name.uppercase()}", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                val featured = rows.firstOrNull { it.title == "Recommended for You" }?.movies?.firstOrNull()
                if (featured != null) FeaturedBanner(featured, onMovie)
            }
        }
        if (movies.isEmpty()) item { EmptyState("Building your library", "The demonstration catalogue will appear shortly.") }
        rows.forEach { row -> item(key = row.title) { MovieRail(row, onMovie) } }
        item { Spacer(Modifier.height(36.dp)) }
    }
}

@Composable
private fun FeaturedBanner(movie: MovieEntity, onMovie: (Long) -> Unit) {
    Box(Modifier.fillMaxWidth().height(248.dp).background(Brush.horizontalGradient(listOf(Color(movie.artColor.toInt()), Panel, Ink)), RoundedCornerShape(18.dp))) {
        Column(Modifier.align(Alignment.CenterStart).padding(28.dp)) {
            Text("FEATURED  ·  DEMONSTRATION FILM", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            Text(movie.title, color = Cream, fontSize = 36.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Spacer(Modifier.height(7.dp))
            Text("${movie.releaseYear}   •   ${movie.language}   •   ${movie.genres.replace("|", " / ")}", color = Cream.copy(alpha = .82f), fontSize = 14.sp)
            Spacer(Modifier.height(19.dp))
            FocusAction("View film", { onMovie(movie.id) }, selected = true)
        }
        Text("CHITRAM", color = Cream.copy(alpha = .08f), fontSize = 80.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp))
    }
}

@Composable
fun SearchScreen(query: String, results: List<MovieEntity>, onQueryChange: (String) -> Unit, onMovie: (Long) -> Unit) {
    Column(Modifier.fillMaxSize().background(Ink).padding(start = 34.dp, end = 34.dp, top = 26.dp)) {
        ScreenHeading("Discover", "Search films", "Search titles in the local demonstration catalogue.")
        OutlinedTextField(value = query, onValueChange = onQueryChange, label = { Text("Film title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(22.dp))
        Text("${results.size} RESULTS", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(13.dp))
        if (results.isEmpty()) EmptyState("No matching films", "Try another title.")
        else MovieGrid(results, onMovie, Modifier.weight(1f))
    }
}

@Composable
fun BrowseScreen(movies: List<MovieEntity>, onMovie: (Long) -> Unit) {
    var language by remember { mutableStateOf("All") }
    var genre by remember { mutableStateOf("All") }
    var year by remember { mutableStateOf("All") }
    var sort by remember { mutableStateOf("Newest") }
    val years = listOf("All", "2023–2025", "2020–2022", "2015–2019")
    val sorts = listOf("Newest", "Rating", "Popularity", "A–Z")
    val filtered = remember(movies, language, genre, year, sort) {
        browseMovies(movies, BrowseOptions(language, genre, year, sort))
    }
    Column(Modifier.fillMaxSize().background(Ink).padding(start = 34.dp, end = 34.dp, top = 26.dp)) {
        ScreenHeading("Explore", "Browse films", "Filter the demonstration catalogue by language, genre and year.")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            (listOf("All") + DemoCatalogue.languages).forEach { item -> FocusAction(item, { language = item }, selected = language == item, quiet = true) }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            (listOf("All") + DemoCatalogue.genres).forEach { item -> FocusAction(item, { genre = item }, selected = genre == item, quiet = true) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FocusAction("Year: $year", { year = years[(years.indexOf(year) + 1) % years.size] }, quiet = true)
            FocusAction("Sort: $sort", { sort = sorts[(sorts.indexOf(sort) + 1) % sorts.size] }, quiet = true)
            FocusAction("Clear filters", { language = "All"; genre = "All"; year = "All"; sort = "Newest" }, quiet = true)
        }
        Spacer(Modifier.height(17.dp))
        Text("${filtered.size} FILMS", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (filtered.isEmpty()) EmptyState("No films found", "Clear your filters to see all films.")
        else MovieGrid(filtered, onMovie, Modifier.weight(1f))
    }
}

@Composable
fun MyListScreen(profile: ProfileEntity?, movies: List<MovieEntity>, onMovie: (Long) -> Unit) {
    Column(Modifier.fillMaxSize().background(Ink).padding(start = 34.dp, end = 34.dp, top = 26.dp)) {
        ScreenHeading("Your collection", "My List", "Saved for ${profile?.name ?: "this profile"}.")
        if (movies.isEmpty()) EmptyState("Your list is empty", "Open a film and choose Add to My List.")
        else MovieGrid(movies, onMovie, Modifier.weight(1f))
    }
}

@Composable
private fun MovieGrid(movies: List<MovieEntity>, onMovie: (Long) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(columns = GridCells.Adaptive(175.dp), modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        items(movies, key = { it.id }) { movie -> MovieCard(movie, { onMovie(movie.id) }) }
    }
}

@Composable
fun DetailsScreen(movie: MovieEntity?, inList: Boolean, onToggleList: () -> Unit, onBack: () -> Unit) {
    var showPlaybackMessage by remember { mutableStateOf(false) }
    if (movie == null) { EmptyState("Film unavailable", "This film is not in the local catalogue."); return }
    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(horizontal = 50.dp, vertical = 34.dp)) {
        FocusAction("←  Back", onBack, quiet = true)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(34.dp)) {
            MovieArtwork(movie, Modifier.width(246.dp).height(335.dp))
            Column(Modifier.weight(1f)) {
                Text("DEMONSTRATION FILM", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(10.dp))
                Text(movie.title, color = Cream, fontSize = 39.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(10.dp))
                Text("${movie.releaseYear}   •   ${movie.language}   •   ${movie.genres.replace("|", " / ")}", color = Muted, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                Text("Demo rating ${"%.1f".format(movie.rating)} / 10", color = Gold, fontSize = 15.sp)
                Spacer(Modifier.height(26.dp))
                Text(movie.synopsis, color = Cream.copy(alpha = .85f), fontSize = 18.sp, lineHeight = 27.sp)
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusAction("Play", { showPlaybackMessage = true }, selected = true)
                    FocusAction(if (inList) "Remove from My List" else "Add to My List", onToggleList, quiet = true)
                }
                if (showPlaybackMessage) {
                    Spacer(Modifier.height(16.dp))
                    Text("Playback integration coming in a later milestone.", color = Gold, fontSize = 16.sp)
                }
            }
        }
    }
}
