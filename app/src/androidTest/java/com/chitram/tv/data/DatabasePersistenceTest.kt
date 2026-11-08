package com.chitram.tv.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabasePersistenceTest {
    @Test fun profilesAndListsSurviveDatabaseReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "persistence-test.db"
        context.deleteDatabase(name)
        val firstDb = Room.databaseBuilder(context, ChitramDatabase::class.java, name).build()
        val firstId = firstDb.profiles().insert(ProfileEntity(name = "A", avatar = "Ocean", primaryLanguage = "Tamil", secondaryLanguages = "Hindi", favouriteGenres = ""))
        val secondId = firstDb.profiles().insert(ProfileEntity(name = "B", avatar = "Cedar", primaryLanguage = "Marathi", secondaryLanguages = "", favouriteGenres = ""))
        firstDb.movies().insertAll(DemoCatalogue.movies.take(1))
        firstDb.watchlist().insert(WatchlistEntity(firstId, DemoCatalogue.movies.first().id))
        firstDb.close()

        val reopened = Room.databaseBuilder(context, ChitramDatabase::class.java, name).build()
        assertEquals(2, reopened.profiles().observeAll().first().size)
        assertTrue(reopened.watchlist().contains(firstId, DemoCatalogue.movies.first().id))
        assertFalse(reopened.watchlist().contains(secondId, DemoCatalogue.movies.first().id))
        assertEquals(1, reopened.movies().count())
        reopened.close()
        context.deleteDatabase(name)
        Unit
    }
}
