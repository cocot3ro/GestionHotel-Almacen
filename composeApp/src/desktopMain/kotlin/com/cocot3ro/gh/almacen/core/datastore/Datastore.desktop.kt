package com.cocot3ro.gh.almacen.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.cocot3ro.gh.almacen.di.AuthDatastore
import com.cocot3ro.gh.almacen.di.PreferencesDatastore
import org.koin.core.annotation.Single

@Single
@PreferencesDatastore
actual fun getPreferencesDatastore(): DataStore<Preferences> {
    TODO("Not yet implemented")
}

@Single
@AuthDatastore
actual fun getAuthDatastore(): DataStore<Preferences> {
    TODO("Not yet implemented")
}