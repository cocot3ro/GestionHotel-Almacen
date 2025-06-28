import com.google.devtools.ksp.KspExperimental
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.kotlin.plugin.serialization)

    alias(libs.plugins.ksp)

    alias(libs.plugins.kotzilla)
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)

            implementation(libs.barcode.scanning)

            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.extensions)

            implementation(libs.accompanist.permissions)

            implementation(libs.androidx.core.splashscreen)
            runtimeOnly(libs.material)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(projects.shared)

            implementation(libs.kotzilla.sdk.ktor3)

            implementation(project.dependencies.platform(libs.ktor.bom))
            implementation(libs.bundles.ktor.client)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin.client)
            api(libs.koin.annotations)

            implementation(project.dependencies.platform(libs.coil.bom))
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.network.cache.control)

            implementation(libs.navigation.compose)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            implementation(libs.core.network.resources)
            implementation(libs.core.network.model)

            implementation(libs.almacen.service.network.model)
            implementation(libs.almacen.service.network.resources)

            implementation(libs.core.shared)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }

    sourceSets.commonMain.configure {
        kotlin.srcDirs(
            "build/generated/ksp/metadata/commonMain/kotlin",
            "build/generated/buildConfig/kotlin/commonMain"
        )
    }
}

val version: String = "1.0.0"

android {
    namespace = "com.cocot3ro.gh.almacen"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.cocot3ro.gh.almacen"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionName = version
        versionCode = version.split(".").map(String::toInt).run {
            reduce { acc, i -> acc * 100 + i }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "null")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    } else {
        dependsOn("generateBuildConfig")
    }
}

dependencies {
    debugImplementation(compose.uiTooling)

    kspCommonMainMetadata(libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspDesktop", libs.koin.ksp.compiler)
}

compose.desktop {
    application {
        mainClass = "com.cocot3ro.gh.almacen.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm
            )
            packageName = "com.cocot3ro.gh.almacen"
            packageVersion = version
        }
    }
}

ksp {
    @OptIn(KspExperimental::class)
    useKsp2 = false

    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
    arg("KOIN_DEFAULT_MODULE", "false")
    arg("KOIN_CONFIG_CHECK", "true")
}

val generateBuildConfig by tasks.registering {
    val outputDir =
        layout.buildDirectory.dir("generated/buildConfig/kotlin/commonMain").get().asFile
    val buildConfigFile = File(outputDir, "com/cocot3ro/gh/almacen/BuildConfig.kt")

    inputs.property("version", version)
    outputs.file(buildConfigFile)

    doLast {
        buildConfigFile.parentFile.mkdirs()
        buildConfigFile.writeText(
            """
            |package com.cocot3ro.gh.almacen
            |
            |object BuildConfig {
            |    const val VERSION_NAME: String = "$version"
            |    const val VERSION_CODE: Int = ${
                version.split(".").map(String::toInt).reduce { acc, i -> acc * 100 + i }
            }
            |    const val APPLICATION_ID: String = "${android.defaultConfig.applicationId}"
            |}
            """.trimMargin()
        )
    }
}

repositories {
    google {

        @Suppress("UnstableApiUsage")
        mavenContent {
            includeGroupAndSubgroups("androidx")
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
        }
    }
    mavenCentral()
    mavenLocal()

    maven {
        url = uri("https://maven.pkg.github.com/cocot3ro/GestionHotel-Core")
        credentials {
            username = project.findProperty("gpr.user") as String
            password = project.findProperty("gpr.key") as String
        }
    }
}