import com.android.build.gradle.AppExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.squareup.sqldelight) apply false
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.google.secrets.gradle)
}

sonarqube {
    properties {
        property("sonar.projectKey", "itachi1706_SingBuses")
        property("sonar.organization", "itachi1706")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.androidLint.reportPaths", "app/build/reports/lint-results-debug.xml")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/coverage/test/debug/report.xml")
        property("sonar.projectVersion", project(":app").extensions.getByType(AppExtension::class.java).defaultConfig.versionName
            ?: "1.0")
    }
}
