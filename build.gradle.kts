import java.net.URI

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version PluginDependencies.Versions.kotlin apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = URI("https://maven.google.com/")
        }
    }
}