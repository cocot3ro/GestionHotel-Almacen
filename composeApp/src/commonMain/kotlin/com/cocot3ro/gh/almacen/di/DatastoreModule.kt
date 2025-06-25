package com.cocot3ro.gh.almacen.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Named
annotation class PreferencesDatastore

@Named
annotation class AuthDatastore

@Module
@ComponentScan("com.cocot3ro.gh.almacen.core.datastore")
object DatastoreModule