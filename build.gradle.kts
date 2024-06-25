import com.android.build.gradle.AppExtension

plugins {
    id("com.android.application") version "8.5.0" apply false
    id("com.squareup.sqldelight") version "1.5.5" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.sonarqube") version "4.4.1.3373"
}

sonarqube {
    properties {
        property("sonar.projectKey", "itachi1706_SingBuses")
        property("sonar.organization", "itachi1706")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.androidLint.reportPaths", "app/build/reports/lint-results-debug.xml")
        property("sonar.projectVersion", project(":app").extensions.getByType(AppExtension::class.java).defaultConfig.versionName
            ?: "1.0")
    }
}
