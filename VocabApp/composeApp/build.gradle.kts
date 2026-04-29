import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()

    var koinVersion = "4.1.0"

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("io.insert-koin:koin-androidx-compose:${koinVersion}")
            // or for multiplatform
            implementation("io.insert-koin:koin-compose:${koinVersion}")
            implementation(libs.ktor.client.android)
            implementation(libs.android.driver)
            implementation(libs.androidx.browser)
            implementation("androidx.credentials:credentials:1.6.0-rc01")
            implementation("androidx.credentials:credentials-play-services-auth:1.6.0-rc01")
            implementation(libs.tink.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("io.insert-koin:koin-compose:${koinVersion}")
            implementation("io.insert-koin:koin-compose-viewmodel:${koinVersion}")
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.cio)
            implementation(libs.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.auth)
            implementation("app.cash.sqldelight:sqlite-driver:2.2.1")
            implementation(libs.kermit) //logger
                // Logger.d { "This is a debug log" }
                // Logger.e(throwable) { "Something went wrong" }
            implementation("io.github.sunildhiman90:kmauth-google:0.3.4")
            implementation("io.github.sunildhiman90:kmauth-google-compose:0.3.4")
            // DataStore library
            implementation(libs.androidx.datastore)
            // The Preferences DataStore library
            implementation(libs.androidx.datastore.preferences)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0-alpha06")
            implementation(compose.materialIconsExtended)
            // image
            implementation("io.coil-kt.coil3:coil-compose:3.4.0")
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")
            implementation("io.ktor:ktor-client-logging:3.4.2")
            implementation("io.ktor:ktor-client-auth:3.4.2")
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.androidx.room.common.jvm)
            implementation("org.slf4j:slf4j-nop:2.0.17")
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation("net.java.dev.jna:jna-platform:5.14.0")
        }
    }
}

android {
    namespace = "personal.jp.vocabapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "personal.jp.vocabapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "personal.jp.vocabapp.MainKt"

        nativeDistributions {
            modules(
                "java.sql",        // DB
                "java.desktop",    // browser
                "java.management", // Ktor server
                "java.naming",     // Ktor server
                "jdk.crypto.ec",   // HTTPS
                "jdk.unsupported"  // JNA(DPAPI)
            )
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "Vocab"
            windows {
                iconFile.set(project.file("src/jvmMain/resources/vocab_icon.ico"))
                menu = true
            }
            packageVersion = "1.0.0"
        }
    }
}

sqldelight {
    databases {
        create("WordDatabase") {
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.0.2")
            packageName.set("db")
        }
    }
}