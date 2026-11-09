package com.chitram.tv.data

import androidx.room.withTransaction
import java.security.MessageDigest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChitramRepository(private val db: ChitramDatabase) {
    private val catalogue = EinthusanCatalogue()
    private val syncMutex = Mutex()
    private val tmdb = TmdbMetadata(com.chitram.tv.BuildConfig.TMDB_READ_TOKEN)
    val profiles: Flow<List<ProfileEntity>> = db.profiles().observeAll()
    val movies: Flow<List<MovieEntity>> = db.movies().observeAll()
    val sourceCount: Flow<Int> = db.movies().observeSourceCount()
    val syncState: Flow<CatalogueSyncEntity?> = db.catalogueSync().observe()

    suspend fun seedMissingDemos() {
        val missingDemos = mutableListOf<MovieEntity>()
        for (demo in DemoCatalogue.movies) {
            if (db.movies().findById(demo.id) == null) missingDemos += demo
        }
        if (missingDemos.isNotEmpty()) db.movies().insertAll(missingDemos)
        db.movies().removeMustWatchBadges()
        db.movies().removeDubbedBadges()
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

    suspend fun enrichFromTmdb(movieId: Long) {
        val movie = db.movies().findById(movieId) ?: return
        if (movie.isDemo || movie.sourceProvider != "einthusan") return
        if (System.currentTimeMillis() - movie.tmdbCheckedAtMs < 30L * 24 * 60 * 60 * 1000) return
        if (com.chitram.tv.BuildConfig.TMDB_READ_TOKEN.isBlank()) return
        val match = tmdb.search(movie.title, movie.releaseYear, movie.language)
        val current = db.movies().findById(movieId) ?: return
        db.movies().update(current.copy(
            tmdbCheckedAtMs = System.currentTimeMillis(),
            tmdbId = match?.id ?: current.tmdbId,
            posterUrl = match?.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" } ?: current.posterUrl,
            backdropUrl = match?.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" } ?: current.backdropUrl,
            rating = match?.rating ?: current.rating,
            ratingSource = if (match != null) "tmdb" else current.ratingSource,
            popularity = match?.popularity ?: current.popularity,
            genres = current.genres.ifBlank { match?.genres.orEmpty() },
            releaseDate = match?.releaseDate ?: current.releaseDate,
            synopsis = current.synopsis.ifBlank { match?.overview.orEmpty() }
        ))
    }

    suspend fun refreshIfDue(report: (String) -> Unit) {
        val now = System.currentTimeMillis()
        val state = db.catalogueSync().get() ?: CatalogueSyncEntity()
        if (now - state.lastQuickAtMs >= 24L * 60 * 60 * 1000) refreshCatalogue(false, report)
        val afterQuick = db.catalogueSync().get() ?: state
        if (System.currentTimeMillis() - afterQuick.lastFullAtMs >= 7L * 24 * 60 * 60 * 1000) {
            refreshCatalogue(true, report)
        }
    }

    suspend fun refreshCatalogue(full: Boolean, report: (String) -> Unit) = syncMutex.withLock {
        try {
            if (full) scanFull(report) else scanRecent(report)
        } catch (exception: Exception) {
            val current = db.catalogueSync().get() ?: CatalogueSyncEntity()
            db.catalogueSync().save(current.copy(lastError = exception.message ?: "Catalogue refresh failed"))
            throw exception
        }
    }

    private suspend fun scanRecent(report: (String) -> Unit) {
        var added = 0
        DemoCatalogue.languages.forEach { language ->
            report("Checking recent $language listings…")
            val page = catalogue.parseRecent(catalogue.fetch(catalogue.recentUrl(language)), language)
            added += saveListings(page.listings)
            delay(150)
        }
        val current = db.catalogueSync().get() ?: CatalogueSyncEntity()
        db.catalogueSync().save(current.copy(lastQuickAtMs = System.currentTimeMillis(), lastError = null, lastAdded = added))
        report("Recent check complete: $added new listings")
    }

    private suspend fun scanFull(report: (String) -> Unit) {
        val letters = listOf("Numbers") + ('A'..'Z').map { it.toString() }
        var state = db.catalogueSync().get() ?: CatalogueSyncEntity()
        var added = 0
        for (languageIndex in state.nextLanguage until DemoCatalogue.languages.size) {
            val language = DemoCatalogue.languages[languageIndex]
            val firstLetter = if (languageIndex == state.nextLanguage) state.nextLetter else 0
            for (letterIndex in firstLetter until letters.size) {
                val letter = letters[letterIndex]
                var pageNumber = if (languageIndex == state.nextLanguage && letterIndex == state.nextLetter) state.nextPage else 1
                var pageCount: Int
                do {
                    report("Scanning $language $letter, page $pageNumber…")
                    val page = catalogue.parseResults(catalogue.fetch(catalogue.alphabetUrl(language, letter, pageNumber)), language)
                    added += saveListings(page.listings)
                    pageCount = page.pageCount
                    pageNumber++
                    state = state.copy(nextLanguage = languageIndex, nextLetter = letterIndex, nextPage = pageNumber, lastError = null)
                    db.catalogueSync().save(state)
                    delay(150)
                } while (pageNumber <= pageCount)
                state = state.copy(nextLanguage = languageIndex, nextLetter = letterIndex + 1, nextPage = 1)
                db.catalogueSync().save(state)
            }
            state = state.copy(nextLanguage = languageIndex + 1, nextLetter = 0, nextPage = 1)
            db.catalogueSync().save(state)
        }
        db.catalogueSync().save(state.copy(lastFullAtMs = System.currentTimeMillis(), lastError = null,
            lastAdded = added, nextLanguage = 0, nextLetter = 0, nextPage = 1))
        report("Full catalogue scan complete: $added new listings")
    }

    internal suspend fun saveListings(listings: List<SourceListing>): Int = db.withTransaction {
        var added = 0
        val updates = listings.mapNotNull { listing ->
            val old = db.movies().findBySource("einthusan", listing.sourceId)
            if (old == null) added++
            val id = old?.id ?: stableId(listing.sourceId)
            val atId = db.movies().findById(id)
            if (atId != null && (atId.sourceProvider != "einthusan" || atId.sourceId != listing.sourceId)) {
                error("Catalogue ID collision; existing movie was kept")
            }
            val item = (old ?: MovieEntity(
                id = id, title = listing.title, language = listing.language,
                releaseYear = listing.year, genres = "", rating = 0.0, popularity = 0.0,
                synopsis = listing.synopsis, isDemo = false, artColor = 0xFF303B46,
                sourceId = listing.sourceId, sourceProvider = "einthusan", availability = "listed"
            )).copy(
                title = listing.title, language = listing.language,
                releaseYear = listing.year.takeIf { it > 0 } ?: old?.releaseYear ?: 0,
                synopsis = listing.synopsis.ifBlank { old?.synopsis.orEmpty() },
                sourcePageUrl = listing.pageUrl
            )
            item.takeIf { it != old }
        }
        if (updates.isNotEmpty()) db.movies().insertAll(updates)
        added
    }

    private fun stableId(sourceId: String): Long {
        val hash = MessageDigest.getInstance("SHA-256").digest("einthusan:$sourceId".toByteArray())
        var value = 0L
        repeat(8) { value = (value shl 8) or (hash[it].toLong() and 0xff) }
        return (value and Long.MAX_VALUE) or (1L shl 60)
    }
}
