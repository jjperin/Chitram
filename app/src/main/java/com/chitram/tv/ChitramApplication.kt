package com.chitram.tv

import android.app.Application
import androidx.room.Room
import com.chitram.tv.data.ChitramDatabase
import com.chitram.tv.data.ChitramRepository
import com.chitram.tv.data.MIGRATION_1_2
import com.chitram.tv.data.MIGRATION_2_3
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class ChitramApplication : Application(), ImageLoaderFactory {
    val database by lazy {
        Room.databaseBuilder(this, ChitramDatabase::class.java, "chitram.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }
    val repository by lazy { ChitramRepository(database) }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(true)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.10).build() }
        .diskCache { DiskCache.Builder().directory(cacheDir.resolve("artwork")).maxSizeBytes(250L * 1024 * 1024).build() }
        .build()
}
