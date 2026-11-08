package com.chitram.tv

import android.app.Application
import androidx.room.Room
import com.chitram.tv.data.ChitramDatabase
import com.chitram.tv.data.ChitramRepository

class ChitramApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, ChitramDatabase::class.java, "chitram.db")
            .build()
    }
    val repository by lazy { ChitramRepository(database) }
}
