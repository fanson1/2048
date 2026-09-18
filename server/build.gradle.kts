plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
}

group = "com.finley.android.merge2048"
version = "1.0.0"
application {
    mainClass = "com.finley.android.merge2048.ApplicationKt"
}

dependencies {
    api(project(":core"))
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
