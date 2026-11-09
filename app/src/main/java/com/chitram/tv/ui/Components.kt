package com.chitram.tv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage
import com.chitram.tv.data.MovieEntity
import com.chitram.tv.R
import com.chitram.tv.data.ProfileEntity
import com.chitram.tv.domain.HomeRow

val LocalMenuFocusRequester = staticCompositionLocalOf<FocusRequester?> { null }
val LocalArtworkPrefetch = staticCompositionLocalOf<((Long) -> Unit)?> { null }

@Composable
fun FocusAction(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    selected: Boolean = false, quiet: Boolean = false, menuOnLeft: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    val menuFocus = LocalMenuFocusRequester.current
    val scale by animateFloatAsState(if (focused) 1.02f else 1f, label = "button focus")
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier.scale(scale)
            .then(if (menuOnLeft && menuFocus != null) Modifier.focusProperties { left = menuFocus } else Modifier)
            .clip(shape)
            .background(if (focused) Cream else if (selected) PanelLight else if (quiet) Color.Transparent else Panel)
            .border(BorderStroke(if (focused) 2.dp else 0.dp, if (focused) Cream else Color.Transparent), shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (focused) Ink else if (selected) Gold else Cream, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun Avatar(profile: ProfileEntity?, modifier: Modifier = Modifier, symbolSize: TextUnit = 26.sp) {
    val symbol = profile?.name?.firstOrNull()?.uppercase() ?: "+"
    val color = when (profile?.avatar) {
        "Cedar" -> Color(0xFF628779)
        "Ocean" -> Color(0xFF5B83A3)
        "Amber" -> Color(0xFFB88955)
        else -> Color(0xFF8C6C9E)
    }
    Box(modifier.clip(RoundedCornerShape(9.dp)).background(color), contentAlignment = Alignment.Center) {
        Text(symbol, color = Cream, fontSize = symbolSize, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MovieArtwork(movie: MovieEntity, modifier: Modifier = Modifier, landscape: Boolean = false, small: Boolean = false) {
    val prefetch = LocalArtworkPrefetch.current
    LaunchedEffect(movie.id) {
        if (!movie.isDemo && movie.posterUrl == null && movie.tmdbCheckedAtMs == 0L) prefetch?.invoke(movie.id)
    }
    val color = Color(movie.artColor.toInt())
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier.clip(shape).background(
            Brush.linearGradient(listOf(color, color.copy(red = color.red * .55f, green = color.green * .55f, blue = color.blue * .55f), Ink))
        )
    ) {
        val url = if (landscape) movie.backdropUrl else movie.posterUrl
        if (url != null) AsyncImage(model = url, contentDescription = "Artwork for ${movie.title}", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        else if (movie.title == "After the Festival") Image(painterResource(R.drawable.after_the_festival), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Ink.copy(alpha = .82f)), startY = 70f)))
        Box(Modifier.align(Alignment.TopEnd).padding(10.dp).clip(RoundedCornerShape(4.dp)).background(Ink.copy(alpha = .88f)).padding(horizontal = 7.dp, vertical = 4.dp)) {
            Text(if (movie.isDemo) "DEMO" else "LISTED", color = Cream, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(if (small) 9.dp else 13.dp)) {
            Text(movie.language.uppercase(), color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(movie.title, color = Cream, fontWeight = FontWeight.Bold, fontSize = if (small) 18.sp else if (landscape) 21.sp else 17.sp, lineHeight = if (small) 21.sp else if (landscape) 24.sp else 20.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun MovieCard(movie: MovieEntity, onClick: () -> Unit, modifier: Modifier = Modifier, landscape: Boolean = false, compact: Boolean = false, menuOnLeft: Boolean = false, onFocus: () -> Unit = {}) {
    var focused by remember { mutableStateOf(false) }
    val menuFocus = LocalMenuFocusRequester.current
    val scale by animateFloatAsState(if (focused) 1.025f else 1f, label = "card focus")
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier.width(if (landscape) 238.dp else 174.dp).scale(scale)
            .then(if (menuOnLeft && menuFocus != null) Modifier.focusProperties { left = menuFocus } else Modifier)
            .clip(shape)
            .border(if (focused) 2.dp else 0.dp, if (focused) Cream else Color.Transparent, shape)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocus()
            }.clickable(onClick = onClick)
            .background(Ink).padding(3.dp)
    ) {
        MovieArtwork(movie, Modifier.fillMaxWidth().height(if (compact) 80.dp else if (landscape) 126.dp else 204.dp), landscape = landscape, small = compact)
        Spacer(Modifier.height(if (compact) 4.dp else 7.dp))
        Text("${movie.releaseYear}  ·  ${movie.language}", color = Muted, fontSize = if (compact) 10.sp else 11.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 3.dp))
        Spacer(Modifier.height(if (compact) 2.dp else 4.dp))
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun MovieRail(row: HomeRow, onMovie: (Long) -> Unit, onMovieFocus: (Long) -> Unit = {}, compact: Boolean = false, entryFocusRequester: FocusRequester? = null) {
    val firstMovieFocus = remember(row.title) { FocusRequester() }
    Column {
        Row(Modifier.fillMaxWidth().padding(start = 34.dp, end = 34.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(row.title, color = Cream, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(14.dp))
            Text("${row.movies.size} films", color = Muted, fontSize = 12.sp)
        }
        LazyRow(
            modifier = Modifier.then(entryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
            contentPadding = PaddingValues(horizontal = 34.dp),
            horizontalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            itemsIndexed(row.movies, key = { _, movie -> movie.id }) { index, movie ->
                MovieCard(
                    movie, { onMovie(movie.id) },
                    modifier = if (index == 0) Modifier.focusRequester(firstMovieFocus) else Modifier,
                    landscape = true, compact = compact, menuOnLeft = index == 0,
                    onFocus = { onMovieFocus(movie.id) }
                )
            }
        }
    }
}

@Composable
fun ScreenHeading(kicker: String, title: String, description: String? = null) {
    Column(Modifier.padding(bottom = 20.dp)) {
        Text(kicker.uppercase(), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text(title, color = Cream, fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
        if (description != null) {
            Spacer(Modifier.height(4.dp))
            Text(description, color = Muted, fontSize = 14.sp)
        }
    }
}

@Composable
fun EmptyState(title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Cream, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(message, color = Muted, fontSize = 15.sp)
    }
}
