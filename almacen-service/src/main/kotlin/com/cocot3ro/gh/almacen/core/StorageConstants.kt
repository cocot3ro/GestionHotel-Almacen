package com.cocot3ro.gh.almacen.core

import java.nio.file.Path
import kotlin.io.path.absolutePathString


object StorageConstants {

    private val localDir: String = System.getProperty("os.name").lowercase().let { os ->
        when {
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
    }

    private val suiteDir: String =
        Path.of(localDir, "Gestion Hotel").also { path ->
            path.toFile().let { file ->
                if (!file.exists()) {
                    file.mkdirs()
                }
            }
        }.absolutePathString()

    private val applicationDir: String =
        Path.of(suiteDir, "GH Almacen").also { path ->
            path.toFile().let { file ->
                if (!file.exists()) {
                    file.mkdirs()
                }
            }
        }.absolutePathString()

    val databaseDir: String = Path.of(applicationDir, "database").also { path ->
        path.toFile().let { file ->
            if (!file.exists()) {
                file.mkdirs()
            }
        }
    }.absolutePathString()

    private val imagesDir: String = Path.of(applicationDir, "images").also { path ->
        path.toFile().let { file ->
            if (!file.exists()) {
                file.mkdirs()
            }
        }
    }.absolutePathString()

    val almacenUserImagesDir: String = Path.of(imagesDir, "users").also { path ->
        path.toFile().let { file ->
            if (!file.exists()) {
                file.mkdirs()
            }
        }
    }.absolutePathString()

    val almacenStoreImagesDir: String = Path.of(imagesDir, "stores").also { path ->
        path.toFile().let { file ->
            if (!file.exists()) {
                file.mkdirs()
            }
        }
    }.absolutePathString()

    val almacenItemImagesDir: String = Path.of(imagesDir, "products").also { path ->
        path.toFile().let { file ->
            if (!file.exists()) {
                file.mkdirs()
            }
        }
    }.absolutePathString()
}
