package com.chitram.tv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chitram.tv.data.MovieEntity
import com.chitram.tv.data.ProfileEntity
import com.chitram.tv.domain.HomeRow

@Composable
fun FocusAction(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    selected: Boolean = false, quiet: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.04f else 1f, label = "button focus")
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier.scale(scale).clip(shape)
            .background(if (selected || focused) Gold else if (quiet) Panel else PanelLight)
            .border(BorderStroke(if (focused) 2.dp else 1.dp, if (focused) Cream else Color.Transparent), shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected || focused) Ink else Cream, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Avatar(profile: ProfileEntity?, modifier: Modifier = Modifier) {
    val symbol = profile?.name?.firstOrNull()?.uppercase() ?: "+"
    val color = when (profile?.avatar) {
        "Cedar" -> Color(0xFF628779)
        "Ocean" -> Color(0xFF5B83A3)
        "Amber" -> Color(0xFFB88955)
        else -> Color(0xFF8C6C9E)
    }
    Box(modifier.clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(symbol, color = Cream, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MovieArtwork(movie: MovieEntity, modifier: Modifier = Modifier, landscape: Boolean = false) {
    val color = Color(movie.artColor.toInt())
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier.clip(shape).background(
            Brush.linearGradient(listOf(color, color.copy(red = color.red * .55f, green = color.green * .55f, blue = color.blue * .55f), Ink))
        )
    ) {
        val url = if (landscape) movie.backdropUrl else movie.posterUrl
        if (url != null) AsyncImage(model = url, contentDescription = "Artwork for ${movie.title}", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        Box(Modifier.align(Alignment.TopEnd).padding(12.dp).clip(RoundedCornerShape(5.dp)).background(Ink.copy(alpha = .78f)).padding(horizontal = 7.dp, vertical = 4.dp)) {
            Text("DEMO", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(movie.language.uppercase(), color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(movie.title, color = Cream, fontWeight = FontWeight.Bold, fontSize = if (landscape) 27.sp else 17.sp, lineHeight = if (landscape) 30.sp else 21.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun MovieCard(movie: MovieEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.055f else 1f, label = "card focus")
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier.width(168.dp).scale(scale).clip(shape)
            .border(if (focused) 2.dp else 0.dp, if (focused) Gold else Color.Transparent, shape)
            .onFocusChanged { focused = it.isFocused }.clickable(onClick = onClick)
            .background(Panel).padding(4.dp)
    ) {
        MovieArtwork(movie, Modifier.fillMaxWidth().height(186.dp))
        Spacer(Modifier.height(8.dp))
        Text(movie.title, color = Cream, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 5.dp))
        Text("${movie.releaseYear}  ·  ${movie.language}", color = Muted, fontSize = 11.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 5.dp))
        Spacer(Modifier.height(7.dp))
    }
}

@Composable
fun MovieRail(row: HomeRow, onMovie: (Long) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().padding(start = 34.dp, end = 34.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(row.title, color = Cream, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(14.dp))
            Text("${row.movies.size} films", color = Muted, fontSize = 12.sp)
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 34.dp), horizontalArrangement = Arrangement.spacedBy(17.dp)) {
            items(row.movies, key = { it.id }) { movie -> MovieCard(movie, { onMovie(movie.id) }) }
        }
    }
}

@Composable
fun ScreenHeading(kicker: String, title: String, description: String? = null) {
    Column(Modifier.padding(bottom = 24.dp)) {
        Text(kicker.uppercase(), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(7.dp))
        Text(title, color = Cream, fontSize = 33.sp, fontWeight = FontWeight.Bold)
        if (description != null) {
            Spacer(Modifier.height(5.dp))
            Text(description, color = Muted, fontSize = 15.sp)
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
