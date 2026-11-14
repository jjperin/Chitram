package com.chitram.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chitram.tv.data.DemoCatalogue
import com.chitram.tv.R
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
    onSwitchProfile: () -> Unit, selectedItemFocusRequester: FocusRequester,
    contentFocusRequester: FocusRequester?,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxHeight().background(Ink).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 20.dp)) {
        Text("CHITRAM", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(Modifier.height(20.dp))
        listOf("home" to "Home", "search" to "Search", "browse" to "Browse", "mylist" to "My List").forEach { (route, label) ->
            FocusAction(
                label, { onNavigate(route) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)
                    .then(if (current == route) Modifier.focusRequester(selectedItemFocusRequester) else Modifier)
                    .then(if (current == route && contentFocusRequester != null) Modifier.focusProperties { right = contentFocusRequester } else Modifier),
                selected = current == route, quiet = true
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("LOCAL LIBRARY", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(9.dp))
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
        Spacer(Modifier.height(8.dp))
        FocusAction("About & credits", { onNavigate("about") },
            modifier = Modifier.fillMaxWidth().then(if (current == "about") Modifier.focusRequester(selectedItemFocusRequester) else Modifier),
            selected = current == "about", quiet = true)
        Spacer(Modifier.height(8.dp))
        FocusAction("Check for updates", { onNavigate("updates") },
            modifier = Modifier.fillMaxWidth().then(if (current == "updates") Modifier.focusRequester(selectedItemFocusRequester) else Modifier),
            selected = current == "updates", quiet = true)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun AboutScreen() {
    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(horizontal = 34.dp, vertical = 26.dp)) {
        ScreenHeading("CHITRAM", "About & credits", "A local film catalogue for Android TV.")
        Spacer(Modifier.height(30.dp))
        Text("Einthusan catalogue pages supply listing titles and descriptions. Listings do not confirm playback availability.",
            color = Cream, fontSize = 16.sp, lineHeight = 25.sp)
        Spacer(Modifier.height(36.dp))
        Image(painterResource(R.drawable.tmdb_logo), contentDescription = "TMDB logo",
            modifier = Modifier.width(205.dp).aspectRatio(273.42f / 35.52f))
        Spacer(Modifier.height(15.dp))
        Text("TMDB supplies matched film metadata and artwork.", color = Cream, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text("This product uses the TMDB API but is not endorsed or certified by TMDB.",
            color = Muted, fontSize = 14.sp, lineHeight = 22.sp)
        Text("www.themoviedb.org", color = Muted, fontSize = 14.sp)
    }
}

@Composable
fun ProfilePickerScreen(
    profiles: List<ProfileEntity>, onSelect: (Long) -> Unit,
    onCreate: () -> Unit, onEdit: (Long) -> Unit, error: String?, loading: Boolean = false
) {
    val firstProfileFocus = remember { FocusRequester() }
    val addProfileFocus = remember { FocusRequester() }
    LaunchedEffect(loading, profiles.map { it.id }) {
        if (!loading) {
            if (profiles.isNotEmpty()) firstProfileFocus.requestFocus() else addProfileFocus.requestFocus()
        }
    }
    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(horizontal = 40.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CHITRAM", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Spacer(Modifier.height(30.dp))
        Text("Who's watching?", color = Cream, fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Text("Choose a profile", color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(34.dp))
        if (loading) {
            Text("Loading profiles…", color = Muted, fontSize = 15.sp)
        } else if (profiles.isEmpty()) {
            EmptyState("Make it yours", "Create a family profile to start exploring the demonstration catalogue.")
        } else {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.Center) {
                profiles.forEachIndexed { index, profile ->
                    Column(Modifier.width(150.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileTile(profile, onSelect, if (index == 0) Modifier.focusRequester(firstProfileFocus) else Modifier)
                        Spacer(Modifier.height(4.dp))
                        FocusAction("Edit profile", { onEdit(profile.id) }, quiet = true)
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        if (!loading) FocusAction("+  Add profile", onCreate, modifier = Modifier.focusRequester(addProfileFocus), quiet = true)
        if (error != null) Text(error, color = Color(0xFFFF9C93), modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun ProfileTile(profile: ProfileEntity, onSelect: (Long) -> Unit, modifier: Modifier = Modifier) {
    var focused by remember(profile.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier.onFocusChanged { focused = it.isFocused }
                .border(if (focused) 3.dp else 0.dp, if (focused) Cream else Color.Transparent, RoundedCornerShape(12.dp))
                .clickable { onSelect(profile.id) }.padding(5.dp)
        ) {
            Avatar(profile, Modifier.size(96.dp), symbolSize = 42.sp)
        }
        Spacer(Modifier.height(9.dp))
        Text(profile.name, color = if (focused) Cream else Muted, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
@OptIn(ExperimentalComposeUiApi::class)
fun HomeScreen(profile: ProfileEntity?, movies: List<MovieEntity>, progress: List<ProgressEntity>, entryFocusRequester: FocusRequester, onMovie: (Long) -> Unit) {
    if (profile == null) { EmptyState("Choose a profile", "Switch to a profile to see your home screen."); return }
    val rows = remember(profile, movies, progress) { homeRows(profile, movies, progress) }
    val featured = rows.firstOrNull { it.title == "Recommended for You" }?.movies?.firstOrNull()
    var selectedMovieId by rememberSaveable(profile.id) { mutableStateOf<Long?>(null) }
    val preview = rows.asSequence().flatMap { it.movies.asSequence() }.firstOrNull { it.id == selectedMovieId } ?: featured
    BoxWithConstraints(Modifier.fillMaxSize().background(Ink)) {
        if (maxHeight < 300.dp) {
            LazyColumn(Modifier.fillMaxSize().focusRequester(entryFocusRequester), verticalArrangement = Arrangement.spacedBy(26.dp)) {
                item {
                    Column(Modifier.padding(start = 34.dp, end = 34.dp, top = 26.dp)) {
                        Text("FOR ${profile.name.uppercase()}", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(13.dp))
                        if (preview != null) FeaturedBanner(preview, selectedMovieId != null, Modifier.height(155.dp))
                    }
                }
                if (movies.isEmpty()) item { EmptyState("Building your library", "The demonstration catalogue will appear shortly.") }
                rows.forEach { row -> item(key = row.title) { MovieRail(row, onMovie, { id -> selectedMovieId = id }, compact = true) } }
                item { Spacer(Modifier.height(36.dp)) }
            }
        } else {
            val compact = maxHeight < 430.dp
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.padding(start = 34.dp, end = 34.dp, top = if (compact) 14.dp else 26.dp)) {
                    if (!compact) {
                        Text("FOR ${profile.name.uppercase()}", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(13.dp))
                    }
                    if (preview != null) FeaturedBanner(preview, selectedMovieId != null, Modifier.height(if (compact) 150.dp else 190.dp))
                }
                Spacer(Modifier.height(if (compact) 10.dp else 18.dp))
                LazyColumn(Modifier.weight(1f).focusRequester(entryFocusRequester), verticalArrangement = Arrangement.spacedBy(26.dp)) {
                    if (movies.isEmpty()) item { EmptyState("Building your library", "The demonstration catalogue will appear shortly.") }
                    rows.forEach { row -> item(key = row.title) { MovieRail(row, onMovie, { id -> selectedMovieId = id }, compact) } }
                    item { Spacer(Modifier.height(36.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FeaturedBanner(movie: MovieEntity, selectedByFocus: Boolean, modifier: Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Panel)) {
        val compact = maxWidth < 540.dp
        val short = maxHeight < 170.dp
        if (movie.title == "After the Festival") {
            Image(painterResource(R.drawable.after_the_festival), contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        } else if (movie.backdropUrl != null || movie.posterUrl != null) {
            AsyncImage(model = movie.backdropUrl ?: movie.posterUrl, contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color(movie.artColor.toInt()), Panel, Ink))))
        }
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Ink.copy(alpha = .92f), Ink.copy(alpha = .60f), Color.Transparent))))
        Column(Modifier.align(Alignment.CenterStart).widthIn(max = maxWidth * if (compact) .88f else .60f).padding(if (short) 8.dp else 18.dp)) {
            Text("${if (selectedByFocus) "SELECTED" else "FEATURED"}  ·  ${if (movie.isDemo) "DEMO FILM" else "CATALOGUE LISTING"}", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text(movie.title, color = Cream, fontSize = if (short) 23.sp else if (compact) 26.sp else 29.sp, lineHeight = if (short) 26.sp else if (compact) 29.sp else 32.sp, fontWeight = FontWeight.Bold, maxLines = if (short) 1 else if (compact) 2 else 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(listOfNotNull(movie.releaseYear.takeIf { it > 0 }?.toString(), movie.language, movie.genres.takeIf { it.isNotBlank() }?.replace("|", " / ")).joinToString("   •   "), color = Cream.copy(alpha = .80f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(movie.synopsis.takeIf { it.isNotBlank() }?.substringBefore('.')?.trimEnd()?.plus(".") ?: "Catalogue listing. Playback unverified.", color = Cream.copy(alpha = .75f), fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SearchScreen(query: String, results: List<MovieEntity>, onQueryChange: (String) -> Unit, entryFocusRequester: FocusRequester, onMovie: (Long) -> Unit) {
    val menuFocus = LocalMenuFocusRequester.current
    val keyboard = LocalSoftwareKeyboardController.current
    val resultsEntry = remember { FocusRequester() }
    Column(Modifier.fillMaxSize().background(Ink).padding(start = 34.dp, end = 34.dp, top = 26.dp)) {
        ScreenHeading("Discover", "Search films", "Search locally saved catalogue listings and demos.")
        OutlinedTextField(
            value = query, onValueChange = onQueryChange, label = { Text("Film title") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(entryFocusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft && query.isEmpty() && menuFocus != null) {
                        keyboard?.hide()
                        menuFocus.requestFocus()
                        true
                    } else if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown && results.isNotEmpty()) {
                        keyboard?.hide()
                        resultsEntry.requestFocus()
                        true
                    } else false
                }
                .then(menuFocus?.let { requester -> Modifier.focusProperties { left = requester } } ?: Modifier)
        )
        Spacer(Modifier.height(22.dp))
        Text("${results.size} RESULTS", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(13.dp))
        if (results.isEmpty()) EmptyState(if (query.isBlank()) "Find a film" else "No matching films",
            if (query.isBlank()) "Enter a title to search your saved catalogue." else "Try another title.")
        else MovieGrid(results, onMovie, Modifier.weight(1f), resultsEntry)
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun BrowseScreen(movies: List<MovieEntity>, entryFocusRequester: FocusRequester, sourceCount: Int,
    syncRunning: Boolean, syncMessage: String?, onRefresh: (Boolean) -> Unit, onMovie: (Long) -> Unit) {
    val genreFocus = remember { FocusRequester() }
    val controlsFocus = remember { FocusRequester() }
    var language by remember { mutableStateOf("All") }
    var genre by remember { mutableStateOf("All") }
    var year by remember { mutableStateOf("All") }
    var sort by remember { mutableStateOf("Newest") }
    val years = listOf("All", "2023+", "2020–2022", "2015–2019")
    val sorts = listOf("Newest", "Rating", "Popularity", "A–Z")
    val filtered = remember(movies, language, genre, year, sort) {
        browseMovies(movies, BrowseOptions(language, genre, year, sort))
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val columns = maxOf(1, ((maxWidth - 54.dp).value / 189f).toInt())
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize().background(Ink).focusRequester(entryFocusRequester),
        contentPadding = PaddingValues(start = 34.dp, end = 34.dp, top = 26.dp, bottom = 34.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ScreenHeading("Explore", "Browse films", "Filter local listings by language and year. Genre filters use matched metadata.")
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth().focusGroup().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                (listOf("All") + DemoCatalogue.languages).forEachIndexed { index, item -> FocusAction(item, { language = item }, selected = language == item, quiet = true, menuOnLeft = index == 0) }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth().focusGroup().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                (listOf("All") + DemoCatalogue.genres).forEachIndexed { index, item -> FocusAction(item, { genre = item }, modifier = if (index == 0) Modifier.focusRequester(genreFocus) else Modifier, selected = genre == item, quiet = true, menuOnLeft = index == 0) }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth().focusGroup().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FocusAction("Year: $year", { year = years[(years.indexOf(year) + 1) % years.size] }, modifier = Modifier.focusRequester(controlsFocus), quiet = true, menuOnLeft = true)
                FocusAction("Sort: $sort", { sort = sorts[(sorts.indexOf(sort) + 1) % sorts.size] }, quiet = true)
                FocusAction("Clear filters", { language = "All"; genre = "All"; year = "All"; sort = "Newest" }, quiet = true)
                FocusAction("Check new", { if (!syncRunning) onRefresh(false) }, quiet = true)
                FocusAction("Full scan", { if (!syncRunning) onRefresh(true) }, quiet = true)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text("${filtered.size} FILMS  ·  $sourceCount EINTHUSAN LISTINGS", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                if (syncRunning || syncMessage != null) Text(syncMessage ?: "Refreshing catalogue…", color = Muted, fontSize = 11.sp, maxLines = 1)
            }
        }
        if (filtered.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("No films found", "Clear your filters to see all films.") }
        } else {
            itemsIndexed(filtered, key = { _, movie -> movie.id }) { index, movie -> MovieCard(movie, { onMovie(movie.id) }, menuOnLeft = index % columns == 0) }
        }
    }
    }
}

@Composable
fun MyListScreen(profile: ProfileEntity?, movies: List<MovieEntity>, entryFocusRequester: FocusRequester, onMovie: (Long) -> Unit) {
    Column(Modifier.fillMaxSize().background(Ink).padding(start = 34.dp, end = 34.dp, top = 26.dp)) {
        ScreenHeading("Your collection", "My List", "Saved for ${profile?.name ?: "this profile"}.")
        if (movies.isEmpty()) EmptyState("Your list is empty", "Open a film and choose Add to My List.")
        else MovieGrid(movies, onMovie, Modifier.weight(1f), entryFocusRequester)
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun MovieGrid(movies: List<MovieEntity>, onMovie: (Long) -> Unit, modifier: Modifier = Modifier, entryFocusRequester: FocusRequester? = null) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = maxOf(1, ((maxWidth.value + 14f) / 189f).toInt())
        LazyVerticalGrid(columns = GridCells.Fixed(columns), modifier = Modifier.fillMaxSize().then(entryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
            itemsIndexed(movies, key = { _, movie -> movie.id }) { index, movie ->
                MovieCard(movie, { onMovie(movie.id) }, menuOnLeft = index % columns == 0)
            }
        }
    }
}

@Composable
fun DetailsScreen(movie: MovieEntity?, inList: Boolean, onToggleList: () -> Unit, onPlay: () -> Unit,
    onTestWebPlayback: () -> Unit, onBack: () -> Unit) {
    if (movie == null) { EmptyState("Film unavailable", "This film is not in the local catalogue."); return }
    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(horizontal = 50.dp, vertical = 34.dp)) {
        FocusAction("←  Back", onBack, quiet = true)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(34.dp)) {
            MovieArtwork(movie, Modifier.width(246.dp).height(335.dp))
            Column(Modifier.weight(1f)) {
                val playable = com.chitram.tv.data.configuredTestPlaybackUrl(movie,
                    com.chitram.tv.BuildConfig.TEST_PLAYBACK_SOURCE_ID,
                    com.chitram.tv.BuildConfig.TEST_PLAYBACK_URL) != null
                Text(if (movie.isDemo) "DEMO FILM" else if (playable) "EINTHUSAN LISTING · TEST STREAM CONFIGURED" else "EINTHUSAN LISTING · PLAYBACK UNVERIFIED", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(10.dp))
                Text(movie.title, color = Cream, fontSize = 33.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Text("${movie.releaseYear}   •   ${movie.language}   •   ${movie.genres.replace("|", " / ")}", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))
                if (movie.isDemo) Text("Demo rating ${"%.1f".format(movie.rating)} / 10", color = Gold, fontSize = 15.sp)
                else if (movie.ratingSource == "tmdb") Text("TMDB community rating ${"%.1f".format(movie.rating)} / 10", color = Gold, fontSize = 15.sp)
                Spacer(Modifier.height(26.dp))
                Text(movie.synopsis, color = Cream.copy(alpha = .85f), fontSize = 16.sp, lineHeight = 24.sp)
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (playable) FocusAction("Play test stream", onPlay, selected = true)
                    if (com.chitram.tv.BuildConfig.DEBUG && com.chitram.tv.data.verifiedWebPlaybackPage(movie) != null) {
                        FocusAction("Test Web Playback", onTestWebPlayback, quiet = true)
                    }
                    FocusAction(if (inList) "Remove from My List" else "Add to My List", onToggleList, quiet = true)
                }
                if (!playable && !movie.isDemo) Text("Playback source has not been verified for this listing.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}
