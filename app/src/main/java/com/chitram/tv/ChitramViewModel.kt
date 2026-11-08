package com.chitram.tv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chitram.tv.data.ProfileEntity
import com.chitram.tv.data.ProgressEntity
import com.chitram.tv.data.WatchlistEntity
import com.chitram.tv.domain.joinChoices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChitramViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ChitramApplication).repository
    val profiles = repository.profiles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val movies = repository.movies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
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
    val searchResults = searchQuery.flatMapLatest { repository.search(it.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val error = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            try { repository.seedIfEmpty() }
            catch (exception: Exception) { error.value = "Unable to open local catalogue: ${exception.message ?: "unknown error"}" }
        }
    }

    fun selectProfile(id: Long) { activeProfileId.value = id }
    fun setSearchQuery(query: String) { searchQuery.value = query }

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
