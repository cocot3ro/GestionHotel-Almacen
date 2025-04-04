package com.cocot3ro.gh.almacen.data.network.repository

import com.cocot3ro.gh.almacen.data.network.client.GhAlmacenClient
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class NetworkRepository(
    @Provided private val ghAlmacenClient: GhAlmacenClient,
)
