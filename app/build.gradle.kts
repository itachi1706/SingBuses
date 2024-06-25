import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.firebase.crashlytics)
}

val isGHActions: Boolean = System.getenv("GITHUB_ACTIONS")?.toBoolean() ?: false
val appNamespace = "com.itachi1706.busarrivalsg"

android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        namespace = appNamespace
        applicationId = appNamespace
        minSdk = 19
        targetSdk = 33
        versionCode = 937
        versionName = "5.2.2"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations.add("en")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
            multiDexEnabled = true
        }
//        create("googlePlay") {
//            initWith(getByName("release"))
//            matchingFallbacks.add("release")
//            isMinifyEnabled = true
//            isShrinkResources = true
//        }
    }
    // This enables long timeouts required on slow environments, e.g. Travis
    installation {
        timeOutInMs = 10 * 60 * 1000 // Set the timeout to 10 minutes
        installOptions.addAll(listOf("-d", "-t"))
    }
    lint {
        abortOnError = !isGHActions
    }
}

configurations.all {
    exclude(module = "httpclient")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(platform(libs.firebase.bom))
    implementation(libs.multidex)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") {
        exclude(group = "com.android.support", module = "support-annotations")
    }

    implementation(libs.appupdater)
    implementation(libs.helperlib)
    implementation(libs.material)
    implementation(libs.legacy.support.v4)
    implementation(libs.preference.ktx)
    implementation(libs.recyclerview)
    implementation(libs.core.ktx)
    implementation(libs.gson)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth.ktx)

    implementation(libs.firebase.perf)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics.ktx)

    implementation(libs.attribouter) {
        exclude(group = "com.google.android", module = "flexbox")
    }
    implementation(libs.flexbox)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    implementation(libs.cepaslib)
}

apply(plugin = libs.plugins.google.services.get().pluginId)