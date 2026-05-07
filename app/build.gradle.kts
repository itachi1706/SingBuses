import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.secrets.gradle)
}

val isGHActions: Boolean = System.getenv("GITHUB_ACTIONS")?.toBoolean() ?: false
val appNamespace = "com.itachi1706.busarrivalsg"

android {
    compileSdk = 36

    defaultConfig {
        namespace = appNamespace
        applicationId = appNamespace
        minSdk = 23
        targetSdk = 36
        versionCode = 1424
        versionName = "6.0.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    androidResources {
        localeFilters += listOf(
            "en", // English
        )
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/LICENSE*")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            multiDexEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false // Disable automatic upload of mapping files
            }
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            multiDexEnabled = true
        }
        create("googlePlay") {
            initWith(getByName("release"))
            matchingFallbacks.add("release")
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
    // This enables long timeouts required on slow environments, e.g. Travis
    installation {
        timeOutInMs = 10 * 60 * 1000 // Set the timeout to 10 minutes
        installOptions.addAll(listOf("-d", "-t"))
    }
    lint {
        abortOnError = !isGHActions
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    testOptions {
        unitTests.all {
            it.jvmArgs(
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

configurations.all {
    exclude(module = "httpclient")
}

afterEvaluate {
    listOf("release", "debug", "googlePlay").forEach { variant ->
        val capVariant = variant.replaceFirstChar { it.uppercase() }
        val bundleTaskName = "bundle$capVariant"
        val zipTaskName = "zipNativeDebugSymbols$capVariant"
        tasks.register<Zip>(zipTaskName) {
            group = "other"
            description = "Zips the native debug symbols for $variant"
            from("build/intermediates/merged_native_libs/$variant/merge${capVariant}NativeLibs/out/lib")
            exclude("armeabi*")
            exclude("mips")
            archiveFileName.set("native-debug-symbols.zip")
            destinationDirectory.set(layout.projectDirectory.dir("$variant/mappings"))
        }
        tasks.named(bundleTaskName).configure {
            finalizedBy(tasks.named(zipTaskName))
        }
    }
}

dependencies {
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(platform(libs.firebase.bom))

    implementation(libs.appupdater)
    implementation(libs.attribouter)
    implementation(libs.cepaslib)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.flexbox)
    implementation(libs.helperlib)
    implementation(libs.material)

    implementation(libs.preference.ktx)
    implementation(libs.recyclerview)

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.perf)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid.identity)

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    implementation(libs.kotlinx.serialization.json)

    // Test for About Page
    implementation(libs.gitrest)
}

apply(plugin = libs.plugins.google.services.get().pluginId)