package com.chitram.tv

import android.app.Application
import com.chitram.tv.data.CatalogueSyncEntity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chitram.tv.data.ProfileEntity
import com.chitram.tv.data.ProgressEntity
import com.chitram.tv.data.WatchlistEntity
import com.chitram.tv.domain.joinChoices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChitramViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ChitramApplication).repository
    val profilesLoaded = MutableStateFlow(false)
    val profiles = repository.profiles.onEach { profilesLoaded.value = true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val movies = repository.movies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val sourceCount = repository.sourceCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val syncState = repository.syncState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null as CatalogueSyncEntity?)
    val syncRunning = MutableStateFlow(false)
    val syncMessage = MutableStateFlow<String?>(null)
    val activeProfileId = MutableStateFlow<Long?>(null)
    val activeProfile = combine(profiles, activeProfileId) { list, id -> list.find { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val watchlist = activeProfileId.flatMapLatest { id ->
        if (id == null) flowOf(id to emptyList<WatchlistEntity>())
        else repository.watchlist(id).map { id to it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null to emptyList())
    val progress = activeProfileId.flatMapLatest { id ->
        if (id == null) flowOf(id to emptyList<ProgressEntity>())
        else repository.progress(id).map { id to it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null to emptyList())
    val searchQuery = MutableStateFlow("")
    val searchResults = searchQuery.flatMapLatest { query ->
        query.trim().takeIf { it.isNotEmpty() }?.let(repository::search) ?: flowOf(emptyList())
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val error = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try { repository.seedMissingDemos() }
            catch (exception: Exception) { error.value = "Unable to open local catalogue: ${exception.message ?: "unknown error"}" }
            syncRunning.value = true
            try { repository.refreshIfDue { syncMessage.value = it } }
            catch (exception: Exception) { syncMessage.value = exception.message ?: "Catalogue refresh failed; saved films are still available" }
            finally { syncRunning.value = false }
        }
    }

    fun selectProfile(id: Long) { activeProfileId.value = id }
    fun setSearchQuery(query: String) { searchQuery.value = query }

    fun enrichMovie(movieId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try { repository.enrichFromTmdb(movieId) }
            catch (_: Exception) { /* Source metadata remains usable when TMDB is unavailable. */ }
        }
    }

    fun refreshCatalogue(full: Boolean) {
        if (syncRunning.value) return
        viewModelScope.launch(Dispatchers.IO) {
            syncRunning.value = true
            try { repository.refreshCatalogue(full) { syncMessage.value = it } }
            catch (exception: Exception) { syncMessage.value = exception.message ?: "Catalogue refresh failed; saved films are still available" }
            finally { syncRunning.value = false }
        }
    }

    fun saveProfile(
        existing: ProfileEntity?, name: String, avatar: String, primary: String,
        secondary: Set<String>, genres: Set<String>, onSaved: () -> Unit
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val item = ProfileEntity(
                    id = existing?.id ?: 0,
                    name = name.trim(), avatar = avatar, primaryLanguage = primary,
                    secondaryLanguages = joinChoices(secondary.filter { it != primary }),
                    favouriteGenres = joinChoices(genres)
                )
                if (existing == null) activeProfileId.value = repository.createProfile(item)
                else repository.updateProfile(item)
                onSaved()
            } catch (exception: Exception) {
                error.value = "Unable to save profile: ${exception.message ?: "unknown error"}"
            }
        }
    }

    fun toggleWatchlist(movieId: Long) {
        val profileId = activeProfileId.value ?: return
        viewModelScope.launch {
            try { repository.toggleWatchlist(profileId, movieId) }
            catch (exception: Exception) { error.value = "Unable to update My List" }
        }
    }
}
