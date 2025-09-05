plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.0"
    `maven-publish`
}

group = "io.github.eugene239"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

buildscript {
    dependencies {
        val pluginClasspath = PluginProvider.getPluginClasspath(project)
        if (pluginClasspath != null) {
            classpath(pluginClasspath)
        }
    }
}

if (PluginProvider.getPluginClasspath(project) != null) {
    apply(plugin = "io.github.eugene239.gradle.plugin.dependency")

    extensions.configure(io.github.eugene239.gradle.plugin.dependency.publication.PublicationExtension::class) {
        bom = io.github.eugene239.gradle.plugin.dependency.publication.BomConfig(
            groupId = "io.github.eugene239",
            artifactId = "demolib-bom",
            version = "1.0.0"
        )
        jar = io.github.eugene239.gradle.plugin.dependency.publication.JarConfig(
            groupId = "io.github.eugene239",
            artifactId = "demolib",
            version = "2.0.0",
            addSource = true
        )
    }

    publishing {
        repositories {
            maven {
                name = "projectLocal"
                url = uri("${rootDir}/localMavenRepo")
            }
        }
    }
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.4.1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.10")
}