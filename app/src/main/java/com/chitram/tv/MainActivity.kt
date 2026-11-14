package com.chitram.tv

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chitram.tv.ui.BrowseScreen
import com.chitram.tv.ui.AboutScreen
import com.chitram.tv.ui.ChitramTheme
import com.chitram.tv.ui.DetailsScreen
import com.chitram.tv.ui.HomeScreen
import com.chitram.tv.ui.Ink
import com.chitram.tv.ui.LocalMenuFocusRequester
import com.chitram.tv.ui.LocalArtworkPrefetch
import com.chitram.tv.ui.MyListScreen
import com.chitram.tv.ui.ProfileEditorScreen
import com.chitram.tv.ui.ProfilePickerScreen
import com.chitram.tv.ui.SearchScreen
import com.chitram.tv.ui.Sidebar
import com.chitram.tv.ui.UpdateScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChitramTheme { ChitramApp() } }
    }
}

@Composable
private fun ChitramApp(vm: ChitramViewModel = viewModel()) {
    val context = LocalContext.current
    val nav = rememberNavController()
    val profiles by vm.profiles.collectAsStateWithLifecycle()
    val profilesLoaded by vm.profilesLoaded.collectAsStateWithLifecycle()
    val movies by vm.movies.collectAsStateWithLifecycle()
    val sourceCount by vm.sourceCount.collectAsStateWithLifecycle()
    val syncState by vm.syncState.collectAsStateWithLifecycle()
    val syncRunning by vm.syncRunning.collectAsStateWithLifecycle()
    val syncMessage by vm.syncMessage.collectAsStateWithLifecycle()
    val profile by vm.activeProfile.collectAsStateWithLifecycle()
    val watchlistState by vm.watchlist.collectAsStateWithLifecycle()
    val progressState by vm.progress.collectAsStateWithLifecycle()
    val searchResults by vm.searchResults.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val savedIds = if (watchlistState.first == profile?.id) watchlistState.second.map { it.movieId }.toSet() else emptySet()
    val savedMovies = movies.filter { it.id in savedIds }
    val homeMovies = movies
    val progress = if (progressState.first == profile?.id) progressState.second else emptyList()

    fun openMain(route: String) {
        nav.navigate(route) { launchSingleTop = true }
    }

    NavHost(
        navController = nav,
        startDestination = "profiles",
        modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        composable("profiles") {
            ProfilePickerScreen(
                profiles = profiles,
                loading = !profilesLoaded,
                onSelect = { vm.selectProfile(it); nav.navigate("home") { popUpTo("profiles") { inclusive = true } } },
                onCreate = { openMain("editor/0") },
                onEdit = { openMain("editor/$it") },
                error = error
            )
        }
        composable("editor/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val existing = profiles.find { it.id == entry.arguments?.getLong("id") }
            ProfileEditorScreen(existing = existing, onSave = { name, avatar, primary, secondary, genres ->
                vm.saveProfile(existing, name, avatar, primary, secondary, genres) {
                    if (existing == null) nav.navigate("home") { popUpTo("profiles") { inclusive = true } }
                    else nav.popBackStack()
                }
            }, onBack = { nav.popBackStack() }, error = error)
        }
        listOf("home", "search", "browse", "mylist", "about", "updates").forEach { route ->
            composable(route) {
                val selectedMenuItem = remember(route) { FocusRequester() }
                val contentEntry = remember(route) { FocusRequester() }
                LaunchedEffect(route) { selectedMenuItem.requestFocus() }
                Row(Modifier.fillMaxSize()) {
                    Sidebar(
                        current = route, profile = profile,
                        onNavigate = ::openMain,
                        onSwitchProfile = { nav.navigate("profiles") },
                        selectedItemFocusRequester = selectedMenuItem,
                        contentFocusRequester = if (route == "about" || (route == "mylist" && savedMovies.isEmpty()) || (route == "home" && homeMovies.isEmpty())) null else contentEntry,
                        modifier = Modifier.width(202.dp)
                    )
                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalMenuFocusRequester provides selectedMenuItem,
                            LocalArtworkPrefetch provides vm::prefetchArtwork) {
                            when (route) {
                                "home" -> HomeScreen(profile, homeMovies, progress, contentEntry, onMovie = { openMain("details/$it") })
                                "search" -> SearchScreen(searchQuery, searchResults, vm::setSearchQuery, contentEntry, onMovie = { openMain("details/$it") })
                                "browse" -> BrowseScreen(movies, contentEntry, sourceCount, syncRunning,
                                    syncMessage ?: syncState?.lastError, vm::refreshCatalogue, onMovie = { openMain("details/$it") })
                                "mylist" -> MyListScreen(profile, savedMovies, contentEntry, onMovie = { openMain("details/$it") })
                                "about" -> AboutScreen()
                                "updates" -> UpdateScreen(contentEntry)
                            }
                        }
                    }
                }
            }
        }
        composable("details/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val movie = movies.find { it.id == entry.arguments?.getLong("id") }
            LaunchedEffect(movie?.id) { movie?.id?.let(vm::enrichMovie) }
            CompositionLocalProvider(LocalArtworkPrefetch provides vm::prefetchArtwork) {
                DetailsScreen(movie, movie?.id in savedIds, onToggleList = { movie?.let { vm.toggleWatchlist(it.id) } },
                    onPlay = {
                        val source = movie ?: return@DetailsScreen
                        val viewer = profile ?: return@DetailsScreen
                        val saved = progress.firstOrNull { it.movieId == source.id }
                        context.startActivity(Intent(context, PlayerActivity::class.java)
                            .putExtra(PlayerActivity.MOVIE_ID, source.id)
                            .putExtra(PlayerActivity.PROFILE_ID, viewer.id)
                            .putExtra(PlayerActivity.TITLE, source.title)
                            .putExtra(PlayerActivity.START_POSITION_MS, saved?.takeUnless { it.completed }?.positionMs ?: 0L))
                    }, onTestWebPlayback = {
                        val source = movie ?: return@DetailsScreen
                        if (com.chitram.tv.BuildConfig.DEBUG && com.chitram.tv.data.verifiedWebPlaybackPage(source) != null) {
                            context.startActivity(Intent(context, WebPlaybackActivity::class.java)
                                .putExtra(WebPlaybackActivity.MOVIE_ID, source.id))
                        }
                    }, onBack = { nav.popBackStack() })
            }
        }
    }
}
