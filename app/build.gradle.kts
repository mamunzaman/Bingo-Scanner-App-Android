import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun quoteBuildConfigString(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp") version "2.3.6"
}

android {
    namespace = "com.example.mamunbingoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mamunbingoapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            quoteBuildConfigString(localProperties.getProperty("SUPABASE_URL", "")),
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            quoteBuildConfigString(localProperties.getProperty("SUPABASE_ANON_KEY", "")),
        )
    }

    buildTypes {

        debug {
            buildConfigField("boolean", "DEMO_MODE", "true")

        }

        release {
            // Production: separated from debug (demo seed).
            isDebuggable = false
            // R8/ProGuard: rules wired; keep off until shrink is validated end-to-end.
            isMinifyEnabled = false

            buildConfigField("boolean", "DEMO_MODE", "false")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
    lint {
        abortOnError = true
        warningsAsErrors = false
    }
    installation {
        // Replace existing install (signature/debug changes) — fixes common Android Studio deploy failures.
        installOptions.add("-r")
        installOptions.add("-d")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// CameraX 1.5.x can pull Lifecycle 2.10; atomic group would upgrade *-compose past Compose BOM 1.6.x.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.lifecycle") {
            useVersion("2.7.0")
            because("Align Lifecycle with Compose BOM 2024.02.00 (compose-runtime 1.6.x)")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.zxing.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(enforcedPlatform(libs.androidx.compose.bom))
    androidTestImplementation(enforcedPlatform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material")
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // lifecycle-process + startup-runtime: transitive via supabase-auth-kt (do not duplicate)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.camera:camera-core:1.5.0")
    implementation("androidx.camera:camera-camera2:1.5.0")
    implementation("androidx.camera:camera-lifecycle:1.5.0")
    implementation("androidx.camera:camera-view:1.5.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation(libs.coil.compose)
    implementation("com.github.yalantis:ucrop:2.2.8")
    // Supabase: Auth + profiles table only. Bingo data stays in Room on-device.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth.kt)
    implementation(libs.supabase.postgrest.kt)
    implementation(libs.supabase.storage.kt)
    implementation(libs.ktor.client.okhttp)
    implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.foundation.ExperimentalFoundationApi")
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
        freeCompilerArgs.add("-opt-in=androidx.compose.material.ExperimentalMaterialApi")
    }
}
