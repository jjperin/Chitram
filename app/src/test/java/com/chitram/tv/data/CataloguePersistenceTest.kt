package com.chitram.tv.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CataloguePersistenceTest {
    @Test fun upsertKeepsExistingDetailsAndWatchlist() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "catalogue-upsert.db"
        context.deleteDatabase(name)
        val db = Room.databaseBuilder(context, ChitramDatabase::class.java, name).build()
        val repo = ChitramRepository(db)
        val profileId = db.profiles().insert(ProfileEntity(name = "A", avatar = "Ocean", primaryLanguage = "Tamil", secondaryLanguages = "", favouriteGenres = ""))
        val first = SourceListing("6193", "Example Film", "Tamil", 2009, "Full description", "https://einthusan.tv/movie/watch/6193/?lang=tamil")
        assertEquals(1, repo.saveListings(listOf(first)))
        val stored = db.movies().findBySource("einthusan", "6193")!!
        db.watchlist().insert(WatchlistEntity(profileId, stored.id))
        assertEquals(0, repo.saveListings(listOf(first.copy(synopsis = "", title = "Example Film Updated"))))
        db.close()

        val reopened = Room.databaseBuilder(context, ChitramDatabase::class.java, name).build()
        val after = reopened.movies().findBySource("einthusan", "6193")!!
        assertEquals(stored.id, after.id)
        assertEquals("Example Film Updated", after.title)
        assertEquals("Full description", after.synopsis)
        assertEquals("listed", after.availability)
        assertTrue(after.firstSeenAtMs > 0)
        assertEquals(stored.firstSeenAtMs, after.firstSeenAtMs)
        assertTrue(reopened.watchlist().contains(profileId, after.id))
        reopened.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test fun migratesVersionOneWithoutLosingProfileMovieOrWatchlist() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "catalogue-migration.db"
        context.deleteDatabase(name)
        val schema = JSONObject(File("schemas/com.chitram.tv.data.ChitramDatabase/1.json").readText())
            .getJSONObject("database").getJSONArray("entities")
        val legacy = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
        for (index in 0 until schema.length()) {
            val entity = schema.getJSONObject(index)
            val table = entity.getString("tableName")
            legacy.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
            val indices = entity.getJSONArray("indices")
            for (i in 0 until indices.length()) {
                legacy.execSQL(indices.getJSONObject(i).getString("createSql").replace("\${TABLE_NAME}", table))
            }
        }
        legacy.execSQL("INSERT INTO profiles (id,name,avatar,primaryLanguage,secondaryLanguages,favouriteGenres) VALUES (1,'Jake','Ocean','Tamil','','')")
        legacy.execSQL("INSERT INTO movies (id,title,language,releaseYear,genres,rating,popularity,synopsis,isDemo,artColor) VALUES (1,'Demo','Tamil',2020,'Drama',7.0,1.0,'Saved film',1,1)")
        legacy.execSQL("INSERT INTO watchlist (profileId,movieId) VALUES (1,1)")
        legacy.version = 1
        legacy.close()

        val upgraded = Room.databaseBuilder(context, ChitramDatabase::class.java, name).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
        assertEquals("Jake", upgraded.profiles().observeAll().first().single().name)
        assertEquals("demo", upgraded.movies().findById(1)!!.availability)
        assertEquals(0, upgraded.movies().findById(1)!!.tmdbCheckedAtMs)
        assertEquals(0, upgraded.movies().findById(1)!!.firstSeenAtMs)
        assertTrue(upgraded.watchlist().contains(1, 1))
        upgraded.close()
        context.deleteDatabase(name)
        Unit
    }
}
