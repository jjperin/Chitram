package com.chitram.tv.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "movies", indices = [Index("language"), Index("title"), Index("releaseYear")])
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
    val artColor: Long
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
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(movies: List<MovieEntity>)
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
    entities = [MovieEntity::class, ProfileEntity::class, WatchlistEntity::class, ProgressEntity::class],
    version = 1,
    exportSchema = true
)
abstract class ChitramDatabase : RoomDatabase() {
    abstract fun movies(): MovieDao
    abstract fun profiles(): ProfileDao
    abstract fun watchlist(): WatchlistDao
    abstract fun progress(): ProgressDao
}
