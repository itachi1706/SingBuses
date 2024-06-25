import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.firebase.crashlytics")
}

val isGHActions: Boolean = System.getenv("GITHUB_ACTIONS")?.toBoolean() ?: false
val appNamespace = "com.itachi1706.busarrivalsg"

android {
    compileSdk = 33
    buildToolsVersion = "33.0.2"

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
}

configurations.all {
    exclude(module = "httpclient")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("androidx.multidex:multidex:2.0.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") {
        exclude(group = "com.android.support", module = "support-annotations")
    }

    implementation("com.itachi1706.appupdater:appupdater:3.0.2")
    implementation("com.itachi1706.helpers:helperlib:1.4.3")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-auth-ktx")

    implementation("com.google.firebase:firebase-perf")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("me.jfenn:Attribouter:0.1.9")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    implementation("com.itachi1706.cepaslib:cepaslib:2.4.3")
}

apply(plugin = "com.google.gms.google-services")