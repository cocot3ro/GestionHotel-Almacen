package com.cocot3ro.gh.almacen

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.cocot3ro.gh.almacen.core.storage.StorageConstants
import com.cocot3ro.gh.almacen.di.initKoin
import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class GhAlmacen : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@GhAlmacen)

            analytics()
        }
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(StorageConstants.IMAGE_CACHE))
                    .build()
            }
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }
}