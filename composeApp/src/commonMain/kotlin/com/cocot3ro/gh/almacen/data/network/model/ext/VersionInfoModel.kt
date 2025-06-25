package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.data.network.model.VersionInfoModel
import com.cocot3ro.gh.almacen.domain.model.VersionInfoDomain

fun VersionInfoModel.toDomain(): VersionInfoDomain = VersionInfoDomain(
    versionName = version,
    url = url
)