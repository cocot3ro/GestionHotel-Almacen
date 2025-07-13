package com.cocot3ro.gh.almacen.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.cocot3ro.gh.almacen.di.AuthDatastore
import com.cocot3ro.gh.almacen.di.PreferencesDatastore
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Single
@PreferencesDatastore
expect fun getPreferencesDatastore(scope: Scope): DataStore<Preferences>

@Single
@AuthDatastore
expect fun getAuthDatastore(scope: Scope): DataStore<Preferences>
