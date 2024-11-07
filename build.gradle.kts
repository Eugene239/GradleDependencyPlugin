import java.net.URI

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath("io.epavlov:gradle-plugin-dependency:1.5.3-ff61869-SNAPSHOT")
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