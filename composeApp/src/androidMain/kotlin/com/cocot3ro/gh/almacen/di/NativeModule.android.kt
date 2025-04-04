package com.cocot3ro.gh.almacen.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.cocot3ro.gh.almacen.core.datastore.DatastoreConstants
import com.cocot3ro.gh.almacen.ui.screens.setup.SetupFase2ViewModel
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val nativeModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                get<Context>().filesDir.resolve(DatastoreConstants.DATA_STORE_FILE_NAME)
                    .toOkioPath()
            }
        )
    }

    viewModelOf(::SetupFase2ViewModel)

}