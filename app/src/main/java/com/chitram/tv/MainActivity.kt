package com.chitram.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chitram.tv.ui.BrowseScreen
import com.chitram.tv.ui.ChitramTheme
import com.chitram.tv.ui.DetailsScreen
import com.chitram.tv.ui.HomeScreen
import com.chitram.tv.ui.MyListScreen
import com.chitram.tv.ui.ProfileEditorScreen
import com.chitram.tv.ui.ProfilePickerScreen
import com.chitram.tv.ui.SearchScreen
import com.chitram.tv.ui.Sidebar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChitramTheme { ChitramApp() } }
    }
}

@Composable
private fun ChitramApp(vm: ChitramViewModel = viewModel()) {
    val nav = rememberNavController()
    val profiles by vm.profiles.collectAsStateWithLifecycle()
    val movies by vm.movies.collectAsStateWithLifecycle()
    val profile by vm.activeProfile.collectAsStateWithLifecycle()
    val watchlistState by vm.watchlist.collectAsStateWithLifecycle()
    val progressState by vm.progress.collectAsStateWithLifecycle()
    val searchResults by vm.searchResults.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val savedIds = if (watchlistState.first == profile?.id) watchlistState.second.map { it.movieId }.toSet() else emptySet()
    val progress = if (progressState.first == profile?.id) progressState.second else emptyList()

    fun openMain(route: String) {
        nav.navigate(route) { launchSingleTop = true }
    }

    NavHost(navController = nav, startDestination = "profiles", modifier = Modifier.fillMaxSize()) {
        composable("profiles") {
            ProfilePickerScreen(
                profiles = profiles,
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
        listOf("home", "search", "browse", "mylist").forEach { route ->
            composable(route) {
                Row(Modifier.fillMaxSize()) {
                    Sidebar(
                        current = route, profile = profile,
                        onNavigate = ::openMain,
                        onSwitchProfile = { nav.navigate("profiles") },
                        modifier = Modifier.width(202.dp)
                    )
                    Box(Modifier.weight(1f)) {
                        when (route) {
                            "home" -> HomeScreen(profile, movies, progress, onMovie = { openMain("details/$it") })
                            "search" -> SearchScreen(searchQuery, searchResults, vm::setSearchQuery, onMovie = { openMain("details/$it") })
                            "browse" -> BrowseScreen(movies, onMovie = { openMain("details/$it") })
                            "mylist" -> MyListScreen(profile, movies.filter { it.id in savedIds }, onMovie = { openMain("details/$it") })
                        }
                    }
                }
            }
        }
        composable("details/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val movie = movies.find { it.id == entry.arguments?.getLong("id") }
            DetailsScreen(movie, movie?.id in savedIds, onToggleList = { movie?.let { vm.toggleWatchlist(it.id) } }, onBack = { nav.popBackStack() })
        }
    }
}
