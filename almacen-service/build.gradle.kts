import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application

    alias(libs.plugins.sqldelight)

    alias(libs.plugins.kotlin.plugin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

group = "com.cocot3ro.gh_almacen"
version = "0.1.1"

application {
    mainClass = "com.cocot3ro.gh_almacen.ApplicationKt"
    applicationDefaultJvmArgs =
        listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(projects.almacenServiceNetworkResources)

    implementation(libs.logback)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit)
    testImplementation(libs.bundles.koin.test)

    implementation(platform(libs.ktor.bom))
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    implementation(libs.sqldelight.sqlite.driver)
    implementation(libs.sqldelight.coroutines.extensions)
    implementation(libs.sqldelight.primitive.adapters)
}

sqldelight {
    databases {
        create("AlmacenDatabase") {
            packageName = "com.cocot3ro.gh_almacen"
        }
    }
}

tasks.register("getProjectVersion") {
    doLast {
        println(project.version)
    }
}
