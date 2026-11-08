package com.chitram.tv.data

import kotlinx.coroutines.flow.Flow

class ChitramRepository(private val db: ChitramDatabase) {
    val profiles: Flow<List<ProfileEntity>> = db.profiles().observeAll()
    val movies: Flow<List<MovieEntity>> = db.movies().observeAll()

    suspend fun seedIfEmpty() {
        if (db.movies().count() == 0) db.movies().insertAll(DemoCatalogue.movies)
    }

    suspend fun createProfile(profile: ProfileEntity): Long = db.profiles().insert(profile)
    suspend fun updateProfile(profile: ProfileEntity) = db.profiles().update(profile)
    fun watchlist(profileId: Long) = db.watchlist().observeForProfile(profileId)
    fun progress(profileId: Long) = db.progress().observeForProfile(profileId)
    fun search(query: String) = db.movies().search(query)

    suspend fun toggleWatchlist(profileId: Long, movieId: Long) {
        if (db.watchlist().contains(profileId, movieId)) db.watchlist().delete(profileId, movieId)
        else db.watchlist().insert(WatchlistEntity(profileId, movieId))
    }
}
