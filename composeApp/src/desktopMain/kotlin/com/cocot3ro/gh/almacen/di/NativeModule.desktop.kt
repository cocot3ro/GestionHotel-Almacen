package com.cocot3ro.gh.almacen.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.cocot3ro.gh.almacen.core.datastore.DatastoreConstants
import okio.Path.Companion.toOkioPath
import org.koin.dsl.module
import java.nio.file.Path
import kotlin.io.path.absolutePathString

actual val nativeModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val os = System.getProperty("os.name").lowercase()
                val path = when {
                    os.startsWith("win", ignoreCase = false) -> {
                        Path.of(System.getProperty("LOCALAPPDATA"))
                    }

                    else -> {
                        Path.of(
                            System.getProperty("user.home"),
                            ".local",
                            "shared"
                        )
                    }
                }.absolutePathString()

                Path.of(
                    path,
                    "Gestion Hotel",
                    "GH Almacen",
                    DatastoreConstants.DATA_STORE_FILE_NAME
                ).toOkioPath()
            }
        )
    }
}
