plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

buildscript {
    repositories {
        maven {
            name = "projectLocal"
            url = uri("${rootDir}/localMavenRepo")
        }
    }

    dependencies {
        val pluginClasspath = PluginProvider.getPluginClasspath(project)
        if (pluginClasspath != null) {
            classpath(pluginClasspath)
        }
    }
}

if (PluginProvider.getPluginClasspath(project) != null) {
    apply(plugin = "io.github.eugene239.gradle.plugin.dependency")

    afterEvaluate {
        tasks.named("dependencyReport") {
            dependsOn(":plugin:publishDemolibPublicationToMavenLocal")
        }
        tasks.named("dependencyGraph") {
            dependsOn(":plugin:publishDemolibPublicationToMavenLocal")
        }
    }
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.4.1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.10")
    implementation("org.robolectric:robolectric:4.12.2")
}