package com.chitram.tv.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomPersistenceTest {
    @Test fun profilesAndWatchlistsSurviveReopenAndStaySeparate() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "chitram-test.db"
        context.deleteDatabase(databaseName)
        val db = Room.databaseBuilder(context, ChitramDatabase::class.java, databaseName).build()
        val a = db.profiles().insert(ProfileEntity(name = "A", avatar = "Ocean", primaryLanguage = "Tamil", secondaryLanguages = "Hindi", favouriteGenres = ""))
        val b = db.profiles().insert(ProfileEntity(name = "B", avatar = "Cedar", primaryLanguage = "Marathi", secondaryLanguages = "", favouriteGenres = ""))
        db.movies().insertAll(DemoCatalogue.movies.take(1))
        db.watchlist().insert(WatchlistEntity(a, 1))
        db.close()

        val reopened = Room.databaseBuilder(context, ChitramDatabase::class.java, databaseName).build()
        assertEquals(2, reopened.profiles().observeAll().first().size)
        assertEquals(1, reopened.movies().count())
        assertTrue(reopened.watchlist().contains(a, 1))
        assertFalse(reopened.watchlist().contains(b, 1))
        reopened.close()
        context.deleteDatabase(databaseName)
        Unit
    }
}
