import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.secrets.gradle)
}

val isGHActions: Boolean = System.getenv("GITHUB_ACTIONS")?.toBoolean() ?: false
val appNamespace = "com.itachi1706.busarrivalsg"

android {
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        namespace = appNamespace
        applicationId = appNamespace
        minSdk = 23
        targetSdk = 34
        versionCode = 1090
        versionName = "5.2.3"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations.add("en")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            configure<CrashlyticsExtension> {
                isMappingFileUploadEnabled = false // Disabled mapping file uploading for DEBUG builds
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
    implementation(libs.attribouter) {
        exclude(group = "com.google.android", module = "flexbox")
    }
    implementation(libs.cepaslib)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.flexbox)
    implementation(libs.gson)
    implementation(libs.helperlib)
    implementation(libs.legacy.support.v4)
    implementation(libs.material)
    implementation(libs.multidex)

    implementation(libs.preference.ktx)
    implementation(libs.recyclerview)

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.perf)

    implementation(libs.play.services.auth)
    implementation(libs.play.services.maps)
}

apply(plugin = libs.plugins.google.services.get().pluginId)