plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("io.epavlov.gradle.plugin.dependency")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.4.1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.10")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("org.robolectric:robolectric:4.12.2")
}