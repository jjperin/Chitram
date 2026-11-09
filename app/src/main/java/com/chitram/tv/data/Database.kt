package com.chitram.tv.data

import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "movies", indices = [Index("language"), Index("title"), Index("releaseYear"), Index(value = ["sourceProvider", "sourceId"], unique = true)])
data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val language: String,
    val releaseYear: Int,
    val genres: String,
    val rating: Double,
    val popularity: Double,
    val synopsis: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val tmdbId: Long? = null,
    val sourceId: String? = null,
    val isDemo: Boolean = true,
    val artColor: Long,
    val sourceProvider: String? = null,
    val sourcePageUrl: String? = null,
    val releaseDate: String? = null,
    val ratingSource: String? = null,
    @ColumnInfo(defaultValue = "'demo'") val availability: String = "demo",
    @ColumnInfo(defaultValue = "0") val tmdbCheckedAtMs: Long = 0
)

@Entity(tableName = "catalogue_sync")
data class CatalogueSyncEntity(
    @PrimaryKey val id: Int = 1,
    val lastQuickAtMs: Long = 0,
    val lastFullAtMs: Long = 0,
    val lastError: String? = null,
    val lastAdded: Int = 0,
    val nextLanguage: Int = 0,
    val nextLetter: Int = 0,
    val nextPage: Int = 1
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatar: String,
    val primaryLanguage: String,
    val secondaryLanguages: String,
    val favouriteGenres: String
)

@Entity(tableName = "watchlist", primaryKeys = ["profileId", "movieId"], indices = [Index("movieId")])
data class WatchlistEntity(val profileId: Long, val movieId: Long)

@Entity(tableName = "progress", primaryKeys = ["profileId", "movieId"], indices = [Index("movieId")])
data class ProgressEntity(
    val profileId: Long,
    val movieId: Long,
    val positionMs: Long,
    val durationMs: Long,
    val completed: Boolean,
    val updatedAtMs: Long
)

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY title") fun observeAll(): Flow<List<MovieEntity>>
    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY title")
    fun search(query: String): Flow<List<MovieEntity>>
    @Query("SELECT COUNT(*) FROM movies") suspend fun count(): Int
    @Query("SELECT COUNT(*) FROM movies WHERE sourceProvider = 'einthusan'") suspend fun sourceCount(): Int
    @Query("SELECT COUNT(*) FROM movies WHERE sourceProvider = 'einthusan'") fun observeSourceCount(): Flow<Int>
    @Query("SELECT * FROM movies WHERE sourceProvider = :provider AND sourceId = :sourceId LIMIT 1")
    suspend fun findBySource(provider: String, sourceId: String): MovieEntity?
    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1") suspend fun findById(id: Long): MovieEntity?
    @Update suspend fun update(movie: MovieEntity)
    @Query("UPDATE movies SET title = substr(title, 1, length(title) - 11) WHERE sourceProvider = 'einthusan' AND title LIKE '% Must Watch'")
    suspend fun removeMustWatchBadges()
    @Query("UPDATE movies SET title = substr(title, 1, length(title) - 7) WHERE sourceProvider = 'einthusan' AND title LIKE '% Dubbed'")
    suspend fun removeDubbedBadges()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(movies: List<MovieEntity>)
}

@Dao
interface CatalogueSyncDao {
    @Query("SELECT * FROM catalogue_sync WHERE id = 1") fun observe(): Flow<CatalogueSyncEntity?>
    @Query("SELECT * FROM catalogue_sync WHERE id = 1") suspend fun get(): CatalogueSyncEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(state: CatalogueSyncEntity)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id") fun observeAll(): Flow<List<ProfileEntity>>
    @Insert suspend fun insert(profile: ProfileEntity): Long
    @Update suspend fun update(profile: ProfileEntity)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist WHERE profileId = :profileId")
    fun observeForProfile(profileId: Long): Flow<List<WatchlistEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE profileId = :profileId AND movieId = :movieId)")
    suspend fun contains(profileId: Long, movieId: Long): Boolean
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(item: WatchlistEntity)
    @Query("DELETE FROM watchlist WHERE profileId = :profileId AND movieId = :movieId")
    suspend fun delete(profileId: Long, movieId: Long)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE profileId = :profileId")
    fun observeForProfile(profileId: Long): Flow<List<ProgressEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: ProgressEntity)
}

@Database(
    entities = [MovieEntity::class, ProfileEntity::class, WatchlistEntity::class, ProgressEntity::class, CatalogueSyncEntity::class],
    version = 3,
    exportSchema = true
)
abstract class ChitramDatabase : RoomDatabase() {
    abstract fun movies(): MovieDao
    abstract fun catalogueSync(): CatalogueSyncDao
    abstract fun profiles(): ProfileDao
    abstract fun watchlist(): WatchlistDao
    abstract fun progress(): ProgressDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE movies ADD COLUMN sourceProvider TEXT")
        db.execSQL("ALTER TABLE movies ADD COLUMN sourcePageUrl TEXT")
        db.execSQL("ALTER TABLE movies ADD COLUMN releaseDate TEXT")
        db.execSQL("ALTER TABLE movies ADD COLUMN ratingSource TEXT")
        db.execSQL("ALTER TABLE movies ADD COLUMN availability TEXT NOT NULL DEFAULT 'demo'")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_movies_sourceProvider_sourceId ON movies(sourceProvider, sourceId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS catalogue_sync (id INTEGER NOT NULL PRIMARY KEY, lastQuickAtMs INTEGER NOT NULL, lastFullAtMs INTEGER NOT NULL, lastError TEXT, lastAdded INTEGER NOT NULL, nextLanguage INTEGER NOT NULL, nextLetter INTEGER NOT NULL, nextPage INTEGER NOT NULL)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE movies ADD COLUMN tmdbCheckedAtMs INTEGER NOT NULL DEFAULT 0")
    }
}
